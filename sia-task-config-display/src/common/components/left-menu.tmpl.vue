<template>
    <div :class="['left-menu', {'active': $store.state.frame.leftMeunWidth}]">
      <div class="logo">
        <span></span>
        <span v-show="!$store.state.frame.leftMeunWidth">调度系统</span>
      </div>
      <el-menu  :default-active="onRoutes()" class="el-menu-vertical" @select="handleSelectMenu" :collapse="$store.state.frame.leftMeunWidth">
          <el-menu-item index="home">
            <i></i>
            <span slot="title">首页</span>
          </el-menu-item>
          <el-menu-item index="dispatch-manage" v-if="showAdminMeun">
            <i></i>
            <span slot="title">调度器管理</span>
          </el-menu-item>
          <el-menu-item index="monitor-manage">
            <i></i>
            <span slot="title">调度监控</span>
          </el-menu-item>
          <el-menu-item index="task-manage-list">
            <i></i>
            <span slot="title">TASK管理</span>
          </el-menu-item>
          <!--<el-menu-item index="connex-test">
            <i></i>
            <span slot="title">连通性测试</span>
          </el-menu-item>-->
          <el-menu-item index="job-manage-list">
            <i></i>
            <span slot="title">JOB管理</span>
          </el-menu-item>
          <el-menu-item index="task-log-list">
            <i></i>
            <span slot="title">调度日志</span>
          </el-menu-item>
          <el-menu-item index="log-list-kibana">
            <i></i>
            <span slot="title">调度日志</span>
          </el-menu-item>
      </el-menu>
    </div>
</template>

<script>
export default {
  name: 'LeftMenuTmpl',
  data () {
    return {
      isCollapse: false,
      delayLogo: true,
      showAdminMeun: true
    }
  },
  watch: {
    '$store.state.frame.leftMeunWidth': function () {
      setTimeout(() => {
        this.delayLogo = !this.delayLogo
      }, 60)
    }
  },
  created () {
    let self = this
    let roleList = sessionStorage.getItem('selectAuth')
    if (roleList !== null) {
      if (JSON.stringify(roleList).indexOf('admin') !== -1) {
        self.showAdminMeun = true
      } else {
        self.showAdminMeun = false
      }
    }
  },
  methods: {
    onRoutes: function () {
      return this.$route.path.replace('/', '')
    },
    handleSelectMenu: function (index) {
      this.$store.dispatch('CHANGE_MENU_ACTION', {activeMenuIndex: index}).then(() => {
        switch (index) {
          case 'home':
            this.$router.push('/home')
            break
          case 'dispatch-manage':
            this.$router.push('/dispatch-manage')
            break
          case 'monitor-manage':
            this.$router.push('/monitor-manage')
            break
          case 'task-manage-list':
            this.$router.push('/task-manage-list')
            break
          // case 'connex-test':
          //   this.$router.push('/connex-test')
          //   break
          case 'job-manage-list':
            this.$router.push({path: '/job-manage-list'})
            break
          case 'task-log-list':
            this.$router.push({path: '/task-log-list'})
            break
          case 'log-list-kibana':
            this.$router.push({path: '/log-list-kibana'})
            break
        }
      })
    }
  }
}
</script>
<style lang="less">
@import '../styles/common/left-menu.tmpl.less';
</style>
<style lang="less" scoped>
@import '../styles/transition.less';
</style>
