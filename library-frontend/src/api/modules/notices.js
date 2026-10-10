// 公告模块 API
import service from '../request'

/** 公告分页列表（公开） */
export const listNotices = (params) => service.get('/api/notices', { params })

/** 公告详情（公开） */
export const getNotice = (id) => service.get(`/api/notices/${id}`)

/** 最新公告（公开） */
export const getLatestNotices = (limit = 5) => service.get('/api/notices/latest', { params: { limit } })

/** 发布公告（管理员） */
export const createNotice = (data) => service.post('/api/notices', data)

/** 编辑公告（管理员） */
export const updateNotice = (id, data) => service.put(`/api/notices/${id}`, data)

/** 删除公告（管理员） */
export const deleteNotice = (id) => service.delete(`/api/notices/${id}`)
