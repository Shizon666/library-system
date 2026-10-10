// 个人中心接口（个人信息、在借、历史、罚款）
import service from '../request'

/** 获取个人信息 */
export const getMyProfile = () => service.get('/api/me/profile')

/** 修改密码 */
export const changePassword = (data) => service.put('/api/me/password', data)

/**
 * 获取当前在借图书
 * @param {string} sno - 学号
 * @returns {Promise} BorrowingVO[] = [{ recNo, barCode, bookName, ISBN, borDate, retDate, overdueDays, status }]
 */
export const getMyBorrowings = (sno) => service.get('/api/me/borrowings', { params: { sno } })

/**
 * 获取借书历史
 * @param {string} sno - 学号
 * @returns {Promise} HistoryVO[] = [{ recNo, barCode, bookName, borDate, retDate, realRetDate, status }]
 */
export const getMyHistory = (sno) => service.get('/api/me/history', { params: { sno } })

/**
 * 获取超期罚款
 * @param {string} sno - 学号
 * @returns {Promise} FineVO[] = [{ fineId, recNo, barCode, bookName, amount, payStatus, createTime, overdueDays }]
 */
export const getMyFines = (sno) => service.get('/api/me/fines', { params: { sno } })

/**
 * 获取已缴记录
 * @param {string} sno - 学号
 * @returns {Promise} FinePayment[] = [{ paymentId, cardNo, sno, paymentAmount, paymentTime, operator, remark }]
 */
export const getMyPayments = (sno) => service.get('/api/me/payments', { params: { sno } })
