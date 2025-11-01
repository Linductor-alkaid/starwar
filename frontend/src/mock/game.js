import Mock from 'mockjs'

const API_BASE = process.env.VUE_APP_API_BASE_URL || '/api/v1'

// Mock 保存游戏记录
Mock.mock(new RegExp(`${API_BASE}/game/record`), 'post', () => {
  return {
    code: 200,
    message: '保存成功',
    data: null
  }
})

// Mock 获取个人游戏历史
Mock.mock(new RegExp(`${API_BASE}/game/history`), 'get', () => {
  const list = []
  for (let i = 0; i < 10; i++) {
    list.push({
      id: i + 1,
      score: Mock.Random.integer(1000, 5000),
      level: Mock.Random.integer(1, 10),
      playTime: Mock.Random.integer(60, 300),
      recordDate: Mock.Random.datetime('yyyy-MM-dd HH:mm:ss')
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

// Mock 获取排行榜
Mock.mock(new RegExp(`${API_BASE}/game/leaderboard`), 'get', () => {
  const list = []
  const usernames = ['top_player', 'star_wars_fan', 'game_master', 'space_hero', 'defender_pro']
  
  for (let i = 0; i < 20; i++) {
    list.push({
      rank: i + 1,
      userId: i + 1,
      username: i < 5 ? usernames[i] : Mock.Random.word(5, 10),
      score: Mock.Random.integer(5000 - i * 100, 5500 - i * 100),
      level: Mock.Random.integer(8, 12),
      recordDate: Mock.Random.datetime('yyyy-MM-dd HH:mm:ss')
    })
  }
  
  return {
    code: 200,
    message: 'success',
    data: {
      total: 100,
      list: list
    }
  }
})

