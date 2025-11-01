const state = {
  currentScore: 0,
  currentLevel: 1,
  playTime: 0,
  gameHistory: []
}

const mutations = {
  SET_CURRENT_SCORE(state, score) {
    state.currentScore = score
  },
  SET_CURRENT_LEVEL(state, level) {
    state.currentLevel = level
  },
  SET_PLAY_TIME(state, time) {
    state.playTime = time
  },
  SET_GAME_HISTORY(state, history) {
    state.gameHistory = history
  },
  RESET_GAME(state) {
    state.currentScore = 0
    state.currentLevel = 1
    state.playTime = 0
  }
}

const actions = {
  // 更新游戏分数
  updateScore({ commit }, score) {
    commit('SET_CURRENT_SCORE', score)
  },

  // 更新游戏等级
  updateLevel({ commit }, level) {
    commit('SET_CURRENT_LEVEL', level)
  },

  // 更新游戏时长
  updatePlayTime({ commit }, time) {
    commit('SET_PLAY_TIME', time)
  },

  // 重置游戏状态
  resetGame({ commit }) {
    commit('RESET_GAME')
  }
}

const getters = {
  currentScore: state => state.currentScore,
  currentLevel: state => state.currentLevel,
  playTime: state => state.playTime,
  gameHistory: state => state.gameHistory
}

export default {
  namespaced: true,
  state,
  mutations,
  actions,
  getters
}

