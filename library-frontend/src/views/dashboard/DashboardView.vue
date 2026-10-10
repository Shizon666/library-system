<template>
  <!-- 首页容器 -->
  <div class="page-container">
    <h2 class="page-title">📊 首页概览</h2>

    <!-- KPI 统计卡片区 -->
    <div class="stat-cards">
      <!-- 馆藏图书数：所有角色可见 -->
      <el-card shadow="hover" class="stat-card">
        <div class="stat-card-content">
          <div class="stat-icon stat-icon-primary">
            <el-icon :size="28"><Collection /></el-icon>
          </div>
          <div class="stat-info">
            <!-- 馆藏图书总数，接口未返回时显示 0 -->
            <div class="stat-value">{{ stats.totalBooks || 0 }}</div>
            <div class="stat-label">馆藏图书（种）</div>
          </div>
        </div>
      </el-card>

      <!-- v-if 管理员才展示：当前借出册数 -->
      <el-card v-if="isAdmin" shadow="hover" class="stat-card">
        <div class="stat-card-content">
          <div class="stat-icon stat-icon-warning">
            <el-icon :size="28"><Reading /></el-icon>
          </div>
          <div class="stat-info">
            <!-- 当前被借出的总册数 -->
            <div class="stat-value">{{ stats.borrowedCopies || 0 }}</div>
            <div class="stat-label">当前借出（册）</div>
          </div>
        </div>
      </el-card>

      <!-- v-if 管理员才展示：有效借书证数量 -->
      <el-card v-if="isAdmin" shadow="hover" class="stat-card">
        <div class="stat-card-content">
          <div class="stat-icon stat-icon-success">
            <el-icon :size="28"><CreditCard /></el-icon>
          </div>
          <div class="stat-info">
            <!-- 仍在有效期内的借书证数 -->
            <div class="stat-value">{{ stats.activeCards || 0 }}</div>
            <div class="stat-label">有效借书证</div>
          </div>
        </div>
      </el-card>

      <!-- 未缴罚款笔数：所有角色可见 -->
      <el-card shadow="hover" class="stat-card">
        <div class="stat-card-content">
          <div class="stat-icon stat-icon-danger">
            <el-icon :size="28"><WarningFilled /></el-icon>
          </div>
          <div class="stat-info">
            <!-- 未缴纳的罚款条数 -->
            <div class="stat-value">{{ stats.unpaidFines || 0 }}</div>
            <div class="stat-label">未缴罚款（笔）</div>
          </div>
        </div>
      </el-card>
    </div>

    <!-- 排行榜区域：左右两栏布局 -->
    <el-row :gutter="20">
      <!-- 左栏：学生借阅排行榜 -->
      <el-col :span="12">
        <el-card shadow="never" class="rank-card">
          <template #header>
            <div class="rank-header">
              <span>🏆 当月借书学生排行 TOP 10</span>
            </div>
          </template>
          <!-- 学生排行表格，加载中显示骨架屏 -->
          <el-table :data="studentRankings" v-loading="loading" stripe size="default" max-height="400">
            <el-table-column type="index" label="排名" width="60" />
            <el-table-column prop="studentName" label="学生姓名" min-width="100" />
            <el-table-column prop="sno" label="学号" width="130" />
            <!-- 借阅次数用 tag 高亮展示 -->
            <el-table-column prop="borrowCount" label="借阅次数" width="100" align="center">
              <template #default="{ row }">
                <el-tag type="primary">{{ row.borrowCount }}</el-tag>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>

      <!-- 右栏：热门图书排行榜 -->
      <el-col :span="12">
        <el-card shadow="never" class="rank-card">
          <template #header>
            <div class="rank-header">
              <span>📖 当月热门图书排行 TOP 10</span>
            </div>
          </template>
          <!-- 图书排行表格 -->
          <el-table :data="bookRankings" v-loading="loading" stripe size="default" max-height="400">
            <el-table-column type="index" label="排名" width="60" />
            <el-table-column prop="bookName" label="书名" min-width="150" show-overflow-tooltip />
            <el-table-column prop="ISBN" label="ISBN" width="140" show-overflow-tooltip />
            <!-- 借阅次数用绿色 tag 区分 -->
            <el-table-column prop="borrowCount" label="借阅次数" width="100" align="center">
              <template #default="{ row }">
                <el-tag type="success">{{ row.borrowCount }}</el-tag>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
