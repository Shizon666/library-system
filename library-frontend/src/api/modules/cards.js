// 借书证全生命周期管理（新办、挂失、补办、注销）
import service from '../request'

/**
 * 新办借书证（仅需学号、姓名；证号=学号+三位序号，由后端生成）
 * @param {{ sno: string, studentName: string, password?: string }} data
 */
export const createCard = (data) => service.post('/api/cards', data)

/** 预览新办证号 */
export const previewNextCardNo = (sno) =>
  service.get('/api/cards/next-cardno', { params: { sno } })

/** 按学号查询学生及借书证信息（新办/挂失/补办/注销通用） */
export const queryStudentCard = (sno) =>
  service.get('/api/cards/student-query', { params: { sno } })

/**
 * 按借书证号或学号查询（学号返回当前正常证）
 */
export const getCardInfo = (keyword) =>
  service.get('/api/cards/query', { params: { keyword } })

/** 借书前校验（是否可借、剩余额度） */
export const checkBorrowEligibility = (cardNo) =>
  service.get(`/api/cards/${encodeURIComponent(cardNo)}/borrow-check`)

/** 预览补办新证号 */
export const previewReplaceCardNo = (cardNo) =>
  service.get(`/api/cards/${cardNo}/replace-preview`)

/** 挂失 */
export const reportLoss = (cardNo) => service.put(`/api/cards/${cardNo}/loss`)

/** 补办（旧卡作废，生成新卡号） */
export const replaceCard = (cardNo, data = {}) =>
  service.put(`/api/cards/${cardNo}/replace`, data)

/** 注销借书证 */
export const deleteCard = (cardNo) => service.delete(`/api/cards/${cardNo}`)

/** 某学生的全部办证历史（含已注销的卡，用于学生详情页展示历史记录） */
export const getCardRecords = (sno) =>
  service.get('/api/cards/records', { params: { sno } })
