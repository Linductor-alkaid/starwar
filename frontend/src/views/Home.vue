<template>
  <div class="home">
    <div class="stars" ref="starsContainer"></div>
    <div class="background-glow"></div>
    
    <!-- 导航栏 -->
    <div class="navbar">
      <div class="logo-section">
        <div class="logo-icon">🚀</div>
        <h1 class="logo-text">星际防卫战</h1>
      </div>
      <div class="header-actions">
        <template v-if="!isLoggedIn">
          <el-button 
            class="nav-btn login-btn" 
            @click="$router.push('/login')"
            icon="el-icon-user"
          >
            登录
          </el-button>
          <el-button 
            type="primary" 
            class="nav-btn register-btn"
            @click="$router.push('/register')"
            icon="el-icon-edit"
          >
            注册
          </el-button>
        </template>
        <template v-else>
          <div class="user-info">
            <i class="el-icon-user-solid user-icon"></i>
            <span class="username">欢迎，{{ username }}</span>
          </div>
          <el-button 
            class="nav-btn logout-btn"
            @click="handleLogout"
            icon="el-icon-switch-button"
          >
            退出
          </el-button>
        </template>
      </div>
    </div>

    <!-- 主要内容区域 -->
    <div class="main-content">
      <!-- 欢迎区域 -->
      <div class="welcome-section">
        <div class="title-wrapper">
          <h2 class="main-title">
            <span class="title-line">欢迎来到</span>
            <span class="title-highlight">星际防卫战</span>
          </h2>
          <p class="subtitle">保卫地球，击败外星入侵者！</p>
        </div>

        <!-- 功能卡片 -->
        <div class="feature-cards">
          <div 
            class="feature-card game-card" 
            @click="$router.push('/game')"
            @mouseenter="handleCardHover($event, 'game')"
            @mouseleave="handleCardLeave($event)"
          >
            <div class="card-icon">
              <i class="el-icon-video-camera"></i>
            </div>
            <h3 class="card-title">开始游戏</h3>
            <p class="card-desc">进入激动人心的太空射击之旅</p>
            <div class="card-glow"></div>
          </div>

          <div 
            class="feature-card leaderboard-card"
            @click="$router.push('/leaderboard')"
            @mouseenter="handleCardHover($event, 'leaderboard')"
            @mouseleave="handleCardLeave($event)"
          >
            <div class="card-icon">
              <i class="el-icon-trophy"></i>
            </div>
            <h3 class="card-title">排行榜</h3>
            <p class="card-desc">查看全球玩家的最高得分</p>
            <div class="card-glow"></div>
          </div>

          <div 
            class="feature-card forum-card"
            @click="$router.push('/forum')"
            @mouseenter="handleCardHover($event, 'forum')"
            @mouseleave="handleCardLeave($event)"
          >
            <div class="card-icon">
              <i class="el-icon-chat-line-square"></i>
            </div>
            <h3 class="card-title">讨论区</h3>
            <p class="card-desc">与其他玩家分享游戏心得</p>
            <div class="card-glow"></div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { mapGetters } from 'vuex'

