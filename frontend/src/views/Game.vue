<template>
  <div class="game-page">
    <!-- 驾驶舱背景框架 -->
    <div class="cockpit-frame">
      <!-- 顶部仪表盘 -->
      <div class="cockpit-top">
        <div class="hud-line top-line"></div>
        <div class="corner-decor corner-top-left"></div>
        <div class="corner-decor corner-top-right"></div>
        <div class="hud-scanline"></div>
        
        <!-- 仪表盘信息 -->
        <div class="dashboard-info" :class="{ 'low-health-warning': isLowHealth }">
          <!-- 生命值显示 -->
          <div class="info-item health-display">
            <span class="info-label">生命值</span>
            <span class="info-value" :class="{ 'low-health': isLowHealth }">
              {{ health }}%
            </span>
            <div class="health-bar-dashboard">
              <div class="health-fill-dashboard" :style="{ width: health + '%' }"></div>
            </div>
          </div>
          
          <!-- 速度显示 -->
          <div class="info-item speed-display">
            <span class="info-label">速度</span>
            <span class="info-value" :class="{ 'high-speed': Math.abs(playerVelocity) > 5 }">
              {{ formatVelocity(playerVelocity) }}
            </span>
            <div class="speed-bar">
              <div class="speed-fill" :style="{ width: speedPercentage + '%' }"></div>
            </div>
          </div>
          
          <!-- 弹药显示 -->
          <div class="info-item ammo-display">
            <span class="info-label">弹药</span>
            <span class="info-value" :class="{ 'low-ammo': ammoPercentage < 30 }">
              {{ ammo }} / 999
            </span>
            <div class="ammo-bar">
              <div class="ammo-fill" :style="{ width: ammoPercentage + '%' }"></div>
            </div>
          </div>
          
          <!-- 得分和等级 -->
          <div class="info-item score-display">
            <span class="info-label">得分</span>
            <span class="info-value score-value">
              {{ formatScore(score) }}
            </span>
          </div>
          
          <div class="info-item level-display">
            <span class="info-label">等级</span>
            <span class="info-value level-value">
              {{ level }}
            </span>
          </div>
        </div>
      </div>
      
      <!-- 左侧控制台 -->
      <div class="cockpit-left">
        <div class="control-panel">
          <div class="panel-indicator"></div>
          <div class="panel-indicator"></div>
          <div class="panel-indicator"></div>
        </div>
      </div>
      
      <!-- 右侧控制台 -->
      <div class="cockpit-right">
        <div class="control-panel">
          <div class="panel-indicator"></div>
          <div class="panel-indicator"></div>
          <div class="panel-indicator"></div>
        </div>
      </div>
      
      <!-- 底部控制台 -->
      <div class="cockpit-bottom">
        <div class="hud-line bottom-line"></div>
        <div class="corner-decor corner-bottom-left"></div>
        <div class="corner-decor corner-bottom-right"></div>
        <div class="status-lights">
          <div class="status-light active"></div>
          <div class="status-light active"></div>
          <div class="status-light"></div>
        </div>
      </div>
      
      <!-- 游戏容器 -->
      <div class="game-container">
        <div class="stars" ref="starsContainer"></div>
        <!-- HUD网格效果 -->
        <div class="hud-grid"></div>
        <canvas ref="gameCanvas" width="800" height="600"></canvas>
        
        <!-- 受击警告 - 显示在游戏容器中央 -->
        <transition name="alert-pulse">
          <div v-if="hitWarning" class="warning-alert-center">
            <i class="el-icon-warning"></i>
            <span>警告：受到攻击！</span>
          </div>
        </transition>
        
        <!-- 游戏中的返回按钮 -->
        <el-button 
          v-if="gameState === 'playing'" 
          class="back-btn" 
          icon="el-icon-arrow-left"
          @click="handlePauseAndBack"
          circle
          size="small"
        ></el-button>
      </div>
    </div>

    <!-- 开始界面 - 移到容器外 -->
    <div v-if="gameState === 'start'" class="start-screen">
      <h1>🚀 星际防卫战 🚀</h1>
      <p>保卫地球，击败外星入侵者！</p>
      <el-button type="primary" size="large" @click="startGame" class="game-btn">
        开始游戏
      </el-button>
      <el-button size="large" @click="goHome" class="game-btn-secondary">
        返回首页
      </el-button>
      <div class="instructions">
        <p>🎮 操作方式：</p>
        <p>← → 方向键加速移动飞船（松开后自动减速）</p>
        <p>空格键发射激光（消耗弹药）</p>
        <p>躲避敌人攻击，收集道具！</p>
        <p>🎁 道具说明：</p>
        <p>🟢 绿十字：恢复生命值</p>
        <p>🔵 蓝菱形：补充弹药</p>
        <p>🟡 黄星星：额外得分</p>
      </div>
    </div>

    <!-- 游戏结束界面 - 移到容器外 -->
    <div v-if="gameState === 'gameOver'" class="game-over">
      <h2>💥 游戏结束 💥</h2>
      <p>最终得分: <span>{{ formatScore(score) }}</span></p>
      <p>到达等级: <span>{{ level }}</span></p>
      <p>游戏时长: <span>{{ formatPlayTime(playTime) }}</span></p>
      <div class="game-over-actions">
        <el-button type="primary" @click="handleSaveAndRestart">保存并重新开始</el-button>
        <el-button @click="restartGame">重新开始</el-button>
        <el-button @click="goHome">返回首页</el-button>
      </div>
    </div>
  </div>
</template>

<script>
import { saveRecord } from '@/api/game'
import { mapGetters } from 'vuex'
import { formatScore, formatPlayTime } from '@/utils'

