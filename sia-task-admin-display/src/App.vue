<template>
  <div id="app">
    <transition name="page-fade" mode="out-in">
      <router-view></router-view>
    </transition>
  </div>
</template>

<script>
export default {
  name: 'app'
}
</script>

<style lang="less">
@import './common/styles/transition.less';
@import './common/styles/theme.less'; // [ext] inject config.less as global setting
</style>
