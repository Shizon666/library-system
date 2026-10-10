// 门户首页公开数据接口（无需登录）
import service from '../request'

/** 门户首页：热读推荐 / 借书明星 / 新书上架 */
export const getPortalHome = () => service.get('/api/portal/home')