/**
 * 管理员首页概览：上方四张 KPI 卡片（馆藏/借出/有效证/未缴罚款），
 * 下方左右两个当月排行榜（借书学生 TOP10、热门图书 TOP10）。
 */
import { ref, computed, onMounted } from 'vue'
import { useAuthStore } from '@/stores/auth'
import { getStats, getRankings } from '@/api/modules/dashboard'
import logger from '@/utils/logger'

const COMPONENT = 'DashboardView > ' // 日志前缀，方便定位
const authStore = useAuthStore() // 获取认证 store 实例
const isAdmin = computed(() => authStore.role === 'admin')  // 是否为管理员，决定部分卡片是否展示
const loading = ref(false) // 页面整体加载状态，控制表格骨架屏
const stats = ref({}) // 首页 KPI 统计数据（馆藏/借出/有效证/罚款）
const studentRankings = ref([]) // 当月学生借阅排行 TOP10
const bookRankings = ref([]) // 当月热门图书排行 TOP10

// 加载首页全部数据：统计 + 排行榜，两路并行请求
async function loadDashboard() {
  logger.render(`${COMPONENT}loadDashboard`, '▶ 进入', { triggeredBy: 'onMounted' })
  logger.operation(`${COMPONENT}loadDashboard`, '⏳ 发起并行请求', {
    requests: ['GET /api/dashboard/stats', 'GET /api/dashboard/rankings'],
  })

  loading.value = true // 开启加载态
  try {
    // 统计和排行榜接口互相独立，并行请求加快加载
    const [statsRes, rankRes] = await Promise.all([
      getStats(), // 获取 KPI 统计
      getRankings(), // 获取排行榜数据
    ])

    stats.value = statsRes.data // 赋值统计卡片数据
    studentRankings.value = rankRes.data?.studentRankings || [] // 学生排行，防空数组
    bookRankings.value = rankRes.data?.bookRankings || [] // 图书排行，防空数组

    // 打印加载结果摘要，方便调试
    logger.render(`${COMPONENT}loadDashboard`, '🔚 结束', {
      totalBooks: statsRes.data?.totalBooks,
      totalCopies: statsRes.data?.totalCopies,
      borrowedCopies: statsRes.data?.borrowedCopies,
      activeCards: statsRes.data?.activeCards,
      unpaidFines: statsRes.data?.unpaidFines,
      studentRankingCount: studentRankings.value.length,
      bookRankingCount: bookRankings.value.length,
    })
  } catch (e) {
    // 接口异常时记录错误日志，不影响页面渲染
    logger.error(`${COMPONENT}loadDashboard`, '❌ 加载首页失败', {
      errorMessage: e.message,
    })
  } finally {
    loading.value = false // 无论成功失败都关闭加载态
  }
}

// 组件挂载时自动拉取首页数据
onMounted(() => {
  logger.render(COMPONENT, '🔄 组件挂载', {})
  loadDashboard()
})
</script>

<style scoped>
.stat-card {
  border-radius: var(--radius-base);
}

.stat-card-content {
  display: flex;
  align-items: center;
  gap: 16px;
}

.stat-icon {
  width: 56px;
  height: 56px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.stat-info {
  flex: 1;
}

.stat-icon-primary {
  background: var(--color-primary-bg);
  color: var(--color-primary);
}

.stat-icon-warning {
  background: var(--color-secondary-bg);
  color: var(--color-warning);
}

.stat-icon-success {
  background: var(--color-muted-bg);
  color: var(--color-primary-dark);
}

.stat-icon-danger {
  background: var(--color-accent-bg);
  color: var(--color-danger);
}

.stat-value {
  font-size: 28px;
  font-weight: 700;
  color: var(--color-text-primary);
  line-height: 1.2;
}

.stat-label {
  font-size: 13px;
  color: var(--color-text-secondary);
  margin-top: 4px;
}

.rank-card {
  border-radius: var(--radius-base);
}

.rank-header {
  font-size: 15px;
  font-weight: 600;
  color: var(--color-text-primary);
}
</style>
