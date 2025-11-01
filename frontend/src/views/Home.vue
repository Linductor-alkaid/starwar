<template>
  <div class="home">
    <div class="stars" ref="starsContainer"></div>
    <el-container>
      <el-header>
        <h1>🚀 星际防卫战</h1>
        <div class="header-actions">
          <el-button v-if="!isLoggedIn" @click="$router.push('/login')">登录</el-button>
          <el-button v-if="!isLoggedIn" type="primary" @click="$router.push('/register')">注册</el-button>
          <template v-else>
            <span class="username">欢迎，{{ username }}</span>
            <el-button @click="handleLogout">退出</el-button>
          </template>
        </div>
      </el-header>
      <el-main>
        <div class="welcome-section">
          <h2>欢迎来到星际防卫战</h2>
          <p>一个激动人心的太空射击游戏</p>
        </div>
        <div class="action-buttons">
          <el-button type="primary" size="large" @click="$router.push('/game')">
            开始游戏
          </el-button>
          <el-button size="large" @click="$router.push('/leaderboard')">
            查看排行榜
          </el-button>
          <el-button size="large" @click="$router.push('/forum')">
            进入讨论区
          </el-button>
        </div>
      </el-main>
    </el-container>
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
      this.$router.push('/')
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
  height: 100vh;
  background: linear-gradient(135deg, #0c0c2e 0%, #1a1a3e 50%, #2d1b69 100%);
  color: white;
  position: relative;
  overflow: hidden;
  /* 确保星星有足够空间 */
  min-height: 100vh;
}

.stars {
  position: fixed;
  top: 0;
  left: 0;
  width: 100vw;
  height: 100vh;
  pointer-events: none;
  z-index: 0;
  overflow: hidden;
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

.el-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 30px;
  background: rgba(0, 0, 0, 0.3);
  position: relative;
  z-index: 10;
}

.el-header h1 {
  margin: 0;
  font-size: 28px;
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 15px;
}

.username {
  margin-right: 10px;
}

.el-main {
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  padding: 50px;
  position: relative;
  z-index: 10;
}

.welcome-section {
  text-align: center;
  margin-bottom: 40px;
}

.welcome-section h2 {
  font-size: 48px;
  margin-bottom: 20px;
}

.welcome-section p {
  font-size: 20px;
  opacity: 0.8;
}

.action-buttons {
  display: flex;
  gap: 20px;
  margin-top: 30px;
}
</style>

