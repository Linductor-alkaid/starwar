<template>
  <div class="post-detail-page">
    <!-- 背景效果 -->
    <div class="stars" ref="starsContainer"></div>
    <div class="background-glow"></div>

    <!-- 导航栏 -->
    <div class="navbar">
      <div class="nav-left">
        <el-button 
          class="nav-btn back-btn"
          icon="el-icon-arrow-left"
          @click="$router.push('/forum')"
          circle
        ></el-button>
        <h1 class="page-title">
          <i class="el-icon-document title-icon"></i>
          帖子详情
        </h1>
      </div>
    </div>

    <!-- 主要内容区域 -->
    <div class="main-container">
      <div class="detail-content">
        <!-- 加载状态 -->
        <div v-if="loading" class="loading-wrapper">
          <i class="el-icon-loading loading-icon"></i>
          <p>加载中...</p>
        </div>

        <!-- 帖子内容 -->
        <div v-else-if="postDetail" class="post-wrapper">
          <!-- 帖子主体 -->
          <div class="post-card">
            <div class="post-header">
              <h2 class="post-title">{{ postDetail.title }}</h2>
              <div class="post-meta">
                <span class="meta-item">
                  <i class="el-icon-user"></i>
                  {{ postDetail.nickname || postDetail.username || '匿名用户' }}
                </span>
                <span class="meta-divider">•</span>
                <span class="meta-item">
                  <i class="el-icon-time"></i>
                  {{ formatTime(postDetail.createdAt) }}
                </span>
                <span class="meta-divider">•</span>
                <span class="meta-item">
                  <i class="el-icon-view"></i>
                  {{ postDetail.viewCount || 0 }} 次浏览
                </span>
                <span class="meta-divider">•</span>
                <span class="meta-item">
                  <i class="el-icon-chat-dot-round"></i>
                  {{ postDetail.replyCount || 0 }} 条回复
                </span>
              </div>
            </div>
            
            <div class="post-body">
              <div class="post-content-text">{{ postDetail.content }}</div>
            </div>
          </div>

          <!-- 回复列表 -->
          <div class="replies-section">
            <div class="section-header">
              <h3 class="section-title">
                <i class="el-icon-chat-dot-round"></i>
                全部回复
                <span class="reply-count">({{ replies.length }})</span>
              </h3>
            </div>

            <!-- 空状态 -->
            <div v-if="replies.length === 0" class="empty-replies">
              <i class="el-icon-chat-line-square empty-icon"></i>
              <p>还没有回复，快来抢沙发吧！</p>
            </div>

            <!-- 回复列表 -->
            <div v-else class="replies-list">
              <transition-group name="fade-in-up" tag="div">
                <div
                  v-for="(reply, index) in replies"
                  :key="reply.id"
                  class="reply-item"
                  :style="{ animationDelay: `${index * 0.1}s` }"
                >
                  <div class="reply-floor">
                    <span class="floor-number">#{{ index + 1 }}</span>
                  </div>
                  <div class="reply-content">
                    <div class="reply-header">
                      <div class="reply-author">
                        <i class="el-icon-user-solid author-icon"></i>
                        <span class="author-name">{{ reply.nickname || reply.username || '匿名用户' }}</span>
                      </div>
                      <span class="reply-time">{{ formatTime(reply.createdAt) }}</span>
                    </div>
                    <div class="reply-text">{{ reply.content }}</div>
                  </div>
                </div>
              </transition-group>
            </div>
          </div>

          <!-- 发表回复区域 -->
          <div v-if="isLoggedIn" class="reply-form-section">
            <div class="form-header">
              <h3 class="form-title">
                <i class="el-icon-edit-outline"></i>
                发表回复
              </h3>
            </div>
            <div class="reply-form">
              <el-input
                type="textarea"
                v-model="replyContent"
                :rows="5"
                placeholder="分享你的想法...支持换行输入"
                maxlength="500"
                show-word-limit
                class="reply-textarea"
              ></el-input>
              <div class="form-actions">
                <el-button 
                  type="primary" 
                  class="submit-reply-btn"
                  @click="handleReply"
                  :loading="submitting"
                >
                  <i class="el-icon-check"></i>
                  提交回复
                </el-button>
              </div>
            </div>
          </div>

          <!-- 未登录提示 -->
          <div v-else class="login-tip">
            <el-alert
              title="需要登录后才能回复"
              type="info"
              :closable="false"
              show-icon
            >
              <template slot="default">
                <span>请先</span>
                <el-button 
                  type="text" 
                  class="login-link"
                  @click="$router.push('/auth?redirect=' + encodeURIComponent($route.fullPath))"
                >
                  登录
                </el-button>
                <span>后再发表回复</span>
              </template>
            </el-alert>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { getPostDetail, replyPost, getReplies } from '@/api/post'
import { mapGetters } from 'vuex'