export default {
  name: 'Home',
  computed: {
    ...mapGetters('user', ['isLoggedIn', 'username'])
  },
  mounted() {
    // 延迟创建，确保DOM已渲染
    this.$nextTick(() => {
      setTimeout(() => {
        this.createStars()
        const container = this.$refs.starsContainer
        if (container) {
          console.log('✅ Home stars container found')
        } else {
          console.error('❌ Home stars container not found!')
        }
      }, 100)
    })
  },
  methods: {
    handleLogout() {
      this.$store.dispatch('user/logout')
      this.$message.success('已退出登录')
      // 如果当前不在首页，才进行导航
      if (this.$route.path !== '/') {
        this.$router.push('/')
      }
    },
    // 卡片悬停效果
    handleCardHover(event, type) {
      const card = event.currentTarget
      card.style.transform = 'translateY(-10px) scale(1.05)'
    },
    handleCardLeave(event) {
      const card = event.currentTarget
      card.style.transform = 'translateY(0) scale(1)'
    },
    createStars() {
      const starContainer = this.$refs.starsContainer
      if (!starContainer) {
        setTimeout(() => this.createStars(), 100)
        return
      }
      
      starContainer.innerHTML = ''
      
      // 创建150颗星星（主页面可以多一些）
      for (let i = 0; i < 150; i++) {
        const star = document.createElement('div')
        
        // 设置所有样式为内联样式（因为scoped CSS无法应用到动态元素）
        star.style.position = 'absolute'
        star.style.left = Math.random() * 100 + '%'
        star.style.top = Math.random() * 100 + '%'
        
        // 增大尺寸，提高可见性（3-6px）
        const size = Math.random() * 3 + 3
        star.style.width = size + 'px'
        star.style.height = size + 'px'
        star.style.borderRadius = '50%'
        star.style.background = 'white'
        
        // 发光效果
        star.style.boxShadow = '0 0 6px rgba(255, 255, 255, 1), 0 0 12px rgba(255, 255, 255, 0.8), 0 0 18px rgba(255, 255, 255, 0.4)'
        
        // 动画设置 - 使用缓慢的轨道运动
        star.style.animationDelay = Math.random() * 10 + 's'
        const twinkleSpeed = Math.random() * 2 + 1.5
        // 轨道运动速度：30-60秒一个周期（非常缓慢，像真实太空中的运动）
        const orbitSpeed = Math.random() * 30 + 30
        
        // 使用闪烁和轨道运动动画（ease-in-out让运动更自然）
        star.style.animation = `star-twinkle ${twinkleSpeed}s infinite alternate, star-orbit ${orbitSpeed}s infinite ease-in-out`
        
        star.style.opacity = '1'
        star.style.pointerEvents = 'none'
        star.style.willChange = 'opacity, transform'
        star.style.transformOrigin = 'center center'
        
        starContainer.appendChild(star)
      }
      
      console.log('✅ Home stars created:', starContainer.children.length)
    }
  }
}
</script>

