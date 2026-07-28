/**
 * SSE 流式请求客户端
 *
 * 用于消费后端的 SSE (Server-Sent Events) 流接口。
 * 因为 POST 请求不能使用原生 EventSource，用 fetch + ReadableStream 手动解析。
 *
 * SSE 数据格式（后端 SseEmitter 发送）：
 *   event:delta
 *   data:逐字内容
 *
 *   event:done
 *   data:完整文本
 *
 *   event:error
 *   data:错误信息
 */

let _doneCalled = false // 防止 onDone 重复调用

/**
 * 发送流式请求并逐块处理
 *
 * @param {string} url - 请求地址
 * @param {Object} body - POST 请求体
 * @param {Object} callbacks - { onDelta, onDone, onError }
 */
export async function fetchStream(url, body, callbacks = {}) {
  const { onDelta, onDone, onError } = callbacks
  _doneCalled = false

  const callOnce = (fn, data) => {
    if (!_doneCalled && fn) { _doneCalled = true; fn(data) }
  }

  try {
    const response = await fetch(url, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${localStorage.getItem('token')}`
      },
      body: JSON.stringify(body)
    })

    if (!response.ok) {
      const errText = await response.text().catch(() => '请求失败')
      onError?.(`服务器错误 (${response.status}): ${errText}`)
      return
    }

    const contentType = response.headers.get('Content-Type') || ''
    if (!contentType.includes('text/event-stream')) {
      const text = await response.text()
      onDelta?.(text)
      callOnce(onDone, text)
      return
    }

    // 逐块读取流
    const reader = response.body.getReader()
    const decoder = new TextDecoder()
    let buffer = ''
    let fullText = ''

    while (true) {
      const { done, value } = await reader.read()
      if (done) break

      buffer += decoder.decode(value, { stream: true })
      const parts = buffer.split('\n')
      buffer = parts.pop() || ''

      let currentEvent = ''
      for (const line of parts) {
        const trimmed = line.trim()
        if (!trimmed) continue

        if (trimmed.startsWith('event:')) {
          currentEvent = trimmed.slice(6).trim()
        } else if (trimmed.startsWith('data:')) {
          const data = trimmed.slice(5).trim()

          switch (currentEvent) {
            case 'delta':
              fullText += data
              onDelta?.(data)
              break
            case 'done':
              fullText = data
              callOnce(onDone, data)
              break
            case 'error':
              onError?.(data)
              break
            default:
              if (!currentEvent) {
                fullText += data
                onDelta?.(data)
              }
          }
        }
      }
    }

    // 兜底：流结束但没收到 done 事件（兼容某些后端实现）
    if (fullText) {
      callOnce(onDone, fullText)
    }

  } catch (e) {
    console.error('[SSE] 流式请求失败:', e)
    onError?.(e.message || '网络错误')
  }
}
