const DiapatchManageListPage = resolve => require(['../views/dispatch-manage.page.vue'], resolve)
const logManageModuleRouter = {}
logManageModuleRouter.routers = [
  {
    path: '/dispatch-manage',
    component: DiapatchManageListPage,
    name: 'DispatchManagePage',
    meta: {
      title: '调度器管理',
      auth: true
    }
  }
]
export default logManageModuleRouter
