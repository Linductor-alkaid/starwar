<template>
  <div class="game-page">
    <div class="game-container">
      <div class="stars" ref="starsContainer"></div>
      <canvas ref="gameCanvas" width="800" height="600"></canvas>
      
      <div class="game-ui">
        <div class="score">得分: <span>{{ formatScore(score) }}</span></div>
        <div class="health">生命值:</div>
        <div class="health-bar">
          <div class="health-fill" :style="{ width: health + '%' }"></div>
        </div>
        <div class="level">等级: <span>{{ level }}</span></div>
        <div class="ammo">弹药: <span>{{ ammo }}</span></div>
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

      <!-- 开始界面 -->
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
          <p>← → 方向键移动飞船</p>
          <p>空格键发射激光（消耗弹药）</p>
          <p>躲避敌人攻击，收集道具！</p>
          <p>🎁 道具说明：</p>
          <p>🟢 绿十字：恢复生命值</p>
          <p>🔵 蓝菱形：补充弹药</p>
          <p>🟡 黄星星：额外得分</p>
        </div>
      </div>

      <!-- 游戏结束界面 -->
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
        speed: 5,
        color: '#00ffff'
      },
      bullets: [],
      enemies: [],
      enemyBullets: [],
      powerups: [],
      particles: [],
      keys: {},
      animationFrameId: null,
      starCheckInterval: null
    }
  },
  computed: {
    ...mapGetters('user', ['isLoggedIn'])
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
      this.ammo = 20
      this.bullets = []
      this.enemies = []
      this.enemyBullets = []
      this.powerups = []
      this.particles = []
      this.player.x = this.canvas.width / 2 - 25
      this.gameStartTime = Date.now()
      this.lastTime = this.gameStartTime
      this.deltascore = 0
      this.playTime = 0
      // 重新创建星空背景
      this.$nextTick(() => {
        this.createStars()
      })
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
      
      // 玩家移动
      if (this.keys['ArrowLeft'] && this.player.x > 0) {
        this.player.x -= this.player.speed
      }
      if (this.keys['ArrowRight'] && this.player.x < this.canvas.width - this.player.width) {
        this.player.x += this.player.speed
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
          this.health -= 10
          this.enemyBullets.splice(bulletIndex, 1)
          this.createExplosion(this.player.x + this.player.width / 2, this.player.y + this.player.height / 2)
        }
      })
      
      // 碰撞检测 - 敌人撞击玩家
      this.enemies.forEach((enemy, enemyIndex) => {
        if (this.checkCollision(enemy, this.player)) {
          this.health -= 20
          this.enemies.splice(enemyIndex, 1)
          this.createExplosion(this.player.x + this.player.width / 2, this.player.y + this.player.height / 2)
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
  height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  background: linear-gradient(135deg, #0c0c2e 0%, #1a1a3e 50%, #2d1b69 100%);
  padding: 20px;
}

.game-container {
  position: relative;
  width: 800px;
  height: 600px;
  background: linear-gradient(180deg, #000428 0%, #004e92 100%);
  border: 3px solid #00ffff;
  border-radius: 15px;
  overflow: visible;
  box-shadow: 0 0 30px rgba(0, 255, 255, 0.5);
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

.game-ui {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  pointer-events: none;
  z-index: 10;
}

.game-ui .score,
.game-ui .health,
.game-ui .level,
.game-ui .ammo {
  position: absolute;
  color: #00ffff;
  font-size: 18px;
  font-weight: bold;
  text-shadow: 0 0 10px rgba(0, 255, 255, 0.8);
  padding: 5px 10px;
  background: rgba(0, 0, 0, 0.5);
  border-radius: 5px;
}

.game-ui .score {
  top: 20px;
  left: 20px;
}

.game-ui .score span {
  color: #ffff00;
}

.game-ui .health {
  top: 20px;
  right: 20px;
}

.game-ui .level {
  top: 60px;
  left: 20px;
}

.game-ui .level span {
  color: #ff00ff;
}

.game-ui .ammo {
  top: 80px;
  right: 20px;
}

.game-ui .ammo span {
  color: #00ff00;
}

.back-btn {
  position: absolute !important;
  top: 10px !important;
  right: 10px !important;
  z-index: 100;
  background: rgba(0, 0, 0, 0.6) !important;
  border: 2px solid #00ffff !important;
  color: #00ffff !important;
  pointer-events: auto !important;
}

.back-btn:hover {
  background: rgba(0, 255, 255, 0.2) !important;
  transform: scale(1.1);
}

.health-bar {
  position: absolute;
  top: 50px;
  right: 20px;
  width: 150px;
  height: 12px;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 6px;
  overflow: hidden;
  border: 1px solid rgba(0, 255, 255, 0.5);
}

.health-fill {
  height: 100%;
  background: linear-gradient(90deg, #ff0000 0%, #ffff00 50%, #00ff00 100%);
  transition: width 0.3s ease;
  border-radius: 5px;
  box-shadow: 0 0 10px rgba(0, 255, 0, 0.5);
}

.start-screen,
.game-over {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  text-align: center;
  color: #00ffff;
  background: rgba(0, 0, 0, 0.9);
  padding: 40px;
  border-radius: 15px;
  border: 2px solid #00ffff;
  box-shadow: 0 0 30px rgba(0, 255, 255, 0.5);
  z-index: 20;
  min-width: 400px;
  backdrop-filter: blur(10px);
}

.start-screen h1 {
  font-size: 36px;
  margin-bottom: 20px;
  text-shadow: 0 0 20px rgba(0, 255, 255, 0.8);
  animation: pulse 2s infinite;
}

@keyframes pulse {
  0%, 100% { transform: scale(1); }
  50% { transform: scale(1.05); }
}

.start-screen p {
  font-size: 18px;
  margin-bottom: 30px;
  color: #ffffff;
}

.start-screen .game-btn {
  margin: 20px 0;
  padding: 12px 40px;
  font-size: 18px;
  background: linear-gradient(135deg, #00ffff 0%, #0080ff 100%);
  border: none;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.start-screen .game-btn:hover {
  transform: scale(1.1);
  box-shadow: 0 0 20px rgba(0, 255, 255, 0.8);
}

.start-screen .game-btn-secondary {
  margin: 10px 0;
  padding: 12px 40px;
  font-size: 18px;
  background: rgba(255, 255, 255, 0.1);
  border: 2px solid #00ffff;
  color: #00ffff;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.start-screen .game-btn-secondary:hover {
  background: rgba(0, 255, 255, 0.2);
  transform: scale(1.05);
}

.instructions {
  margin-top: 30px;
  text-align: left;
  font-size: 14px;
  line-height: 1.8;
  color: #cccccc;
  background: rgba(0, 255, 255, 0.1);
  padding: 20px;
  border-radius: 10px;
}

.instructions p {
  margin: 8px 0;
  font-size: 14px;
}

.game-over h2 {
  font-size: 32px;
  margin-bottom: 20px;
  color: #ff4444;
  text-shadow: 0 0 20px rgba(255, 68, 68, 0.8);
}

.game-over p {
  font-size: 18px;
  margin: 15px 0;
  color: #ffffff;
}

.game-over p span {
  color: #ffff00;
  font-weight: bold;
  font-size: 20px;
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
}

/* 响应式设计 */
@media (max-width: 900px) {
  .game-container {
    width: 100%;
    max-width: 800px;
    height: auto;
    aspect-ratio: 800 / 600;
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

  .game-ui .score,
  .game-ui .health,
  .game-ui .level,
  .game-ui .ammo {
    font-size: 14px;
    padding: 3px 8px;
  }

  .health-bar {
    width: 120px;
    height: 10px;
  }
}
</style>