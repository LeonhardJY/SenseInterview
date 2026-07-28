/**
 * 常量映射表和工具函数
 *
 * 为什么要抽出来？
 * 之前 statusText、levelText、modeText 在每个页面都写了一遍，
 * 改一个地方容易漏改别处。集中管理，一处修改处处生效。
 */

// ========== 面试模式 ==========
/** 面试模式 → 中文显示 */
export const MODE_MAP = {
  TEXT: '文字面试',
  VOICE: '语音面试',
  VIDEO: '视频面试'
}

export const modeText = (mode) => MODE_MAP[mode] || mode || '未知'

// ========== 难度等级 ==========
/** 难度等级 → 中文显示 */
export const LEVEL_MAP = {
  EASY: '初级',
  MEDIUM: '中级',
  HARD: '高级',
  '初级': '初级',
  '中级': '中级',
  '高级': '高级'
}

/** 难度等级 → CSS 类名 */
export const LEVEL_CLASS_MAP = {
  EASY: 'level-easy',
  MEDIUM: 'level-medium',
  HARD: 'level-hard',
  '初级': 'level-easy',
  '中级': 'level-medium',
  '高级': 'level-hard'
}

export const levelText = (level) => LEVEL_MAP[level] || level || '未知'
export const levelClass = (level) => LEVEL_CLASS_MAP[level] || 'level-medium'

// ========== 面试状态 ==========
/** 面试状态 → 中文显示（与后端一致） */
export const STATUS_MAP = {
  CREATED: '待开始',
  RUNNING: '进行中',
  IN_PROGRESS: '进行中',
  FINISHED: '已完成',
  COMPLETED: '已完成'
}

/** 面试状态 → CSS 类名 */
export const STATUS_CLASS_MAP = {
  CREATED: 'created',
  RUNNING: 'running',
  IN_PROGRESS: 'running',
  FINISHED: 'finished',
  COMPLETED: 'finished'
}

export const statusText = (status) => STATUS_MAP[status] || status || '未知'
export const statusClass = (status) => STATUS_CLASS_MAP[status] || ''

// ========== 分数等级 ==========
/**
 * 根据分数返回等级（用于 CSS 样式）
 * 90+ → excellent, 75+ → good, 60+ → average, <60 → poor
 */
export const scoreLevel = (score) => {
  if (score >= 90) return 'excellent'
  if (score >= 75) return 'good'
  if (score >= 60) return 'average'
  return 'poor'
}

// ========== 日期工具 ==========
/** 格式化日期为中文短格式，如 "07/27 14:30" */
export const formatDate = (dateStr) => {
  if (!dateStr) return '-'
  const date = new Date(dateStr)
  return date.toLocaleString('zh-CN', {
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}
