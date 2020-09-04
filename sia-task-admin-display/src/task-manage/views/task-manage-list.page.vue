<template>
    <div class="dispatch-system-default task-manage-list-page"
      v-loading="loading"
      element-loading-text="加载中"
      element-loading-spinner="el-icon-loading"
      element-loading-background="rgba(0, 0, 0, 0.2)">
      <div class="section-container">
        <div class="section-header">
          <span class="label">项目组名</span>
          <el-select v-model="searchProjectName" :multiple="false" placeholder="项目组名" filterable>
            <el-option v-for="(item,index) in searchTitleGroup" :key="index" :label="item" :value="item"></el-option>
          </el-select>
          <!--<el-button class="btn-large refresh-btn btn-ml-auto" icon="el-icon-refresh">刷新</el-button>-->
          <el-button class="btn-large refresh-btn btn-ml-auto" icon="el-icon-refresh" :loading="loadingRefresh" @click="showHiddenRefreshTaskList">{{loadingRefresh?'加载中':'刷新'}}</el-button>
          <!--<el-button class="btn-large edit-btn" @click="handleClickTConnexTest"> 连通性测试 </el-button>-->
          <el-button class="btn-large edit-btn" @click="handleClickAddTask"> 添加Task </el-button>
        </div>
        <div class="section-content scroll-bar">
          <el-collapse v-model="activeNames" accordion @change="handleChangeList">
            <el-collapse-item title="一致性 Consistency" v-for="(item,index) in searchListGroup" :key="index" :name="item.taskGroupName">
              <template slot="title">
                <i class="icon-arrow" :class="{'active':activeNames === item.taskGroupName}"><img src="../../common//images/arrow-right-list.png" alt=""></i>
                <i class="list-title">{{item.taskGroupName}}</i>
                <i class="count-task">task总数：<em>{{item.groupTaskNum}}</em></i>
              </template>
              <el-table :data="viewTaskManageList" style="width: 100%" class="task-manage-table">
                <el-table-column prop="taskId" label="task_ID" width="80">
                </el-table-column>
                <el-table-column prop="taskAppName">
                  <template slot="header" slot-scope="scope">
                    <el-popover
                      placement="bottom-start"
                      popper-class="select-box-popver"
                      :ref="item.taskGroupName"
                      trigger="click">
                      <el-input type="text" v-model="searchApplyNameTip" @input="changeApplyNameSearch" auto-complete="off" placeholder="请输入应用名称"></el-input>
                      <el-radio-group v-model="searchApplyName" class="scroll-bar">
                        <el-radio v-for="(item, index) in searchApplyDataList" :key="index" :label="item" :value="item"></el-radio>
                      </el-radio-group>
                      <div style="text-align: right;">
                        <el-button class="tip-cancel-btn" @click="hiddenSearchCancel(item.taskGroupName, index, 1)">取消</el-button>
                        <el-button class="tip-save-btn"  @click="hiddenSearchCancel(item.taskGroupName, index, 2)">确定</el-button>
                      </div>
                      <span slot="reference" class="table-search-box">
                        <i>应用名称</i>
                        <i><img src="../images/list-search-icon.png" alt=""></i>
                      </span>
                    </el-popover>
                  </template>
                  <template slot-scope="scope">
                    {{scope.row.taskAppName}}
                  </template>
                </el-table-column>
                <el-table-column prop="taskKey"  show-overflow-tooltip label="task名称" width="" min-width="120px">
                  <template slot="header" slot-scope="scope">
                    <el-popover
                      placement="bottom-start"
                      popper-class="select-box-popver"
                      :ref="item.taskGroupName + index"
                      trigger="click">
                      <el-input type="text" v-model="searchTaskNameTip" @input="changeTaskNameSearch" auto-complete="off" placeholder="请输入应用名称"></el-input>
                      <el-radio-group v-model="searchTaskName" class="scroll-bar">
                        <el-radio v-for="(item, index) in searchTaskList" :key="index" :label="item" :value="item"></el-radio>
                      </el-radio-group>
                      <div style="text-align: right;">
                        <el-button class="tip-cancel-btn" @click="hiddenSearchTaskCancel(item.taskGroupName, index, 1)">取消</el-button>
                        <el-button class="tip-save-btn"  @click="hiddenSearchTaskCancel(item.taskGroupName, index, 2)">确定</el-button>
                      </div>
                      <span slot="reference" class="table-search-box">
                        <i>task名称</i>
                        <i><img src="../images/list-search-icon.png" alt=""></i>
                      </span>
                    </el-popover>
                  </template>
                  <template slot-scope="scope">
                    {{scope.row.taskKey}}
                  </template>
                </el-table-column>
                <el-table-column prop="ipPost" label="OnLine 机器地址" min-width="120px">
                  <template slot-scope="scope">
                    <p class="ip-post-list" v-if="scope.row.taskAppIpPort!=='' && scope.row.taskAppIpPort !== null">
                      <span v-for="(item,index) in scope.row.taskAppIpPort.split(',')" :key="index" :label="item">{{item}}</span>
                    </p>
                  </template>
                </el-table-column>
                <el-table-column prop="taskDesc" label="描述" show-overflow-tooltip>
                </el-table-column>
                <el-table-column label="操作" width="240">
                  <template slot-scope="scope">
                    <el-button class="check-btn btn-radius" @click="handleClickLookTaskQuote(scope.row)"> 查看 </el-button> 
                    <el-button class="edit-btn btn-radius" :disabled="scope.row.taskSource === 'TASK_SOURCE_ZK'" @click="handleClickEditTask(scope.row)"> 修改 </el-button> 
                    <el-button class="edit-btn btn-radius" @click="handleClickConnexTest(scope.row)"> 连通性 </el-button> 
                    <el-popover
                      placement="top-end"
                      width="200"
                      popper-class="alert-detele-box"
                      trigger="click"
                      v-model="scope.row.check"
                      :ref="scope.row.taskAppName + scope.$index"
                      >
                      <p class="title">
                        <span class="el-icon-warning"></span>
                        <span>{{scope.row.taskAppName + scope.$index}}你确定要删除{{scope.row.taskKey}}吗？</span>
                      </p>
                      <div style="text-align: right;">
                        <el-button class="tip-cancel-btn" @click="handleClickDeleteTask(scope.row, scope.$index, 3)">取消</el-button>
                        <el-button class="tip-save-btn" @click="handleClickDeleteTask(scope.row, scope.$index, 2)">确定</el-button>
                      </div>
                      <el-button class="delete-btn btn-radius" slot="reference" @click="handleClickDeleteTask(scope.row, scope.$index, 1)"> 删除 </el-button>
                    </el-popover>
                  </template>
                </el-table-column>
                <template slot="empty">
                  <p class="no-data">
                    <img src="../../common/images/no-data.png" alt="">
                    <span>暂无数据！</span>
                  </p>
                </template>
              </el-table>
              <el-pagination v-show="pageCount!=0 && activeNames === item.taskGroupName" layout="prev, pager, next, jumper" prev-text="< Previous" next-text="Next >" :page-count="pageCount" :current-page="currentPageIndex" :page-size="pageSize" @current-change="handleCurrentChange">
              </el-pagination>
            </el-collapse-item>
          </el-collapse>
        </div>
        <!--<el-pagination v-show="pageCount!=0" layout="prev, pager, next" :page-count="pageCount" :current-page="currentPageIndex" :page-size="pageSize" @current-change="handleCurrentChange">
        </el-pagination>-->
        </div>
        <!-- add task-->
        <add-task-tmpl v-if="addtaskShow" :addParamsSearch="addParamsSearch" v-on:showHiddenAddtask="showHiddenAddtask"></add-task-tmpl>
        <!-- add task end -->
        <!-- edit task-->
        <edit-task-tmpl v-if="editTaskShow" :editParamsSearch="editParamsSearch" :editParamsInfo="editParamsInfo" v-on:showHiddenEdittask="showHiddenEdittask"></edit-task-tmpl>
        <!-- edit task end -->
        <!-- see task-->
        <task-quote-tmpl v-if="taskQuoteShow" :taskQuoteParamsSearch="taskQuoteParamsSearch" v-on:showHiddenTaskQuote="showHiddenTaskQuote"></task-quote-tmpl>
        <!-- see task end -->
    </div>
