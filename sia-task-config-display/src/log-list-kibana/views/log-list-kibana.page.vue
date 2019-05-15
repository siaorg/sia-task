<template>
    <div class="dispatch-system-default log-list-kibana-page">
      <div class="log-details"></div>
    </div>
</template>

<script>
export default {
  name: 'LogListKibanaPage',
  data () {
    return {
    }
  },
  mounted () {
    let domEle = document.getElementsByClassName('log-details')[0]
    let ipAddress = this.$api.getApiAddress('/', 'CESHI_API_HOST_LOG')
    domEle.innerHTML = `<iframe src="${ipAddress}app/kibana" width='100%' height='100%'></iframe>`
  },
  methods: {
  }
}
</script>
<style lang="less" scoped>
@import "../styles/log-list-kibana.page.less";
</style>