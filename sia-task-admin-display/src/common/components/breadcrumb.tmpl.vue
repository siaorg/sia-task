<template>
  <div class="bread-crumb">
    <div class="left-move"  @click="transitionLeftMenu">
      <img src="../images/transition-left-icon.png" alt="">
    </div>
    <div class="frame-breadcrumb">
      <div class="left-icon" @click="handleClickMoveRight"><span class="el-icon-arrow-left"></span></div>
      <div class="current-router">
        {{$route.meta.title}}
      </div>
      <transition-group class="bread-crumb-main" ref='breadCrumbMainStyle'>
        <ul class="bread-crumb-list" key="home" ref="breadCrumbListStyle">
          <li class="tags-li" v-for="(item,index) in tagsList" :class="{'active': isActive(item.title)}" :key="index">
              <router-link :to="item.path">
                  {{item.title}}
              </router-link>
              <span @click="closeTags(index)"><i class="el-icon-close"></i></span>
          </li>
        </ul>
      </transition-group>
      <div class="right-icon" @click="handleClickMoveLeft"><span class="el-icon-arrow-right right"></span></div>
    </div>
    <div class="current-gateway">
      <el-dropdown @command="handleTags">
          <el-button size="mini" type="primary">
              关闭操作<i class="el-icon-arrow-down el-icon--right"></i>
          </el-button>
          <el-dropdown-menu size="small" slot="dropdown">
              <el-dropdown-item command="other">关闭其他</el-dropdown-item>
              <el-dropdown-item command="all">关闭所有</el-dropdown-item>
              <el-dropdown-item command="logout">退出</el-dropdown-item>
          </el-dropdown-menu>
      </el-dropdown>
    </div>
  </div>
</template>
<script>
export default {
  data () {
    return {
      tagsList: []
    }
  },
  watch: {
    $route: function (newValue, oldValue) {
      this.getBreadcrumb(newValue)
    }
  },
  created () {
    this.getBreadcrumb(this.$route)
  },
  methods: {
    // 左侧菜单 收缩方法
    transitionLeftMenu: function () {
      this.isCollapse = !this.isCollapse
      this.$store.dispatch('CHANGE_MENU_WIDTH_ACTION', this.isCollapse)
    },
    isActive: function (path) {
      return path === this.$route.meta.title
    },
    getBreadcrumb: function (route) {
      const isExist = this.tagsList.some(item => {
        return item.title === route.meta.title
      })
      if (!isExist) {
        this.tagsList.push({
          title: route.meta.title,
          path: route.fullPath,
          name: route.matched[1].components.default.name,
          meta: route.meta
        })
      }
      this.$emit('showKeepLiveList', this.tagsList)
    },
    handleTags: function (command) {
      if (command === 'other') {
        this.closeOther()
      } else if (command === 'all') {
        this.closeAll()
      } else {
        sessionStorage.removeItem('login')
        this.$router.push({path: '/login'})
      }
    },
    // 关闭全部标签
    closeAll: function () {
      this.tagsList = []
      this.$router.push('/home')
    },
    // 关闭其他标签
    closeOther: function () {
      const curItem = this.tagsList.filter(item => {
        return item.title === this.$route.meta.title
      })
      this.tagsList = curItem
    },
    // 关闭单个标签
    closeTags: function (index) {
      const delItem = this.tagsList.splice(index, 1)[0]
      const item = this.tagsList[index] ? this.tagsList[index] : this.tagsList[index - 1]
      if (item) {
        delItem.path === this.$route.fullPath && this.$router.push(item.path)
      } else {
        this.$router.push('/')
      }
    },
    handleClickMoveLeft: function () {
      let breadCrumbListStyle = parseInt(this.getStyle(this.$refs.breadCrumbListStyle, 'width'))
      let breadCrumbMainStyle = parseInt(this.getStyle(this.$refs.breadCrumbMainStyle.$el, 'width'))
      if (breadCrumbListStyle <= breadCrumbMainStyle) {
        return
      }
      let dis = parseInt(this.getStyle(this.$refs.breadCrumbListStyle, 'left')) - 200
      let minWidth = breadCrumbListStyle - breadCrumbMainStyle
      if (dis <= -minWidth) {
        dis = -minWidth
      }
      this.$refs.breadCrumbListStyle.style.left = dis + 'px'
    },
    handleClickMoveRight: function () {
      let breadCrumbListStyle = parseInt(this.getStyle(this.$refs.breadCrumbListStyle, 'width'))
      let breadCrumbMainStyle = parseInt(this.getStyle(this.$refs.breadCrumbMainStyle.$el, 'width'))
      if (breadCrumbListStyle <= breadCrumbMainStyle && parseInt(this.getStyle(this.$refs.breadCrumbListStyle, 'left')) > 0) {
        return
      }
      let dis = parseInt(this.getStyle(this.$refs.breadCrumbListStyle, 'left')) + 200
      if (dis > 0) {
        dis = 0
      }
      this.$refs.breadCrumbListStyle.style.left = dis + 'px'
    },
    getStyle (obj, attr) {
      return obj.currentStyle ? obj.currentStyle[attr] : getComputedStyle(obj)[attr]
    }
  }
}
</script>
<style lang="less" scoped>
@import '../styles/common/breadcrumb.tmpl.less';
@import '../styles/transition.less';
</style>

