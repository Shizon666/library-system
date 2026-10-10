// 借书、还书、挂失等流通业务接口
import service from '../request'

/** 借书（支持批量，一次传多个条码） */
export const borrowBooks = (data) => service.post('/api/borrow', data)

/** 根据条码号查询单个副本信息（含书目、在架状态等） */
export const getCopyInfo = (barCode) => service.get(`/api/copies/${barCode}`)

/** 检索可借副本 */
export const searchCopyForBorrow = (keyword) =>
  service.get('/api/copies/search', { params: { keyword } })

/** 还书 */
export const returnBook = (data) => service.post('/api/return', data)

/** 挂失书籍（按定价赔偿） */
export const reportBookLoss = (data) => service.post('/api/borrow/report-loss', data)

/** 按借书证号查在借列表（先查卡信息拿学号，再查该学号的在借记录） */
export const getBorrowingsByCard = async (cardNo) => {
  // 第一步：根据证号查询借书证，拿到关联的学号
  const cardRes = await service.get('/api/cards/query', { params: { keyword: cardNo } })
  const sno = cardRes.data?.sno
  // 无学号说明卡未绑定学生，直接返回空借阅列表
  if (!sno) return { card: cardRes.data, borrowings: [] }
  // 第二步：用学号查询该学生所有在借图书
  const borRes = await service.get('/api/me/borrowings', { params: { sno } })
  // 合并返回卡信息和借阅记录
  return { card: cardRes.data, borrowings: borRes.data || [] }
}
