import DispatchManageModuleRouter from '../../dispatch-manage/routers/dispatch-manage.router'
import TaskManageModuleRouter from '../../task-manage/routers/task-manage.router.js'
import JobManageModuleRouter from '../../job-manage/routers/job-manage.router.js'
import LogManageModuleRouter from '../../log-manage/routers/log-manage.router.js'
import HomeModuleRouter from '../../home/routers/home.router.js'
import MonitorManageModuleRouter from '../../monitor-manage/routers/monitor-manage-manage.router.js'
const FrameIndexPage = resolve => require(['../views/index.page'], resolve)

const frameRouter = {}
let _routerArray = [
  {
    path: '/',
    redirect: '/login'
  }
]

_routerArray = _routerArray.concat(DispatchManageModuleRouter.routers) // dispatch
_routerArray = _routerArray.concat(HomeModuleRouter.routers) // home
_routerArray = _routerArray.concat(MonitorManageModuleRouter.routers) // monitor
_routerArray = _routerArray.concat(TaskManageModuleRouter.routers) // task
_routerArray = _routerArray.concat(JobManageModuleRouter.routers) // job
_routerArray = _routerArray.concat(LogManageModuleRouter.routers) // log

frameRouter.routers = [
  {
    path: '/',
    component: FrameIndexPage,
    hidden: true,
    meta: {
      title: '任务调度中心',
      auth: true,
      access: {module: 'frame', page: 'index'}
    },
    children: _routerArray
  }
]

export default frameRouter
