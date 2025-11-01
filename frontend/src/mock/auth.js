import Mock from 'mockjs'

const API_BASE = process.env.VUE_APP_API_BASE_URL || '/api/v1'

// Mock 用户登录
Mock.mock(new RegExp(`${API_BASE}/auth/login`), 'post', (options) => {
  const { username, password } = JSON.parse(options.body)
  
  if (username === 'testuser' && password === '123456') {
    return {
      code: 200,
      message: '登录成功',
      data: {
        token: 'mock_jwt_token_' + Date.now(),
        userId: 1,
        username: 'testuser'
      }
    }
  } else {
    return {
      code: 401,
      message: '用户名或密码错误',
      data: null
    }
  }
})

// Mock 用户注册
Mock.mock(new RegExp(`${API_BASE}/auth/register`), 'post', (options) => {
  const registerData = JSON.parse(options.body)
  
  return {
    code: 200,
    message: '注册成功',
    data: {
      id: Mock.Random.id(),
      username: registerData.username
    }
  }
})

// Mock 获取用户信息
Mock.mock(new RegExp(`${API_BASE}/user/info`), 'get', () => {
  return {
    code: 200,
    message: 'success',
    data: {
      id: 1,
      username: 'testuser',
      nickname: '测试用户',
      email: 'test@example.com',
      avatar: ''
    }
  }
})

