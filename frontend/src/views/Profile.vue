<template>
  <div class="profile">
    <el-container>
      <el-header>
        <h1>个人中心</h1>
        <el-button @click="$router.push('/')">返回首页</el-button>
      </el-header>
      <el-main>
        <el-card>
          <div slot="header">
            <span>个人信息</span>
          </div>
          <el-form :model="userInfo" label-width="100px" v-if="userInfo">
            <el-form-item label="用户名">
              <el-input v-model="userInfo.username" disabled></el-input>
            </el-form-item>
            <el-form-item label="昵称">
              <el-input v-model="userInfo.nickname"></el-input>
            </el-form-item>
            <el-form-item label="邮箱">
              <el-input v-model="userInfo.email" disabled></el-input>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="handleUpdate">保存修改</el-button>
            </el-form-item>
          </el-form>
        </el-card>

        <el-card style="margin-top: 20px;">
          <div slot="header">
            <span>游戏记录</span>
          </div>
          <el-table :data="gameHistory" v-loading="loading">
            <el-table-column prop="score" label="分数" width="150" align="right"></el-table-column>
            <el-table-column prop="level" label="等级" width="100" align="center"></el-table-column>
            <el-table-column prop="playTime" label="时长" width="120">
              <template slot-scope="scope">
                {{ formatPlayTime(scope.row.playTime) }}
              </template>
            </el-table-column>
            <el-table-column prop="recordDate" label="记录时间" width="180"></el-table-column>
          </el-table>
        </el-card>
      </el-main>
    </el-container>
  </div>
</template>

<script>
import { getHistory } from '@/api/game'
import { updateUserInfo } from '@/api/user'
import { formatPlayTime } from '@/utils'

export default {
  name: 'Profile',
  data() {
    return {
      gameHistory: [],
      loading: false
    }
  },
  computed: {
    userInfo() {
      return this.$store.getters['user/userInfo']
    }
  },
  methods: {
    formatPlayTime,
    async loadGameHistory() {
      this.loading = true
      try {
        const res = await getHistory({ page: 1, size: 10 })
        if (res.code === 200) {
          this.gameHistory = res.data.list
        }
      } catch (error) {
        this.$message.error('加载游戏记录失败')
      } finally {
        this.loading = false
      }
    },
    async handleUpdate() {
      try {
        const res = await updateUserInfo({
          nickname: this.userInfo.nickname
        })
        if (res.code === 200) {
          this.$message.success('更新成功')
          this.$store.dispatch('user/getUserInfo')
        }
      } catch (error) {
        this.$message.error('更新失败')
      }
    }
  },
  mounted() {
    this.loadGameHistory()
  }
}
</script>

<style scoped>
.profile {
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
  max-width: 1200px;
  margin: 0 auto;
}
</style>

