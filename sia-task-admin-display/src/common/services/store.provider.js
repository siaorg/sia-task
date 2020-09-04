'use strict'
import Vue from 'vue'
import Vuex from 'vuex'
import FrameStore from '../../frame/services/frame.store.js'
import TaskManageStore from '../../task-manage/services/task-manage.store.js'

Vue.use(Vuex)

export default new Vuex.Store(
  {
    strict: process.env.NODE_ENV !== 'production', // [ext] open vuex strict mode
    modules: {
      frame: FrameStore,
      TaskManage: TaskManageStore
    }
  }
)