export default {
  name: 'Game',
  data() {
    return {
      canvas: null,
      ctx: null,
      gameState: 'start', // start, playing, gameOver
      score: 0,
      timeScore: 0,
      level: 1,
      health: 100,
      ammo: 20,
      playTime: 0,
      lastTime: 0,
      gameStartTime: 0,
      deltascore: 0,
      
      // 游戏对象
      player: {
        x: 375,
        y: 520,
        width: 50,
        height: 40,
        velocity: 0, // 当前速度（横向）
        maxSpeed: 8, // 最大速度
        acceleration: 0.5, // 加速度
        deceleration: 1.2, // 减速度（比加速度大）
        color: '#00ffff'
      },
      bullets: [],
      enemies: [],
      enemyBullets: [],
      powerups: [],
      particles: [],
      keys: {},
      animationFrameId: null,
      starCheckInterval: null,
      hitWarning: false, // 受击警告状态
      lastHealth: 100 // 上次生命值，用于检测受击
    }
  },
  computed: {
    ...mapGetters('user', ['isLoggedIn']),
    // 飞船当前速度（格式化）
    playerVelocity() {
      return this.player.velocity || 0
    },
    // 速度百分比（用于显示速度条）
    speedPercentage() {
      const percentage = (Math.abs(this.playerVelocity) / this.player.maxSpeed) * 100
      return Math.min(100, percentage)
    },
    // 弹药百分比
    ammoPercentage() {
      return (this.ammo / 999) * 100
    },
    // 是否低血量（小于30%）
    isLowHealth() {
      return this.health < 30
    }
  },
  watch: {
    // 监听生命值变化，检测受击
    health(newHealth, oldHealth) {
      if (newHealth < oldHealth && this.gameState === 'playing') {
        this.triggerHitWarning()
      }
      this.lastHealth = newHealth
    }
  },
  mounted() {
    this.initCanvas()
    // 延迟创建星空，确保DOM已渲染
    this.$nextTick(() => {
      setTimeout(() => {
        this.createStars()
        // 调试信息
        const container = this.$refs.starsContainer
        if (container) {
          console.log('✅ Game stars created:', container.children.length)
        } else {
          console.error('❌ Game stars container not found!')
        }
      }, 150)
    })
    this.setupKeyboardEvents()
  },
  beforeDestroy() {
    // 清理资源
    if (this.animationFrameId) {
      cancelAnimationFrame(this.animationFrameId)
    }
    if (this.starCheckInterval) {
      clearInterval(this.starCheckInterval)
      this.starCheckInterval = null
    }
    this.removeKeyboardEvents()
  },
  methods: {
    formatScore,
    formatPlayTime,
    initCanvas() {
      this.canvas = this.$refs.gameCanvas
      this.ctx = this.canvas.getContext('2d')
      this.player.x = this.canvas.width / 2 - 25
    },
    
    createStars() {
      const starContainer = this.$refs.starsContainer
      if (!starContainer) {
        // 如果容器还没准备好，延迟重试
        setTimeout(() => this.createStars(), 100)
        return
      }
      
      // 清空现有星星
      starContainer.innerHTML = ''
      
      // 创建100颗星星，从顶部开始向下缓慢移动
      for (let i = 0; i < 100; i++) {
        const star = document.createElement('div')
        
        // 设置所有样式为内联样式（因为scoped CSS无法应用到动态元素）
        star.style.position = 'absolute'
        // 星星从顶部随机位置开始（包括容器上方，让初始时有星星在容器内）
        star.style.left = Math.random() * 100 + '%'
        // 使用像素值，从-10px到600px，确保初始有星星在容器内
        star.style.top = (Math.random() * 610 - 10) + 'px'
        
        // 增大星星尺寸，提高可见性（3-6px）
        const size = Math.random() * 3 + 3
        star.style.width = size + 'px'
        star.style.height = size + 'px'
        star.style.borderRadius = '50%'
        star.style.background = 'white'
        
        // 发光效果
        star.style.boxShadow = '0 0 6px rgba(255, 255, 255, 1), 0 0 12px rgba(255, 255, 255, 0.8), 0 0 18px rgba(255, 255, 255, 0.4)'
        
        // 动画设置 - 使用向下缓慢移动
        star.style.animationDelay = Math.random() * 5 + 's'
        const twinkleSpeed = Math.random() * 2 + 1.5
        // 下落速度：20-35秒缓慢下落（600px高度）
        const fallSpeed = Math.random() * 15 + 20
        
        // 使用闪烁和向下移动动画
        star.style.animation = `star-twinkle ${twinkleSpeed}s infinite alternate, star-fall-game ${fallSpeed}s linear infinite`
        
        star.style.opacity = '1'
        star.style.pointerEvents = 'none'
        star.style.willChange = 'opacity, transform'
        
        starContainer.appendChild(star)
      }
      
      console.log('✅ Stars created:', starContainer.children.length)
      
      // 启动星星循环检查
      this.startStarRecycle()
    },
    
    // 启动星星回收循环
    startStarRecycle() {
      // 如果已有定时器，先清除
      if (this.starCheckInterval) {
        clearInterval(this.starCheckInterval)
      }
      
      // 每500ms检查一次星星位置
      this.starCheckInterval = setInterval(() => {
        this.checkAndRecycleStars()
      }, 500)
    },
    
    // 检查并回收移出容器的星星
    checkAndRecycleStars() {
      const starContainer = this.$refs.starsContainer
      if (!starContainer) return
      
      const containerRect = starContainer.getBoundingClientRect()
      const containerHeight = containerRect.height || 600
      
      // 遍历所有星星
      Array.from(starContainer.children).forEach((star) => {
        const rect = star.getBoundingClientRect()
        
        // 如果星星移出容器底部（容差：50px）
        if (rect.top > containerRect.bottom + 50) {
          // 重置transform，确保从初始位置开始
          star.style.transform = 'translateY(0)'
          
          // 重置到顶部随机位置（-10px到0之间，确保从容器上方开始）
          star.style.top = (Math.random() * 10 - 10) + 'px'
          star.style.left = Math.random() * 100 + '%'
          
          // 强制重新触发动画：先移除动画，再添加
          star.style.animation = 'none'
          // 使用requestAnimationFrame确保DOM更新后再应用新动画
          requestAnimationFrame(() => {
            const twinkleSpeed = Math.random() * 2 + 1.5
            const fallSpeed = Math.random() * 15 + 20 // 20-35秒缓慢下落
            star.style.animation = `star-twinkle ${twinkleSpeed}s infinite alternate, star-fall-game ${fallSpeed}s linear infinite`
          })
        }
      })
    },
    
    setupKeyboardEvents() {
      document.addEventListener('keydown', this.handleKeyDown)
      document.addEventListener('keyup', this.handleKeyUp)
    },
    
    removeKeyboardEvents() {
      document.removeEventListener('keydown', this.handleKeyDown)
      document.removeEventListener('keyup', this.handleKeyUp)
    },
    
    handleKeyDown(e) {
      this.keys[e.key] = true
      if (e.key === ' ') e.preventDefault()
    },
    
    handleKeyUp(e) {
      this.keys[e.key] = false
    },
    
    initGame() {
      this.score = 0
      this.timeScore = 0
      this.level = 1
      this.health = 100
      this.lastHealth = 100
      this.ammo = 20
      this.bullets = []
      this.enemies = []
      this.enemyBullets = []
      this.powerups = []
      this.particles = []
      this.player.x = this.canvas.width / 2 - 25
      this.player.velocity = 0 // 重置速度
      this.hitWarning = false // 重置警告状态
      this.gameStartTime = Date.now()
      this.lastTime = this.gameStartTime
      this.deltascore = 0
      this.playTime = 0
      // 重新创建星空背景
      this.$nextTick(() => {
        this.createStars()
      })
    },
    // 格式化速度显示
    formatVelocity(velocity) {
      const absVelocity = Math.abs(velocity)
      if (absVelocity < 0.1) return '0.0'
      return absVelocity.toFixed(1)
    },
    // 触发受击警告
    triggerHitWarning() {
      this.hitWarning = true
      // 1.5秒后关闭警告
      setTimeout(() => {
        this.hitWarning = false
      }, 1500)
    },
    
    startGame() {
      this.gameState = 'playing'
      this.initGame()
      this.gameLoop()
    },
    
    restartGame() {
      this.gameState = 'playing'
      this.initGame()
      this.gameLoop()
    },
    
    async handleSaveAndRestart() {
      if (this.isLoggedIn && this.score > 0) {
        try {
          await saveRecord({
            score: this.score,
            level: this.level,
            playTime: this.playTime
          })
          this.$message.success('游戏记录已保存')
        } catch (error) {
          this.$message.warning('保存失败，但可以继续游戏')
        }
      } else {
        this.$message.info('请先登录以保存游戏记录')
      }
      this.restartGame()
    },
    
    goHome() {
      // 停止游戏循环
      if (this.gameState === 'playing' && this.animationFrameId) {
        cancelAnimationFrame(this.animationFrameId)
        this.animationFrameId = null
      }
      this.$router.push('/')
    },
    
    handlePauseAndBack() {
      this.$confirm('确定要退出游戏吗？当前进度将不会被保存。', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        this.goHome()
      }).catch(() => {
        // 用户取消
      })
    },
    
    createBullet(x, y) {
      if (this.ammo > 0) {
        this.bullets.push({
          x: x,
          y: y,
          width: 4,
          height: 15,
          speed: 8,
          color: '#ffff00'
        })
        this.ammo--
      }
    },
    
    createEnemy() {
      const types = [
        { width: 40, height: 30, speed: 2, color: '#ff4444', points: 10 },
        { width: 35, height: 25, speed: 3, color: '#ff8844', points: 15 },
        { width: 45, height: 35, speed: 1.5, color: '#ff44ff', points: 20 }
      ]
      
      const type = types[Math.floor(Math.random() * types.length)]
      this.enemies.push({
        x: Math.random() * (this.canvas.width - type.width),
        y: -type.height,
        width: type.width,
        height: type.height,
        speed: type.speed + this.level * 0.5,
        color: type.color,
        points: type.points,
        shootTimer: Math.random() * 120 + 60
      })
    },
    
    createEnemyBullet(x, y) {
      this.enemyBullets.push({
        x: x,
        y: y,
        width: 3,
        height: 10,
        speed: 4,
        color: '#ff6666'
      })
    },
    
    createPowerup(x, y) {
      const rand = Math.random()
      if (rand < 0.4) {
        let type, color
        if (rand < 0.15) {
          type = 'health'
          color = '#00ff00'
        } else if (rand < 0.3) {
          type = 'ammo'
          color = '#00aaff'
        } else {
          type = 'score'
          color = '#ffff00'
        }
        
        this.powerups.push({
          x: x,
          y: y,
          width: 25,
          height: 25,
          speed: 2,
          type: type,
          color: color,
          pulse: 0
        })
      }
    },
    
    createExplosion(x, y) {
      for (let i = 0; i < 8; i++) {
        this.particles.push({
          x: x,
          y: y,
          vx: (Math.random() - 0.5) * 8,
          vy: (Math.random() - 0.5) * 8,
          life: 30,
          color: `hsl(${Math.random() * 60 + 15}, 100%, 70%)`
        })
      }
    },
    
    checkCollision(a, b) {
      return a.x < b.x + b.width &&
             a.x + a.width > b.x &&
             a.y < b.y + b.height &&
             a.y + a.height > b.y
    },
    
    drawPlayer() {
      const pulse = Math.sin(Date.now() * 0.01) * 0.3 + 1
      
      // 主体
      this.ctx.fillStyle = '#00ffff'
      this.ctx.shadowColor = '#00ffff'
      this.ctx.shadowBlur = 15
      this.ctx.beginPath()
      this.ctx.moveTo(this.player.x + this.player.width / 2, this.player.y)
      this.ctx.lineTo(this.player.x, this.player.y + this.player.height)
      this.ctx.lineTo(this.player.x + this.player.width / 4, this.player.y + this.player.height * 0.8)
      this.ctx.lineTo(this.player.x + this.player.width * 0.75, this.player.y + this.player.height * 0.8)
      this.ctx.lineTo(this.player.x + this.player.width, this.player.y + this.player.height)
      this.ctx.closePath()
      this.ctx.fill()
      
      // 驾驶舱
      this.ctx.fillStyle = '#88ffff'
      this.ctx.beginPath()
      this.ctx.ellipse(this.player.x + this.player.width / 2, this.player.y + this.player.height * 0.3, 8, 6, 0, 0, 2 * Math.PI)
      this.ctx.fill()
      
      // 引擎喷火效果
      this.ctx.fillStyle = `rgba(255, 100, 0, ${pulse})`
      this.ctx.shadowColor = '#ff6400'
      this.ctx.shadowBlur = 20
      this.ctx.beginPath()
      this.ctx.ellipse(this.player.x + this.player.width * 0.25, this.player.y + this.player.height + 5, 3, 8 * pulse, 0, 0, 2 * Math.PI)
      this.ctx.fill()
      this.ctx.beginPath()
      this.ctx.ellipse(this.player.x + this.player.width * 0.75, this.player.y + this.player.height + 5, 3, 8 * pulse, 0, 0, 2 * Math.PI)
      this.ctx.fill()
      
      this.ctx.shadowBlur = 0
    },
    
    drawEnemy(enemy) {
      this.ctx.fillStyle = enemy.color
      this.ctx.shadowColor = enemy.color
      this.ctx.shadowBlur = 10
      
      this.ctx.beginPath()
      this.ctx.moveTo(enemy.x + enemy.width / 2, enemy.y + enemy.height)
      this.ctx.lineTo(enemy.x, enemy.y + enemy.height * 0.7)
      this.ctx.lineTo(enemy.x, enemy.y + enemy.height * 0.3)
      this.ctx.lineTo(enemy.x + enemy.width / 2, enemy.y)
      this.ctx.lineTo(enemy.x + enemy.width, enemy.y + enemy.height * 0.3)
      this.ctx.lineTo(enemy.x + enemy.width, enemy.y + enemy.height * 0.7)
      this.ctx.closePath()
      this.ctx.fill()
      
      this.ctx.fillStyle = '#ffaaaa'
      this.ctx.fillRect(enemy.x + enemy.width * 0.2, enemy.y + enemy.height * 0.8, 4, 8)
      this.ctx.fillRect(enemy.x + enemy.width * 0.8 - 4, enemy.y + enemy.height * 0.8, 4, 8)
      
      const pulse = Math.sin(Date.now() * 0.02 + enemy.x) * 0.5 + 0.5
      this.ctx.fillStyle = `rgba(255, 0, 100, ${pulse})`
      this.ctx.shadowColor = '#ff0064'
      this.ctx.shadowBlur = 15
      this.ctx.beginPath()
      this.ctx.ellipse(enemy.x + enemy.width / 2, enemy.y - 3, 6, 4 * pulse, 0, 0, 2 * Math.PI)
      this.ctx.fill()
      
      this.ctx.shadowBlur = 0
    },
    
    drawBullet(bullet) {
      const gradient = this.ctx.createLinearGradient(bullet.x, bullet.y, bullet.x, bullet.y + bullet.height)
      gradient.addColorStop(0, '#ffffff')
      gradient.addColorStop(0.5, bullet.color)
      gradient.addColorStop(1, 'rgba(255, 255, 0, 0.2)')
      
      this.ctx.fillStyle = gradient
      this.ctx.shadowColor = bullet.color
      this.ctx.shadowBlur = 15
      
      this.ctx.beginPath()
      this.ctx.ellipse(bullet.x + bullet.width / 2, bullet.y + bullet.height / 2, bullet.width / 2, bullet.height / 2, 0, 0, 2 * Math.PI)
      this.ctx.fill()
      
      this.ctx.fillStyle = '#ffffff'
      this.ctx.fillRect(bullet.x + 1, bullet.y, bullet.width - 2, bullet.height)
      
      this.ctx.shadowBlur = 0
    },
    
    drawEnemyBullet(bullet) {
      const gradient = this.ctx.createLinearGradient(bullet.x, bullet.y, bullet.x, bullet.y + bullet.height)
      gradient.addColorStop(0, 'rgba(255, 100, 100, 0.2)')
      gradient.addColorStop(0.5, bullet.color)
      gradient.addColorStop(1, '#ffffff')
      
      this.ctx.fillStyle = gradient
      this.ctx.shadowColor = bullet.color
      this.ctx.shadowBlur = 12
      
      this.ctx.beginPath()
      this.ctx.ellipse(bullet.x + bullet.width / 2, bullet.y + bullet.height / 2, bullet.width / 2, bullet.height / 2, 0, 0, 2 * Math.PI)
      this.ctx.fill()
      
      this.ctx.shadowBlur = 0
    },
    
    drawPowerup(powerup) {
      const pulseFactor = Math.sin(powerup.pulse) * 0.3 + 1
      const size = 25 * pulseFactor
      const x = powerup.x - (size - 25) / 2
      const y = powerup.y - (size - 25) / 2
      
      this.ctx.shadowColor = powerup.color
      this.ctx.shadowBlur = 20
      
      if (powerup.type === 'health') {
        this.ctx.fillStyle = powerup.color
        this.ctx.fillRect(x + size * 0.4, y, size * 0.2, size)
        this.ctx.fillRect(x, y + size * 0.4, size, size * 0.2)
        
        this.ctx.fillStyle = '#ffffff'
        this.ctx.fillRect(x + size * 0.45, y + size * 0.1, size * 0.1, size * 0.8)
        this.ctx.fillRect(x + size * 0.1, y + size * 0.45, size * 0.8, size * 0.1)
      } else if (powerup.type === 'ammo') {
        this.ctx.fillStyle = powerup.color
        this.ctx.beginPath()
        this.ctx.moveTo(x + size / 2, y)
        this.ctx.lineTo(x + size, y + size / 2)
        this.ctx.lineTo(x + size / 2, y + size)
        this.ctx.lineTo(x, y + size / 2)
        this.ctx.closePath()
        this.ctx.fill()
        
        this.ctx.fillStyle = '#ffffff'
        this.ctx.beginPath()
        this.ctx.moveTo(x + size / 2, y + size * 0.2)
        this.ctx.lineTo(x + size * 0.8, y + size / 2)
        this.ctx.lineTo(x + size / 2, y + size * 0.8)
        this.ctx.lineTo(x + size * 0.2, y + size / 2)
        this.ctx.closePath()
        this.ctx.fill()
      } else {
        this.ctx.fillStyle = powerup.color
        this.ctx.beginPath()
        for (let i = 0; i < 5; i++) {
          const angle = (i * 4 * Math.PI) / 5
          const radius = i % 2 === 0 ? size / 2 : size / 4
          const px = x + size / 2 + Math.cos(angle) * radius
          const py = y + size / 2 + Math.sin(angle) * radius
          if (i === 0) this.ctx.moveTo(px, py)
          else this.ctx.lineTo(px, py)
        }
        this.ctx.closePath()
        this.ctx.fill()
        
        this.ctx.fillStyle = '#ffffff'
        this.ctx.beginPath()
        this.ctx.ellipse(x + size / 2, y + size / 2, 4, 4, 0, 0, 2 * Math.PI)
        this.ctx.fill()
      }
      
      this.ctx.shadowBlur = 0
    },
    
    drawGame() {
      // 绘制非常透明的背景层，让星星清楚显示
      this.ctx.fillStyle = 'rgba(0, 0, 0, 0.15)'
      this.ctx.fillRect(0, 0, this.canvas.width, this.canvas.height)
      
      this.drawPlayer()
      
      this.bullets.forEach(bullet => {
        this.drawBullet(bullet)
      })
      
      this.enemies.forEach(enemy => {
        this.drawEnemy(enemy)
      })
      
      this.enemyBullets.forEach(bullet => {
        this.drawEnemyBullet(bullet)
      })
      
      this.powerups.forEach(powerup => {
        this.drawPowerup(powerup)
      })
      
      this.particles.forEach(particle => {
        this.ctx.fillStyle = particle.color
        this.ctx.globalAlpha = particle.life / 30
        this.ctx.shadowColor = particle.color
        this.ctx.shadowBlur = 5
        this.ctx.beginPath()
        this.ctx.ellipse(particle.x, particle.y, 2, 2, 0, 0, 2 * Math.PI)
        this.ctx.fill()
        this.ctx.shadowBlur = 0
        this.ctx.globalAlpha = 1
      })
    },
    
    gameLoop() {
      if (this.gameState !== 'playing') return
      
      const currentTime = Date.now()
      const deltaTime = currentTime - this.lastTime
      this.lastTime = currentTime
      
      this.ctx.clearRect(0, 0, this.canvas.width, this.canvas.height)
      
      // 玩家移动 - 使用加速度/减速度机制
      // 基于帧率归一化（假设60fps）
      const frameRate = 60
      const normalizedDelta = deltaTime / (1000 / frameRate)
      
      // 根据按键状态改变速度
      if (this.keys['ArrowLeft']) {
        // 向左加速
        this.player.velocity -= this.player.acceleration * normalizedDelta
      } else if (this.keys['ArrowRight']) {
        // 向右加速
        this.player.velocity += this.player.acceleration * normalizedDelta
      } else {
        // 没有按键时减速（减速度比加速度大，减速更快）
        if (this.player.velocity > 0) {
          this.player.velocity -= this.player.deceleration * normalizedDelta
          if (this.player.velocity < 0) this.player.velocity = 0
        } else if (this.player.velocity < 0) {
          this.player.velocity += this.player.deceleration * normalizedDelta
          if (this.player.velocity > 0) this.player.velocity = 0
        }
      }
      
      // 限制最大速度
      if (this.player.velocity > this.player.maxSpeed) {
        this.player.velocity = this.player.maxSpeed
      } else if (this.player.velocity < -this.player.maxSpeed) {
        this.player.velocity = -this.player.maxSpeed
      }
      
      // 更新位置（velocity是每帧移动的像素数）
      this.player.x += this.player.velocity * normalizedDelta
      
      // 边界检测
      if (this.player.x < 0) {
        this.player.x = 0
        this.player.velocity = 0
      } else if (this.player.x > this.canvas.width - this.player.width) {
        this.player.x = this.canvas.width - this.player.width
        this.player.velocity = 0
      }
      
      // 发射子弹
      if (this.keys[' ']) {
        if (!this.keys.spacePressed && this.ammo > 0) {
          this.createBullet(this.player.x + this.player.width / 2 - 2, this.player.y)
          this.keys.spacePressed = true
        }
      } else {
        this.keys.spacePressed = false
      }
      
      // 更新时间分数
      this.timeScore = Math.floor((currentTime - this.gameStartTime) / 1000)
      this.playTime = this.timeScore
      this.deltascore += deltaTime * 0.001
      
      if (this.deltascore >= 1) {
        this.score += 1
        this.deltascore -= 1
      }
      
      // 生成敌人
      if (Math.random() < 0.02 + this.level * 0.005) {
        this.createEnemy()
      }
      
      // 更新子弹
      this.bullets = this.bullets.filter(bullet => {
        bullet.y -= bullet.speed
        return bullet.y > -bullet.height
      })
      
      // 更新敌人
      this.enemies.forEach(enemy => {
        enemy.y += enemy.speed
        enemy.shootTimer--
        if (enemy.shootTimer <= 0 && Math.random() < 0.005) {
          this.createEnemyBullet(enemy.x + enemy.width / 2, enemy.y + enemy.height)
          enemy.shootTimer = Math.random() * 120 + 60
        }
      })
      this.enemies = this.enemies.filter(enemy => enemy.y < this.canvas.height)
      
      // 更新敌人子弹
      this.enemyBullets = this.enemyBullets.filter(bullet => {
        bullet.y += bullet.speed
        return bullet.y < this.canvas.height
      })
      
      // 更新道具
      this.powerups.forEach(powerup => {
        powerup.y += powerup.speed
        powerup.pulse += 0.2
      })
      this.powerups = this.powerups.filter(powerup => powerup.y < this.canvas.height)
      
      // 更新粒子效果
      this.particles = this.particles.filter(particle => {
        particle.x += particle.vx
        particle.y += particle.vy
        particle.life--
        return particle.life > 0
      })
      
      // 碰撞检测 - 玩家子弹击中敌人
      this.bullets.forEach((bullet, bulletIndex) => {
        this.enemies.forEach((enemy, enemyIndex) => {
          if (this.checkCollision(bullet, enemy)) {
            this.createExplosion(enemy.x + enemy.width / 2, enemy.y + enemy.height / 2)
            this.createPowerup(enemy.x + enemy.width / 2, enemy.y + enemy.height / 2)
            this.score += enemy.points
            this.bullets.splice(bulletIndex, 1)
            this.enemies.splice(enemyIndex, 1)
          }
        })
      })
      
      // 碰撞检测 - 敌人子弹击中玩家
      this.enemyBullets.forEach((bullet, bulletIndex) => {
        if (this.checkCollision(bullet, this.player)) {
          const oldHealth = this.health
          this.health -= 10
          this.enemyBullets.splice(bulletIndex, 1)
          this.createExplosion(this.player.x + this.player.width / 2, this.player.y + this.player.height / 2)
          if (this.health < oldHealth) {
            this.triggerHitWarning()
          }
        }
      })
      
      // 碰撞检测 - 敌人撞击玩家
      this.enemies.forEach((enemy, enemyIndex) => {
        if (this.checkCollision(enemy, this.player)) {
          const oldHealth = this.health
          this.health -= 20
          this.enemies.splice(enemyIndex, 1)
          this.createExplosion(this.player.x + this.player.width / 2, this.player.y + this.player.height / 2)
          if (this.health < oldHealth) {
            this.triggerHitWarning()
          }
        }
      })
      
      // 碰撞检测 - 玩家收集道具
      this.powerups.forEach((powerup, powerupIndex) => {
        if (this.checkCollision(powerup, this.player)) {
          if (powerup.type === 'health') {
            this.health = Math.min(100, this.health + 20)
          } else if (powerup.type === 'ammo') {
            this.ammo = Math.min(999, this.ammo + 5)
          } else {
            this.score += 50
          }
          this.powerups.splice(powerupIndex, 1)
        }
      })
      
      // 升级检测
      const newLevel = Math.floor(this.score / 500) + 1
      if (newLevel > this.level) {
        this.level = newLevel
        this.health = Math.min(100, this.health + 30)
        this.ammo = Math.min(999, this.ammo + 20)
      }
      
      // 绘制所有对象
      this.drawGame()
      
      // 检查游戏结束
      if (this.health <= 0) {
        this.gameState = 'gameOver'
        // 如果用户已登录，尝试自动保存
        if (this.isLoggedIn && this.score > 0) {
          this.handleAutoSave()
        }
        return
      }
      
      this.animationFrameId = requestAnimationFrame(() => this.gameLoop())
    },
    
    async handleAutoSave() {
      try {
        await saveRecord({
          score: this.score,
          level: this.level,
          playTime: this.playTime
        })
      } catch (error) {
        // 静默失败，不打扰用户
      }
    }
  }
}
</script>

