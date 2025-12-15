<template>
  <div class="leaderboard-page">
    <!-- 背景效果 -->
    <div class="stars" ref="starsContainer"></div>
    <div class="background-glow"></div>

    <!-- 导航栏 -->
    <div class="navbar">
      <div class="nav-left">
        <el-button 
          class="nav-btn back-btn"
          icon="el-icon-arrow-left"
          @click="$router.push('/')"
          circle
        ></el-button>
        <h1 class="page-title">
          <i class="el-icon-trophy title-icon"></i>
          排行榜
        </h1>
      </div>
      <div class="nav-right">
        <div class="stats-info">
          <span>共 {{ totalUsers || 0 }} 名玩家</span>
        </div>
      </div>
    </div>

    <!-- 主要内容区域 - 居中显示 -->
    <div class="main-container">
      <div class="leaderboard-content">
        <!-- 加载状态 -->
        <div v-if="loading" class="loading-wrapper">
          <i class="el-icon-loading loading-icon"></i>
          <p>加载中...</p>
        </div>

        <!-- 排行榜列表 -->
        <div v-else class="leaderboard-list">
          <!-- 前10名列表 -->
          <transition-group name="fade-in-up" tag="div">
            <div
              v-for="(item, index) in topList"
              :key="'top-' + item.id"
              class="rank-card"
              :class="[getRankClass(index), { 'current-user-in-top': isCurrentUser(item.userId) }]"
              @mouseenter="handleCardHover($event)"
              @mouseleave="handleCardLeave($event)"
            >
              <!-- 排名标识 -->
              <div class="rank-badge" :class="getRankBadgeClass(index)">
                <span v-if="index < 3" class="medal-icon">
                  {{ ['🥇', '🥈', '🥉'][index] }}
                </span>
                <span v-else class="rank-number">#{{ index + 1 }}</span>
              </div>

              <!-- 玩家信息 -->
              <div class="player-info">
                <div class="player-avatar">
                  <i class="el-icon-user-solid"></i>
                </div>
                <div class="player-details">
                  <h3 class="player-name">
                    {{ item.username }}
                    <span v-if="isCurrentUser(item.userId)" class="me-badge">我</span>
                  </h3>
                  <div class="player-meta">
                    <span class="meta-item">
                      <i class="el-icon-star-on"></i>
                      等级 {{ item.level || 1 }}
                    </span>
                    <span class="meta-divider">•</span>
                    <span class="meta-item">
                      <i class="el-icon-time"></i>
                      {{ formatTime(item.recordDate) }}
                    </span>
                  </div>
                </div>
              </div>

              <!-- 得分信息 -->
              <div class="score-info">
                <div class="score-label">得分</div>
                <div class="score-value">{{ formatScore(item.score) }}</div>
              </div>

              <!-- 卡片光晕 -->
              <div class="card-glow"></div>
            </div>
          </transition-group>

          <!-- 分隔线（如果当前用户不在前10名内） -->
          <div v-if="currentUser && !isCurrentUserInTop10" class="divider-section">
            <div class="divider-line"></div>
            <div class="divider-text">我的排名</div>
            <div class="divider-line"></div>
          </div>

          <!-- 当前用户记录（如果不在前10名内） -->
          <div
            v-if="currentUser && !isCurrentUserInTop10"
            class="rank-card current-user-card"
            @mouseenter="handleCardHover($event)"
            @mouseleave="handleCardLeave($event)"
          >
            <!-- 排名标识 -->
            <div class="rank-badge badge-current-user">
              <span class="rank-number">#{{ currentUserRank }}</span>
            </div>

            <!-- 玩家信息 -->
            <div class="player-info">
              <div class="player-avatar">
                <i class="el-icon-user-solid"></i>
              </div>
              <div class="player-details">
                <h3 class="player-name">{{ currentUser.username }} <span class="me-badge">我</span></h3>
                <div class="player-meta">
                  <span class="meta-item">
                    <i class="el-icon-star-on"></i>
                    等级 {{ currentUser.level || 1 }}
                  </span>
                  <span class="meta-divider">•</span>
                  <span class="meta-item">
                    <i class="el-icon-time"></i>
                    {{ formatTime(currentUser.recordDate) }}
                  </span>
                </div>
              </div>
            </div>

            <!-- 得分信息 -->
            <div class="score-info">
              <div class="score-label">得分</div>
              <div class="score-value">{{ formatScore(currentUser.score) }}</div>
            </div>

            <!-- 卡片光晕 -->
            <div class="card-glow"></div>
          </div>

          <!-- 空状态 -->
          <div v-if="!loading && topList.length === 0" class="empty-state">
            <i class="el-icon-trophy empty-icon"></i>
            <p>暂无排行数据</p>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { getLeaderboard } from '@/api/game'
