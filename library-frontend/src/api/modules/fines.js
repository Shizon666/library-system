// 罚款查询与缴纳接口
import service from '../request'

/**
 * 查询罚款列表
 * @param {Object} params - { payStatus?: 0|1 }  0=未缴 1=已缴，不传则查询全部
 * @returns {Promise} FineVO[] = [{ fineId, recNo, barCode, bookName, amount, payStatus, createTime, overdueDays }]
 */
export const getFines = (params) => service.get('/api/fines', { params })

/**
 * 缴纳罚款（支持合并缴纳）
 * @param {Object} data - FinePayDTO = { cardNo: string, fineIds: number[], operator?: string, remark?: string }
 * @returns {Promise} FinePayment = { paymentId, cardNo, sno, paymentAmount, paymentTime, operator, remark }
 */
export const payFines = (data) => service.post('/api/fines/pay', data)
