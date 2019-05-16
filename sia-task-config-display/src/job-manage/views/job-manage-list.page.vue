<template>
    <div class="dispatch-system-default job-manage-list-page" 
      v-loading="loading"
      element-loading-text="加载中"
      element-loading-spinner="el-icon-loading"
      element-loading-background="rgba(0, 0, 0, 0.2)">
      <div class="section-container">
      <div class="section-header">
        <span class="label">项目名称</span>
        <el-select v-model="jobGroupName" placeholder="项目名称" filterable>
          <el-option v-for="(item,index) in jobGroupList" :key="index" :label="item" :value="item"></el-option>
        </el-select>
        <el-button class="btn-large refresh-btn btn-ml-auto" icon="el-icon-refresh" :loading="loadingRefresh" @click="showHiddenRefreshTaskList">{{loadingRefresh?'加载中':'刷新'}}</el-button>
        <el-button class="btn-large edit-btn" @click="handleClickAddJob"> 添加Job </el-button>
      </div>
      <div class="section-content scroll-bar">
        <el-collapse v-model="activeNames" accordion @change="handleChangeList">
          <el-collapse-item v-for="(item,index) in jobGroupTitleList" :key="index" :name="item.jobGroupName" v-if="item.jobGroupName!==''">
            <template slot="title">
              <i class="icon-arrow" :class="{'active':activeNames === item.jobGroupName}"><img src="../../common//images/arrow-right-list.png" alt=""></i>
              <i class="list-title">{{item.jobGroupName}}</i>
              <i class="count-task">JOB数：<em>{{item.jobNum}}</em></i>
            </template>
            <el-table :data="viewJobManageList" style="width: 100%" class="task-manage-table">
              <el-table-column prop="jobId" label="Job_ID" align="center">
              </el-table-column>
              <el-table-column align="center" prop="jobKey" label="Job_Key" show-overflow-tooltip min-width="120">
                <template slot="header" slot-scope="scope">
                    <el-popover
                      placement="bottom-start"
                      popper-class="select-box-popver"
                      :ref="item.jobGroupName"
                      trigger="click">
                      <el-input type="text" auto-complete="off" v-model="searchJobkeyNmae" @input="selectSearchList" placeholder="请输入应用名称"></el-input>
                      <el-radio-group v-model="jobKeyName" class="scroll-bar">
                        <el-radio v-for="(item, index) in jobKeyDataList" :key="index" :label="item" :value="item" :title="item"></el-radio>
                      </el-radio-group>
                      <div style="text-align: right;">
                        <el-button class="tip-cancel-btn" @click="hiddenSearchCancel(item.jobGroupName, index, 1)">取消</el-button>
                        <el-button class="tip-save-btn"  @click="hiddenSearchCancel(item.jobGroupName, index, 2)">确定</el-button>
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
              <el-table-column show-overflow-tooltip prop="triggerInstance" min-width="150" label="调度器" align="center">
              </el-table-column>
              <el-table-column show-overflow-tooltip prop="jobTrigerType" label="类型" align="center">
                <template slot-scope="scope">
                  {{scope.row.jobTrigerType | fileterType}}
                </template>
              </el-table-column>
              <el-table-column show-overflow-tooltip prop="jobTrigerValue" width="100" label="Job类型值" align="center">
              </el-table-column>
              <el-table-column show-overflow-tooltip prop="jobAlarmEmail" label="预警邮箱" align="center">
              </el-table-column>
              <el-table-column show-overflow-tooltip prop="jobPlan" label="Job_Plan" align="center" width="100">
              </el-table-column>
              <el-table-column show-overflow-tooltip prop="jobDesc" label="描述" align="center">
              </el-table-column>
              <el-table-column label="查看" align="center" width="140">
                <template slot-scope="scope">
                  <el-button class="btn-radius check-btn" @click="handleClickTaskList(scope.row)"> TASK信息 </el-button>
                  <el-popover
                    placement="leftCenter"
                    width="30"
                    trigger="hover"
                    popper-class="job-list"
                    @show="showJobStatus(scope.row, index)"
                    >
                    <p class="job-status" :class="statusClassShow" v-show="isStatusBox">{{statusShow}}</p>
                    <el-button class="btn-radius check-btn" slot="reference">状态查看</el-button>
                  </el-popover>
                </template>
              </el-table-column>
              </el-table-column>
              <el-table-column label="操作" width="240">
                <template slot-scope="scope">
                  <el-button class="btn-radius edit-btn" @click="handleClickCreateTask(scope.row)"> 配置TASK </el-button> 
                  <el-button class="btn-radius edit-btn" @click="handleClickEditJob(scope.row)"> 修改Job </el-button>
                  <el-dropdown class="dropdown-select"  @command="handleSelectStatus" @visible-change="handleChangeStatus(scope.row, $event)">
                    <el-button type="primary">
                      状态操作<i class="el-icon-arrow-down el-icon--right"></i>
                    </el-button>
                    <el-dropdown-menu slot="dropdown">
                      <el-dropdown-item v-for="(item, index) in jobStatusList" :key="index" :disabled="item.disabled" :command="composeValue(item.status, scope.row)">{{item.status}}</el-dropdown-item>
                    </el-dropdown-menu>
                  </el-dropdown>
                  <el-button class="btn-radius edit-btn ml" @click="handleClickSetCasca(scope.row, item.jobNum)"> 级联设置 </el-button>
                </template>
              </el-table-column>
              <template slot="empty">
                <p class="no-data">
                  <img src="../../common/images/no-data.png" alt="">
                  <span>暂无数据！</span>
                </p>
              </template>
            </el-table>
            <el-pagination v-show="pageCount!=0 && activeNames === item.jobGroupName" layout="prev, pager, next, jumper" prev-text="< Previous" next-text="Next >" :page-count="pageCount" :current-page="currentPageIndex" :page-size="pageSize" @current-change="handleCurrentChange">
            </el-pagination>
          </el-collapse-item>
        </el-collapse>
      </div>
    </div>
    <!-- add job-->
    <add-job-tmpl v-if="addJobShow" :addParamsSearch="addParamsSearch" v-on:showHiddenAddJob="showHiddenAddJob"></add-job-tmpl>
    <!-- add job end -->
    <!-- edit job-->
    <edit-job-tmpl v-if="editJobShow" :editParamsSearch="editParamsSearch" :editParamsInfo="editParamsInfo" v-on:showHiddenEditJob="showHiddenEditJob"></edit-job-tmpl>
    <!-- edit job end -->
    <!-- edit job-->
    <task-info-details-tmpl v-if="taskInfoDetailsShow" :taskInfoDetailsParams="taskInfoDetailsParams" v-on:showHiddenTaskInfoDetail="showHiddenTaskInfoDetail"></task-info-details-tmpl>
    <!-- edit job end -->
    <!-- set casca -->
    <set-casca-tmpl v-if="setCascaShow" :jobKeyListCasca="jobKeyListCasca" v-on:showHiddenSetCasca="showHiddenSetCasca"></set-casca-tmpl>
    <!-- set casca end -->
    </div>
