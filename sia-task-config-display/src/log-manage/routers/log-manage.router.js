const TaskLogListPage = resolve => require(['../views/task-log-list.page.vue'], resolve)
const logManageModuleRouter = {}
logManageModuleRouter.routers = [
  {
    path: '/task-log-list',
    component: TaskLogListPage,
    name: 'TaskLogPage',
    meta: {
      title: '调度日志',
      auth: false
    }
  }
]
export default logManageModuleRouter
