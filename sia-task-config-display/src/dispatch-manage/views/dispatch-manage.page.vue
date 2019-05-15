<template>
    <div class="dispatch-system-default job-manage-edit-page">
      <el-tabs>
        <el-tab-pane label="资源分配详情">
          <div v-show="!showNoDataBox" class="dispatch-info" id="show-list-info" style="display: 'block', width: '100%', height: '400px'"></div>
          <div v-show="showNoDataBox" class="no-data">
            <img src="../../common/images/no-data.png" alt="">
            <span>暂无数据！</span>
          </div>
          <el-table  v-show="!showNoDataBox" :data="echartsData" style="width: calc(100% - 44px);margin-left:14px" class="dispatch-table">
            <el-table-column prop="jobId" label="Job_ID" align="center" width="80">
            </el-table-column>
            <el-table-column align="center" prop="jobKey" label="Job_Key" show-overflow-tooltip>
              <template slot-scope="scope">
                {{scope.row.jobKey}}
              </template>
            </el-table-column>
            <el-table-column show-overflow-tooltip prop="jobTrigerType" label="类型" align="center" width="90" >
              <template slot-scope="scope">
                {{scope.row.jobTrigerType | fileterType}}
              </template>
            </el-table-column>
            <el-table-column show-overflow-tooltip prop="jobTrigerValue" label="Job类型值" align="center">
            </el-table-column>
            <el-table-column show-overflow-tooltip prop="jobAlarmEmail" label="预警邮箱" align="center">
            </el-table-column>
            <el-table-column show-overflow-tooltip prop="jobDesc" label="描述" align="center">
            </el-table-column>
            <template slot="empty">
              <p class="no-data">
                <img src="../../common/images/no-data.png" alt="">
                <span>暂无数据！</span>
              </p>
            </template>
          </el-table>
        </el-tab-pane>
        <el-tab-pane label="调度器管理">
          <div class="dispatch-option">
            <div class="option-list">
              <div class="title">
                <span><img src="../images/run-dispatch.png" alt=""></span>
                <span>工作调度器</span>
              </div>
              <div class="batch-update">
                <el-button class="btn-small delete-btn" :disabled="showDeleteWork.length===0" @click="batchEdit(1, '下线')">批量下线</el-button>
              </div>
              <el-table :data="onLineList" style="width: calc(100% - 20px)" :class="{'plus-height':showDeleteWork.length<=5}" stripe class="option-list-table" @selection-change="(value) => handleSelectionChange(value, 1)">
                <el-table-column type="selection" width="20"></el-table-column>
                <el-table-column prop="name" label="工作调度器" show-overflow-tooltip></el-table-column>
                <el-table-column label="操作" width="48px" align="center">
                  <template slot-scope="scope">
                    <span title="下线" @click="handelClickSaveOnLine(scope.row)"><img src="../images/down-line-btn.png" alt=""></span>
                  </template>
                </el-table-column>
              </el-table>
              <div v-if="showDeleteWork.length>5" class="show-more-list el-icon-d-arrow-right" @click="showMoreListManage($event)"></div>
            </div>
            <div class="option-list">
              <div class="title">
                <span><img src="../images/down-line.png" alt=""></span>
                <span>下线调度器</span>
              </div>
              <div class="batch-update">
                <el-button class="add-btn btn-small" @click="manageAdd('1')">添加</el-button>
                <el-button class="btn-small delete-btn" :disabled="showDeleteOff.length===0" @click="batchEdit(2, '上线')">批量上线</el-button>
              </div>
              <el-table :data="offLineList" style="width: calc(100% - 20px)" :class="{'plus-height':showDeleteOff.length<=5}" stripe class="option-list-table" @selection-change="(value) => handleSelectionChange(value, 2)">
                <el-table-column type="selection" width="20"></el-table-column>
                <el-table-column prop="name" label="下线调度器" show-overflow-tooltip></el-table-column>
                <el-table-column label="操作" width="48px" align="center">
                  <template slot-scope="scope">
                    <span title="上线" @click="handelClickSaveOffLine(scope.row, '上线')"><img src="../images/on-line-btn.png" alt=""></span>
                  </template>
                </el-table-column>
              </el-table>
              <div v-if="showDeleteOff.length>5" class="show-more-list el-icon-d-arrow-right" @click="showMoreListManage($event)"></div>
            </div>

            <div class="option-list">
              <div class="title">
                <span><img src="../images/live-line.png" alt=""></span>
                <span>离线调度器</span>
              </div>
              <div class="batch-update">
                <el-button class="btn-small delete-btn" :disabled="showDeleteLive.length===0" @click="batchEdit(3, '删除')">批量删除</el-button>
              </div>
              <el-table :data="moveLineList" style="width: calc(100% - 20px)" :class="{'plus-height':moveLineList.length<=5}" stripe class="option-list-table" @selection-change="(value) => handleSelectionChange(value, 3)">
                <el-table-column type="selection" width="20"></el-table-column>
                <el-table-column prop="name" label="离线调度器" show-overflow-tooltip></el-table-column>
                <el-table-column label="操作" width="48px" align="center">
                  <template slot-scope="scope">
                    <span class="el-icon-delete" title="删除" @click="handelClickSaveOffLine(scope.row, '删除')"></span>
                  </template>
                </el-table-column>
              </el-table>
              <div v-if="moveLineList.length>5" class="show-more-list el-icon-d-arrow-right" @click="showMoreListManage($event)"></div>
            </div>
            <div class="option-list">
              <div class="title">
                <span><img src="../images/name-list.png" alt=""></span>
                <span>白名单</span>
              </div>
              <div class="batch-update">
                <el-button class="add-btn btn-small" @click="manageAdd('2')">添加</el-button>
                <el-button class="btn-small delete-btn" :disabled="showDeleteName.length===0" @click="batchEdit(4, '删除')">批量删除</el-button>
              </div>
              <div class="table-box">
                <el-table :data="nameBlackList" style="width: calc(100% - 20px)" stripe :class="{'plus-height':nameBlackList.length<=5}" class="option-list-table" @selection-change="(value) => handleSelectionChange(value, 4)">
                  <el-table-column type="selection" width="20"></el-table-column>
                  <el-table-column prop="name" label="白名单" show-overflow-tooltip></el-table-column>
                  <el-table-column label="操作" width="48px" align="center">
                    <template slot-scope="scope">
                      <span class="el-icon-delete" title="删除" @click="handelClickSaveNameBlack(scope.row)"></span>
                    </template>
                  </el-table-column>
                </el-table>
                <div v-if="nameBlackList.length>5" class="show-more-list el-icon-d-arrow-right" @click="showMoreListManage($event)"></div>
              </div>
            </div>
          </div>
        </el-tab-pane>
      </el-tabs>
      <add-manage-tmpl v-if="showAddManage" :addManageType="addManageType" @showHiddenAddManage="showHiddenAddManage"></add-manage-tmpl>
    </div>