import { formatScore } from '@/utils'

export default {
  name: 'Leaderboard',
  data() {
    return {
      topList: [],
      currentUser: null,
      currentUserRank: null,
      totalUsers: 0,
      loading: false
    }
  },
  computed: {
    // 判断当前用户是否在前10名内
    isCurrentUserInTop10() {
      if (!this.currentUser) return false
      return this.topList.some(item => item.userId === this.currentUser.userId)
    }
  },
  methods: {
    formatScore,
    // 判断是否是当前用户
    isCurrentUser(userId) {
      return this.currentUser && this.currentUser.userId === userId
    },
    async loadLeaderboard() {
      this.loading = true
      try {
        const res = await getLeaderboard()
        if (res.code === 200) {
          const data = res.data
          this.topList = data.topList || []
          this.currentUser = data.currentUser || null
          this.currentUserRank = data.currentUserRank || null
          this.totalUsers = data.totalUsers || 0
        }
      } catch (error) {
        this.$message.error('加载排行榜失败')
      } finally {
        this.loading = false
      }
    },
    // 获取排名样式类
    getRankClass(index) {
      if (index === 0) return 'rank-first'
      if (index === 1) return 'rank-second'
      if (index === 2) return 'rank-third'
      return ''
    },
    // 获取排名徽章样式类
    getRankBadgeClass(index) {
      if (index === 0) return 'badge-gold'
      if (index === 1) return 'badge-silver'
      if (index === 2) return 'badge-bronze'
      return 'badge-normal'
    },
    // 格式化时间
    formatTime(timeStr) {
      if (!timeStr) return ''
      const date = new Date(timeStr)
      const now = new Date()
      const diff = now - date
      
      // 小于1小时
      if (diff < 3600000) return `${Math.floor(diff / 60000)}分钟前`
      // 小于1天
      if (diff < 86400000) return `${Math.floor(diff / 3600000)}小时前`
      // 小于7天
      if (diff < 604800000) return `${Math.floor(diff / 86400000)}天前`
      
      // 超过7天显示具体日期
      return date.toLocaleDateString('zh-CN', {
        month: '2-digit',
        day: '2-digit'
      })
    },
    // 卡片悬停效果
    handleCardHover(event) {
      const card = event.currentTarget
      card.style.transform = 'translateY(-5px) scale(1.02)'
    },
    handleCardLeave(event) {
      const card = event.currentTarget
      card.style.transform = 'translateY(0) scale(1)'
    },
    // 创建星空背景
    createStars() {
      const starContainer = this.$refs.starsContainer
      if (!starContainer) {
        setTimeout(() => this.createStars(), 100)
        return
      }
      
      starContainer.innerHTML = ''
      
      for (let i = 0; i < 120; i++) {
        const star = document.createElement('div')
        star.style.position = 'absolute'
        star.style.left = Math.random() * 100 + '%'
        star.style.top = Math.random() * 100 + '%'
        const size = Math.random() * 2 + 1
        star.style.width = size + 'px'
        star.style.height = size + 'px'
        star.style.borderRadius = '50%'
        star.style.background = 'white'
        star.style.boxShadow = '0 0 4px rgba(255, 255, 255, 1)'
        star.style.animationDelay = Math.random() * 3 + 's'
        const twinkleSpeed = Math.random() * 2 + 1
        star.style.animation = `star-twinkle ${twinkleSpeed}s infinite alternate`
        star.style.opacity = '0.8'
        star.style.pointerEvents = 'none'
        starContainer.appendChild(star)
      }
    }
  },
  mounted() {
    this.loadLeaderboard()
    this.createStars()
  }
}
</script>