<style scoped>
.game-page {
  width: 100%;
  min-height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  /* 与主页面一致的背景 */
  background: linear-gradient(135deg, #0c0c2e 0%, #1a1a3e 30%, #2d1b69 60%, #4a148c 100%);
  background-size: 200% 200%;
  animation: gradientMove 15s ease infinite;
  padding: 20px;
  position: relative;
  overflow: hidden;
}

/* 添加驾驶舱环境的背景光效 */
.game-page::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: 
    radial-gradient(circle at 20% 30%, rgba(0, 191, 255, 0.15) 0%, transparent 40%),
    radial-gradient(circle at 80% 70%, rgba(138, 43, 226, 0.15) 0%, transparent 40%);
  pointer-events: none;
  z-index: 0;
}

@keyframes gradientMove {
  0% { background-position: 0% 50%; }
  50% { background-position: 100% 50%; }
  100% { background-position: 0% 50%; }
}

/* 驾驶舱框架容器 */
.cockpit-frame {
  position: relative;
  width: 900px;
  height: 720px;
  z-index: 10;
}

/* 游戏容器 - 驾驶舱视窗 */
.game-container {
  position: absolute;
  top: 80px;
  left: 50px;
  width: 800px;
  height: 600px;
  background: rgba(0, 0, 0, 0.5);
  backdrop-filter: blur(10px);
  border: 2px solid rgba(0, 191, 255, 0.4);
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 
    0 0 30px rgba(0, 191, 255, 0.3),
    inset 0 0 50px rgba(0, 191, 255, 0.1);
  /* 玻璃反射效果 */
  background-image: 
    linear-gradient(180deg, rgba(0, 191, 255, 0.05) 0%, transparent 20%),
    linear-gradient(0deg, rgba(0, 191, 255, 0.05) 0%, transparent 20%);
  z-index: 1;
}

