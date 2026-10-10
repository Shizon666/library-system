// 书目与副本管理接口（检索、编目、入库、上下架）
import service from '../request'

/** 公共检索（仅返回上架书目，供学生和管理员使用） */
export const searchBooks = (keyword) => service.get('/api/books/search', { params: { keyword } })

/** 编目列表（管理员视角，含下架书目，支持分页和筛选） */
export const listCatalog = (params) => service.get('/api/books/catalog', { params })

/** 书目详情（含全部副本） */
export const getBookDetail = (isbn) => service.get(`/api/books/${isbn}`)

/** 新书入库（可无副本，仅创建书目） */
export const acquireBook = (data) => service.post('/api/books/acquire', data)

/** 更新书目信息 */
export const updateBook = (isbn, data) => service.put(`/api/books/${isbn}`, data)

/** 为已有书目新增副本 */
export const addBookCopies = (isbn, copies) => service.post(`/api/books/${isbn}/copies`, { copies })

/** 更新副本馆藏位置 */
export const updateCopy = (barCode, data) => service.put(`/api/books/copies/${barCode}`, data)

/** 整书下架 */
export const offShelfBook = (isbn) => service.put(`/api/books/${isbn}/off-shelf`)

/** 注销单个副本 */
export const discardCopy = (barCode) => service.delete(`/api/books/copies/${barCode}`)
