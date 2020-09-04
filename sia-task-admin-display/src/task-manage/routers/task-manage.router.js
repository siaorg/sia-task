const TaskManageListPage = resolve => require(['../views/task-manage-list.page.vue'], resolve)
const ConnextestPage = resolve => require(['../views/connex-test.page.vue'], resolve)
const TaskManageModuleRouter = {}
TaskManageModuleRouter.routers = [
  {
    path: '/task-manage-list',
    component: TaskManageListPage,
    name: 'TaskManagePage',
    meta: {
      title: '任务管理',
      auth: true
    }
  },
  {
    path: '/connex-test',
    component: ConnextestPage,
    name: 'ConnextestPage',
    meta: {
      title: 'TASK连通性测试',
      auth: false
    }
  }
]
export default TaskManageModuleRouter
