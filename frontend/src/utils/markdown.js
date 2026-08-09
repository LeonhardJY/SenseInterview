/**
 * 轻量级 Markdown 渲染（安全版）
 *
 * 先转义 HTML 防止 XSS，再按顺序应用 markdown 变换。
 * 支持 Agent 面试官常用的格式：标题、粗体、斜体、行内代码、列表、表格、引用、分割线。
 */

/** 转义 HTML 特殊字符 */
function escapeHtml(text) {
  return text
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;')
    .replace(/'/g, '&#39;')
}

/** 行内格式：粗体 / 斜体 / 行内代码 */
function renderInline(text) {
  return text
    .replace(/`([^`]+)`/g, '<code>$1</code>')
    .replace(/\*\*([^*]+)\*\*/g, '<strong>$1</strong>')
    .replace(/\*([^*]+)\*/g, '<em>$1</em>')
    .replace(/~~([^~]+)~~/g, '<del>$1</del>')
}

/** 渲染表格行 */
function renderTableRow(cells, isHeader) {
  const tag = isHeader ? 'th' : 'td'
  const row = cells.map(c => `<${tag}>${renderInline(c.trim())}</${tag}>`).join('')
  return `<tr>${row}</tr>`
}

/**
 * 主渲染函数：将 markdown 文本转为安全 HTML
 */
export function renderMarkdown(text) {
  if (!text) return ''

  const lines = escapeHtml(text).split('\n')
  const html = []
  let i = 0
  let inList = false
  let listType = ''
  let inTable = false
  let tableRows = []

  const closeList = () => {
    if (inList) { html.push(`</${listType}>`); inList = false; listType = '' }
  }
  const closeTable = () => {
    if (inTable) {
      html.push('<table><thead>' + tableRows[0] + '</thead><tbody>' + tableRows.slice(1).join('') + '</tbody></table>')
      tableRows = []; inTable = false
    }
  }

  for (let line of lines) {
    const trimmed = line.trim()

    // 空行
    if (!trimmed) { closeList(); closeTable(); continue }

    // 表格分隔行 |---|---| 跳过
    if (inTable && /^\|?[\s:|-]+\|?$/.test(trimmed) && trimmed.includes('-')) continue

    // 表格行
    if (trimmed.startsWith('|') && trimmed.endsWith('|') && trimmed.includes('|')) {
      if (!inTable) { inTable = true; tableRows = [] }
      const cells = trimmed.slice(1, -1).split('|')
      tableRows.push(renderTableRow(cells, tableRows.length === 0))
      continue
    } else if (inTable) {
      closeTable()
    }

    // 标题
    const hMatch = trimmed.match(/^(#{1,4})\s+(.*)/)
    if (hMatch) {
      closeList()
      const level = hMatch[1].length
      html.push(`<h${level}>${renderInline(hMatch[2])}</h${level}>`)
      continue
    }

    // 分割线
    if (/^(-{3,}|\*{3,})$/.test(trimmed)) {
      closeList()
      html.push('<hr>')
      continue
    }

    // 引用
    if (trimmed.startsWith('>')) {
      closeList()
      html.push(`<blockquote>${renderInline(trimmed.slice(1).trim())}</blockquote>`)
      continue
    }

    // 无序列表
    const ulMatch = trimmed.match(/^\s*[-*+]\s+(.*)/)
    if (ulMatch) {
      if (!inList || listType !== 'ul') { closeList(); html.push('<ul>'); inList = true; listType = 'ul' }
      html.push(`<li>${renderInline(ulMatch[1])}</li>`)
      continue
    }

    // 有序列表
    const olMatch = trimmed.match(/^\s*\d+\.\s+(.*)/)
    if (olMatch) {
      if (!inList || listType !== 'ol') { closeList(); html.push('<ol>'); inList = true; listType = 'ol' }
      html.push(`<li>${renderInline(olMatch[1])}</li>`)
      continue
    }

    // 普通段落
    closeList()
    html.push(`<p>${renderInline(trimmed)}</p>`)
  }

  closeList()
  closeTable()

  return html.join('\n')
}
