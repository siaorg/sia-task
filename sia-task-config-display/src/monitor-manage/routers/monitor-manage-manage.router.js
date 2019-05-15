const MonitorManageListPage = resolve => require(['../views/monitor-manage-manage.page.vue'], resolve)
const LogDetailsPage = resolve => require(['../views/log-details-page.vue'], resolve)
const monitorManageModuleRouter = {}
monitorManageModuleRouter.routers = [
  {
    path: '/monitor-manage',
    component: MonitorManageListPage,
    name: 'MonitorManagePage',
    meta: {
      title: '调度器监控',
      auth: true
    }
  },
  {
    path: '/log-details',
    component: LogDetailsPage,
    name: 'LogDeTailsPage',
    meta: {
      title: 'Job日志运行详情',
      auth: false
    }
  }
]
export default monitorManageModuleRouter
