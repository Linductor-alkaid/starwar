import Mock from 'mockjs'

// 引入Mock数据
import './auth'
import './game'
import './post'

// 设置延迟响应，模拟真实网络请求
Mock.setup({
  timeout: '200-600'
})

console.log('%c[Mock] Mock数据已启用', 'color: #42b983; font-weight: bold')

