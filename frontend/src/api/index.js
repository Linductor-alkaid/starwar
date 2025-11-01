import request from '@/utils/request'

// 用户认证相关
export const authApi = {
  login: (data) => request.post('/auth/login', data),
  register: (data) => request.post('/auth/register', data)
}

// 用户信息相关
export const userApi = {
  getUserInfo: () => request.get('/user/info'),
  updateUserInfo: (data) => request.put('/user/info', data)
}

// 游戏相关
export const gameApi = {
  saveRecord: (data) => request.post('/game/record', data),
  getHistory: (params) => request.get('/game/history', { params }),
  getLeaderboard: (params) => request.get('/game/leaderboard', { params })
}

// 讨论区相关
export const postApi = {
  getPostList: (params) => request.get('/post/list', { params }),
  getPostDetail: (id) => request.get(`/post/detail/${id}`),
  createPost: (data) => request.post('/post/create', data),
  replyPost: (data) => request.post('/post/reply', data)
}