</template>
<script>
const addJobTmpl = resolve => require(['../components/add-job.tmpl'], resolve)
const editJobTmpl = resolve => require(['../components/edit-job.tmpl'], resolve)
const taskInfoDetailsTmpl = resolve => require(['../components/task-info-details.tmpl'], resolve)
const setCascaTmpl = resolve => require(['../components/set-casca.tmpl'], resolve)
export default {
  name: 'JobManagePage',
  components: {addJobTmpl, editJobTmpl, taskInfoDetailsTmpl, setCascaTmpl},
  data () {
    return {
      activeNames: '',
      searchJobkeyNmae: '', // 选择附框内容
      currentPageIndex: 1,
      pageSize: 5,
      pageCount: 0,
      jobKeyListCasca: {}, // 设置级联关系参数
      setCascaShow: false, // 设置级联关系显示隐藏
      addJobShow: false, // add信息详情显示隐藏
      editJobShow: false, // edit信息详情显示隐藏
      taskInfoDetailsShow: false, // task信息详情显示隐藏
      addParamsSearch: {}, // 添加参数配置
      editParamsSearch: {}, // 修改搜索参数配置
      taskInfoDetailsParams: {}, // task详情弹出框参数
      editParamsInfo: {}, // 信息参数
      jobGroupName: '', // 选中项目组名称
      jobGroupList: [], // 搜索框搜索列表
      jobGroupTitleList: [], // list搜索列表
      jobKeyName: '', // 选中jobkey名称
      jobKeyDataList: [], // jobKey列表
      jobKeyDataListTotal: [], // 搜索框总list
      statusShow: '暂无状态', // 状态变量
      statusClassShow: 'null', // 不同状态显示不同颜色
      isStatusBox: false, // 请求完成显示状态盒子
      jobStatusList: [
        {
          disabled: false,
          status: '执行一次'
        },
        {
          disabled: false,
          status: '激活'
        },
        {
          disabled: false,
          status: '停止'
        },
        {
          disabled: true,
          status: '删除'
        }
      ],
      viewSearchDateList: [],
      viewJobManageList: [],
      loadingRefresh: false,
      selectJobKeyName: [], // jobkey搜索框内容
      loading: false, // 加载中
      elCollapseValue: '' // 当折叠面板选择值改变时，存放改变值变量
    }
  },
  filters: {
    fileterType: function (val) {
      if (val !== '') {
        return val.slice(13)
      }
    }
  },
  watch: {
    'jobGroupName': function (newVal, oldVal) {
      this.loading = true
      if (newVal === '全部') {
        this.activeNames = ''
        this.getListProject('')
      } else {
        this.activeNames = newVal
        this.getListProject(newVal)
      }
    }
  },
  created () {
    this.loading = true
    this.getListProject('')
    this.getSearchList()
  },
  methods: {
    // jobkey搜索框
    hiddenSearchCancel: function (val, index, type) {
      this.$refs[val][0].doClose()
      if (type === 2) {
        this.loading = true
        this.getJobList(this.activeNames, this.jobKeyName, this.pageSize, '1')
      } else {
        this.jobKeyName = ''
      }
    },
    selectSearchList: function () {
      this.jobKeyDataList = this.jobKeyDataListTotal
      if (this.searchJobkeyNmae !== '') {
        this.jobKeyDataList = this.jobKeyDataListTotal.filter((ele) => ele.indexOf(this.searchJobkeyNmae) !== -1)
      }
    },
    // 刷新按钮事件
    showHiddenRefreshTaskList: function () {
      let self = this
      self.loadingRefresh = true
      this.activeNames = ''
      this.getListProject('')
      // self.getJobList(self.jobGroupName, self.jobKeyName, self.pageSize, self.currentPageIndex)
      setTimeout(function () {
        self.loadingRefresh = false
      }, 2000)
    },
    // 项目组列表 =》 带有 jobkey  number
    getListProject: function (val) {
      let self = this
      self.$http.get(self.$api.getApiAddress('/jobapi/selectGroupAndJobCount', 'CESHI_API_HOST'), {
        jobGroupName: this.jobGroupName === '全部' ? '' : val
      }).then((res) => {
        if (res.data.code === 0) {
          self.jobGroupTitleList = res.data.data
          if (val !== '' && val !== '全部') {
            self.getJobList(val, this.jobKeyName, self.pageSize, self.currentPageIndex)
          } else {
            this.loading = false
          }
        } else {
          self.$message({message: res.data.message, type: 'error'})
        }
      }).catch(() => {
        this.loading = false
        self.$message({message: '服务未响应！', type: 'error'})
      })
    },
    // 项目组列表 =》 带有 jobkey  list
    getSearchList: function () {
      let self = this
      self.$http.get(self.$api.getApiAddress('/jobapi/selectAuth', 'CESHI_API_HOST')).then((res) => {
        self.viewSearchDateList = res.data.data
        self.jobGroupList = Object.keys(self.viewSearchDateList)
        if (self.jobGroupList.length !== 0) {
          if (self.jobGroupList.indexOf('全部') === -1) {
            self.jobGroupList.unshift('全部')
          }
          self.jobGroupName = self.jobGroupList[0]
        } else {
          self.$message({message: res.data.message, type: 'error'})
        }
      }).catch(() => {
        self.$message({message: '服务未响应！', type: 'error'})
      })
    },
    getJobList: function (jobGroup, jobKey, pageSize, currentPageIndex) {
      let self = this
      self.$http.get(self.$api.getApiAddress('/jobapi/selectjobs?jobGroupName=' + jobGroup + '&' + 'jobKey=' + jobKey + '&' + 'pageSize=' + pageSize + '&' + 'currentPage=' + currentPageIndex, 'CESHI_API_HOST')).then((res) => {
        if (res.data.code === 0) {
          self.viewJobManageList = res.data.data.items
          self.pageCount = res.data.data.totalPage
          self.currentPageIndex = currentPageIndex
          if (jobGroup !== '全部') {
            this.jobKeyDataList = this.viewSearchDateList[jobGroup]
            this.jobKeyDataListTotal = this.viewSearchDateList[jobGroup]
          }
        } else {
          self.$message({message: res.data.message, type: 'error'})
        }
        setTimeout(() => {
          this.loading = false
          this.activeNames = jobGroup
        }, 200)
      }).catch(() => {
        this.loading = false
        self.$message({message: '服务未响应！', type: 'error'})
      })
    },
    handleChangeList: function (val) {
      this.activeNames = ''
      this.jobKeyName = ''
      this.searchJobkeyNmae = ''
      if (val !== '') {
        this.loading = true
        setTimeout(() => {
          this.viewJobManageList = []
          this.getJobList(val, '', this.pageSize, this.currentPageIndex)
        }, 500)
      }
    },
    deleteJobGroup: function (jobGroup, jobKey) {
      let self = this
      self.$http.get(self.$api.getApiAddress('/jobapi/deleteJobByJobKeyAndGroup', 'CESHI_API_HOST'), {
        jobGroupName: jobGroup,
        jobKey: jobKey
      }).then((res) => {
        switch (res.data.code) {
          case 0:
            this.getListProject(jobGroup)
            self.$message({message: '删除成功', type: 'success'})
            break
          default:
            self.$message({message: res.data.message, type: 'error'})
            break
        }
      }).catch(() => {
        self.$message({message: '服务未响应！', type: 'error'})
      })
    },
    handleSelectStatus: function (val) {
      let self = this
      if (val.status === '删除') {
        this.getJobStatus(val.row).then((params) => {
          if (params !== 'pause') {
            this.$confirm(val.row.jobGroup + '处于' + params + '你确定要删除该Job么?', '', {
              confirmButtonText: '确定',
              cancelButtonText: '取消',
              type: 'warning'
            }).then(() => {
              self.deleteJobGroup(val.row.jobGroup, val.row.jobKey)
            })
          } else {
            self.deleteJobGroup(val.row.jobGroup, val.row.jobKey)
          }
        }, () => {
          this.$message({message: '服务未响应！', type: 'error'})
        })
      } else if (val.status === '执行一次') {
        self.$http.get(self.$api.getApiAddress('/jobapi/runOnceforweb/' + val.row.jobGroup + '/' + val.row.jobKey, 'CESHI_API_HOST')).then((res) => {
          switch (res.data.code) {
            case 0:
              self.$message({message: '操作成功', type: 'success'})
              break
            default:
              self.$message({message: res.data.message, type: 'error'})
              break
          }
        }).catch(() => {
          self.$message({message: '服务未响应！', type: 'error'})
        })
      } else if (val.status === '停止') {
        this.$confirm('你确定要停止' + val.row.jobGroup + '吗?', '', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        }).then(() => {
          self.$http.get(self.$api.getApiAddress('/jobapi/stopJob/' + val.row.jobGroup + '/' + val.row.jobKey, 'CESHI_API_HOST')).then((res) => {
            switch (res.data.code) {
              case 0:
                self.$message({message: '状态操作成功', type: 'success'})
                break
              default:
                self.$message({message: res.data.message, type: 'error'})
                break
            }
          }).catch(() => {
            self.$message({message: '服务未响应！', type: 'error'})
          })
        })
      } else if (val.status === '激活') {
        self.$http.get(self.$api.getApiAddress('/jobapi/activateJob/' + val.row.jobGroup + '/' + val.row.jobKey, 'CESHI_API_HOST')).then((res) => {
          switch (res.data.code) {
            case 0:
              self.$message({message: '状态操作成功', type: 'success'})
              break
            default:
              self.$message({message: res.data.message, type: 'error'})
              break
          }
        }).catch(() => {
          self.$message({message: '服务未响应！', type: 'error'})
        })
      }
    },
    composeValue: function (item, row) {
      return {
        'status': item,
        'row': row
      }
    },
    handleChangeStatus: function (row, event) {
      let self = this
      self.$http.get(self.$api.getApiAddress('/taskinjobapi/selectTaskByJobKey', 'CESHI_API_HOST'), {
        jobGroup: row.jobGroup,
        jobKey: row.jobKey
      }).then((res) => {
        if (res.data.data.length === 0) {
          this.jobStatusList.forEach(function (val) {
            if (val.status === '删除') {
              val.disabled = false
            } else {
              val.disabled = true
            }
          })
          return false
        } else {
          if (event) {
            this.getJobStatus(row).then((res) => {
              switch (res) {
                case null:
                  this.jobStatusList.forEach(function (val) {
                    if (val.status === '删除' || val.status === '激活') {
                      val.disabled = false
                    } else {
                      val.disabled = true
                    }
                  })
                  break
                case 'stop':
                  this.jobStatusList.forEach(function (val) {
                    if (val.status === '激活' || val.status === '删除' || val.status === '停止') {
                      val.disabled = false
                    } else {
                      val.disabled = true
                    }
                  })
                  break
                case 'ready':
                  this.jobStatusList.forEach(function (val) {
                    if (val.status === '停止' || val.status === '删除' || val.status === '执行一次') {
                      val.disabled = false
                    } else {
                      val.disabled = true
                    }
                  })
                  break
                case 'running':
                  this.jobStatusList.forEach(function (val) {
                    if (val.status === '停止' || val.status === '删除' || val.status === '暂停') {
                      val.disabled = false
                    } else {
                      val.disabled = true
                    }
                  })
                  break
                case 'pause':
                  this.jobStatusList.forEach(function (val) {
                    if (val.status === '删除' || val.status === '恢复') {
                      val.disabled = false
                    } else {
                      val.disabled = true
                    }
                  })
                  break
              }
            }, () => {
              this.$message({message: '服务未响应！', type: 'error'})
            })
          }
        }
        if (res.data.code !== 0) {
          self.$message({message: res.data.message, type: 'error'})
        }
      }).catch(() => {
        self.$message({message: '服务未响应！', type: 'error'})
      })
    },
    changeStatusDisable: function (params) {
      this.jobStatusList.forEach(function (val) {
        if (params === val.jobStatus) {
          val.disabled = true
        }
      })
    },
    getJobStatus: function (val) {
      let self = this
      return new Promise((resolve, reject) => {
        this.isStatusBox = false
        self.$http.get(self.$api.getApiAddress('/jobapi/selectJobStatus/' + val.jobGroup + '/' + val.jobKey, 'CESHI_API_HOST')).then((res) => {
          resolve(res.data.data)
        }).catch((err) => {
          reject(err)
        })
      })
    },
    showJobStatus: function (val, index) {
      this.getJobStatus(val).then((res) => {
        this.statusClassShow = res
        this.isStatusBox = true
        switch (res) {
          case null:
            this.statusShow = '已停止'
            break
          case 'ready':
            this.statusShow = '准备中'
            break
          case 'stop':
            this.statusShow = '异常停止'
            break
          case 'running':
            this.statusShow = '正在运行'
            break
        }
      }, () => {
        this.$message({message: '服务未响应！', type: 'error'})
      })
    },
    handleClickCreateTask: function (val) {
      this.$router.push({ path: '/job-manage-create', query: {jobGroupParams: this.jobGroupName, jobKeyParams: this.jobKeyName, jobGroup: val.jobGroup, jobKey: val.jobKey, jobId: val.jobId, currentPage: this.currentPageIndex} })
    },
    // 显示task信息详情事件
    handleClickTaskList: function (val) {
      this.taskInfoDetailsParams = val
      this.taskInfoDetailsShow = true
    },
    // 隐藏task信息详情
    showHiddenTaskInfoDetail: function (val) {
      this.taskInfoDetailsShow = val
    },
    showHiddenSetCasca: function (val) {
      this.getJobList(this.activeNames, '', this.pageSize, this.currentPageIndex)
      this.setCascaShow = val
    },
    // 设置级联job
    handleClickSetCasca: function (val, num) {
      let self = this
      let jonKey = ''
      self.$http.get(self.$api.getApiAddress('/jobapi/selectjobs?jobGroupName=' + this.activeNames + '&' + 'jobKey=' + jonKey + '&' + 'pageSize=' + num + '&' + 'currentPage=1', 'CESHI_API_HOST')).then((res) => {
        if (res.data.code === 0) {
          this.jobKeyListCasca = {
            jobMsg: val,
            jobKeyList: res.data.data.items
          }
          this.setCascaShow = true
        } else {
          self.$message({message: res.data.message, type: 'error'})
        }
      }).catch(() => {
        self.$message({message: '服务未响应！', type: 'error'})
      })
    },
    handleClickEditJob: function (val) {
      this.getJobStatus(val).then((params) => {
        if (params === 'stop' || params === null || params === '') {
          this.editParamsInfo = val
          this.editParamsSearch = {
            jobGroupName: this.jobGroupName,
            jobKeyName: this.jobKeyName,
            currentPageIndex: this.currentPageIndex
          }
          this.editJobShow = true
        } else {
          this.$message({message: val.jobKey + '处于' + params + '状态，请先停止Job在执行操作', type: 'error'})
        }
      }, () => {
        this.$message({message: '服务未响应！', type: 'error'})
      })
    },
    handleClickAddJob: function () {
      this.addParamsSearch = {
        jobGroupName: this.jobGroupName,
        jobKeyName: this.jobKeyName
      }
      this.addJobShow = true
    },
    showHiddenEditJob: function (data, params) {
      let jobGrounp = params.jobGroupName === '全部' ? this.activeNames : params.jobGroupName
      this.activeNames = jobGrounp
      this.getJobList(jobGrounp, params.jobKeyName, this.pageSize, params.currentPageIndex)
      this.editJobShow = data
    },
    showHiddenAddJob: function (data, params) {
      let jobGrounp = params.jobGroupName === '全部' ? this.activeNames : params.jobGroupName
      this.activeNames = jobGrounp
      this.getListProject(jobGrounp)
      this.addJobShow = data
    },
    handleCurrentChange: function (pageIndex) {
      var self = this
      self.currentPageIndex = pageIndex
      this.getJobList(this.activeNames, this.jobKeyName, self.pageSize, pageIndex)
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
