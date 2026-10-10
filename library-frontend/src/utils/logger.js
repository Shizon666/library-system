/**
 * 统一日志工具
 *
 * 功能：
 * 1. 结构化的操作日志，可追溯每一步的来源
 * 2. 自动注入当前用户上下文（角色、姓名、学号/证号）
 * 3. 支持分组日志（console.group）和性能计时
 * 4. 开发环境全量输出，生产环境可通过开关控制
 *
 * 使用示例：
 *   logger.operation('BorrowView > queryCard', '▶ 进入', { cardNo: 'C2024001' })
 *   logger.network('GET /api/cards/C2024001', '📤 请求')
 *   logger.network('GET /api/cards/C2024001', '📥 响应', { status: 200, data: {...} })
 */

// 线上排查时把 LOG_ENABLED 改为 false 即可关闭所有日志
const LOG_ENABLED = true
const LOG_LEVEL = 'debug'

// 控制台彩色标签样式，对应不同日志分类（蓝/紫/绿/橙/灰/红/黄）
const STYLES = {
  operation: 'background: #1E3A8A; color: #fff; padding: 2px 6px; border-radius: 3px; font-weight: bold;', // 蓝色-用户操作
  network:   'background: #7C3AED; color: #fff; padding: 2px 6px; border-radius: 3px; font-weight: bold;', // 紫色-网络请求
  navigation:'background: #059669; color: #fff; padding: 2px 6px; border-radius: 3px; font-weight: bold;', // 绿色-路由导航
  state:     'background: #D97706; color: #fff; padding: 2px 6px; border-radius: 3px; font-weight: bold;', // 橙色-状态变更
  render:    'background: #6B7280; color: #fff; padding: 2px 6px; border-radius: 3px; font-weight: bold;', // 灰色-组件渲染
  error:     'background: #DC2626; color: #fff; padding: 2px 6px; border-radius: 3px; font-weight: bold;', // 红色-错误异常
  validate:  'background: #CA8A04; color: #fff; padding: 2px 6px; border-radius: 3px; font-weight: bold;', // 黄色-数据校验
}

// 日志分类标签文字，与 STYLES 一一对应
const TAG_MAP = {
  operation: '[操作]',  // 按钮点击、表单提交等用户操作
  network:   '[网络]',  // Axios 请求/响应
  navigation:'[导航]',  // Vue Router 路由跳转
  state:     '[状态]',  // Pinia Store 数据变更
  render:    '[渲染]',  // 组件生命周期、条件渲染
  error:     '[错误]',  // 异常捕获、业务错误
  validate:  '[校验]',  // 表单校验、参数合法性检查
}

/**
 * 获取当前用户上下文
 * @returns {{ role: string, id: string, name: string }}
 */
function getUserContext() {
  try {
    const raw = localStorage.getItem('userInfo')
    if (!raw) return { role: '未登录', id: '-', name: '-' }
    const info = JSON.parse(raw)
    return {
      role: info.role || '未知',
      id: info.sno || info.cardNo || info.username || '-',
      name: info.displayName || info.studentName || '-',
    }
  } catch {
    return { role: '未知', id: '-', name: '-' }
  }
}

/**
 * 格式化时间戳
 * @returns {string} HH:mm:ss.SSS
 */
function timestamp() {
  const d = new Date()
  const pad = (n) => String(n).padStart(2, '0')
  return `${pad(d.getHours())}:${pad(d.getMinutes())}:${pad(d.getSeconds())}.${String(d.getMilliseconds()).padStart(3, '0')}`
}

/**
 * 核心日志方法
 * @param {string} category - 日志分类
 * @param {string} source - 来源标识
 * @param {string} step - 步骤标签
 * @param {object} [detail] - 详细信息对象
 * @param {string} [level='info'] - 日志级别
 */
function log(category, source, step, detail = null, level = 'info') {
  if (!LOG_ENABLED) return              // 全局开关关闭时直接跳过

  const user = getUserContext()         // 获取当前登录用户信息
  const time = timestamp()              // 生成 HH:mm:ss.SSS 格式时间戳
  const tag = TAG_MAP[category] || `[${category}]`  // 匹配分类标签文字
  const style = STYLES[category] || ''  // 匹配分类对应的 CSS 样式
  const userStr = `👤 ${user.role}|${user.id}|${user.name}` // 用户上下文字符串

  // 拼接带颜色占位符的前缀，%c 用于 console 样式注入
  const prefix = `%c${tag}%c ${time} ${source} ${step}`

  if (detail !== null && detail !== undefined) {
    const detailStr = typeof detail === 'object' ? detail : { value: detail } // 非对象转为包装对象
    if (level === 'error') {
      console.error(prefix, style, '', userStr, detailStr) // 红色错误日志
    } else if (level === 'warn') {
      console.warn(prefix, style, '', userStr, detailStr)  // 黄色警告日志
    } else {
      console.log(prefix, style, '', userStr, detailStr)   // 默认普通日志
    }
  } else {
    // 无详情时只输出前缀和用户信息
    console.log(prefix, style, '', userStr)
  }
}

/**
 * 启动一个分组日志（console.group 折叠多条相关日志）
 */
function group(title) {
  if (!LOG_ENABLED) return
  const time = timestamp()
  console.group(`%c📦 ${time} ${title}`, 'font-weight: bold; font-size: 13px;')
}

/**
 * 结束当前分组日志（与 group 配对使用）
 */
function groupEnd() {
  if (!LOG_ENABLED) return
  console.groupEnd()
}

/**
 * 启动性能计时器（用于测量某段代码执行时间）
 */
function time(label) {
  if (!LOG_ENABLED) return
  console.time(`⏱ ${label}`)
}

/**
 * 结束性能计时器并输出耗时（与 time 配对使用）
 */
function timeEnd(label) {
  if (!LOG_ENABLED) return
  console.timeEnd(`⏱ ${label}`)
}

// 对外暴露的 logger 对象，每个分类方法签名一致：(来源, 步骤, 详情?, 级别?)
export const logger = {
  operation: (source, step, detail, level) => log('operation', source, step, detail, level),    // 用户操作日志
  network: (source, step, detail, level) => log('network', source, step, detail, level),        // 网络请求日志
  navigation: (source, step, detail, level) => log('navigation', source, step, detail, level),  // 路由导航日志
  state: (source, step, detail, level) => log('state', source, step, detail, level),            // 状态变更日志
  render: (source, step, detail, level) => log('render', source, step, detail, level),          // 组件渲染日志
  error: (source, step, detail) => log('error', source, step, detail, 'error'),                 // 错误日志（固定 error 级别）
  validate: (source, step, detail, level) => log('validate', source, step, detail, level),      // 校验日志
  group,      // 开启分组
  groupEnd,   // 关闭分组
  time,       // 开始计时
  timeEnd,    // 结束计时
}

export default logger
