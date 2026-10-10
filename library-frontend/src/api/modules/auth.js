// 登录认证相关接口
import service from '../request'

/**
 * 登录（管理员/学生共用同一个接口，后端根据 role 字段走不同校验逻辑）
 * @param {Object} data - { role: 'admin'|'student', username: string, password: string }
 * @returns {Promise} LoginVO = { role, username, sno, cardNo, displayName }
 */
export const login = (data) => service.post('/api/login', data)
