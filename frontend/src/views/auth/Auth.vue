<template>
  <div class="auth-container">
    <!-- 动态背景 -->
    <div class="background-animation"></div>
    <div class="stars" ref="starsContainer"></div>
    
    <!-- 返回按钮 -->
    <el-button 
      class="back-home-btn" 
      icon="el-icon-arrow-left"
      @click="$router.push('/')"
      circle
    ></el-button>
    
    <!-- 认证卡片 -->
    <div 
      class="auth-card" 
      :class="{ 'flip': isAnimating }"
      ref="authCard"
      @mousemove="handleMouseMove"
      @mouseleave="handleMouseLeave"
      :style="cardTransformStyle"
    >
      <div class="card-inner">
        <!-- 登录面板 -->
        <div class="panel login-panel" :class="{ 'active': activeTab === 'login', 'hidden': activeTab !== 'login' }">
          <div class="panel-header">
            <div class="icon-wrapper">
              <i class="el-icon-user-solid"></i>
            </div>
            <h2>欢迎回来</h2>
            <p>登录您的账户继续游戏</p>
          </div>
          
          <el-form 
            :model="loginForm" 
            :rules="loginRules" 
            ref="loginForm" 
            class="auth-form"
            @submit.native.prevent="handleLogin"
          >
            <transition-group name="fade-in" tag="div">
              <el-form-item key="username" prop="username" class="form-item-enhanced">
                <div class="input-wrapper">
                  <i class="el-icon-user input-icon"></i>
                  <el-input 
                    v-model="loginForm.username" 
                    placeholder="用户名"
                    class="enhanced-input"
                    @focus="handleInputFocus"
                    @blur="handleInputBlur"
                  ></el-input>
                  <transition name="slide-fade">
                    <i v-if="loginForm.username && isValidUsername(loginForm.username)" 
                       class="el-icon-success input-success-icon"></i>
                  </transition>
                </div>
              </el-form-item>
              
              <el-form-item key="password" prop="password" class="form-item-enhanced">
                <div class="input-wrapper">
                  <i class="el-icon-lock input-icon"></i>
                  <el-input
                    v-model="loginForm.password"
                    type="password"
                    placeholder="密码"
                    class="enhanced-input"
                    @focus="handleInputFocus"
                    @blur="handleInputBlur"
                    @keyup.enter.native="handleLogin"
                    show-password
                  ></el-input>
                  <transition name="slide-fade">
                    <i v-if="loginForm.password && loginForm.password.length >= 6" 
                       class="el-icon-success input-success-icon"></i>
                  </transition>
                </div>
              </el-form-item>
            </transition-group>
            
            <el-button 
              type="primary" 
              @click="handleLogin" 
              :loading="loginLoading" 
              class="submit-btn"
              :class="{ 'pulse': loginLoading }"
            >
              <span v-if="!loginLoading">登录</span>
              <span v-else>登录中...</span>
            </el-button>
            
            <div class="form-footer">
              <span class="switch-text">还没有账号？</span>
              <el-link @click="switchToRegister" class="switch-link">立即注册</el-link>
            </div>
          </el-form>
        </div>
        
        <!-- 注册面板 -->
        <div class="panel register-panel" :class="{ 'active': activeTab === 'register', 'hidden': activeTab !== 'register' }">
          <div class="panel-header">
            <div class="icon-wrapper">
              <i class="el-icon-user"></i>
            </div>
            <h2>加入我们</h2>
            <p>创建新账户开始游戏之旅</p>
          </div>
          
          <el-form 
            :model="registerForm" 
            :rules="registerRules" 
            ref="registerForm" 
            class="auth-form"
            @submit.native.prevent="handleRegister"
          >
            <el-form-item prop="username" class="form-item-enhanced">
              <div class="input-wrapper">
                <i class="el-icon-user input-icon"></i>
                <el-input 
                  v-model="registerForm.username" 
                  placeholder="用户名 (3-20个字符)"
                  class="enhanced-input"
                  @focus="handleInputFocus"
                  @blur="handleInputBlur"
                  @input="checkUsername"
                ></el-input>
                <transition name="slide-fade">
                  <i v-if="usernameStatus === 'valid'" 
                     class="el-icon-success input-success-icon"></i>
                  <i v-else-if="usernameStatus === 'invalid'" 
                     class="el-icon-error input-error-icon"></i>
                </transition>
              </div>
            </el-form-item>
            
            <el-form-item prop="email" class="form-item-enhanced">
              <div class="input-wrapper">
                <i class="el-icon-message input-icon"></i>
                <el-input 
                  v-model="registerForm.email" 
                  placeholder="邮箱地址"
                  class="enhanced-input"
                  @focus="handleInputFocus"
                  @blur="handleInputBlur"
                  @input="checkEmail"
                ></el-input>
                <transition name="slide-fade">
                  <i v-if="emailStatus === 'valid'" 
                     class="el-icon-success input-success-icon"></i>
                  <i v-else-if="emailStatus === 'invalid'" 
                     class="el-icon-error input-error-icon"></i>
                </transition>
              </div>
            </el-form-item>
            
            <el-form-item prop="nickname" class="form-item-enhanced">
              <div class="input-wrapper">
                <i class="el-icon-star-on input-icon"></i>
                <el-input 
                  v-model="registerForm.nickname" 
                  placeholder="昵称"
                  class="enhanced-input"
                  @focus="handleInputFocus"
                  @blur="handleInputBlur"
                ></el-input>
              </div>
            </el-form-item>
            
            <el-form-item prop="password" class="form-item-enhanced">
              <div class="input-wrapper">
                <i class="el-icon-lock input-icon"></i>
                <el-input
                  v-model="registerForm.password"
                  type="password"
                  placeholder="密码 (至少6位)"
                  class="enhanced-input"
                  @focus="handleInputFocus"
                  @blur="handleInputBlur"
                  @input="checkPasswordStrength"
                  show-password
                ></el-input>
                <transition name="slide-fade">
                  <i v-if="passwordStrength >= 3" 
                     class="el-icon-success input-success-icon"></i>
                </transition>
              </div>
              <!-- 密码强度指示器 -->
              <transition name="fade">
                <div v-if="registerForm.password" class="password-strength">
                  <div class="strength-bar">
                    <div 
                      class="strength-fill" 
                      :class="getPasswordStrengthClass()"
                      :style="{ width: passwordStrength * 33.33 + '%' }"
                    ></div>
                  </div>
                  <span class="strength-text" :class="getPasswordStrengthClass()">
                    {{ getPasswordStrengthText() }}
                  </span>
                </div>
              </transition>
            </el-form-item>
            
            <el-form-item prop="confirmPassword" class="form-item-enhanced">
              <div class="input-wrapper">
                <i class="el-icon-lock input-icon"></i>
                <el-input
                  v-model="registerForm.confirmPassword"
                  type="password"
                  placeholder="确认密码"
                  class="enhanced-input"
                  @focus="handleInputFocus"
                  @blur="handleInputBlur"
                  @input="checkPasswordMatch"
                  show-password
                ></el-input>
                <transition name="slide-fade">
                  <i v-if="confirmPasswordStatus === 'valid'" 
                     class="el-icon-success input-success-icon"></i>
                  <i v-else-if="confirmPasswordStatus === 'invalid' && registerForm.confirmPassword" 
                     class="el-icon-error input-error-icon"></i>
                </transition>
              </div>
            </el-form-item>
            
            <el-button 
              type="primary" 
              @click="handleRegister" 
              :loading="registerLoading" 
              class="submit-btn"
              :class="{ 'pulse': registerLoading }"
            >
              <span v-if="!registerLoading">立即注册</span>
              <span v-else>注册中...</span>
            </el-button>
            
            <div class="form-footer">
              <span class="switch-text">已有账号？</span>
              <el-link @click="switchToLogin" class="switch-link">立即登录</el-link>
            </div>
          </el-form>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'Auth',
  data() {
    // 确认密码验证
    const validateConfirmPassword = (rule, value, callback) => {
      if (!value) {
        callback(new Error('请确认密码'))
      } else if (value !== this.registerForm.password) {
        callback(new Error('两次输入密码不一致'))
      } else {
        callback()
      }
    }
    
    return {
      activeTab: 'login',
      isAnimating: false,
      loginForm: {
        username: '',
        password: ''
      },
      registerForm: {
        username: '',
        email: '',
        nickname: '',
        password: '',
        confirmPassword: ''
      },
      loginRules: {
        username: [
          { required: true, message: '请输入用户名', trigger: 'blur' }
        ],
        password: [
          { required: true, message: '请输入密码', trigger: 'blur' },
          { min: 6, message: '密码长度不能少于6位', trigger: 'blur' }
        ]
      },
      registerRules: {
        username: [
          { required: true, message: '请输入用户名', trigger: 'blur' },
          { min: 3, max: 20, message: '用户名长度在3到20个字符', trigger: 'blur' }
        ],
        email: [
          { required: true, message: '请输入邮箱', trigger: 'blur' },
          { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
        ],
        nickname: [
          { required: true, message: '请输入昵称', trigger: 'blur' }
        ],
        password: [
          { required: true, message: '请输入密码', trigger: 'blur' },
          { min: 6, message: '密码长度不能少于6位', trigger: 'blur' }
        ],
        confirmPassword: [
          { required: true, message: '请确认密码', trigger: 'blur' },
          { validator: validateConfirmPassword, trigger: 'blur' }
        ]
      },
      loginLoading: false,
      registerLoading: false,
      usernameStatus: '', // '', 'valid', 'invalid'
      emailStatus: '',
      confirmPasswordStatus: '',
      passwordStrength: 0, // 0-3
      // 3D卡片倾斜效果
      rotateX: 0,
      rotateY: 0
    }
  },
  computed: {
    cardTransformStyle() {
      // 计算阴影偏移，使阴影随倾斜角度变化
      const shadowX = this.rotateY * 2
      const shadowY = -this.rotateX * 2
      const shadowBlur = 30 + Math.abs(this.rotateX) + Math.abs(this.rotateY)
      
      return {
        transform: `perspective(1000px) rotateX(${this.rotateX}deg) rotateY(${this.rotateY}deg)`,
        boxShadow: `${shadowX}px ${shadowY}px ${shadowBlur}px rgba(0, 0, 0, 0.3), 0 0 50px rgba(102, 126, 234, 0.2)`
      }
    }
  },
  mounted() {
    // 根据路由路径决定显示哪个标签页
    if (this.$route.path === '/register' || this.$route.query.tab === 'register') {
      this.activeTab = 'register'
    } else {
      this.activeTab = 'login'
    }
    this.createStars()
  },
  watch: {
    '$route'(to) {
      if (to.path === '/register') {
        this.switchToRegister()
      } else if (to.path === '/login') {
        this.switchToLogin()
      }
    }
  },
  methods: {
    createStars() {
      const starContainer = this.$refs.starsContainer
      if (!starContainer) {
        setTimeout(() => this.createStars(), 100)
        return
      }
      
      starContainer.innerHTML = ''
      
      for (let i = 0; i < 80; i++) {
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
    },
    switchToLogin() {
      if (this.activeTab === 'login') return
      this.isAnimating = true
      setTimeout(() => {
        this.activeTab = 'login'
        this.isAnimating = false
        this.$refs.registerForm && this.$refs.registerForm.clearValidate()
      }, 150)
    },
    switchToRegister() {
      if (this.activeTab === 'register') return
      this.isAnimating = true
      setTimeout(() => {
        this.activeTab = 'register'
        this.isAnimating = false
        this.$refs.loginForm && this.$refs.loginForm.clearValidate()
      }, 150)
    },
    handleTabClick(tab) {
      if (tab.name === 'login') {
        this.switchToLogin()
      } else {
        this.switchToRegister()
      }
    },
    handleInputFocus(event) {
      event.currentTarget.closest('.input-wrapper')?.classList.add('focused')
    },
    handleInputBlur(event) {
      event.currentTarget.closest('.input-wrapper')?.classList.remove('focused')
    },
    isValidUsername(username) {
      return username.length >= 3 && username.length <= 20
    },
    checkUsername() {
      if (!this.registerForm.username) {
        this.usernameStatus = ''
        return
      }
      this.usernameStatus = this.isValidUsername(this.registerForm.username) ? 'valid' : 'invalid'
    },
    checkEmail() {
      if (!this.registerForm.email) {
        this.emailStatus = ''
        return
      }
      const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
      this.emailStatus = emailRegex.test(this.registerForm.email) ? 'valid' : 'invalid'
    },
    checkPasswordStrength() {
      if (!this.registerForm.password) {
        this.passwordStrength = 0
        return
      }
      
      let strength = 0
      if (this.registerForm.password.length >= 6) strength++
      if (this.registerForm.password.length >= 8) strength++
      if (/[A-Z]/.test(this.registerForm.password) && /[a-z]/.test(this.registerForm.password)) strength++
      if (/\d/.test(this.registerForm.password) && /[!@#$%^&*]/.test(this.registerForm.password)) strength++
      
      this.passwordStrength = Math.min(strength, 3)
    },
    getPasswordStrengthClass() {
      if (this.passwordStrength === 0) return 'weak'
      if (this.passwordStrength === 1) return 'weak'
      if (this.passwordStrength === 2) return 'medium'
      return 'strong'
    },
    getPasswordStrengthText() {
      if (this.passwordStrength === 0) return '弱'
      if (this.passwordStrength === 1) return '弱'
      if (this.passwordStrength === 2) return '中'
      return '强'
    },
    checkPasswordMatch() {
      if (!this.registerForm.confirmPassword) {
        this.confirmPasswordStatus = ''
        return
      }
      this.confirmPasswordStatus = this.registerForm.confirmPassword === this.registerForm.password ? 'valid' : 'invalid'
    },
    async handleLogin() {
      this.$refs.loginForm.validate(async (valid) => {
        if (valid) {
          this.loginLoading = true
          try {
            await this.$store.dispatch('user/login', this.loginForm)
            this.$message({
              message: '登录成功！',
              type: 'success',
              duration: 2000,
              customClass: 'auth-message'
            })
            const redirect = this.$route.query.redirect || '/'
            setTimeout(() => {
              this.$router.push(redirect)
            }, 500)
          } catch (error) {
            this.$message({
              message: error.message || '登录失败，请检查用户名和密码',
              type: 'error',
              duration: 3000,
              customClass: 'auth-message'
            })
          } finally {
            this.loginLoading = false
          }
        }
      })
    },
    async handleRegister() {
      this.$refs.registerForm.validate(async (valid) => {
        if (valid) {
          this.registerLoading = true
          try {
            const { confirmPassword, ...registerData } = this.registerForm
            await this.$store.dispatch('user/register', registerData)
            this.$message({
              message: '注册成功！正在跳转到登录...',
              type: 'success',
              duration: 2000,
              customClass: 'auth-message'
            })
            setTimeout(() => {
              this.activeTab = 'login'
              this.loginForm.username = this.registerForm.username
              this.registerForm = {
                username: '',
                email: '',
                nickname: '',
                password: '',
                confirmPassword: ''
              }
              this.usernameStatus = ''
              this.emailStatus = ''
              this.confirmPasswordStatus = ''
              this.passwordStrength = 0
              this.$refs.registerForm.clearValidate()
            }, 1500)
          } catch (error) {
            this.$message({
              message: error.message || '注册失败，请重试',
              type: 'error',
              duration: 3000,
              customClass: 'auth-message'
            })
          } finally {
            this.registerLoading = false
          }
        }
      })
    },
    // 处理鼠标移动，实现3D倾斜效果
    handleMouseMove(event) {
      const card = this.$refs.authCard
      if (!card) return
      
      const rect = card.getBoundingClientRect()
      const centerX = rect.left + rect.width / 2
      const centerY = rect.top + rect.height / 2
      
      const mouseX = event.clientX
      const mouseY = event.clientY
      
      // 计算鼠标相对于卡片中心的位置（-1 到 1）
      const deltaX = (mouseX - centerX) / (rect.width / 2)
      const deltaY = (mouseY - centerY) / (rect.height / 2)
      
      // 应用旋转（限制在合理范围内，这里设为最大8度，更柔和）
      // 使用缓动函数使动画更平滑
      const targetRotateY = deltaX * 8
      const targetRotateX = -deltaY * 8 // 反向，使效果更自然
      
      // 使用线性插值实现平滑过渡
      this.rotateY = targetRotateY
      this.rotateX = targetRotateX
    },
    // 鼠标离开时恢复原状（平滑动画）
    handleMouseLeave() {
      // 使用动画平滑恢复
      const animate = () => {
        const speed = 0.15
        const diffX = 0 - this.rotateX
        const diffY = 0 - this.rotateY
        
        if (Math.abs(diffX) > 0.1 || Math.abs(diffY) > 0.1) {
          this.rotateX += diffX * speed
          this.rotateY += diffY * speed
          requestAnimationFrame(animate)
        } else {
          this.rotateX = 0
          this.rotateY = 0
        }
      }
      animate()
    }
  }
}
</script>

<style scoped>
/* 容器 */
.auth-container {
  width: 100%;
  min-height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  position: relative;
  overflow: hidden;
  /* 与主页面一致的背景 */
  background: linear-gradient(135deg, #0c0c2e 0%, #1a1a3e 30%, #2d1b69 60%, #4a148c 100%);
  background-size: 200% 200%;
  animation: gradientShift 15s ease infinite;
  padding: 20px;
}

@keyframes gradientShift {
  0% { background-position: 0% 50%; }
  50% { background-position: 100% 50%; }
  100% { background-position: 0% 50%; }
}

/* 动态背景光晕 - 与主页面一致 */
.background-animation {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: 
    radial-gradient(circle at 20% 30%, rgba(102, 126, 234, 0.3) 0%, transparent 50%),
    radial-gradient(circle at 80% 70%, rgba(138, 43, 226, 0.3) 0%, transparent 50%),
    radial-gradient(circle at 50% 50%, rgba(0, 191, 255, 0.2) 0%, transparent 50%);
  animation: backgroundPulse 8s ease-in-out infinite;
  pointer-events: none;
  z-index: 0;
}

@keyframes backgroundPulse {
  0%, 100% { opacity: 0.5; transform: scale(1); }
  50% { opacity: 0.8; transform: scale(1.1); }
}

/* 星空背景 */
.stars {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  pointer-events: none;
  z-index: 1;
}

/* 返回按钮 - 与主页面导航栏风格一致 */
.back-home-btn {
  position: absolute;
  top: 20px;
  left: 20px;
  background: rgba(0, 0, 0, 0.2);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.1);
  color: white;
  z-index: 100;
  transition: all 0.3s ease;
}

.back-home-btn:hover {
  background: rgba(255, 255, 255, 0.1);
  border-color: rgba(255, 255, 255, 0.3);
  transform: translateX(-3px);
  box-shadow: 0 5px 15px rgba(102, 126, 234, 0.4);
}

/* 认证卡片 */
.auth-card {
  position: relative;
  width: 100%;
  max-width: 450px;
  z-index: 10;
  transition: transform 0.05s linear, box-shadow 0.05s linear;
  transform-style: preserve-3d;
  cursor: pointer;
  will-change: transform, box-shadow;
}

.card-inner {
  position: relative;
  width: 100%;
}

/* 面板 */
.panel {
  background: rgba(255, 255, 255, 0.15);
  backdrop-filter: blur(20px);
  border-radius: 20px;
  padding: 40px;
  border: 1px solid rgba(255, 255, 255, 0.3);
  /* 移除固定的box-shadow，让卡片级别的阴影生效 */
  box-shadow: 
    0 8px 32px rgba(0, 0, 0, 0.1),
    inset 0 1px 0 rgba(255, 255, 255, 0.2);
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  position: absolute;
  width: 100%;
  top: 0;
  left: 0;
  opacity: 0;
  transform: translateY(20px) scale(0.95);
  pointer-events: none;
  transform-style: preserve-3d;
  backface-visibility: hidden;
}

.panel.active {
  opacity: 1;
  transform: translateY(0) scale(1);
  pointer-events: auto;
  position: relative;
}

.panel.hidden {
  opacity: 0;
  transform: translateY(-20px) scale(0.95);
  pointer-events: none;
}

/* 面板头部 */
.panel-header {
  text-align: center;
  margin-bottom: 35px;
}

.icon-wrapper {
  width: 70px;
  height: 70px;
  margin: 0 auto 20px;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  border: 2px solid rgba(255, 255, 255, 0.3);
  transition: all 0.3s ease;
}

.icon-wrapper:hover {
  transform: scale(1.1) rotate(5deg);
  background: rgba(255, 255, 255, 0.3);
}

.icon-wrapper i {
  font-size: 32px;
  color: white;
}

.panel-header h2 {
  font-size: 28px;
  font-weight: 600;
  color: white;
  margin: 0 0 8px;
  text-shadow: 0 2px 10px rgba(0, 0, 0, 0.2);
}

.panel-header p {
  color: rgba(255, 255, 255, 0.9);
  font-size: 14px;
  margin: 0;
}

/* 表单 */
.auth-form {
  width: 100%;
}

.form-item-enhanced {
  margin-bottom: 20px;
}

/* 输入框包装器 */
.input-wrapper {
  position: relative;
  display: flex;
  align-items: center;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 12px;
  border: 2px solid rgba(255, 255, 255, 0.3);
  transition: all 0.3s ease;
  overflow: hidden;
}

.input-wrapper.focused {
  background: rgba(255, 255, 255, 0.3);
  border-color: rgba(255, 255, 255, 0.6);
  box-shadow: 0 0 20px rgba(255, 255, 255, 0.3);
  transform: translateY(-2px);
}

.input-icon {
  padding: 0 15px;
  color: rgba(255, 255, 255, 0.8);
  font-size: 18px;
  display: flex;
  align-items: center;
}

.enhanced-input {
  flex: 1;
}

::v-deep .enhanced-input .el-input__inner {
  background: transparent !important;
  border: none !important;
  color: white !important;
  font-size: 15px;
  padding: 15px 15px 15px 0 !important;
  height: auto !important;
}

::v-deep .enhanced-input .el-input__inner::placeholder {
  color: rgba(255, 255, 255, 0.6) !important;
}

.input-success-icon,
.input-error-icon {
  position: absolute;
  right: 15px;
  color: #67c23a;
  font-size: 18px;
  z-index: 10;
}

.input-error-icon {
  color: #f56c6c;
}

/* 密码强度指示器 */
.password-strength {
  margin-top: 8px;
  padding: 0 5px;
}

.strength-bar {
  height: 4px;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 2px;
  overflow: hidden;
  margin-bottom: 6px;
}

.strength-fill {
  height: 100%;
  transition: all 0.3s ease;
  border-radius: 2px;
}

.strength-fill.weak {
  background: #f56c6c;
}

.strength-fill.medium {
  background: #e6a23c;
}

.strength-fill.strong {
  background: #67c23a;
}

.strength-text {
  font-size: 12px;
  color: rgba(255, 255, 255, 0.8);
}

.strength-text.weak {
  color: #f56c6c;
}

.strength-text.medium {
  color: #e6a23c;
}

.strength-text.strong {
  color: #67c23a;
}

/* 提交按钮 */
.submit-btn {
  width: 100%;
  height: 50px;
  margin-top: 10px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
  border-radius: 12px;
  font-size: 16px;
  font-weight: 600;
  color: white;
  box-shadow: 0 4px 15px rgba(102, 126, 234, 0.4);
  transition: all 0.3s ease;
  position: relative;
  overflow: hidden;
}

.submit-btn::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.3), transparent);
  transition: left 0.5s;
}

.submit-btn:hover::before {
  left: 100%;
}

.submit-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(102, 126, 234, 0.6);
}

.submit-btn:active {
  transform: translateY(0);
}

.submit-btn.pulse {
  animation: pulse 1.5s ease infinite;
}

@keyframes pulse {
  0% { transform: scale(1); }
  50% { transform: scale(1.05); }
  100% { transform: scale(1); }
}

/* 表单底部 */
.form-footer {
  text-align: center;
  margin-top: 20px;
}
</style>