<style scoped>
.home {
  width: 100%;
  min-height: 100vh;
  background: linear-gradient(135deg, #0c0c2e 0%, #1a1a3e 30%, #2d1b69 60%, #4a148c 100%);
  background-size: 200% 200%;
  animation: gradientMove 15s ease infinite;
  color: white;
  position: relative;
  overflow: hidden;
}

@keyframes gradientMove {
  0% { background-position: 0% 50%; }
  50% { background-position: 100% 50%; }
  100% { background-position: 0% 50%; }
}

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

.star {
  position: absolute;
  background: white;
  border-radius: 50%;
  animation: twinkle 2s infinite alternate, float 20s infinite linear;
  /* 增强发光效果 */
  box-shadow: 0 0 6px rgba(255, 255, 255, 1), 
              0 0 12px rgba(255, 255, 255, 0.8),
              0 0 18px rgba(255, 255, 255, 0.4);
  will-change: opacity, transform;
  /* 确保星星足够亮 */
  opacity: 1;
}

@keyframes twinkle {
  0% { opacity: 0.6; }
  100% { opacity: 1; }
}

@keyframes float {
  0% {
    transform: translateY(-100%);
    opacity: 0;
  }
  10% {
    opacity: 1;
  }
  90% {
    opacity: 1;
  }
  100% {
    transform: translateY(100vh);
    opacity: 0;
  }
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

.logo-section {
  display: flex;
  align-items: center;
  gap: 15px;
}

.logo-icon {
  font-size: 36px;
  animation: floatIcon 3s ease-in-out infinite;
}

@keyframes floatIcon {
  0%, 100% { transform: translateY(0) rotate(0deg); }
  50% { transform: translateY(-10px) rotate(10deg); }
}

.logo-text {
  margin: 0;
  font-size: 32px;
  font-weight: 700;
  background: linear-gradient(135deg, #fff 0%, #a8b5ff 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  text-shadow: 0 0 30px rgba(168, 181, 255, 0.5);
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 15px;
}

.nav-btn {
  padding: 10px 20px;
  border-radius: 20px;
  transition: all 0.3s ease;
  border: 1px solid rgba(255, 255, 255, 0.2);
}

.nav-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 5px 15px rgba(102, 126, 234, 0.4);
}

.login-btn {
  background: rgba(255, 255, 255, 0.1);
  color: white;
}

.register-btn {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 8px 20px;
  background: rgba(255, 255, 255, 0.1);
  border-radius: 20px;
  backdrop-filter: blur(10px);
}

.user-icon {
  font-size: 18px;
  color: #a8b5ff;
}

.username {
  font-size: 16px;
  font-weight: 500;
}

/* 主要内容区域 */
.main-content {
  position: relative;
  z-index: 10;
  padding: 80px 50px;
  min-height: calc(100vh - 80px);
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
}

/* 欢迎区域 */
.welcome-section {
  text-align: center;
  width: 100%;
  max-width: 1200px;
}

.title-wrapper {
  margin-bottom: 60px;
  animation: fadeInUp 1s ease-out;
}

@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(30px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.main-title {
  font-size: 64px;
  font-weight: 800;
  margin: 0 0 20px;
  line-height: 1.2;
}

.title-line {
  display: block;
  color: rgba(255, 255, 255, 0.9);
  font-size: 48px;
  margin-bottom: 10px;
}

.title-highlight {
  display: block;
  background: linear-gradient(135deg, #fff 0%, #a8b5ff 50%, #667eea 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  font-size: 72px;
  text-shadow: 0 0 40px rgba(168, 181, 255, 0.6);
  animation: shimmer 3s ease-in-out infinite;
}

@keyframes shimmer {
  0%, 100% { filter: brightness(1); }
  50% { filter: brightness(1.2); }
}

.subtitle {
  font-size: 24px;
  color: rgba(255, 255, 255, 0.8);
  margin: 0;
  font-weight: 300;
}

/* 功能卡片 */
.feature-cards {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: 30px;
  margin-top: 50px;
  width: 100%;
  max-width: 1200px;
}

.feature-card {
  position: relative;
  background: rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(20px);
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-radius: 20px;
  padding: 40px 30px;
  text-align: center;
  cursor: pointer;
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  overflow: hidden;
  animation: fadeInUp 1s ease-out;
  animation-fill-mode: both;
}

.feature-card:nth-child(1) { animation-delay: 0.2s; }
.feature-card:nth-child(2) { animation-delay: 0.4s; }
.feature-card:nth-child(3) { animation-delay: 0.6s; }

.feature-card:hover {
  background: rgba(255, 255, 255, 0.15);
  border-color: rgba(255, 255, 255, 0.4);
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.3);
}

.feature-card:hover .card-icon {
  transform: scale(1.2) rotate(5deg);
}

.feature-card:hover .card-glow {
  opacity: 1;
}

.card-icon {
  width: 80px;
  height: 80px;
  margin: 0 auto 20px;
  background: rgba(255, 255, 255, 0.1);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 36px;
  transition: all 0.3s ease;
  border: 2px solid rgba(255, 255, 255, 0.2);
}

.game-card .card-icon {
  color: #4facfe;
}

.leaderboard-card .card-icon {
  color: #ffd700;
}

.forum-card .card-icon {
  color: #a8e6cf;
}

.card-title {
  font-size: 24px;
  font-weight: 600;
  margin: 0 0 10px;
  color: white;
}

.card-desc {
  font-size: 16px;
  color: rgba(255, 255, 255, 0.7);
  margin: 0;
  line-height: 1.6;
}

.card-glow {
  position: absolute;
  top: -50%;
  left: -50%;
  width: 200%;
  height: 200%;
  background: radial-gradient(circle, rgba(168, 181, 255, 0.3) 0%, transparent 70%);
  opacity: 0;
  transition: opacity 0.4s ease;
  pointer-events: none;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .navbar {
    padding: 15px 20px;
  }

  .logo-text {
    font-size: 24px;
  }

  .main-content {
    padding: 40px 20px;
  }

  .main-title {
    font-size: 48px;
  }

  .title-line {
    font-size: 36px;
  }

  .title-highlight {
    font-size: 56px;
  }

  .subtitle {
    font-size: 18px;
  }

  .feature-cards {
    grid-template-columns: 1fr;
    gap: 20px;
  }

  .feature-card {
    padding: 30px 20px;
  }
}
</style>

