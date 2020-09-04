'use strict'

const taskManageStore = {}

/* @usage : store state data shcema
 this.$store.state.taskMsg  */
taskManageStore.state = {
  taskMsg: JSON.parse(sessionStorage.getItem('taskMsg')) || {}
}

/* @usage : unit operation
 this.$store.commit('TASK_MSG') */
taskManageStore.mutations = {
  'TASK_MSG' (state, taskMsg) {
    state.taskMsg = taskMsg
    sessionStorage.setItem('taskMsg', JSON.stringify(state.taskMsg))
  }
}

/* @usage : based on state ,  return state's length or filter state data */
taskManageStore.getters = {
}

/* @usage : basic the operation for view component , this.$store.dispatch('TASK_MSG_ACTION') */
taskManageStore.actions = {
  'TASK_MSG_ACTION' ({ commit }, obj) {
    return new Promise((resolve, reject) => {
      commit('TASK_MSG', obj)
      resolve()
    })
  }
}

export default taskManageStore
