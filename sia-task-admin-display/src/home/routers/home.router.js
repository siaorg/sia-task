const HomePage = resolve => require(['../views/home.page.vue'], resolve)
const HomeModuleRouter = {}
HomeModuleRouter.routers = [
  {
    path: '/home',
    component: HomePage,
    name: 'HomePage',
    meta: {
      title: '首页',
      auth: true
    }
  }
]
export default HomeModuleRouter
