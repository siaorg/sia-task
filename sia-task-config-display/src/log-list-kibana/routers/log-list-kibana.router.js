const TaskLogListPage = resolve => require(['../views/log-list-kibana.page.vue'], resolve)
const logManageModuleRouter = {}
logManageModuleRouter.routers = [
  {
    path: '/log-list-kibana',
    component: TaskLogListPage,
    name: 'LogListKibanaPage',
    meta: {
      title: '调度日志->kibana',
      auth: false
    }
  }
]
export default logManageModuleRouter