<style scoped>
/* 页面容器 */
.leaderboard-page {
  width: 100%;
  min-height: 100vh;
  position: relative;
  overflow: hidden;
  /* 与主页面一致的背景 */
  background: linear-gradient(135deg, #0c0c2e 0%, #1a1a3e 30%, #2d1b69 60%, #4a148c 100%);
  background-size: 200% 200%;
  animation: gradientMove 15s ease infinite;
  color: white;
}

@keyframes gradientMove {
  0% { background-position: 0% 50%; }
  50% { background-position: 100% 50%; }
  100% { background-position: 0% 50%; }
}

/* 星空背景 */
.stars {
  position: fixed;
  top: 0;
  left: 0;
  width: 100vw;
  height: 100vh;
  pointer-events: none;
  z-index: 1;
  overflow: hidden;
}

/* 背景光晕效果 */
.background-glow {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: 
    radial-gradient(circle at 20% 30%, rgba(102, 126, 234, 0.3) 0%, transparent 50%),
    radial-gradient(circle at 80% 70%, rgba(138, 43, 226, 0.3) 0%, transparent 50%),
    radial-gradient(circle at 50% 50%, rgba(0, 191, 255, 0.2) 0%, transparent 50%);
  animation: glowPulse 8s ease-in-out infinite;
  pointer-events: none;
  z-index: 0;
}

@keyframes glowPulse {
  0%, 100% { opacity: 0.5; transform: scale(1); }
  50% { opacity: 0.8; transform: scale(1.1); }
}

/* 导航栏 */
.navbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 50px;
  background: rgba(0, 0, 0, 0.2);
  backdrop-filter: blur(10px);
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
  position: relative;
  z-index: 100;
}

.nav-left {
  display: flex;
  align-items: center;
  gap: 20px;
}

.back-btn {
  background: rgba(255, 255, 255, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.2);
  color: white;
  transition: all 0.3s ease;
}

.back-btn:hover {
  background: rgba(255, 255, 255, 0.2);
  transform: translateX(-3px);
  box-shadow: 0 5px 15px rgba(102, 126, 234, 0.4);
}