</template>

<script>
const addTaskTmpl = resolve => require(['../components/add-task.tmpl'], resolve)
const editTaskTmpl = resolve => require(['../components/edit-task.tmpl'], resolve)
const taskQuoteTmpl = resolve => require(['../components/task-quote.tmpl'], resolve)
export default {
  name: 'TaskManagePage',
  components: {addTaskTmpl, editTaskTmpl, taskQuoteTmpl},
  data () {
    return {
      loading: false,
      selectApplyName: [], // 选中弹出框应用名称
      visibleDeteleTask: false, // 删除浮框
      activeNames: '',
      currentPageIndex: 1,
      pageSize: 5,
      pageCount: 0,
      addParamsSearch: {},
      editParamsSearch: {},
      editParamsInfo: {},
      taskQuoteParamsSearch: {},
      addtaskShow: false,
      editTaskShow: false,
      taskQuoteShow: false,
      searchProjectName: '',
      searchTitleGroup: [],
      searchListGroup: [], // 列表
      searchApplyName: '',
      searchApplyDataList: [],
      searchApplyDataListTotal: [], // 总应用名称列表
      searchTaskName: '',
      searchTaskList: [],
      viewTaskManageList: [],
      searchApplyNameTip: '', // 选择附框内容
      searchTaskNameTip: '', // 选择附框内容
      loadingRefresh: false // 刷新按钮动画
    }
  },
  watch: {
    'searchProjectName': function (newVal, oldVal) {
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
    this.loading = true
    this.getSearchProjectList()
  },
  methods: {
    // 连通性测试
    handleClickTConnexTest: function () {
      this.$router.push({path: '/connex-test'})
    },
    changeApplyNameSearch: function () {
      this.searchApplyDataList = this.searchApplyDataListTotal
      if (this.searchApplyNameTip !== '') {
        this.searchApplyDataList = this.searchApplyDataListTotal.filter((ele) => ele.indexOf(this.searchApplyNameTip) !== -1)
      }
    },
    changeTaskNameSearch: function () {
      this.searchTaskList = this.searchTaskListTotal
      if (this.searchTaskNameTip !== '') {
        this.searchTaskList = this.searchTaskListTotal.filter((ele) => ele.indexOf(this.searchTaskNameTip) !== -1)
      }
    },
    // 刷新按钮事件
    showHiddenRefreshTaskList: function () {
      let self = this
      self.loadingRefresh = true
      this.activeNames = ''
      this.getSearchTitleList('')
      setTimeout(function () {
        self.loadingRefresh = false
      }, 2000)
    },
    // 应用名称搜索框
    hiddenSearchCancel: function (val, index, type) {
      this.$refs[val][0].doClose()
      this.searchTaskName = ''
      if (type === 2) {
        this.loading = true
        this.getTaskList(val, this.searchApplyName, this.searchTaskName, this.pageSize, '1')
      } else {
        this.searchApplyName = ''
      }
    },
    // task名称搜索框
    hiddenSearchTaskCancel: function (val, index, type) {
      this.$refs[val + index][0].doClose()
      if (type === 2) {
        this.loading = true
        this.getTaskList(val, this.searchApplyName, this.searchTaskName, this.pageSize, '1')
      } else {
        this.searchTaskName = ''
      }
    },
    // 查看项目组task信息
    handleChangeList: function (val) {
      this.activeNames = ''
      this.searchApplyName = ''
      this.searchTaskName = ''
      if (val !== '') {
        this.viewTaskManageList = []
        this.loading = true
        setTimeout(() => {
          this.getTaskList(val, this.searchApplyName, this.searchTaskName, this.pageSize, this.currentPageIndex)
        }, 500)
      }
    },
    // 列表数据  task => 总数
    getSearchTitleList: function (val) {
      let self = this
      self.$http.get(self.$api.getApiAddress('/taskapi/selectGroupAndCount', 'CESHI_API_HOST'), {
        taskGroupName: this.searchProjectName === '全部' ? '' : val
      }).then((res) => {
        if (res.data.code === 0) {
          self.searchListGroup = res.data.data
          if (val !== '') {
            self.getTaskList(val, this.searchApplyName, this.searchTaskName, this.pageSize, this.currentPageIndex)
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
    getSearchProjectList: function () {
      let self = this
      self.$http.get(self.$api.getApiAddress('/taskapi/selectAuth', 'CESHI_API_HOST')).then((res) => {
        if (res.data.code !== 0) {
          self.$message({message: res.data.message, type: 'error'})
        } else {
          self.searchTitleGroup = res.data.data
          self.searchTitleGroup.unshift('全部')
          self.searchProjectName = self.searchTitleGroup[0]
        }
      }).catch(() => {
        self.$message({message: '服务未响应！', type: 'error'})
      })
    },
    getSearchApplyList: function (val) {
      let self = this
      self.$http.get(self.$api.getApiAddress('/taskapi/selectappsbygroup', 'CESHI_API_HOST'), {
        'groupName': val
      }).then((res) => {
        if (res.data.code !== 0) {
          self.$message({message: res.data.message, type: 'error'})
        } else {
          self.searchApplyDataList = res.data.data
          self.searchApplyDataListTotal = res.data.data
        }
      }).catch(() => {
        self.$message({message: '服务未响应！', type: 'error'})
      })
    },
    getSearchTaskList: function (peoject, apply) {
      let self = this
      self.$http.get(self.$api.getApiAddress('/taskapi/selecttaskkeys', 'CESHI_API_HOST'), {
        'groupName': peoject,
        'appName': ''
      }).then((res) => {
        if (res.data.code !== 0) {
          self.$message({message: res.data.message, type: 'error'})
        } else {
          self.searchTaskList = res.data.data
          self.searchTaskListTotal = res.data.data
        }
      }).catch(() => {
        self.$message({message: '服务未响应！', type: 'error'})
      })
    },
    getTaskList: function (project, apply, task, pageSize, currentPageIndex) {
      let self = this
      self.$http.get(self.$api.getApiAddress('/taskapi/selectTasksByPage', 'CESHI_API_HOST'), {
        pageSize: pageSize,
        currentPage: currentPageIndex,
        taskGroupName: project === '全部' ? '' : project,
        taskAppName: apply === '全部' ? '' : apply,
        taskKey: task === '全部' ? '' : task
      }).then((res) => {
        if (res.data.code !== 0) {
          self.$message({message: res.data.message, type: 'error'})
        } else {
          self.viewTaskManageList = res.data.data.items
          self.pageCount = res.data.data.totalPage
          self.viewTaskManageList.forEach((ele) => {
            ele.check = false
          })
          if (project !== '全部') {
            self.getSearchApplyList(project)
            self.getSearchTaskList(project, apply)
          }
        }
        setTimeout(() => {
          this.loading = false
          this.activeNames = project
        }, 200)
      }).catch(() => {
        self.$message({message: '服务未响应！', type: 'error'})
      })
    },
    handleClickEditTask: function (val) {
      this.editParamsInfo = val
      this.editParamsSearch = {
        searchProjectName: this.searchProjectName,
        searchApplyName: this.searchApplyName,
        searchTaskName: this.searchTaskName,
        currentPageIndex: this.currentPageIndex
      }
      this.editTaskShow = true
    },
    handleClickDeleteTask: function (val, index, type) {
      let self = this
      this.viewTaskManageList.forEach((ele, ind) => {
        if (index === ind) {
          if (type === 1) {
            ele.check = true
          } else if (type === 2) {
            self.deleteTaskListTag(val.taskGroupName, val.taskAppName, val.taskKey)
            ele.check = false
          } else if (type === 3) {
            ele.check = false
            this.$refs[val.taskAppName + index][0].doClose()
          }
        } else {
          ele.check = false
        }
      })
    },
    deleteTaskListTag: function (taskGroupName, taskAppName, taskKey) {
      let self = this
      self.$http.get(self.$api.getApiAddress('/taskapi/deleteTaskByPrimaryKey', 'CESHI_API_HOST'), {
        taskGroupName: taskGroupName,
        taskAppName: taskAppName,
        taskKey: taskKey
      }).then((res) => {
        switch (res.data.code) {
          case 0:
            self.activeNames = taskGroupName
            self.getSearchTitleList(taskGroupName)
            self.$message({message: '删除成功', type: 'success'})
            break
          // case 5002:
          //   self.$message({message: '该Task被Job引用', type: 'error'})
          //   break
          default:
            self.$message({message: res.data.message, type: 'error'})
            break
        }
      }).catch(() => {
        self.$message({message: '服务未响应！', type: 'error'})
      })
    },
    handleClickAddTask: function () {
      this.addParamsSearch = {
        searchProjectName: this.searchProjectName,
        searchApplyName: this.searchApplyName,
        searchTaskName: this.searchTaskName
      }
      this.addtaskShow = true
    },
    showHiddenEdittask: function (data, params) {
      let projectName = params.searchProjectName === '全部' ? this.activeNames : params.searchProjectName
      this.getSearchTitleList(projectName)
      this.editTaskShow = data
      this.activeNames = projectName
    },
    showHiddenAddtask: function (data, params) {
      let projectName = params.searchProjectName === '全部' ? this.activeNames : params.searchProjectName
      this.getSearchTitleList(projectName)
      this.addtaskShow = data
      this.activeNames = projectName
    },
    handleClickLookTaskQuote: function (params) {
      this.taskQuoteParamsSearch = params
      this.taskQuoteShow = true
    },
    handleClickConnexTest: function (params) {
      this.$store.dispatch('TASK_MSG_ACTION', params)
      this.$router.push({path: '/connex-test'})
    },
    showHiddenTaskQuote: function (data) {
      this.taskQuoteShow = data
    },
    handleCurrentChange: function (pageIndex) {
      var self = this
      self.currentPageIndex = pageIndex
      this.getTaskList(this.activeNames, self.searchApplyName, self.searchTaskName, self.pageSize, pageIndex)
    }
  }
}
</script>
<style lang="less" scoped>
@import "../styles/task-manage-list.page.less";
</style>
<style lang="less">
@import "../styles/task-manage-list.page.reset.less";
</style>