export default {
  name: 'PostDetail',
  data() {
    return {
      postDetail: null,
      replies: [],
      loading: false,
      submitting: false,
      replyContent: ''
    }
  },
  computed: {
    ...mapGetters('user', ['isLoggedIn'])
  },
  methods: {
    async loadPostDetail() {
      this.loading = true
      try {
        const postId = this.$route.params.id
        const [postRes, repliesRes] = await Promise.all([
          getPostDetail(postId),
          getReplies(postId)
        ])
        if (postRes.code === 200) {
          this.postDetail = postRes.data
        }
        if (repliesRes.code === 200) {
          // 按创建时间排序，确保按楼层顺序显示
          this.replies = repliesRes.data.sort((a, b) => {
            return new Date(a.createdAt) - new Date(b.createdAt)
          })
        }
      } catch (error) {
        this.$message.error('加载帖子详情失败')
      } finally {
        this.loading = false
      }
    },
    async handleReply() {
      if (!this.replyContent.trim()) {
        this.$message.warning('请输入回复内容')
        return
      }
      if (this.replyContent.trim().length < 5) {
        this.$message.warning('回复内容至少需要5个字符')
        return
      }
      this.submitting = true
      try {
        const res = await replyPost({
          postId: this.postDetail.id,
          content: this.replyContent.trim(),
          parentId: null
        })
        if (res.code === 200) {
          this.$message.success('回复成功！')
          this.replyContent = ''
          // 重新加载帖子详情和回复列表
          await this.loadPostDetail()
          // 滚动到回复列表
          this.$nextTick(() => {
            const repliesSection = document.querySelector('.replies-section')
            if (repliesSection) {
              repliesSection.scrollIntoView({ behavior: 'smooth', block: 'start' })
            }
          })
        }
      } catch (error) {
        this.$message.error('回复失败，请稍后重试')
      } finally {
        this.submitting = false
      }
    },
    // 格式化时间
    formatTime(timeStr) {
      if (!timeStr) return ''
      const date = new Date(timeStr)
      const now = new Date()
      const diff = now - date
      
      // 小于1分钟
      if (diff < 60000) return '刚刚'
      // 小于1小时
      if (diff < 3600000) return `${Math.floor(diff / 60000)}分钟前`
      // 小于1天
      if (diff < 86400000) return `${Math.floor(diff / 3600000)}小时前`
      // 小于7天
      if (diff < 604800000) return `${Math.floor(diff / 86400000)}天前`
      
      // 超过7天显示具体日期
      return date.toLocaleDateString('zh-CN', {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit',
        hour: '2-digit',
        minute: '2-digit'
      })
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
    this.loadPostDetail()
    this.createStars()
  }
}
</script>

<style scoped>
/* 页面容器 */
.post-detail-page {
  width: 100%;
  min-height: 100vh;
  position: relative;
  overflow-x: hidden;
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
  background: linear-gradient(135deg, #fff 0%, #a8b5ff 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.title-icon {
  font-size: 32px;
  color: #a8b5ff;
  -webkit-text-fill-color: #a8b5ff;
}

/* 主容器 */
.main-container {
  position: relative;
  z-index: 10;
  padding: 40px 20px 80px;
  min-height: calc(100vh - 80px);
  display: flex;
  justify-content: center;
  align-items: flex-start;
}

.detail-content {
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

/* 帖子卡片 */
.post-wrapper {
  display: flex;
  flex-direction: column;
  gap: 30px;
}

.post-card {
  background: rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(20px);
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-radius: 16px;
  padding: 32px;
  animation: fadeInUp 0.6s ease-out;
}

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

.post-header {
  margin-bottom: 24px;
  padding-bottom: 20px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}

.post-title {
  margin: 0 0 16px;
  font-size: 28px;
  font-weight: 700;
  color: white;
  line-height: 1.4;
}

.post-meta {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 12px;
  font-size: 14px;
  color: rgba(255, 255, 255, 0.7);
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 6px;
}

.meta-item i {
  color: #a8b5ff;
  font-size: 16px;
}

.meta-divider {
  color: rgba(255, 255, 255, 0.4);
}

.post-body {
  margin-top: 24px;
}

.post-content-text {
  font-size: 16px;
  line-height: 1.8;
  color: rgba(255, 255, 255, 0.9);
  white-space: pre-wrap;
  word-break: break-word;
}

/* 回复区域 */
.replies-section {
  background: rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(20px);
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-radius: 16px;
  padding: 32px;
  animation: fadeInUp 0.6s ease-out 0.2s both;
}

.section-header {
  margin-bottom: 24px;
  padding-bottom: 16px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}

.section-title {
  margin: 0;
  font-size: 22px;
  font-weight: 600;
  color: white;
  display: flex;
  align-items: center;
  gap: 10px;
}

.section-title i {
  color: #a8b5ff;
  font-size: 24px;
}

.reply-count {
  color: rgba(255, 255, 255, 0.6);
  font-size: 16px;
  font-weight: 400;
}

/* 空状态 */
.empty-replies {
  text-align: center;
  padding: 60px 20px;
  color: rgba(255, 255, 255, 0.6);
}

.empty-icon {
  font-size: 64px;
  margin-bottom: 20px;
  opacity: 0.5;
  color: #a8b5ff;
}

/* 回复列表 */
.replies-list {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.reply-item {
  display: flex;
  gap: 16px;
  padding: 20px;
  background: rgba(255, 255, 255, 0.05);
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 12px;
  transition: all 0.3s ease;
  animation: fadeInUp 0.5s ease-out both;
}

.reply-item:hover {
  background: rgba(255, 255, 255, 0.08);
  border-color: rgba(255, 255, 255, 0.2);
  transform: translateX(5px);
}

.reply-floor {
  flex-shrink: 0;
  width: 50px;
  display: flex;
  align-items: flex-start;
  justify-content: center;
  padding-top: 4px;
}

.floor-number {
  display: inline-block;
  width: 36px;
  height: 36px;
  line-height: 36px;
  text-align: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 8px;
  font-size: 14px;
  font-weight: 600;
  color: white;
  box-shadow: 0 2px 8px rgba(102, 126, 234, 0.3);
}

.reply-content {
  flex: 1;
  min-width: 0;
}

.reply-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
  flex-wrap: wrap;
  gap: 10px;
}

.reply-author {
  display: flex;
  align-items: center;
  gap: 8px;
}

.author-icon {
  color: #a8b5ff;
  font-size: 18px;
}

.author-name {
  font-weight: 500;
  color: rgba(255, 255, 255, 0.9);
  font-size: 15px;
}

.reply-time {
  font-size: 13px;
  color: rgba(255, 255, 255, 0.5);
}

.reply-text {
  font-size: 15px;
  line-height: 1.7;
  color: rgba(255, 255, 255, 0.85);
  white-space: pre-wrap;
  word-break: break-word;
}

/* 回复表单区域 */
.reply-form-section {
  background: rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(20px);
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-radius: 16px;
  padding: 32px;
  animation: fadeInUp 0.6s ease-out 0.4s both;
}

.form-header {
  margin-bottom: 20px;
  padding-bottom: 16px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}

.form-title {
  margin: 0;
  font-size: 20px;
  font-weight: 600;
  color: white;
  display: flex;
  align-items: center;
  gap: 10px;
}

.form-title i {
  color: #a8b5ff;
  font-size: 22px;
}

.reply-form {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

::v-deep .reply-textarea .el-textarea__inner {
  background: rgba(255, 255, 255, 0.08);
  border: 2px solid rgba(255, 255, 255, 0.15);
  color: white;
  font-size: 15px;
  border-radius: 12px;
  padding: 16px;
  transition: all 0.3s ease;
  resize: vertical;
}

::v-deep .reply-textarea .el-textarea__inner:focus {
  background: rgba(255, 255, 255, 0.12);
  border-color: #a8b5ff;
  box-shadow: 0 0 0 3px rgba(168, 181, 255, 0.2);
}

::v-deep .reply-textarea .el-textarea__inner::placeholder {
  color: rgba(255, 255, 255, 0.5);
}

::v-deep .reply-textarea .el-input__count {
  color: rgba(255, 255, 255, 0.6);
  background: transparent;
  right: 12px;
  bottom: 8px;
}

.form-actions {
  display: flex;
  justify-content: flex-end;
}

.submit-reply-btn {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
  color: white;
  padding: 12px 28px;
  border-radius: 10px;
  font-weight: 500;
  transition: all 0.3s ease;
  box-shadow: 0 4px 15px rgba(102, 126, 234, 0.3);
}

.submit-reply-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(102, 126, 234, 0.4);
}

.submit-reply-btn:active {
  transform: translateY(0);
}

.submit-reply-btn i {
  margin-right: 6px;
}

/* 登录提示 */
.login-tip {
  background: rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(20px);
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-radius: 16px;
  padding: 24px;
  animation: fadeInUp 0.6s ease-out 0.4s both;
}

::v-deep .login-tip .el-alert {
  background: rgba(255, 255, 255, 0.05);
  border: 1px solid rgba(255, 255, 255, 0.1);
}

::v-deep .login-tip .el-alert__title {
  color: rgba(255, 255, 255, 0.9);
}

::v-deep .login-tip .el-alert__content {
  color: rgba(255, 255, 255, 0.8);
}

.login-link {
  color: #a8b5ff !important;
  font-weight: 500;
  padding: 0 4px;
}

.login-link:hover {
  color: #c4d0ff !important;
  text-decoration: underline;
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

  .post-card,
  .replies-section,
  .reply-form-section {
    padding: 20px;
  }

  .post-title {
    font-size: 22px;
  }

  .reply-item {
    flex-direction: column;
    gap: 12px;
  }

  .reply-floor {
    width: auto;
    padding-top: 0;
  }
}
</style>
