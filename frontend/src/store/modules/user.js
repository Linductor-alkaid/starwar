import { login, register, getUserInfo } from '@/api/auth'
import { setToken, removeToken } from '@/utils/auth'

const state = {
  token: localStorage.getItem('token') || '',
  userInfo: null
}

const mutations = {
  SET_TOKEN(state, token) {
    state.token = token
    if (token) {
      localStorage.setItem('token', token)
    } else {
      localStorage.removeItem('token')
    }
  },
  SET_USER_INFO(state, userInfo) {
    state.userInfo = userInfo
  },
  CLEAR_USER(state) {
    state.token = ''
    state.userInfo = null
    removeToken()
  }
}

const actions = {
  // 登录
  async login({ commit, dispatch }, loginForm) {
    try {
      const res = await login(loginForm)
      if (res.code === 200) {
        commit('SET_TOKEN', res.data.token)
        // 获取用户信息
        await dispatch('getUserInfo')
        return Promise.resolve(res)
      } else {
        return Promise.reject(new Error(res.message))
      }
    } catch (error) {
      return Promise.reject(error)
    }
  },

  // 注册
  async register({ commit }, registerForm) {
    try {
      const res = await register(registerForm)
      return Promise.resolve(res)
    } catch (error) {
      return Promise.reject(error)
    }
  },

  // 获取用户信息
  async getUserInfo({ commit }) {
    try {
      const res = await getUserInfo()
      if (res.code === 200) {
        commit('SET_USER_INFO', res.data)
        return Promise.resolve(res)
      } else {
        return Promise.reject(new Error(res.message))
      }
    } catch (error) {
      commit('CLEAR_USER')
      return Promise.reject(error)
    }
  },

  // 退出登录
  logout({ commit }) {
    commit('CLEAR_USER')
  }
}

const getters = {
  isLoggedIn: state => !!state.token,
  userInfo: state => state.userInfo,
  username: state => state.userInfo?.username || ''
}

export default {
  namespaced: true,
  state,
  mutations,
  actions,
  getters
}

