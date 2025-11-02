<template>
  <div class="forum-page">
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
          <i class="el-icon-chat-line-square title-icon"></i>
          讨论区
        </h1>
      </div>
      <div class="nav-right">
        <el-button 
          v-if="isLoggedIn" 
          type="primary" 
          class="create-btn"
          icon="el-icon-edit"
          @click="showCreateDialog = true"
        >
          发布帖子
        </el-button>
      </div>
    </div>

    <!-- 主要内容区域 - 居中显示 -->
    <div class="main-container">
      <div class="forum-content">
        <!-- 加载状态 -->
        <div v-if="loading" class="loading-wrapper">
          <i class="el-icon-loading loading-icon"></i>
          <p>加载中...</p>
        </div>

        <!-- 帖子列表 -->
        <div v-else class="post-list">
          <transition-group name="fade-in-up" tag="div">
            <div
              v-for="post in postList"
              :key="post.id"
              class="post-card"
              @click="goToDetail(post.id)"
              @mouseenter="handleCardHover($event)"
              @mouseleave="handleCardLeave($event)"
            >
              <div class="card-header">
                <h3 class="post-title">{{ post.title }}</h3>
                <div class="post-meta">
                  <span class="meta-item">
                    <i class="el-icon-user"></i>
                    {{ post.author }}
                  </span>
                  <span class="meta-divider">•</span>
                  <span class="meta-item">
                    <i class="el-icon-time"></i>
                    {{ formatTime(post.createdAt) }}
                  </span>
                </div>
              </div>
              
              <div class="card-content">
                <p class="post-preview">{{ getPreview(post.content) }}</p>
              </div>
              
              <div class="card-footer">
                <div class="stats">
                  <span class="stat-item">
                    <i class="el-icon-view"></i>
                    {{ post.viewCount || 0 }}
                  </span>
                  <span class="stat-item">
                    <i class="el-icon-chat-dot-round"></i>
                    {{ post.replyCount || 0 }}
                  </span>
                </div>
                <div class="card-glow"></div>
              </div>
            </div>
          </transition-group>

          <!-- 空状态 -->
          <div v-if="!loading && postList.length === 0" class="empty-state">
            <i class="el-icon-document empty-icon"></i>
            <p>暂无帖子，快来发布第一个吧！</p>
          </div>
        </div>

        <!-- 分页 -->
        <div class="pagination-wrapper" v-if="total > pageSize">
          <el-pagination
            @current-change="handlePageChange"
            :current-page="currentPage"
            :page-size="pageSize"
            :total="total"
            layout="total, prev, pager, next"
            background
          />
        </div>
      </div>
    </div>

    <!-- 发布帖子对话框 -->
    <el-dialog 
      title="发布帖子" 
      :visible.sync="showCreateDialog" 
      width="600px"
      class="create-dialog"
    >
      <el-form :model="postForm" label-width="80px">
        <el-form-item label="标题">
          <el-input 
            v-model="postForm.title" 
            placeholder="请输入帖子标题"
            maxlength="50"
            show-word-limit
          ></el-input>
        </el-form-item>
        <el-form-item label="内容">
          <el-input
            type="textarea"
            v-model="postForm.content"
            :rows="6"
            placeholder="请输入帖子内容"
            maxlength="1000"
            show-word-limit
          ></el-input>
        </el-form-item>
      </el-form>
      <div slot="footer">
        <el-button @click="showCreateDialog = false">取消</el-button>
        <el-button type="primary" @click="handleCreatePost">发布</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { getPostList, createPost } from '@/api/post'
import { mapGetters } from 'vuex'

