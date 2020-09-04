<template>
    <div class="dispatch-system-default monitor-manage-page">
      <div class="section-container">
        <div :class="['section-left', {'active': isShowLeftContent, 'showLeftList': !isShowLeftContent}]">
          <div class="title">
            <i><img src="../images/home-title-icon.png" alt=""></i>任务概览
          </div>
          <div class="work-status">
            <span>
              <i>{{taskStatusTotalNumber.ready}}</i>
              <div class="btn">
                <el-button class="btn-radius ready">准备中</el-button>
              </div>
            </span>
            <span>
              <i>{{taskStatusTotalNumber.running}}</i>
              <div class="btn">
                <el-button class="btn-radius running">正在运行</el-button>
              </div>
            </span>
            <span>
              <i>{{taskStatusTotalNumber.stop}}</i>
              <div class="btn">
                <el-button class="btn-radius stop">已停止</el-button>
              </div>
            </span>
            <span>
              <i>{{taskStatusTotalNumber.expStop}}</i>
              <div class="btn">
                <el-button class="btn-radius errorStop">异常停止</el-button>
              </div>
            </span>
          </div>
          <div class="work-collect">
            <ul>
              <li>
                <span>项目汇总数量</span>
                <span>{{taskProjectTotalNumber.projectNum}}</span>
              </li>
              <!--<li>
                <span>项目对接数量</span>
                <span>30</span>
              </li>-->
              <li>
                <span>TASK汇总数量</span>
                <span>{{taskProjectTotalNumber.taskNum}}</span>
              </li>
              <li>
                <span>JOB汇总数量</span>
                <span>{{taskProjectTotalNumber.jobNum}}</span>
              </li>
            </ul>
          </div>
        </div>
        <div class="center-bg"></div>
        <div class="section-right">
          <div :class="['transition-left', {'active': isShowLeftContent}]" @click="showLeftContent"><img src="../images/tranfrom-left.png" alt=""></div>
          <div class="title-box">
            <div class="title"><i><img src="../images/home-title-icon.png" alt=""></i>调度器运行详情</div>
            <div class="refresh">
              <span>项目组名称</span>
              <el-input type="text" v-model="serachProjectVal" auto-complete="off" placeholder="请输入项目组名称" @change="searchProjectDetail"></el-input>
              <!--<el-button class="btn-large edit-btn"> 搜索 </el-button>-->
              <el-button class="refresh-btn btn-large btn-ml-auto" icon="el-icon-refresh" :loading="loadingRefresh" @click="showHiddenRefreshTaskList">{{loadingRefresh?'加载中':'刷新'}}</el-button>
            </div>
          </div>
          <div class="project-info">
            <div :class="['project-list', {'active': isShowLeftContent}]" v-for="(item, index) of projectStatusList" :key="index">
              <div class="project-name">
                <span>{{item.group}}</span>
              </div>
              <div class="status-count">
                <span>
                  <i>{{item.total.activated}}</i>
                  <i>已激活</i> 
                </span> 
                <span> 
                  <i>{{item.total.stop}}</i>
                  <i>异常停止</i>
                </span>
                <span>
                  <i>{{item.total.sum}}</i>
                  <i>总数 </i>
                </span>
              </div>
              <ul class="project-tag" :class="{'heightPlus': item.portrait.length <= 5}">
                <li v-for="(items, index) of item.portrait" :key="index">
                  <span :title="items.jobKey">{{items.jobKey}}</span>
                  <span :class="items.jobDesc === null ? 'null' : items.jobDesc" @click="showLogDetail(item, items)">
                    {{items.jobDesc | filterStatus}}
                  </span>
                </li>
              </ul>
              <div v-if="item.portrait.length > 5" class="show-more-list el-icon-d-arrow-right" @click="showMoreListManage($event)"></div>
            </div>
          </div>
        </div>
      </div>
    </div>
</template>

<script>
import moment from 'moment'
export default {
  name: 'MonitorManagePage',
  data () {
    return {
      loadingRefresh: false,
      isCollapse: true,
      isShowLeftContent: false,
      projectStatusList: [],
      taskStatusTotalNumber: {
        expStop: 0,
        stop: 0,
        ready: 0,
        running: 0
      }, // 左侧任务概览状态总数
      taskProjectTotalNumber: {
        jobNum: 0,
        projectNum: 0,
        taskNum: 0
      }, // 任务概览 项目统计值
      serachProjectVal: '' // 搜素项目名称
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
    this.getProjectList('')
    // 任务概览 状态统计值
    this.taskStatusTotal()
    // // 任务概览 项目统计值
    this.taskProjectTotal()
  },
  methods: {
    // 点击更多展开相应的列表
    showMoreListManage: function (e) {
      if (e.target.classList.contains('active')) {
        e.target.classList.remove('active')
      } else {
        e.target.classList.add('active')
      }
      if (e.target.previousSibling.previousSibling.classList.contains('active')) {
        e.target.previousSibling.previousSibling.classList.remove('active')
      } else {
        e.target.previousSibling.previousSibling.classList.add('active')
      }
    },
    // 根据项目名字  搜索相应的项目
    searchProjectDetail: function () {
      this.getProjectList(this.serachProjectVal)
    },
    // 任务概览 状态统计值
    taskStatusTotal: function () {
      let self = this
      self.$http.get(self.$api.getApiAddress('/jobapi/selectTaskView', 'CESHI_API_HOST')).then((res) => {
        if (res.data.code === 0) {
          self.taskStatusTotalNumber = res.data.data
        } else {
          self.$message({message: res.data.message, type: 'error'})
        }
      }).catch(() => {
        self.$message({message: '服务未响应！', type: 'error'})
      })
    },
    // 任务概览 项目统计值
    taskProjectTotal: function () {
      let self = this
      self.$http.get(self.$api.getApiAddress('/jobapi/selectSummary', 'CESHI_API_HOST')).then((res) => {
        if (res.data.code === 0) {
          self.taskProjectTotalNumber = res.data.data
        } else {
          self.$message({message: res.data.message, type: 'error'})
        }
      }).catch(() => {
        self.$message({message: '服务未响应！', type: 'error'})
      })
    },
    // 刷新按钮，刷新列表
    showHiddenRefreshTaskList: function () {
      let self = this
      self.loadingRefresh = true
      this.serachProjectVal = ''
      this.taskProjectTotal()
      this.taskStatusTotal()
      self.getProjectList('')
      setTimeout(function () {
        self.loadingRefresh = false
      }, 2000)
    },
    // 跳转日志详情页面
    showLogDetail: function (jobGroup, jobkey) {
      this.$router.push({ path: '/log-details', query: {jobGroup: jobGroup.group, jobKey: jobkey.jobKey} })
    },
    // 左侧菜单显示与影藏
    showLeftContent: function () {
      this.isShowLeftContent = !this.isShowLeftContent
    },
    // 获取列表详情
    getProjectList: function (val) {
      let self = this
      self.$http.get(self.$api.getApiAddress('/jobapi/jobGroupPortrait', 'CESHI_API_HOST'), {
        jobGroupName: val
      }).then((res) => {
        if (res.data.code === 0) {
          self.projectStatusList = res.data.data
        } else {
          self.$message({message: res.data.message, type: 'error'})
        }
      }).catch(() => {
        self.$message({message: '服务未响应！', type: 'error'})
      })
    }
  }
}
</script>
<style lang="less" scoped>
@import "../styles/monitor-manage-manage.page.less";
</style>
<style lang="less">
@import "../styles/monitor-manage-manage.page.reset.less";
</style>
