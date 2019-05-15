<template>
    <div class="dispatch-system-default task-manage-list-page" 
      v-loading="loading"
      element-loading-text="加载中"
      element-loading-spinner="el-icon-loading"
      element-loading-background="rgba(0, 0, 0, 0.2)">
      <div class="section-container">
      <div class="section-header">
        <span class="label">项目名称</span>
        <el-select v-model="jobGroupName" placeholder="项目名称">
          <el-option v-for="(item,index) in jobGroupList" :key="index" :label="item" :value="item"></el-option>
        </el-select>
        <!--<span class="label">Job_key</span>
        <el-select placeholder="Job_key" :disabled="jobGroupName==''" v-model="jobKeyName" filterable allow-create>
          <el-option v-for="(item,index) in jobKeyDataList" :key="index" :label="item" :value="item"></el-option>
        </el-select>-->
        <el-button class="btn-large refresh-btn btn-ml-auto" icon="el-icon-refresh" :loading="loadingRefresh" @click="showHiddenRefreshTaskList">{{loadingRefresh?'加载中':'刷新'}}</el-button>
      </div>
      <div class="section-content">
        <el-collapse v-model="activeNames" accordion @change="handleChangeList">
          <el-collapse-item v-for="(item,index) in titleListCount" :key="index" :name="item.groups" v-if="item.groups!==''">
            <template slot="title">
              <i class="icon-arrow" :class="{'active':activeNames === item.groups}"><img src="../../common//images/arrow-right-list.png" alt=""></i>
              <i class="list-title">{{item.groups}}</i>
              <i class="count-task">Log总数：<em>{{item.jobLogCount}}</em></i>
            </template>
            <el-table
              :data="viewJobLogManageList"
              class="log-manage-table">
              <el-table-column type="expand">
                <template slot-scope="scope" v-if="scope.row.taskLogList!==0">
                <div class="task-info" v-for="(taskInfoList, index) in scope.row.taskLogList" :key="index">
                  <div class="task-name">{{taskInfoList.taskKey}}</div>
                  <div class="task-desc">
                      <!--<span v-show="taskInfoList.taskKey!==null"><i>Task_Key：</i><i>{{taskInfoList.taskKey}}</i></span>-->
                      <span v-show="taskInfoList.taskLogId!==null"><i>日志ID：</i><i>{{taskInfoList.taskLogId}}</i></span>
                      <span v-show="taskInfoList.taskStatus!==null"><i>执行状态：</i><i>{{taskInfoList.taskStatus}}</i></span>
                      <span v-show="taskInfoList.taskHandleTime!==null"><i>执行时间：</i><i>{{taskInfoList.taskHandleTime | formatTime}}</i></span>
                      <span v-show="taskInfoList.taskFinishedTime!==null"><i>执行完成时间：</i><i>{{taskInfoList.taskFinishedTime | formatTime}}</i></span>
                      <span v-show="taskInfoList.taskMsg!==null"><i>执行信息：</i><i>{{taskInfoList.taskMsg}}</i></span>
                    </div>
                  </div>
                </template>
              </el-table-column>
              <el-table-column label="Job_Key" width="120" prop="jobKey" align="center" >
                <template slot="header" slot-scope="scope">
                  <el-popover
                    placement="bottom-start"
                    popper-class="select-box-popver"
                    :ref="item.groups"
                    trigger="click">
                      <el-input type="text" auto-complete="off" v-model="searchJobkeyNmae" @input="selectSearchList" placeholder="请输入应用名称"></el-input>
                      <el-radio-group v-model="jobKeyName" class="scroll-bar">
                        <el-radio v-for="(item, index) in jobKeyDataList" :key="index" :label="item" :value="item"></el-radio>
                      </el-radio-group>
                    <div style="text-align: right;">
                      <el-button class="tip-cancel-btn" @click="hiddenSearchCancel(item.groups, index, 1)">取消</el-button>
                      <el-button class="tip-save-btn"  @click="hiddenSearchCancel(item.groups, index, 2)">确定</el-button>
                    </div>
                    <span slot="reference" class="table-search-box">
                      <i>Job_Key</i>
                      <i><img src="../images/list-search-icon.png" alt=""></i>
                    </span>
                  </el-popover>
                </template>
                <template slot-scope="scope">
                  {{scope.row.jobKey}}
                </template>
              </el-table-column>
              <el-table-column label="Job_调度时间" width="160" align="center" >
                <template slot-scope="scope">
                  {{scope.row.jobTriggerTime | formatTime}}
                </template>
              </el-table-column>
              <el-table-column
                label="日志详情"
                prop="id">
                <template slot-scope="scope">
                  <p class="job-detail">
                    <span v-show="scope.row.jobLogId!==null"><i>日志ID:</i><i>{{scope.row.jobLogId}}</i></span>
                    <!--<span v-show="scope.row.jobTriggerCode!==null"><i>调度状态:</i><i>{{scope.row.jobTriggerCode}}</i></span>-->
                    <span v-show="scope.row.jobHandleCode!==null"><i>执行状态:</i><i>{{scope.row.jobHandleCode}}</i></span>
                    <span v-show="scope.row.jobHandleCode!==null"><i>执行时间:</i><i>{{scope.row.jobHandleTime | formatTime}}</i></span>
                    <span v-show="scope.row.jobHandleFinishedTime!==null"><i>执行完成时间:</i><i>{{scope.row.jobHandleFinishedTime | formatTime}}</i></span>
                    <span v-show="scope.row.jobTriggerMsg!==null"><i>调度信息:</i><i>{{scope.row.jobTriggerMsg}}</i></span>
                    <span v-show="scope.row.jobHandleMsg!==null"><i>执行信息:</i><i>{{scope.row.jobHandleMsg}}</i></span>
                  </p>
                </template>
              </el-table-column>
              <template slot="empty">
                <p class="no-data">
                  <img src="../../common/images/no-data.png" alt="">
                  <span>暂无数据！</span>
                </p>
              </template>
            </el-table>
            <el-pagination v-show="pageCount!=0 && activeNames === item.groups" layout="prev, pager, next, jumper" prev-text="< Previous" next-text="Next >" :page-count="pageCount" :current-page="currentPageIndex" :page-size="pageSize" @current-change="handleCurrentChange">
            </el-pagination>
          </el-collapse-item>
        </el-collapse>
      </div>
    </div>
    </div>
