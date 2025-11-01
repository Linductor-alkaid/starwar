import request from '@/utils/request'

/**
 * 获取帖子列表
 */
export function getPostList(params) {
  return request({
    url: '/post/list',
    method: 'get',
    params
  })
}

/**
 * 获取帖子详情
 */
export function getPostDetail(id) {
  return request({
    url: `/post/detail/${id}`,
    method: 'get'
  })
}

/**
 * 发布帖子
 */
export function createPost(data) {
  return request({
    url: '/post/create',
    method: 'post',
    data
  })
}

/**
 * 回复帖子
 */
export function replyPost(data) {
  return request({
    url: '/post/reply',
    method: 'post',
    data
  })
}