canvas {
  display: block;
  position: absolute;
  top: 0;
  left: 0;
  z-index: 2;
  /* Canvas完全透明，让星星和背景透过显示 */
  background: transparent !important;
  /* 确保canvas不影响星星 */
  mix-blend-mode: normal;
}

.stars {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  pointer-events: none;
  z-index: 1;
  overflow: hidden;
  /* 确保星星可见 */
  background: transparent;
}

/* HUD网格效果 */
.hud-grid {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  pointer-events: none;
  z-index: 1;
  background-image: 
    linear-gradient(rgba(0, 191, 255, 0.1) 1px, transparent 1px),
    linear-gradient(90deg, rgba(0, 191, 255, 0.1) 1px, transparent 1px);
  background-size: 50px 50px;
  opacity: 0.3;
  animation: gridPulse 3s ease-in-out infinite;
}

@keyframes gridPulse {
  0%, 100% { opacity: 0.2; }
  50% { opacity: 0.4; }
}

/* 驾驶舱顶部 */
.cockpit-top {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 80px;
  background: linear-gradient(180deg, rgba(0, 20, 40, 0.95) 0%, rgba(0, 20, 40, 0.7) 100%);
  border-top-left-radius: 15px;
  border-top-right-radius: 15px;
  border: 2px solid rgba(0, 191, 255, 0.3);
  border-bottom: 1px solid rgba(0, 191, 255, 0.2);
  z-index: 5;
  pointer-events: none;
  display: flex;
  align-items: center;
  justify-content: space-around;
  padding: 0 20px;
}

