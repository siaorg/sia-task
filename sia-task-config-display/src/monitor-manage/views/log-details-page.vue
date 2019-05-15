<template>
    <div class="dispatch-system-default log-details-page">
      <div class="section-container">
        <div class="section-header">
          <span>日志详情</span>
          <span>{{$route.query.jobGroup}} → {{$route.query.jobKey}}</span>
          <el-button class="btn-large refresh-btn btn-ml-auto" icon="el-icon-refresh" :loading="loadingRefresh" @click="showHiddenRefreshTaskList">{{loadingRefresh?'加载中':'刷新'}}</el-button>
        </div>
        <div class="section-content">
          <el-collapse class="scroll-bar" v-if="showLogList" v-model="activeNames" accordion @change="handleChange">
            <el-collapse-item title="一致性 Consistency" v-for="(item, index) in viewJobLogManageList" :name="index" :key="index">
              <template slot="title">
                <div class="title-box">
                  <div class="show-icon " :class="activeNames === index? 'el-icon-minus':'el-icon-plus'"></div>
                  <div class="job-info">
                    <div class="job-name">{{item.jobKey}}</div>
                    <div class="job-desc">
                      <span v-show="item.jobLogId!==null"><i>日志ID:</i><i>{{item.jobLogId}}</i></span>
                      <!--<span v-show="item.jobTriggerCode!==null"><i>调度状态:</i><i>{{item.jobTriggerCode}}</i></span>-->
                      <span v-show="item.jobHandleCode!==null"><i>执行状态:</i><i>{{item.jobHandleCode}}</i></span>
                      <span v-show="item.jobHandleTime!==null"><i>执行时间:</i><i>{{item.jobHandleTime | formatTime}}</i></span>
                      <span v-show="item.jobHandleFinishedTime!==null"><i>执行完成时间:</i><i>{{item.jobHandleFinishedTime | formatTime}}</i></span>
                      <span v-show="item.jobTriggerMsg!==null"><i>调度信息:</i><i>{{item.jobTriggerMsg}}</i></span>
                      <span v-show="item.jobHandleMsg!==null"><i>执行信息:</i><i>{{item.jobHandleMsg}}</i></span>
                    </div>
                  </div>
                </div>
                <p class="left-range-icon"><span>{{index + 1}}</span><span :class="{'active':activeNames==index}"></span></p>
              </template>
              <div class="task-info" v-if="item.taskLogList!==0" v-for="(taskInfoList, index) in item.taskLogList" :key="index">
                <div class="task-name">{{taskInfoList.taskKey}}</div>
                <div class="task-desc">
                  <!--<span v-show="taskInfoList.taskKey!==null"><i>Task_Key:</i><i>{{taskInfoList.taskKey}}</i></span>-->
                  <span v-show="taskInfoList.taskLogId!==null"><i>日志ID:</i><i>{{taskInfoList.taskLogId}}</i></span>
                  <span v-show="taskInfoList.taskStatus!==null"><i>执行状态:</i><i>{{taskInfoList.taskStatus}}</i></span>
                  <span v-show="taskInfoList.taskHandleTime!==null"><i>执行时间：</i><i>{{taskInfoList.taskHandleTime | formatTime}}</i></span>
                  <span v-show="taskInfoList.taskFinishedTime!==null"><i>执行完成时间:</i><i>{{taskInfoList.taskFinishedTime | formatTime}}</i></span>
                  <span v-show="taskInfoList.taskMsg!==null"><i>执行信息：</i><i>{{taskInfoList.taskMsg}}</i></span>
                </div>
              </div>
            </el-collapse-item>
          </el-collapse>
          <div class="no-data" v-if="!showLogList">
            <img src="../../common/images/no-data.png" alt="">
            <span>暂无数据！</span>
          </div>
          <el-pagination v-show="pageCount!=0"  layout="prev, pager, next, jumper" prev-text="< Previous" next-text="Next >" :page-count="pageCount" :current-page="currentPageIndex" :page-size="pageSize" @current-change="handleCurrentChange">
          </el-pagination>
        </div>
    	</div>
    </div>
</template>
<script>
import moment from 'moment'
export default {
  name: 'LogDeTailsPage',
  data () {
    return {
      pageCount: 0,
      currentPageIndex: 1,
      pageSize: 10,
      activeNames: 0,
      viewJobLogManageList: [],
      showLogList: true,
      loadingRefresh: false
    }
  },
  filters: {
    filterStatus: function (val) {
      switch (val) {
        case null:
          return '已停止'
        case 'ready':
          return '准备中'
        case 'stop':
          return '异常停止'
        case 'running':
          return '正在运行'
      }
    },
    formatTime: function (val) {
      if (val === null) {
        return ''
      } else {
        return moment(new Date(val)).format('YYYY-MM-DD HH:mm:ss')
      }
    }
  },
  created () {
    this.getJobLogList(1)
  },
  methods: {
    // 刷新按钮事件
    showHiddenRefreshTaskList: function () {
      let self = this
      self.loadingRefresh = true
      this.getJobLogList(this.currentPageIndex)
      setTimeout(function () {
        self.loadingRefresh = false
      }, 2000)
    },
    handleChange (val) {
      if (val === '') {
        this.activeNames = 0
      }
    },
    getJobLogList: function (currentPageIndex) {
      let self = this
      self.$http.get(self.$api.getApiAddress('/logapi/jobAndTaskLogVos', 'CESHI_API_HOST'), {
        pageSize: this.pageSize,
        currentPage: currentPageIndex,
        jobGroupName: this.$route.query.jobGroup === '全部' ? '' : this.$route.query.jobGroup,
        jobKey: this.$route.query.jobKey === '全部' ? '' : this.$route.query.jobKey
      }).then((res) => {
        if (res.data.code !== 0) {
          self.$message({message: res.data.message, type: 'error'})
        } else {
          self.viewJobLogManageList = res.data.data.items
          self.showLogList = JSON.stringify(self.viewJobLogManageList) !== '[]'
          self.pageCount = res.data.data.totalPage
        }
      }).catch(() => {
        self.$message({message: '服务未响应！', type: 'error'})
      })
    },
    handleCurrentChange: function (pageIndex) {
      var self = this
      self.currentPageIndex = pageIndex
      this.getJobLogList(pageIndex)
    }
  }
}
</script>
<style lang="less" scoped>
@import "../styles/log-details.page.less";
</style>
<style lang="less">
@import "../styles/log-details.reste.page.less";
</style>