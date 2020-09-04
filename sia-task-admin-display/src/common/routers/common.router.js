const NotFoundPage = resolve => require(['../views/404.page'], resolve)
const NotPowerPage = resolve => require(['../views/401.page'], resolve)

const CommonRouter = {}

CommonRouter.routers = [
  {
    path: '/404',
    component: NotFoundPage,
    hidden: true,
    meta: {
      title: '404',
      auth: false
    }
  },
  {
    path: '/401',
    component: NotPowerPage,
    hidden: true,
    meta: {
      title: '401',
      auth: false
    }
  },
  {
    path: '*',
    hidden: true,
    redirect: { path: '/404' }
  }
]

export default CommonRouter