export default {
  name: 'Forum',
  data() {
    return {
      postList: [],
      loading: false,
      currentPage: 1,
      pageSize: 10,
      total: 0,
      showCreateDialog: false,
      postForm: {
        title: '',
        content: ''
      }
    }
  },
  computed: {
    ...mapGetters('user', ['isLoggedIn'])
  },
  methods: {
    async loadPostList() {
      this.loading = true
      try {
        const res = await getPostList({
          page: this.currentPage,
          size: this.pageSize
        })
        if (res.code === 200) {
          this.postList = res.data.list
          this.total = res.data.total
        }
      } catch (error) {
        this.$message.error('加载帖子列表失败')
      } finally {
        this.loading = false
      }
    },
    handlePageChange(page) {
      this.currentPage = page
      this.loadPostList()
    },
    goToDetail(id) {
      this.$router.push(`/forum/${id}`)
    },
    async handleCreatePost() {
      if (!this.postForm.title || !this.postForm.content) {
        this.$message.warning('请填写完整信息')
        return
      }
      try {
        const res = await createPost(this.postForm)
        if (res.code === 200) {
          this.$message.success('发布成功')
          this.showCreateDialog = false
          this.postForm = { title: '', content: '' }
          this.loadPostList()
        }
      } catch (error) {
        this.$message.error('发布失败')
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
        month: '2-digit',
        day: '2-digit'
      })
    },
    // 获取内容预览
    getPreview(content) {
      if (!content) return '暂无内容'
      // 移除HTML标签并截取前100个字符
      const text = content.replace(/<[^>]*>/g, '').replace(/\s+/g, ' ')
      return text.length > 100 ? text.substring(0, 100) + '...' : text
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
    this.loadPostList()
    this.createStars()
  }
}
</script>

<style scoped>
/* 页面容器 */
.forum-page {
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

.create-btn {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
  transition: all 0.3s ease;
}

.create-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 5px 15px rgba(102, 126, 234, 0.4);
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

.forum-content {
  width: 100%;
  max-width: 1200px;
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

/* 帖子列表 */
.post-list {
  display: grid;
  gap: 20px;
}

/* 帖子卡片 - 毛玻璃效果 */
.post-card {
  position: relative;
  background: rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(20px);
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-radius: 16px;
  padding: 24px;
  cursor: pointer;
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  overflow: hidden;
  animation: fadeInUp 0.6s ease-out;
  animation-fill-mode: both;
}

.post-card:nth-child(1) { animation-delay: 0.1s; }
.post-card:nth-child(2) { animation-delay: 0.2s; }
.post-card:nth-child(3) { animation-delay: 0.3s; }
.post-card:nth-child(4) { animation-delay: 0.4s; }
.post-card:nth-child(5) { animation-delay: 0.5s; }

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

.post-card:hover {
  background: rgba(255, 255, 255, 0.15);
  border-color: rgba(255, 255, 255, 0.4);
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.3);
}

.post-card:hover .card-glow {
  opacity: 1;
}

/* 卡片头部 */
.card-header {
  margin-bottom: 16px;
}

.post-title {
  margin: 0 0 12px;
  font-size: 20px;
  font-weight: 600;
  color: white;
  line-height: 1.4;
  transition: color 0.3s ease;
}

.post-card:hover .post-title {
  color: #a8b5ff;
}

.post-meta {
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

/* 卡片内容 */
.card-content {
  margin-bottom: 16px;
}

.post-preview {
  margin: 0;
  font-size: 15px;
  color: rgba(255, 255, 255, 0.8);
  line-height: 1.6;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

/* 卡片底部 */
.card-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 16px;
  border-top: 1px solid rgba(255, 255, 255, 0.1);
}

.stats {
  display: flex;
  gap: 20px;
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 14px;
  color: rgba(255, 255, 255, 0.7);
}

.stat-item i {
  font-size: 16px;
  color: #a8b5ff;
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

/* 对话框样式 */
::v-deep .create-dialog .el-dialog {
  background: rgba(26, 26, 62, 0.95);
  backdrop-filter: blur(20px);
  border: 1px solid rgba(255, 255, 255, 0.2);
}

::v-deep .create-dialog .el-dialog__header {
  background: transparent;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
  padding: 20px;
}

::v-deep .create-dialog .el-dialog__title {
  color: white;
  font-size: 20px;
  font-weight: 600;
}

::v-deep .create-dialog .el-dialog__body {
  background: transparent;
  padding: 30px;
}

::v-deep .create-dialog .el-input__inner,
::v-deep .create-dialog .el-textarea__inner {
  background: rgba(255, 255, 255, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.2);
  color: white;
}

::v-deep .create-dialog .el-input__inner::placeholder,
::v-deep .create-dialog .el-textarea__inner::placeholder {
  color: rgba(255, 255, 255, 0.5);
}

::v-deep .create-dialog .el-form-item__label {
  color: rgba(255, 255, 255, 0.9);
}

::v-deep .create-dialog .el-dialog__footer {
  background: transparent;
  border-top: 1px solid rgba(255, 255, 255, 0.1);
  padding: 20px;
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

  .post-card {
    padding: 20px;
  }

  .post-title {
    font-size: 18px;
  }
}
</style>

