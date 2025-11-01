<template>
  <div class="leaderboard">
    <el-container>
      <el-header>
        <h1>🏆 排行榜</h1>
        <el-button @click="$router.push('/')">返回首页</el-button>
      </el-header>
      <el-main>
        <el-table :data="leaderboardList" style="width: 100%" v-loading="loading">
          <el-table-column type="index" label="排名" width="80" align="center">
            <template slot-scope="scope">
              <span v-if="scope.$index < 3" class="medal">
                {{ ['🥇', '🥈', '🥉'][scope.$index] }}
              </span>
              <span v-else>{{ scope.$index + 1 }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="username" label="用户名" width="200"></el-table-column>
          <el-table-column prop="score" label="分数" width="150" align="right">
            <template slot-scope="scope">
              {{ formatScore(scope.row.score) }}
            </template>
          </el-table-column>
          <el-table-column prop="level" label="等级" width="100" align="center"></el-table-column>
          <el-table-column prop="recordDate" label="记录时间" width="180"></el-table-column>
        </el-table>
        <el-pagination
          v-if="total > pageSize"
          @current-change="handlePageChange"
          :current-page="currentPage"
          :page-size="pageSize"
          :total="total"
          layout="total, prev, pager, next"
          style="margin-top: 20px; text-align: center;"
        />
      </el-main>
    </el-container>
  </div>
</template>

<script>
import { getLeaderboard } from '@/api/game'
import { formatScore } from '@/utils'

export default {
  name: 'Leaderboard',
  data() {
    return {
      leaderboardList: [],
      loading: false,
      currentPage: 1,
      pageSize: 20,
      total: 0
    }
  },
  methods: {
    formatScore,
    async loadLeaderboard() {
      this.loading = true
      try {
        const res = await getLeaderboard({
          page: this.currentPage,
          size: this.pageSize
        })
        if (res.code === 200) {
          this.leaderboardList = res.data.list
          this.total = res.data.total
        }
      } catch (error) {
        this.$message.error('加载排行榜失败')
      } finally {
        this.loading = false
      }
    },
    handlePageChange(page) {
      this.currentPage = page
      this.loadLeaderboard()
    }
  },
  mounted() {
    this.loadLeaderboard()
  }
}
</script>

<style scoped>
.leaderboard {
  width: 100%;
  min-height: 100vh;
  background: #f5f5f5;
  padding: 20px;
}

.el-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: white;
  margin-bottom: 20px;
  border-radius: 4px;
  padding: 0 30px;
}

.el-header h1 {
  margin: 0;
  font-size: 24px;
}

.el-main {
  background: white;
  border-radius: 4px;
  padding: 30px;
}

.medal {
  font-size: 24px;
}
</style>