/* HUD扫描线 */
.hud-line {
  position: absolute;
  width: 100%;
  height: 1px;
  background: linear-gradient(90deg, 
    transparent 0%, 
    rgba(0, 191, 255, 0.6) 20%, 
    rgba(0, 191, 255, 0.8) 50%, 
    rgba(0, 191, 255, 0.6) 80%, 
    transparent 100%);
  box-shadow: 0 0 10px rgba(0, 191, 255, 0.5);
}

.top-line {
  top: 0;
  animation: scanLine 2s ease-in-out infinite;
}

.bottom-line {
  bottom: 0;
  animation: scanLine 2s ease-in-out infinite reverse;
}

@keyframes scanLine {
  0% { opacity: 0.3; }
  50% { opacity: 1; }
  100% { opacity: 0.3; }
}

.hud-scanline {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 2px;
  background: linear-gradient(90deg, 
    transparent 0%, 
    rgba(0, 191, 255, 1) 50%, 
    transparent 100%);
  box-shadow: 0 0 20px rgba(0, 191, 255, 0.8);
  animation: scanMove 3s linear infinite;
}

@keyframes scanMove {
  0% { top: 0; opacity: 1; }
  100% { top: 100%; opacity: 0.5; }
}

/* 角落装饰 */
.corner-decor {
  position: absolute;
  width: 30px;
  height: 30px;
  border: 2px solid rgba(0, 191, 255, 0.6);
  pointer-events: none;
}

