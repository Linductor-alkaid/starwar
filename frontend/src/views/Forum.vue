<template>
  <div class="forum">
    <el-container>
      <el-header>
        <h1>💬 讨论区</h1>
        <div>
          <el-button v-if="isLoggedIn" type="primary" @click="showCreateDialog = true">
            发布帖子
          </el-button>
          <el-button @click="$router.push('/')">返回首页</el-button>
        </div>
      </el-header>
      <el-main>
        <el-table :data="postList" style="width: 100%" v-loading="loading">
          <el-table-column prop="title" label="标题" min-width="200">
            <template slot-scope="scope">
              <el-link @click="goToDetail(scope.row.id)" type="primary">
                {{ scope.row.title }}
              </el-link>
            </template>
          </el-table-column>
          <el-table-column prop="author" label="作者" width="150"></el-table-column>
          <el-table-column prop="viewCount" label="浏览" width="100" align="center"></el-table-column>
          <el-table-column prop="replyCount" label="回复" width="100" align="center"></el-table-column>
          <el-table-column prop="createdAt" label="发布时间" width="180"></el-table-column>
        </el-table>
        
        <el-pagination
          v-if="total > pageSize"
          @current-change="handlePageChange"
          :current-page="currentPage"
          :page-size="pageSize"
          :total="total"
          layout="total, prev, pager, next"
          style="margin-top: 20px; text-align: center;"
        />
      </el-main>
    </el-container>

    <!-- 发布帖子对话框 -->
    <el-dialog title="发布帖子" :visible.sync="showCreateDialog" width="600px">
      <el-form :model="postForm" label-width="80px">
        <el-form-item label="标题">
          <el-input v-model="postForm.title" placeholder="请输入帖子标题"></el-input>
        </el-form-item>
        <el-form-item label="内容">
          <el-input
            type="textarea"
            v-model="postForm.content"
            :rows="6"
            placeholder="请输入帖子内容"
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
    }
  },
  mounted() {
    this.loadPostList()
  }
}
</script>

<style scoped>
.forum {
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
</style>