</template>

<script>
import moment from 'moment'
export default {
  name: 'TaskLogPage',
  data () {
    return {
      searchJobkeyNmae: '', // 选择附框内容
      currentPageIndex: 1,
      pageSize: 5,
      pageCount: 0,
      jobGroupName: '',
      jobGroupList: [],
      jobKeyName: '',
      jobKeyDataList: [],
      jobKeyDataListTotal: [], // 搜索框总list
      viewSearchDateList: {},
      viewJobLogManageList: [],
      loadingRefresh: false,
      titleListCount: [], // title  列表  总数
      activeNames: '',
      loading: false // 加载动画
    }
  },
  filters: {
    formatTime: function (val) {
      if (val === null) {
        return ''
      } else {
        return moment(new Date(val)).format('YYYY-MM-DD HH:mm:ss')
      }
    }
  },
  watch: {
    'jobGroupName': function (newVal, oldVal) {
      this.loading = true
      if (newVal === '全部') {
        this.activeNames = ''
        this.getSearchTitleList('')
      } else {
        this.activeNames = newVal
        this.getSearchTitleList(newVal)
      }
    }
  },
  created () {
    this.getSearchList()
  },
  methods: {
    selectSearchList: function () {
      this.jobKeyDataList = this.jobKeyDataListTotal
      if (this.searchJobkeyNmae !== '') {
        this.jobKeyDataList = this.jobKeyDataListTotal.filter((ele) => ele.indexOf(this.searchJobkeyNmae) !== -1)
      }
    },
    // jobkey搜索框
    hiddenSearchCancel: function (val, index, type) {
      this.$refs[val][0].doClose()
      if (type === 2) {
        this.loading = true
        this.getJobLogList(this.pageSize, '1', this.activeNames, this.jobKeyName)
      } else {
        this.jobKeyName = ''
      }
    },
    showHiddenRefreshTaskList: function () {
      let self = this
      self.loadingRefresh = true
      this.getSearchTitleList('')
      // self.getJobLogList(self.pageSize, self.currentPageIndex, self.jobGroupName, this.jobKeyName)
      setTimeout(function () {
        self.loadingRefresh = false
      }, 2000)
    },
    // 查看项目组日志信息
    handleChangeList: function (val) {
      this.activeNames = ''
      this.jobKeyName = ''
      this.searchJobkeyNmae = ''
      if (val !== '') {
        this.loading = true
        setTimeout(() => {
          this.viewJobLogManageList = []
          this.getJobLogList(this.pageSize, this.currentPageIndex, val, this.jobKeyName)
        }, 500)
      }
    },
    getSearchTitleList: function (val) {
      let self = this
      self.$http.get(self.$api.getApiAddress('/logapi/countGroupsJobLogs', 'CESHI_API_HOST'), {
        jobGroupName: val
      }).then((res) => {
        if (res.data.code === 0) {
          self.titleListCount = res.data.data
          if (val !== '') {
            self.getJobLogList(self.pageSize, self.currentPageIndex, self.jobGroupName, this.jobKeyName)
          }
        } else {
          self.$message({message: res.data.message, type: 'error'})
        }
        this.loading = false
      }).catch(() => {
        this.loading = false
        self.$message({message: '服务未响应！', type: 'error'})
      })
    },
    getSearchList: function () {
      let self = this
      self.$http.get(self.$api.getApiAddress('/jobapi/selectAuth', 'CESHI_API_HOST')).then((res) => {
        if (res.data.code !== 0) {
          self.$message({message: res.data.message, type: 'error'})
        } else {
          self.viewSearchDateList = res.data.data
          self.jobGroupList = Object.keys(self.viewSearchDateList)
          if (self.jobGroupList.length !== 0) {
            if (self.jobGroupList.indexOf('全部') === -1) {
              self.jobGroupList.unshift('全部')
            }
            self.jobGroupName = self.jobGroupList[0]
          }
        }
      }).catch(() => {
        self.$message({message: '服务未响应！', type: 'error'})
      })
    },
    getJobLogList: function (pageSize, currentPageIndex, jobGroupName, jobKey) {
      let self = this
      self.$http.get(self.$api.getApiAddress('/logapi/jobAndTaskLogVos', 'CESHI_API_HOST'), {
        pageSize: pageSize,
        currentPage: currentPageIndex,
        jobGroupName: jobGroupName === '全部' ? '' : jobGroupName,
        jobKey: jobKey
      }).then((res) => {
        self.viewJobLogManageList = res.data.data.items
        self.pageCount = res.data.data.totalPage
        setTimeout(() => {
          this.loading = false
          this.activeNames = jobGroupName
        }, 200)
        if (jobGroupName !== '全部') {
          this.jobKeyDataList = this.viewSearchDateList[jobGroupName]
          this.jobKeyDataListTotal = this.viewSearchDateList[jobGroupName]
        }
        if (res.data.code !== 0) {
          self.$message({message: res.data.message, type: 'error'})
        }
      }).catch(() => {
        self.$message({message: '服务未响应！', type: 'error'})
      })
    },
    handleCurrentChange: function (pageIndex) {
      var self = this
      this.loading = true
      self.currentPageIndex = pageIndex
      self.getJobLogList(self.pageSize, pageIndex, self.activeNames, this.jobKeyName)
    }
  }
}
</script>
<style lang="less" scoped>
@import "../styles/job-manage-list.page.less";
</style>
<style lang="less">
@import "../styles/job-manage-list.page.reset.less";
</style>
