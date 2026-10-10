// 管理员仪表盘统计数据与排行榜接口
import service from '../request'

/**
 * 获取首页统计数据
 * @returns {Promise} DashboardStatsVO = { totalBooks, totalCopies, availableCopies, borrowedCopies, activeStudents, activeCards, currentBorrowings, unpaidFines }
 */
export const getStats = () => service.get('/api/dashboard/stats')

/**
 * 获取排行榜数据
 * @returns {Promise} RankingVO = { studentRankings: [{ sno, studentName, borrowCount }], bookRankings: [{ ISBN, bookName, borrowCount }] }
 */
export const getRankings = () => service.get('/api/dashboard/rankings')