</template>

<script>
const addManageTmpl = resolve => require(['../components/add-manage.tmpl.vue'], resolve)
export default {
  name: 'DispatchManagePage',
  components: {addManageTmpl},
  data () {
    return {
      // 修改值
      showAddManage: false,
      showInfo: true,
      showManage: false,
      addManageType: '',
      showDeleteWork: [],
      showDeleteOff: [],
      showDeleteLive: [],
      showDeleteName: [],
      // 之前值
      offLineList: [],
      onLineList: [],
      moveLineList: [],
      nameBlackList: [],
      echartsData: [],
      showNoDataBox: false
    }
  },
  filters: {
    fileterType: function (val) {
      return val.slice(13)
    }
  },
  mounted () {
    this.getDataList()
    this.getDispatchData()
  },
  methods: {
    // 获取资源分配详情列表
    getEchartsData (val) {
      let self = this
      let params = val === undefined ? '' : val
      self.$http.get(self.$api.getApiAddress('/jobapi/getJobList', 'CESHI_API_HOST'), {
        scheduler: params
      }).then((res) => {
        if (res.data.code === 0) {
          this.echartsData = res.data.data
        } else {
          self.$message({message: res.data.message, type: 'error'})
        }
      }).catch(() => {
        self.$message({message: '服务器未响应！', type: 'error'})
      })
    },
    // 点击展示更多
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
    // 添加开始
    handleSelectionChange (val, type) {
      let params = []
      val.forEach(function (ele) {
        if (params.indexOf(ele.name) === -1) {
          params.push(ele.name)
        }
      })
      switch (type) {
        case 1:
          this.showDeleteWork = params.join(',')
          break
        case 2:
          this.showDeleteOff = params.join(',')
          break
        case 3:
          this.showDeleteLive = params.join(',')
          break
        case 4:
          this.showDeleteName = params.join(',')
          break
      }
    },
    batchEdit: function (type, opera) {
      let self = this
      this.$confirm('你确定要批量' + opera + '么?', '', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        switch (type) {
          case 1:
            self.$http.postNoObj(self.$api.getApiAddress('/scheduler/closeScheduler', 'CESHI_API_HOST'), self.showDeleteWork).then((res) => {
              if (res.data.code === 0) {
                this.getDataList()
                self.$message({message: '下线成功！', type: 'success'})
              } else {
                self.$message({message: res.data.message, type: 'error'})
              }
            }).catch(() => {
              self.$message({message: '服务未响应！', type: 'error'})
            })
            break
          case 2:
            self.$http.postNoObj(self.$api.getApiAddress('/scheduler/openScheduler', 'CESHI_API_HOST'), self.showDeleteOff).then((res) => {
              if (res.data.code === 0) {
                this.getDataList()
                self.$message({message: '上线成功！', type: 'success'})
              } else {
                self.$message({message: res.data.message, type: 'error'})
              }
            }).catch(() => {
              self.$message({message: '服务未响应！', type: 'error'})
            })
            break
          case 3:
            self.$http.postNoObj(self.$api.getApiAddress('/scheduler/openScheduler', 'CESHI_API_HOST'), self.showDeleteLive).then((res) => {
              if (res.data.code === 0) {
                this.getDataList()
                self.$message({message: '删除成功！', type: 'success'})
              } else {
                self.$message({message: res.data.message, type: 'error'})
              }
            }).catch(() => {
              self.$message({message: '服务未响应！', type: 'error'})
            })
            break
          case 4:
            self.$http.postNoObj(self.$api.getApiAddress('/scheduler/removeAuthList', 'CESHI_API_HOST'), self.showDeleteName).then((res) => {
              if (res.data.code === 0) {
                this.getDataList()
                self.$message({message: '删除成功！', type: 'success'})
              } else {
                self.$message({message: res.data.message, type: 'error'})
              }
            }).catch(() => {
              self.$message({message: '服务未响应！', type: 'error'})
            })
            break
        }
      })
    },
    // 切换详情和管理
    switchTab: function (params) {
      switch (params) {
        case 1:
          this.showInfo = true
          this.showManage = false
          break
        case 2:
          this.showManage = true
          this.showInfo = false
          break
      }
    },
    manageAdd: function (params) {
      let self = this
      self.showAddManage = true
      self.addManageType = params
    },
    showHiddenAddManage: function (val) {
      this.showAddManage = val
      this.getDataList()
    },
    getDataList: function () {
      let self = this
      self.onLineList = []
      self.offLineList = []
      self.moveLineList = []
      self.nameBlackList = []
      self.$http.get(self.$api.getApiAddress('/scheduler/workinglist', 'CESHI_API_HOST')).then((res) => {
        if (res.data.code === 0) {
          res.data.data.forEach(function (element) {
            self.onLineList.push({
              name: element
            })
          })
        } else {
          self.$message({message: res.data.message, type: 'error'})
        }
      }).catch(() => {
        self.$message({message: '服务未响应！', type: 'error'})
      })
      self.$http.get(self.$api.getApiAddress('/scheduler/blacklist', 'CESHI_API_HOST')).then((res) => {
        if (res.data.code === 0) {
          res.data.data.forEach(function (element, index) {
            self.offLineList.push({
              name: element
            })
          })
        } else {
          self.$message({message: res.data.message, type: 'error'})
        }
      }).catch(() => {
        self.$message({message: '服务未响应！', type: 'error'})
      })
      self.$http.get(self.$api.getApiAddress('/scheduler/offline', 'CESHI_API_HOST')).then((res) => {
        if (res.data.code === 0) {
          res.data.data.forEach(function (element) {
            self.moveLineList.push({
              name: element
            })
          })
        } else {
          self.$message({message: res.data.message, type: 'error'})
        }
      }).catch(() => {
        self.$message({message: '服务未响应！', type: 'error'})
      })
      self.$http.get(self.$api.getApiAddress('/scheduler/getAuthList', 'CESHI_API_HOST')).then((res) => {
        if (res.data.code === 0) {
          res.data.data.forEach(function (element, index) {
            self.nameBlackList.push({
              name: element
            })
          })
        } else {
          self.$message({message: res.data.message, type: 'error'})
        }
      }).catch(() => {
        self.$message({message: '服务未响应！', type: 'error'})
      })
    },
    handelClickSaveOnLine: function (val) {
      let self = this
      this.$confirm('你确定要下线' + val.name + '么?', '', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        self.$http.postNoObj(self.$api.getApiAddress('/scheduler/closeScheduler', 'CESHI_API_HOST'), val.name).then((res) => {
          if (res.data.code === 0) {
            this.getDataList()
            self.$message({message: '下线成功！', type: 'success'})
          } else {
            self.$message({message: res.data.message, type: 'error'})
          }
        }).catch(() => {
          self.$message({message: '服务未响应！', type: 'error'})
        })
      })
    },
    handelClickSaveOffLine: function (val, type) {
      let self = this
      this.$confirm('你确定要' + type + val.name + '么?', '', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        self.$http.postNoObj(self.$api.getApiAddress('/scheduler/openScheduler', 'CESHI_API_HOST'), val.name).then((res) => {
          if (res.data.code === 0) {
            this.getDataList()
            self.$message({message: '上线成功！', type: 'success'})
          } else {
            self.$message({message: res.data.message, type: 'error'})
          }
        }).catch(() => {
          self.$message({message: '服务未响应！', type: 'error'})
        })
      })
    },
    handelClickSaveNameBlack: function (val) {
      let self = this
      this.$confirm('你确定要删除' + val.name + '么?', '', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        self.$http.postNoObj(self.$api.getApiAddress('/scheduler/removeAuthList', 'CESHI_API_HOST'), val.name).then((res) => {
          if (res.data.code === 0) {
            this.getDataList()
            self.$message({message: '删除成功！', type: 'success'})
          } else {
            self.$message({message: res.data.message, type: 'error'})
          }
        }).catch(() => {
          self.$message({message: '服务未响应！', type: 'error'})
        })
      })
    },
    getDispatchData: function () {
      let self = this
      self.$http.get(self.$api.getApiAddress('/monitor/schedulerInfo', 'CESHI_API_HOST')).then((res) => {
        if (res.data.code === 0) {
          self.dispatchInfo(res.data.data)
        } else {
          self.$message({message: res.data.message, type: 'error'})
        }
      }).catch(() => {
        self.$message({message: '服务器未响应！', type: 'error'})
      })
    },
    dispatchInfo: function (params) {
      let self = this
      let parmasList = Object.keys(params)
      let seriesData1 = []
      let seriesData2 = []
      let seriesData3 = []
      if (parmasList.length !== 0) {
        this.getEchartsData(parmasList[0])
        this.showNoDataBox = false
        this.showDispatchInfo = true
        parmasList.forEach(function (val) {
          seriesData1.push(JSON.parse(params[val])['MAX_JOB_NUM'])
        })
        parmasList.forEach(function (val) {
          seriesData2.push(JSON.parse(params[val])['MY_JOB_NUM'])
        })
        parmasList.forEach(function (val) {
          seriesData3.push(JSON.parse(params[val])['ALARM_JOB_NUM'])
        })
      } else {
        this.showDispatchInfo = false
        this.showNoDataBox = true
        return
      }
      let myChart = this.$echarts.init(document.getElementById('show-list-info'))
      let option = {
        grid: [{bottom: '20%', right: '20px', top: '10px', left: '45px'}],
        tooltip: {
          trigger: 'axis',
          show: true,
          backgroundColor: '#F4F8FB',
          extraCssText: 'box-shadow: 3px 2px 4px 0 #E2E8EC;',
          color: '#666666;',
          textStyle: {
            color: '#666666;'
          }
        },
        legend: {
          data: ['JOB上限值', 'JOB运行数量', 'JOB预警值'],
          y: 'bottom',
          icon: 'rect',
          itemWidth: 12, // 设置宽度
          itemHeight: 10 // 设置高度
        },
        toolbox: {show: true},
        calculable: true,
        xAxis: [
          {
            triggerEvent: true,
            axisLine: {
              lineStyle: {
                color: '#A9B4DA' // 坐标轴线线的颜色
              }
            },
            axisLabel: {
              margin: 10, // 刻度标签与轴线之间的距离
              textStyle: {
                color: '#666666',
                fontSize: 12 // 文字的字体大小
              },
              align: 'center'
            },
            data: parmasList
          }
        ],
        yAxis: [
          {
            type: 'value',
            axisTick: {
              show: true // 是否显示坐标轴刻度
            },
            axisLine: {
              lineStyle: {
                color: '#A9B4DA' // 坐标轴线线的颜色
              }
            },
            axisLabel: {
              textStyle: {
                color: '#666666',
                fontSize: 10 // 文字的字体大小
              }
            },
            splitLine: {
              show: false,
              lineStyle: {
                color: 'rgba(169,180,218,0.27)' // 分隔线颜色设置
              }
            }
          }
        ],
        series: [
          {
            name: 'JOB上限值',
            type: 'bar',
            barWidth: 13,
            tiled: 'JOB信息',
            itemStyle: { // 图形的形状
              color: function (value, index) {
                return index === parmasList[0] ? '#D7F59A' : '#C0E080'
              }
            },
            data: seriesData1
          },
          {
            name: 'JOB运行数量',
            type: 'bar',
            barWidth: 13,
            tiled: 'JOB信息',
            itemStyle: { // 图形的形状
              color: function (value, index) {
                return index === parmasList[0] ? '#5FD7D0' : '#54D2CA'
              }
            },
            data: seriesData2
          },
          {
            name: 'JOB预警值',
            type: 'bar',
            barWidth: 13,
            tiled: 'JOB信息',
            itemStyle: { // 图形的形状
              color: function (value, index) {
                return index === parmasList[0] ? '#3878CB' : '#2A64AF'
              }
            },
            data: seriesData3
          }
        ]
      }
      myChart.setOption(option)
      myChart.on('click', function (params) {
        let data = ''
        if (params.componentType === 'xAxis') {
          data = params.value
          self.getEchartsData(params.value)
        } else {
          data = params.name
          self.getEchartsData(params.name)
        }
        // 点击时柱状图变色
        myChart.setOption({
          series: [
            {
              itemStyle: {
                color: function (value, index) {
                  return index === data ? '#D7F59A' : '#C0E080'
                }
              }
            },
            {
              itemStyle: {
                color: function (value, index) {
                  return index === data ? '#5FD7D0' : '#54D2CA'
                }
              }
            },
            {
              itemStyle: {
                color: function (value, index) {
                  return index === data ? '#3878CB' : '#2A64AF'
                }
              }
            }
          ]
        })
      })
    }
  }
}
</script>
<style lang="less" scoped>
@import "../styles/dispatch-manage.page.less";
</style>
<style lang="less">
@import "../styles/dispatch-manage.page.reset.less";
</style>
