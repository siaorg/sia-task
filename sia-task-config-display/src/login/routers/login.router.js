const LoginIndexPage = resolve => require(['../views/login.page'], resolve)

const LoginRouter = {}

LoginRouter.routers = [
  {
    path: '/login',
    component: LoginIndexPage,
    name: 'LoginIndexPage',
    meta: {
      title: '登录页',
      auth: false,
      access: {
        module: 'login',
        page: 'login'
      }
    }
  }
]

export default LoginRouter
