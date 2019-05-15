'use strict'

const frameStore = {}

/* @usage : store state data shcema
 this.$store.state.frame  */
frameStore.state = {
  frame: JSON.parse(sessionStorage.getItem('frame')) || {
    activeMenuIndex: '0'
  },
  leftMeunWidth: false
}

/* @usage : unit operation
 this.$store.commit('CHANGE_MENU') */
frameStore.mutations = {
  'CHANGE_MENU' (state, frame) {
    let keys = Object.keys(frame)
    keys.forEach((key) => {
      state.frame[key] = frame[key]
    })
    sessionStorage.setItem('frame', JSON.stringify(state.frame))
  },
  'CHANGE_MENU_WIDTH' (state, leftMeunWidth) {
    state.leftMeunWidth = leftMeunWidth
    console.log(state.leftMeunWidth)
  }
}

/* @usage : based on state ,  return state's length or filter state data */
frameStore.getters = {

}

/* @usage : basic the operation for view component , this.$store.dispatch('CHANGE_MENU_ACTION') */
frameStore.actions = {
  'CHANGE_MENU_ACTION' ({ commit }, obj) {
    return new Promise((resolve, reject) => {
      commit('CHANGE_MENU', obj)
      resolve()
    })
  },
  'CHANGE_MENU_WIDTH_ACTION' ({ commit }, obj) {
    return new Promise((resolve, reject) => {
      commit('CHANGE_MENU_WIDTH', obj)
      resolve()
    })
  }
}

export default frameStore
