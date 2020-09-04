<template>
  <div class="mask-task-list-manage" id="mask">
      <div class="mask-content" id="mask-content">
        <div class="mask-main-title">
          <span>引用TASK的JOB</span>
          <i class="close-icon" @click="showHiddenTaskQuote"></i>
        </div>
        <div class="info">
          <p class="router-list" v-show="JSON.stringify(viewDetailList) !== '[]'">
            <span v-for="(item, index) in viewDetailList" :key="index" v-show="item.jobKey!==''"><i>{{index + 1}}</i><i>{{item.jobKey}}</i></span>
          </p>
          <p class="no-data" v-show="JSON.stringify(viewDetailList) === '[]'">
            <span><img src="../images/warnning.png" alt=""></span>
            <span>没有引用该TASK！</span>
          </p>
        </div>
      </div>
  </div>
</template>

<script>
export default {
  name: 'EditJobTmpl',
  props: ['taskQuoteParamsSearch'],
  data () {
    return {
      viewDetailList: null
    }
  },
  created () {
    this.getTaskList()
  },
  methods: {
    getTaskList: function () {
      let self = this
      self.$http.get(self.$api.getApiAddress('/taskapi/selectTaskInJob', 'CESHI_API_HOST'), {
        taskAppName: self.taskQuoteParamsSearch.taskAppName,
        taskGroupName: self.taskQuoteParamsSearch.taskGroupName,
        taskKey: self.taskQuoteParamsSearch.taskKey
      }).then((res) => {
        if (res.data.code === 0) {
          self.viewDetailList = res.data.data
        } else {
          self.$message({message: res.data.message, type: 'error'})
        }
      })
    },
    showHiddenTaskQuote: function () {
      this.$emit('showHiddenTaskQuote', false)
    }
  }
}
</script>
<style lang="less" scoped>
@import '../styles/common/task-quote.tmpl.less';
</style>