.corner-top-left {
  top: 5px;
  left: 5px;
  border-right: none;
  border-bottom: none;
  border-top-left-radius: 5px;
}

.corner-top-right {
  top: 5px;
  right: 5px;
  border-left: none;
  border-bottom: none;
  border-top-right-radius: 5px;
}

.corner-bottom-left {
  bottom: 5px;
  left: 5px;
  border-right: none;
  border-top: none;
  border-bottom-left-radius: 5px;
}

.corner-bottom-right {
  bottom: 5px;
  right: 5px;
  border-left: none;
  border-top: none;
  border-bottom-right-radius: 5px;
}

/* 左侧控制台 */
/* 仪表盘信息 */
.dashboard-info {
  display: flex;
  align-items: center;
  gap: 25px;
  width: 100%;
  justify-content: space-between;
  padding: 0 20px;
  position: relative;
  transition: all 0.3s ease;
}

/* 低血量警告 - 整个仪表盘闪烁（无边框） */
.dashboard-info.low-health-warning {
  background: rgba(255, 0, 0, 0.15);
  animation: dashboardWarning 1s ease-in-out infinite;
  box-shadow: 0 0 30px rgba(255, 0, 0, 0.4);
}

@keyframes dashboardWarning {
  0%, 100% { 
    background: rgba(255, 0, 0, 0.15);
    box-shadow: 0 0 30px rgba(255, 0, 0, 0.4);
  }
  50% { 
    background: rgba(255, 0, 0, 0.25);
    box-shadow: 0 0 50px rgba(255, 0, 0, 0.7);
  }
}

.info-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
  min-width: 100px;
  flex: 0 0 auto;
}

.info-label {
  font-size: 11px;
  color: rgba(0, 191, 255, 0.8);
  text-transform: uppercase;
  letter-spacing: 1px;
}

.info-value {
  font-size: 18px;
  font-weight: 700;
  color: #00ffff;
  font-family: 'Courier New', monospace;
  text-shadow: 0 0 10px rgba(0, 191, 255, 0.8);
  transition: color 0.3s ease;
}

.info-value.high-speed {
  color: #ff6b6b;
  text-shadow: 0 0 10px rgba(255, 107, 107, 0.8);
}