.page-title {
  margin: 0;
  font-size: 28px;
  font-weight: 700;
  display: flex;
  align-items: center;
  gap: 10px;
  background: linear-gradient(135deg, #fff 0%, #ffd700 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.title-icon {
  font-size: 32px;
  color: #ffd700;
  -webkit-text-fill-color: #ffd700;
  animation: rotateIcon 3s ease-in-out infinite;
}

@keyframes rotateIcon {
  0%, 100% { transform: rotate(0deg); }
  50% { transform: rotate(15deg); }
}

.stats-info {
  color: rgba(255, 255, 255, 0.8);
  font-size: 14px;
}

/* 主容器 - 居中显示 */
.main-container {
  position: relative;
  z-index: 10;
  padding: 40px 20px 80px;
  min-height: calc(100vh - 80px);
  display: flex;
  justify-content: center;
  align-items: flex-start;
}

.leaderboard-content {
  width: 100%;
  max-width: 1000px;
}

/* 加载状态 */
.loading-wrapper {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 100px 20px;
  color: rgba(255, 255, 255, 0.8);
}

.loading-icon {
  font-size: 48px;
  margin-bottom: 20px;
  animation: rotate 1s linear infinite;
}

@keyframes rotate {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

/* 排行榜列表 */
.leaderboard-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

/* 排名卡片 - 毛玻璃效果 */
.rank-card {
  position: relative;
  background: rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(20px);
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-radius: 16px;
  padding: 20px 24px;
  display: flex;
  align-items: center;
  gap: 20px;
  cursor: pointer;
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  overflow: hidden;
  animation: fadeInUp 0.6s ease-out;
  animation-fill-mode: both;
}

.rank-card:nth-child(1) { animation-delay: 0.1s; }
.rank-card:nth-child(2) { animation-delay: 0.15s; }
.rank-card:nth-child(3) { animation-delay: 0.2s; }
.rank-card:nth-child(4) { animation-delay: 0.25s; }
.rank-card:nth-child(5) { animation-delay: 0.3s; }

@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.rank-card:hover {
  background: rgba(255, 255, 255, 0.15);
  border-color: rgba(255, 255, 255, 0.4);
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.3);
}

.rank-card:hover .card-glow {
  opacity: 1;
}

/* 前三名特殊样式 */
.rank-first {
  background: rgba(255, 215, 0, 0.15);
  border-color: rgba(255, 215, 0, 0.3);
  box-shadow: 0 0 30px rgba(255, 215, 0, 0.2);
}

.rank-second {
  background: rgba(192, 192, 192, 0.15);
  border-color: rgba(192, 192, 192, 0.3);
  box-shadow: 0 0 25px rgba(192, 192, 192, 0.2);
}

.rank-third {
  background: rgba(205, 127, 50, 0.15);
  border-color: rgba(205, 127, 50, 0.3);
  box-shadow: 0 0 25px rgba(205, 127, 50, 0.2);
}

/* 排名徽章 */
.rank-badge {
  width: 60px;
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 12px;
  font-weight: 700;
  flex-shrink: 0;
}

.badge-gold {
  background: linear-gradient(135deg, #ffd700 0%, #ffed4e 100%);
  box-shadow: 0 0 20px rgba(255, 215, 0, 0.5);
}

.badge-silver {
  background: linear-gradient(135deg, #c0c0c0 0%, #e8e8e8 100%);
  box-shadow: 0 0 20px rgba(192, 192, 192, 0.5);
}

.badge-bronze {
  background: linear-gradient(135deg, #cd7f32 0%, #e6a857 100%);
  box-shadow: 0 0 20px rgba(205, 127, 50, 0.5);
}

.badge-normal {
  background: rgba(255, 255, 255, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.2);
}

.medal-icon {
  font-size: 32px;
}

.rank-number {
  font-size: 18px;
  color: rgba(255, 255, 255, 0.9);
}

.badge-gold .rank-number,
.badge-silver .rank-number,
.badge-bronze .rank-number {
  color: rgba(0, 0, 0, 0.8);
}

/* 玩家信息 */
.player-info {
  flex: 1;
  display: flex;
  align-items: center;
  gap: 16px;
}

.player-avatar {
  width: 50px;
  height: 50px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.15);
  border: 2px solid rgba(255, 255, 255, 0.2);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  color: #a8b5ff;
  flex-shrink: 0;
}

.player-details {
  flex: 1;
}

.player-name {
  margin: 0 0 8px;
  font-size: 18px;
  font-weight: 600;
  color: white;
}

.player-meta {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 14px;
  color: rgba(255, 255, 255, 0.7);
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 4px;
}

.meta-divider {
  color: rgba(255, 255, 255, 0.4);
}

/* 得分信息 */
.score-info {
  text-align: right;
  flex-shrink: 0;
}

.score-label {
  font-size: 12px;
  color: rgba(255, 255, 255, 0.6);
  margin-bottom: 4px;
}

.score-value {
  font-size: 24px;
  font-weight: 700;
  color: #ffd700;
  text-shadow: 0 0 10px rgba(255, 215, 0, 0.5);
}

.rank-first .score-value {
  color: #ffd700;
  text-shadow: 0 0 15px rgba(255, 215, 0, 0.8);
}

.rank-second .score-value {
  color: #c0c0c0;
  text-shadow: 0 0 10px rgba(192, 192, 192, 0.6);
}

.rank-third .score-value {
  color: #cd7f32;
  text-shadow: 0 0 10px rgba(205, 127, 50, 0.6);
}

/* 卡片光晕效果 */
.card-glow {
  position: absolute;
  top: -50%;
  left: -50%;
  width: 200%;
  height: 200%;
  background: radial-gradient(circle, rgba(168, 181, 255, 0.2) 0%, transparent 70%);
  opacity: 0;
  transition: opacity 0.4s ease;
  pointer-events: none;
}

/* 分隔线区域 */
.divider-section {
  display: flex;
  align-items: center;
  gap: 16px;
  margin: 30px 0;
  padding: 0 24px;
}

.divider-line {
  flex: 1;
  height: 1px;
  background: linear-gradient(90deg, 
    transparent 0%, 
    rgba(255, 255, 255, 0.3) 50%, 
    transparent 100%);
}

.divider-text {
  color: rgba(255, 255, 255, 0.6);
  font-size: 14px;
  white-space: nowrap;
}

/* 当前用户在前10名内的特殊样式 */
.current-user-in-top {
  background: rgba(102, 126, 234, 0.2) !important;
  border-color: rgba(102, 126, 234, 0.5) !important;
  box-shadow: 0 0 30px rgba(102, 126, 234, 0.4) !important;
  animation: pulse-glow 2s ease-in-out infinite;
}

/* 当前用户卡片特殊样式（不在前10名内） */
.current-user-card {
  background: rgba(102, 126, 234, 0.2) !important;
  border-color: rgba(102, 126, 234, 0.5) !important;
  box-shadow: 0 0 30px rgba(102, 126, 234, 0.4) !important;
  animation: pulse-glow 2s ease-in-out infinite;
}

@keyframes pulse-glow {
  0%, 100% {
    box-shadow: 0 0 30px rgba(102, 126, 234, 0.4);
  }
  50% {
    box-shadow: 0 0 40px rgba(102, 126, 234, 0.6);
  }
}

.badge-current-user {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  box-shadow: 0 0 20px rgba(102, 126, 234, 0.5);
}

.badge-current-user .rank-number {
  color: white;
  font-weight: 700;
}

.me-badge {
  display: inline-block;
  padding: 2px 8px;
  margin-left: 8px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 10px;
  font-size: 12px;
  font-weight: 600;
  color: white;
  box-shadow: 0 2px 8px rgba(102, 126, 234, 0.4);
}

/* 空状态 */
.empty-state {
  text-align: center;
  padding: 100px 20px;
  color: rgba(255, 255, 255, 0.6);
}

.empty-icon {
  font-size: 64px;
  margin-bottom: 20px;
  opacity: 0.5;
}

/* 分页 */
.pagination-wrapper {
  margin-top: 40px;
  display: flex;
  justify-content: center;
}

::v-deep .el-pagination {
  color: white;
}

::v-deep .el-pagination .el-pager li {
  background: rgba(255, 255, 255, 0.1);
  color: white;
  border: 1px solid rgba(255, 255, 255, 0.2);
}

::v-deep .el-pagination .el-pager li:hover {
  color: #a8b5ff;
}

::v-deep .el-pagination .el-pager li.active {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-color: transparent;
  color: white;
}

::v-deep .el-pagination .btn-prev,
::v-deep .el-pagination .btn-next {
  background: rgba(255, 255, 255, 0.1);
  color: white;
  border: 1px solid rgba(255, 255, 255, 0.2);
}

::v-deep .el-pagination .btn-prev:hover,
::v-deep .el-pagination .btn-next:hover {
  color: #a8b5ff;
}

::v-deep .el-pagination .el-pagination__total {
  color: rgba(255, 255, 255, 0.8);
}

/* 响应式设计 */
@media (max-width: 768px) {
  .navbar {
    padding: 15px 20px;
  }

  .page-title {
    font-size: 20px;
  }

  .main-container {
    padding: 20px 15px 60px;
  }

  .rank-card {
    padding: 16px;
    gap: 12px;
  }

  .rank-badge {
    width: 50px;
    height: 50px;
  }

  .player-name {
    font-size: 16px;
  }

  .score-value {
    font-size: 20px;
  }
}
</style>

