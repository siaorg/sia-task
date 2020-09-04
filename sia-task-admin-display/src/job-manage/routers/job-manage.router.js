const JobManageListPage = resolve => require(['../views/job-manage-list.page.vue'], resolve)
const JobManageCreatePage = resolve => require(['../views/job-manage-create.page.vue'], resolve)
const TaskManageModuleRouter = {}
TaskManageModuleRouter.routers = [
  {
    path: '/job-manage-list',
    component: JobManageListPage,
    name: 'JobManagePage',
    meta: {
      title: 'Job管理',
      auth: true
    }
  },
  {
    path: '/job-manage-create',
    component: JobManageCreatePage,
    name: 'JobManageCreatePage',
    meta: {
      title: '创建task配置',
      auth: false,
      parentnode: {'Job管理': '/job-manage-list'}
    }
  }
]
export default TaskManageModuleRouter