.info-value.low-ammo {
  color: #ffaa44;
  text-shadow: 0 0 10px rgba(255, 170, 68, 0.8);
}

.info-value.low-health {
  color: #ff4444;
  text-shadow: 0 0 10px rgba(255, 68, 68, 0.8);
  animation: healthWarning 0.5s ease-in-out infinite;
}

@keyframes healthWarning {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.6; }
}

.info-value.score-value {
  color: #ffd700;
  text-shadow: 0 0 10px rgba(255, 215, 0, 0.8);
}

.info-value.level-value {
  color: #a8b5ff;
  text-shadow: 0 0 10px rgba(168, 181, 255, 0.8);
}

/* 速度条 */
.speed-bar,
.ammo-bar {
  width: 100%;
  height: 4px;
  background: rgba(0, 191, 255, 0.2);
  border-radius: 2px;
  overflow: hidden;
  margin-top: 2px;
}

.speed-fill {
  height: 100%;
  background: linear-gradient(90deg, #00ffff 0%, #0066ff 100%);
  transition: width 0.1s linear;
  box-shadow: 0 0 8px rgba(0, 191, 255, 0.6);
}

.ammo-fill {
  height: 100%;
  background: linear-gradient(90deg, #4facfe 0%, #00f2fe 100%);
  transition: width 0.3s ease;
  box-shadow: 0 0 8px rgba(79, 172, 254, 0.6);
}

.info-value.high-speed ~ .speed-bar .speed-fill {
  background: linear-gradient(90deg, #ff6b6b 0%, #ff3333 100%);
  box-shadow: 0 0 8px rgba(255, 107, 107, 0.6);
}

.info-value.low-ammo ~ .ammo-bar .ammo-fill {
  background: linear-gradient(90deg, #ffaa44 0%, #ff6600 100%);
  box-shadow: 0 0 8px rgba(255, 170, 68, 0.6);
}

/* 仪表盘生命条 */
.health-bar-dashboard {
  width: 100%;
  height: 6px;
  background: rgba(255, 255, 255, 0.15);
  border-radius: 3px;
  overflow: hidden;
  margin-top: 2px;
  border: 1px solid rgba(255, 255, 255, 0.2);
}

.health-fill-dashboard {
  height: 100%;
  background: linear-gradient(90deg, #ff4444 0%, #ffaa44 50%, #44ff44 100%);
  transition: width 0.3s ease;
  border-radius: 3px;
  box-shadow: 0 0 12px rgba(68, 255, 68, 0.6);
}

.info-value.low-health ~ .health-bar-dashboard .health-fill-dashboard {
  background: linear-gradient(90deg, #ff0000 0%, #ff4444 100%);
  box-shadow: 0 0 15px rgba(255, 0, 0, 0.8);
  animation: healthBarPulse 1s ease-in-out infinite;
}

@keyframes healthBarPulse {
  0%, 100% { 
    box-shadow: 0 0 15px rgba(255, 0, 0, 0.8);
  }
  50% { 
    box-shadow: 0 0 25px rgba(255, 0, 0, 1);
  }
}

/* 警告提示 - 游戏容器中央 */
.warning-alert-center {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px 32px;
  background: rgba(255, 0, 0, 0.4);
  backdrop-filter: blur(10px);
  border: 3px solid rgba(255, 0, 0, 0.9);
  border-radius: 12px;
  color: #ff4444;
  font-size: 24px;
  font-weight: 700;
  text-shadow: 0 0 15px rgba(255, 68, 68, 1);
  box-shadow: 
    0 0 30px rgba(255, 0, 0, 0.8),
    inset 0 0 20px rgba(255, 0, 0, 0.3);
  animation: warningPulseCenter 0.4s ease-in-out infinite;
  z-index: 100;
  white-space: nowrap;
  pointer-events: none;
}

.warning-alert-center i {
  font-size: 32px;
  animation: warningIconSpin 0.5s ease-in-out infinite;
}

@keyframes warningPulseCenter {
  0%, 100% { 
    opacity: 1; 
    transform: translate(-50%, -50%) scale(1);
    box-shadow: 
      0 0 30px rgba(255, 0, 0, 0.8),
      inset 0 0 20px rgba(255, 0, 0, 0.3);
  }
  50% { 
    opacity: 0.9; 
    transform: translate(-50%, -50%) scale(1.08);
    box-shadow: 
      0 0 50px rgba(255, 0, 0, 1),
      inset 0 0 30px rgba(255, 0, 0, 0.5);
  }
}

@keyframes warningIconSpin {
  0%, 100% { transform: rotate(0deg); }
  25% { transform: rotate(-10deg); }
  75% { transform: rotate(10deg); }
}

.alert-pulse-enter-active {
  animation: alertFadeIn 0.2s ease;
}

.alert-pulse-leave-active {
  animation: alertFadeOut 0.2s ease;
}

@keyframes alertFadeIn {
  from {
    opacity: 0;
    transform: translate(-50%, calc(-50% - 15px)) scale(0.8);
  }
  to {
    opacity: 1;
    transform: translate(-50%, -50%) scale(1);
  }
}

@keyframes alertFadeOut {
  from {
    opacity: 1;
    transform: translate(-50%, -50%) scale(1);
  }
  to {
    opacity: 0;
    transform: translate(-50%, calc(-50% - 15px)) scale(0.8);
  }
}

.cockpit-left {
  position: absolute;
  left: 0;
  top: 80px;
  bottom: 40px;
  width: 50px;
  background: linear-gradient(90deg, rgba(0, 20, 40, 0.9) 0%, rgba(0, 20, 40, 0.6) 100%);
  border-left: 2px solid rgba(0, 191, 255, 0.3);
  border-right: 1px solid rgba(0, 191, 255, 0.2);
  z-index: 5;
  pointer-events: none;
  display: flex;
  align-items: center;
  justify-content: center;
}

/* 右侧控制台 */
.cockpit-right {
  position: absolute;
  right: 0;
  top: 80px;
  bottom: 40px;
  width: 50px;
  background: linear-gradient(270deg, rgba(0, 20, 40, 0.9) 0%, rgba(0, 20, 40, 0.6) 100%);
  border-right: 2px solid rgba(0, 191, 255, 0.3);
  border-left: 1px solid rgba(0, 191, 255, 0.2);
  z-index: 5;
  pointer-events: none;
  display: flex;
  align-items: center;
  justify-content: center;
}

/* 控制面板 */
.control-panel {
  display: flex;
  flex-direction: column;
  gap: 8px;
  padding: 10px;
}

.panel-indicator {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: rgba(0, 191, 255, 0.6);
  box-shadow: 0 0 8px rgba(0, 191, 255, 0.8);
  animation: indicatorBlink 2s ease-in-out infinite;
}

.panel-indicator:nth-child(2) {
  animation-delay: 0.3s;
}

.panel-indicator:nth-child(3) {
  animation-delay: 0.6s;
}

@keyframes indicatorBlink {
  0%, 100% { opacity: 0.4; }
  50% { opacity: 1; }
}

/* 底部控制台 */
.cockpit-bottom {
  position: absolute;
  bottom: 0;
  left: 0;
  width: 100%;
  height: 40px;
  background: linear-gradient(0deg, rgba(0, 20, 40, 0.9) 0%, rgba(0, 20, 40, 0.6) 100%);
  border-bottom-left-radius: 15px;
  border-bottom-right-radius: 15px;
  border: 2px solid rgba(0, 191, 255, 0.3);
  border-top: 1px solid rgba(0, 191, 255, 0.2);
  z-index: 5;
  pointer-events: none;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 15px;
}

/* 状态指示灯 */
.status-lights {
  display: flex;
  gap: 10px;
}

.status-light {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.2);
  border: 1px solid rgba(255, 255, 255, 0.3);
}

.status-light.active {
  background: rgba(0, 255, 100, 0.8);
  box-shadow: 0 0 10px rgba(0, 255, 100, 0.6);
  animation: statusPulse 2s ease-in-out infinite;
}

@keyframes statusPulse {
  0%, 100% { opacity: 0.8; transform: scale(1); }
  50% { opacity: 1; transform: scale(1.1); }
}

.star {
  position: absolute;
  background: white;
  border-radius: 50%;
  animation: twinkle 2s infinite alternate, float 20s infinite linear;
  /* 增强可见性 - 更亮的发光效果 */
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
    transform: translateY(-100px);
    opacity: 0;
  }
  10% {
    opacity: 1;
  }
  90% {
    opacity: 1;
  }
  100% {
    transform: translateY(700px);
    opacity: 0;
  }
}


.back-btn {
  position: absolute !important;
  top: 10px !important;
  right: 10px !important;
  z-index: 100;
  background: rgba(255, 255, 255, 0.1) !important;
  backdrop-filter: blur(10px) !important;
  border: 1px solid rgba(255, 255, 255, 0.2) !important;
  color: white !important;
  pointer-events: auto !important;
  transition: all 0.3s ease !important;
}

.back-btn:hover {
  background: rgba(255, 255, 255, 0.2) !important;
  border-color: rgba(255, 255, 255, 0.4) !important;
  transform: scale(1.1);
  box-shadow: 0 5px 15px rgba(102, 126, 234, 0.4) !important;
}



.start-screen,
.game-over {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  z-index: 200;
  background: rgba(0, 0, 0, 0.7);
  backdrop-filter: blur(20px);
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-radius: 20px;
  padding: 40px;
  text-align: center;
  color: white;
  min-width: 400px;
  max-width: 90%;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.5);
  pointer-events: auto;
}

.start-screen h1 {
  font-size: 36px;
  margin-bottom: 20px;
  font-weight: 700;
  background: linear-gradient(135deg, #fff 0%, #a8b5ff 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  animation: pulse 2s infinite;
}

@keyframes pulse {
  0%, 100% { transform: scale(1); filter: brightness(1); }
  50% { transform: scale(1.05); filter: brightness(1.2); }
}

.start-screen p {
  font-size: 18px;
  margin-bottom: 30px;
  color: rgba(255, 255, 255, 0.9);
}

.start-screen .game-btn {
  margin: 20px 10px;
  padding: 12px 40px;
  font-size: 18px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
  border-radius: 12px;
  color: white;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
  box-shadow: 0 4px 15px rgba(102, 126, 234, 0.4);
}

.start-screen .game-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(102, 126, 234, 0.6);
}

.start-screen .game-btn-secondary {
  margin: 10px 10px;
  padding: 12px 40px;
  font-size: 18px;
  background: rgba(255, 255, 255, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.3);
  color: white;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.start-screen .game-btn-secondary:hover {
  background: rgba(255, 255, 255, 0.2);
  border-color: rgba(255, 255, 255, 0.5);
  transform: translateY(-2px);
}

.instructions {
  margin-top: 30px;
  text-align: left;
  font-size: 14px;
  line-height: 1.8;
  color: rgba(255, 255, 255, 0.8);
  background: rgba(255, 255, 255, 0.05);
  backdrop-filter: blur(10px);
  padding: 20px;
  border-radius: 12px;
  border: 1px solid rgba(255, 255, 255, 0.1);
}

.instructions p {
  margin: 8px 0;
  font-size: 14px;
}

.game-over h2 {
  font-size: 32px;
  margin-bottom: 20px;
  font-weight: 700;
  color: #ff6b6b;
  text-shadow: 0 0 20px rgba(255, 107, 107, 0.6);
}

.game-over p {
  font-size: 18px;
  margin: 15px 0;
  color: rgba(255, 255, 255, 0.9);
}

.game-over p span {
  color: #ffd700;
  font-weight: 700;
  font-size: 20px;
  text-shadow: 0 0 10px rgba(255, 215, 0, 0.6);
}

.game-over-actions {
  margin-top: 30px;
  display: flex;
  gap: 15px;
  justify-content: center;
  flex-wrap: wrap;
}

.game-over-actions .el-button {
  padding: 10px 20px;
  font-size: 16px;
  border-radius: 12px;
  transition: all 0.3s ease;
}

.game-over-actions .el-button--primary {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
}

.game-over-actions .el-button:hover {
  transform: translateY(-2px);
  box-shadow: 0 5px 15px rgba(102, 126, 234, 0.4);
}

/* 响应式设计 */
@media (max-width: 900px) {
  .cockpit-frame {
    width: 100%;
    max-width: 900px;
    height: auto;
  }

  .game-container {
    width: 100%;
    max-width: 800px;
    height: auto;
    aspect-ratio: 800 / 600;
  }

  .cockpit-left,
  .cockpit-right {
    width: 30px;
  }

  .cockpit-top {
    height: 60px;
    flex-direction: column;
    padding: 5px;
  }

  .dashboard-info {
    flex-wrap: wrap;
    gap: 15px;
    justify-content: center;
  }

  .info-item {
    min-width: 100px;
  }

  .info-value {
    font-size: 14px;
  }

  .warning-alert {
    font-size: 12px;
    padding: 6px 12px;
  }

  .cockpit-bottom {
    height: 30px;
  }

  canvas {
    width: 100%;
    height: 100%;
  }

  .start-screen,
  .game-over {
    min-width: 90%;
    max-width: 500px;
    padding: 30px 20px;
  }

  .start-screen h1 {
    font-size: 28px;
  }

  .game-over h2 {
    font-size: 24px;
  }

}
</style>