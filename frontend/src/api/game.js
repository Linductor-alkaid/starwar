import request from '@/utils/request'

/**
 * 保存游戏记录
 */
export function saveRecord(data) {
  return request({
    url: '/game/record',
    method: 'post',
    data
  })
}

/**
 * 获取个人游戏历史
 */
export function getHistory(params) {
  return request({
    url: '/game/history',
    method: 'get',
    params
  })
}

/**
 * 获取排行榜（前10名 + 当前用户排名）
 */
export function getLeaderboard() {
  return request({
    url: '/game/leaderboard',
    method: 'get'
  })
}

