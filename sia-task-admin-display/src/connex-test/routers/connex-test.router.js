const ConnextestPage = resolve => require(['../views/connex-test.page.vue'], resolve)
const connexTestRouter = {}
connexTestRouter.routers = [
  {
    path: '/connex-test',
    component: ConnextestPage,
    name: 'ConnextestPage',
    meta: {
      title: 'TASK连通性测试',
      auth: true
    }
  }
]
export default connexTestRouter
