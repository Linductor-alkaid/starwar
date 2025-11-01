import Mock from 'mockjs'

const API_BASE = process.env.VUE_APP_API_BASE_URL || '/api/v1'

// Mock 获取帖子列表
Mock.mock(new RegExp(`${API_BASE}/post/list`), 'get', () => {
  const list = []
  const titles = [
    '游戏攻略分享',
    '新手入门指南',
    '高分技巧讨论',
    '道具使用心得',
    '敌人类型分析'
  ]
  
  for (let i = 0; i < 10; i++) {
    list.push({
      id: i + 1,
      title: titles[i % titles.length] + ` #${i + 1}`,
      content: Mock.Random.sentence(20, 50),
      author: Mock.Random.word(5, 10),
      viewCount: Mock.Random.integer(10, 500),
      replyCount: Mock.Random.integer(0, 20),
      createdAt: Mock.Random.datetime('yyyy-MM-dd HH:mm:ss')
    })
  }
  
  return {
    code: 200,
    message: 'success',
    data: {
      total: 50,
      list: list
    }
  }
})

// Mock 获取帖子详情
Mock.mock(new RegExp(`${API_BASE}/post/detail/\\d+`), 'get', (options) => {
  const postId = options.url.match(/\d+/)[0]
  
  return {
    code: 200,
    message: 'success',
    data: {
      id: parseInt(postId),
      title: '游戏攻略分享',
      content: Mock.Random.paragraph(3, 5),
      author: 'testuser',
      viewCount: Mock.Random.integer(100, 1000),
      replyCount: Mock.Random.integer(5, 30),
      createdAt: Mock.Random.datetime('yyyy-MM-dd HH:mm:ss'),
      replies: []
    }
  }
})

// Mock 发布帖子
Mock.mock(new RegExp(`${API_BASE}/post/create`), 'post', (options) => {
  const postData = JSON.parse(options.body)
  
  return {
    code: 200,
    message: '发布成功',
    data: {
      id: Mock.Random.id(),
      title: postData.title
    }
  }
})

// Mock 回复帖子
Mock.mock(new RegExp(`${API_BASE}/post/reply`), 'post', (options) => {
  const replyData = JSON.parse(options.body)
  
  return {
    code: 200,
    message: '回复成功',
    data: {
      id: Mock.Random.id(),
      postId: replyData.postId,
      content: replyData.content
    }
  }
})

