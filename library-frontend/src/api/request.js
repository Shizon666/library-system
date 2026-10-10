// Axios 实例 + 请求/响应拦截器，统一处理鉴权头注入、日志记录和错误提示
import axios from 'axios'
import { ElMessage } from 'element-plus'
import logger from '@/utils/logger'

// 创建 Axios 实例，baseURL 留空则走当前域名，timeout 防止请求长时间挂起
const service = axios.create({
  baseURL: '',       // 为空时使用相对路径，配合 vite proxy 转发到后端
  timeout: 15000,    // 15 秒超时，覆盖大部分业务场景
})

// 从 sessionStorage 读取 userInfo，构造鉴权请求头（后端通过这三个头识别用户身份）
function getAuthHeaders() {
  try {
    const raw = sessionStorage.getItem('userInfo') || localStorage.getItem('userInfo') || '{}'
    const userInfo = JSON.parse(raw)
    const headers = {}
    if (userInfo.role) headers['X-Role'] = userInfo.role      // admin 或 student
    if (userInfo.sno) headers['X-Sno'] = userInfo.sno         // 学号，用于数据权限过滤
    if (userInfo.cardNo) headers['X-Card-No'] = userInfo.cardNo // 当前借书证号
    if (userInfo.username) headers['X-Username'] = userInfo.username // 管理员用户名
    return headers
  } catch {
    return {}
  }
}

// 请求拦截器：注入角色/学号请求头，供后端按身份过滤数据
service.interceptors.request.use(
  (config) => {
    // 将鉴权头合并到请求头，允许调用方传入的同名头覆盖默认值
    config.headers = { ...getAuthHeaders(), ...config.headers }
    // 拼接日志来源标识，如 "GET /api/books/search"
    const source = `${config.method?.toUpperCase()} ${config.url}`
    // 组装日志详情，password 字段置空防止明文泄露
    const detail = {
      url: config.url,
      method: config.method?.toUpperCase(),
      params: config.params ? { ...config.params, password: undefined } : undefined, // 查询参数（脱敏）
      data: config.data ? trySafeStringify(config.data) : undefined, // 请求体（脱敏）
    }
    logger.network(`Axios > ${source}`, '📤 请求', detail)
    // 记录请求发起时间，用于响应拦截器计算耗时
    config._reqTime = Date.now()
    return config
  },
  (error) => {
    // 请求构造阶段就出错（如拦截器内部异常）
    logger.error('Axios > 请求拦截器', '❌ 失败', { message: error.message })
    return Promise.reject(error)
  }
)

// 响应拦截器：后端返回统一格式 Result<T> = { code, message, data }
service.interceptors.response.use(
  (response) => {
    // 计算本次请求耗时（毫秒）
    const elapsed = Date.now() - (response.config?._reqTime || Date.now())
    const res = response.data // Result<T> 包装对象
    const source = `${response.config?.method?.toUpperCase()} ${response.config?.url}`

    // 打印完整响应日志，含耗时和数据体积
    logger.network(`Axios > ${source}`, `📥 响应 ⏱${elapsed}ms`, {
      status: response.status,          // HTTP 状态码
      code: res?.code,                  // 业务状态码（0=成功）
      message: res?.message,            // 后端提示信息
      dataSize: res?.data ? `${JSON.stringify(res.data).length}B` : '0B', // 响应体大小
    })

    // 业务状态码非 0 表示后端业务错误（如参数校验失败、库存不足等）
    if (res.code !== 0) {
      logger.error(`Axios > ${source}`, '❌ 业务错误', { code: res.code, message: res.message })
      ElMessage.error(res.message || '操作失败') // 全局提示，无需调用方重复处理
      return Promise.reject(new Error(res.message))
    }

    // 业务成功，打印数据摘要便于调试
    logger.network(`Axios > ${source}`, '✅ 成功', {
      code: res.code,
      dataSummary: res.data ? trySummarize(res.data) : null, // 摘要避免大对象刷屏
    })
    return res // 返回整个 Result<T>，调用方通过 res.data 取数据
  },
  (error) => {
    // 计算请求耗时，-1 表示请求未发出就失败了
    const elapsed = error.config?._reqTime ? Date.now() - error.config._reqTime : -1
    const source = error.config ? `${error.config?.method?.toUpperCase()} ${error.config?.url}` : '未知请求'

    if (error.response) {
      // HTTP 层有响应但状态码非 2xx（如 401/403/404/500）
      logger.error(`Axios > ${source}`, `❌ HTTP ${error.response.status}`, {
        status: error.response.status,
        data: error.response.data,
        elapsed: `${elapsed}ms`,
      })
      ElMessage.error(error.response.data?.message || `请求失败 (${error.response.status})`)
    } else if (error.request) {
      // 请求已发出但未收到任何响应（网络断开、超时等）
      logger.error(`Axios > ${source}`, '❌ 网络无响应', { elapsed: `${elapsed}ms` })
      ElMessage.error('网络连接失败，请检查网络')
    } else {
      // 请求构造阶段异常（代码 bug）
      logger.error(`Axios > ${source}`, '❌ 请求异常', { message: error.message })
      ElMessage.error('请求异常：' + error.message)
    }
    return Promise.reject(error)
  }
)

// 日志序列化时把 password 字段脱敏，避免明文打印到控制台
function trySafeStringify(obj) {
  try {
    return JSON.parse(JSON.stringify(obj, (key, value) => {
      if (/password/i.test(key)) return '***'
      return value
    }))
  } catch {
    return { error: 'Failed to serialize' }
  }
}

// 将响应数据压缩为简短摘要，避免日志中打印完整大对象
function trySummarize(data) {
  if (data === null || data === undefined) return 'null'       // 空值直接返回
  if (Array.isArray(data)) return `Array(${data.length})`     // 数组只显示长度
  if (typeof data === 'object') {
    const s = {}
    for (const k of Object.keys(data)) {
      if (Array.isArray(data[k])) s[k] = `Array(${data[k].length})`           // 嵌套数组只保留长度
      else if (typeof data[k] === 'object' && data[k] !== null) s[k] = typeof data[k] // 嵌套对象只保留类型
      else s[k] = data[k] // 基本类型直接保留值
    }
    return s
  }
  return String(data).substring(0, 100) // 字符串截断到 100 字符
}

export default service
