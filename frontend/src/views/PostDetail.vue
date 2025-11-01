<template>
  <div class="post-detail">
    <el-container>
      <el-header>
        <h1>帖子详情</h1>
        <el-button @click="$router.push('/forum')">返回列表</el-button>
      </el-header>
      <el-main>
        <div v-loading="loading">
          <div class="post-content" v-if="postDetail">
            <h2>{{ postDetail.title }}</h2>
            <div class="post-meta">
              <span>作者：{{ postDetail.author }}</span>
              <span>浏览：{{ postDetail.viewCount }}</span>
              <span>回复：{{ postDetail.replyCount }}</span>
              <span>时间：{{ postDetail.createdAt }}</span>
            </div>
            <div class="post-body">
              {{ postDetail.content }}
            </div>
          </div>
          
          <div class="reply-section" v-if="isLoggedIn">
            <h3>发表回复</h3>
            <el-input
              type="textarea"
              v-model="replyContent"
              :rows="4"
              placeholder="请输入回复内容"
            ></el-input>
            <el-button type="primary" @click="handleReply" style="margin-top: 10px">
              提交回复
            </el-button>
          </div>
        </div>
      </el-main>
    </el-container>
  </div>
</template>

<script>
import { getPostDetail, replyPost } from '@/api/post'
import { mapGetters } from 'vuex'

export default {
  name: 'PostDetail',
  data() {
    return {
      postDetail: null,
      loading: false,
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
        const res = await getPostDetail(postId)
        if (res.code === 200) {
          this.postDetail = res.data
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
      try {
        const res = await replyPost({
          postId: this.postDetail.id,
          content: this.replyContent,
          parentId: null
        })
        if (res.code === 200) {
          this.$message.success('回复成功')
          this.replyContent = ''
          this.loadPostDetail()
        }
      } catch (error) {
        this.$message.error('回复失败')
      }
    }
  },
  mounted() {
    this.loadPostDetail()
  }
}
</script>

<style scoped>
.post-detail {
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

.post-content h2 {
  margin-bottom: 20px;
  color: #333;
}

.post-meta {
  display: flex;
  gap: 20px;
  margin-bottom: 20px;
  padding-bottom: 15px;
  border-bottom: 1px solid #eee;
  color: #666;
  font-size: 14px;
}

.post-body {
  line-height: 1.8;
  color: #333;
  margin-bottom: 30px;
}

.reply-section {
  margin-top: 40px;
  padding-top: 30px;
  border-top: 1px solid #eee;
}

.reply-section h3 {
  margin-bottom: 15px;
}
</style>

