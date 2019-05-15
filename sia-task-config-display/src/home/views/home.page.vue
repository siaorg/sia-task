<template>
  <div class="dispatch-system-default home-page scroll-bar">
    <div class="opera-statistics">
      <div class="statistics-list" @click="showMsgClick(1)">
        <span>调度器信息</span>
        <span>{{dispatchInfoData}}</span>
        <span class="img-box"><i></i></span>
        <span>调度中心调度器的数量</span>
      </div>
      <div class="statistics-list" @click="showMsgClick(2)">
        <span>调度次数</span>
        <span>{{diapatchNumber}}</span>
        <span class="img-box"><i></i></span>
        <span>调度中心触发的调度数量</span>
      </div>
      <div class="statistics-list" @click="showMsgClick(3)">
        <span>对接项目总数</span>
        <span>{{diapatchTotal.group_count}}</span>
        <span class="img-box"><i></i></span>
        <span>
          <i>JOB总数：{{diapatchTotal.job_count}}</i>
          <i>调度中心对接项目总数、JOB总数</i>
        </span>
      </div>
    </div>
    
    <div class="run-status" v-show="initShowMsg">
      <span class="title"><i><img src="../images/home-title-icon.png" alt=""></i>资源分配详情</span>
      <div class="show-run-status">
        <div class="show-list" id="show-list-info" v-show="!showNoData" :style="{display: 'block',width: eachartsWidth + 'px' , 'max-width': eachartsWidth + 'px' ,height: '350px'}"></div>
        <div class="no-data" v-show="showNoData">
          <img src="../../common/images/no-data.png" alt="">
          <div>暂无数据！</div>
        </div>
      </div>
    </div>
    <div class="run-status" v-show="initShowCount">
      <span class="title"><i><img src="../images/home-title-icon.png" alt=""></i>调度次数</span>
      <div class="search-box">
        <el-select placeholder="请选择执行器" v-model="schedulerNumberTag">
          <el-option v-for="(item,index) in schedulerNumberList" :key="index" :label="item" :value="item"></el-option>
        </el-select>
        <el-date-picker
            v-model="dataNumberTime"
            type="datetimerange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期">
          </el-date-picker>
      </div>
      <div class="show-list reset-height" v-show="!showNumberNoData" id="show-list-number" :style="{display: 'block',width: eachartsWidth + 'px' ,height: '400px'}"></div>
      <div class="no-data" v-show="showNumberNoData">
        <img src="../../common/images/no-data.png" alt="">
        <div>暂无数据！</div>
      </div>
    </div>
    <div class="run-status" v-show="initShowCount">
      <span class="title"><i><img src="../images/home-title-icon.png" alt=""></i>任务调度详情</span>
      <div class="search-box">
        <el-select placeholder="请选择执行器" v-model="schedulerTag">
          <el-option v-for="(item,index) in schedulerList" :key="index" :label="item" :value="item"></el-option>
        </el-select>
        <el-select placeholder="请选择查询级别" v-model="joBOrTaskTag">
          <el-option v-for="(item,index) in joBOrTaskList" :key="index" :label="item" :value="item"></el-option>
        </el-select>
        <el-date-picker
            v-model="dataTime"
            type="datetimerange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期">
        </el-date-picker>
      </div>
      <div class="show-list reset-height" v-show="!showProjectNoData" id="show-list-total" :style="{display: 'block',width: eachartsWidth + 'px' ,height: '400px'}"></div>
      <div class="no-data" v-show="showProjectNoData">
        <img src="../../common/images/no-data.png" alt="">
        <div>暂无数据！</div>
      </div>
    </div>
    <div class="home-table-box" v-show="initShowStatus">
      <span class="title title-table"><i><img src="../images/home-title-icon.png" alt=""></i>对接项目总数详情</span>
      <el-table :data="projectCountData" class="home-table">
        <el-table-column property="jobGroup" label="jobGroup"></el-table-column>
        <el-table-column property="jobCount" label="JOB数量"></el-table-column>
        <el-table-column property="taskCount" label="TASK数量"></el-table-column>
        <el-table-column property="Emails" label="预警邮箱">
          <template slot-scope="scope">
            <p class="emails-list" v-if="scope.row.hasOwnProperty('Emails')">
              <span v-for="(item,index) in scope.row.Emails.split(',')" :key="index" :label="item">{{item}}</span>
            </p>
          </template>
        </el-table-column>
      </el-table>
    </div>
  </div>
</template>

<script>
export default {
  name: 'HomePage',
  data () {
    return {
      initShowMsg: true, // 初始化显示调度器信息
      initShowCount: true, // 初始化显示调度次数
      initShowStatus: true, // 初始化显示调度状态
      showNoData: false,
      showNumberNoData: false,
      dataTime: '',
      dataNumberTime: '',
      showDispatchInfo: true,
      eachartsWidth: '',
      dispatchInfoData: '0',
      diapatchNumber: '0',
      diapatchTotal: {
        job_count: 0,
        group_count: 0
      },
      warningMonitor: [],
      joBOrTaskList: ['JOB运行概览', 'TASK运行概览'],
      joBOrTaskTag: 'JOB运行概览',
      schedulerList: [],
      schedulerNumberList: ['JOB调度次数', 'TASK调度次数'],
      schedulerTag: '全部',
      schedulerNumberTag: 'JOB调度次数',
      showProjectNoData: false, // 对接项目没有数据显示
      projectCountData: [] // 对接项目总数详情数组
    }
  },
  watch: {
    'dataTime': function (newVal) {
      if (this.initShowCount) {
        if (this.joBOrTaskTag === 'JOB运行概览') {
          this.getDispatchJobTotal(this.$formatDate.dateFormat(newVal[0]), this.$formatDate.dateFormat(newVal[1]), this.schedulerTag)
        } else {
          this.getDispatchTaskTotal(this.$formatDate.dateFormat(newVal[0]), this.$formatDate.dateFormat(newVal[1]), this.schedulerTag)
        }
      }
    },
    'dataNumberTime': function (newVal) {
      if (this.initShowCount) {
        this.getDispatchNumber(this.$formatDate.dateFormat(newVal[0]), this.$formatDate.dateFormat(newVal[1]), this.schedulerNumberTag)
      }
    },
    'schedulerNumberTag': function (newVal) {
      if (this.initShowCount) {
        this.getDispatchNumber(this.$formatDate.dateFormat(this.dataNumberTime[0]), this.$formatDate.dateFormat(this.dataNumberTime[1]), newVal)
      }
    },
    'joBOrTaskTag': function (newVal) {
      if (this.initShowCount) {
        if (newVal === 'JOB运行概览') {
          this.getDispatchJobTotal(this.$formatDate.dateFormat(this.dataTime[0]), this.$formatDate.dateFormat(this.dataTime[1]), this.schedulerTag)
        } else {
          this.getDispatchTaskTotal(this.$formatDate.dateFormat(this.dataTime[0]), this.$formatDate.dateFormat(this.dataTime[1]), this.schedulerTag)
        }
      }
    },
    'schedulerTag': function (newVal) {
      if (this.initShowCount) {
        if (this.joBOrTaskTag === 'JOB运行概览') {
          this.getDispatchJobTotal(this.$formatDate.dateFormat(this.dataTime[0]), this.$formatDate.dateFormat(this.dataTime[1]), newVal)
        } else {
          this.getDispatchTaskTotal(this.$formatDate.dateFormat(this.dataTime[0]), this.$formatDate.dateFormat(this.dataTime[1]), newVal)
        }
      }
    }
  },
  created () {
    this.getPandectdata()
  },
  mounted () {
    this.getDispatchData()
    this.handleClickDispatchTotal()
  },
  methods: {
    handleClickDispatchTotal: function () {
      let start = new Date(this.$formatDate.getDay(0) + ' 00:00:00').toString()
      let end = new Date(this.$formatDate.getDay(0) + ' 23:59:59').toString()
      this.dataTime = [start, end]
      this.dataNumberTime = [start, end]
      this.getDispatchJobTotal(start, end, '全部')
      this.getDispatchNumber(start, end, 'JOB调度次数')
      this.getSchedulerData()
    },
    showMsgClick: function (val) {
      let start = new Date(this.$formatDate.getDay(0) + ' 00:00:00').toString()
      let end = new Date(this.$formatDate.getDay(0) + ' 23:59:59').toString()
      this.dataTime = [start, end]
      this.dataNumberTime = [start, end]
      switch (val) {
        case 1:
          this.getSchedulerData()
          this.initShowMsg = true // 初始化显示调度器信息
          this.initShowCount = false // 初始化显示调度次数
          this.initShowStatus = false // 初始化显示调度状态
          break
        case 2:
          this.getDispatchNumber(start, end, 'JOB调度次数')
          this.getDispatchJobTotal(start, end, '全部')
          this.initShowMsg = false // 初始化显示调度器信息
          this.initShowCount = true // 初始化显示调度次数
          this.initShowStatus = false // 初始化显示调度状态
          break
        case 3:
          this.initShowMsg = false // 初始化显示调度器信息
          this.initShowCount = false // 初始化显示调度次数
          this.initShowStatus = true // 初始化显示调度状态
          break
      }
    },
    getSchedulerData: function () {
      let self = this
      self.$http.get(self.$api.getApiAddress('/monitor/schedulerInfo', 'CESHI_API_HOST')).then((res) => {
        if (res.data.code === 0) {
          self.schedulerList = Object.keys(res.data.data)
          if (JSON.stringify(self.schedulerList).indexOf('全部') === -1) {
            self.schedulerList.unshift('全部')
          }
        } else {
          self.$message({message: res.data.message, type: 'error'})
        }
      }).catch(() => {
        self.$message({message: '服务器未响应！', type: 'error'})
      })
    },
    getDispatchJobTotal: function (start, end, scheduler) {
      let self = this
      self.$http.get(self.$api.getApiAddress('/monitor/jobstatistics', 'CESHI_API_HOST'), {
        startTime: start,
        endTime: end,
        scheduler: scheduler === '全部' ? '' : scheduler
      }).then((res) => {
        if (res.data.code === 0) {
          self.dispatchTotal(res.data.data)
        } else {
          self.$message({message: res.data.message, type: 'error'})
        }
      }).catch(() => {
        self.$message({message: '服务器未响应！', type: 'error'})
      })
    },
    getDispatchTaskTotal: function (start, end, scheduler) {
      let self = this
      self.$http.get(self.$api.getApiAddress('/monitor/taskstatistics', 'CESHI_API_HOST'), {
        startTime: start,
        endTime: end,
        scheduler: scheduler === '全部' ? '' : scheduler
      }).then((res) => {
        if (res.data.code === 0) {
          self.dispatchTotal(res.data.data)
        } else {
          self.$message({message: res.data.message, type: 'error'})
        }
      }).catch(() => {
        self.$message({message: '服务器未响应！', type: 'error'})
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
    getDispatchNumber: function (start, end, type) {
      let self = this
      self.$http.get(self.$api.getApiAddress('/monitor/jobcallstatistics', 'CESHI_API_HOST'), {
        scheduler: '',
        startTime: start,
        endTime: end
      }).then((res) => {
        if (res.data.code === 0) {
          self.dispatchNumber(res.data.data, type)
        } else {
          self.$message({message: res.data.message, type: 'error'})
        }
      })
    },
    dispatchTotal: function (params) {
      this.showDispatchInfo = false
      this.eachartsWidth = document.getElementsByClassName('show-run-status')[0].clientWidth
      let startTime = ''
      let timeList = []
      let successList = []
      let readyList = []
      let errorList = []
      if (JSON.stringify(params) !== '[]') {
        this.showProjectNoData = false
        startTime = params[0].times
        if (this.joBOrTaskTag === 'JOB运行概览') {
          params.forEach(function (val) {
            timeList.push(val.times)
            successList.push(val.successJobTotal)
            readyList.push(val.startJobTotal)
            errorList.push(val.failJobTotal)
          })
        } else {
          params.forEach(function (val) {
            timeList.push(val.times)
            successList.push(val['successTaskTotal'])
            readyList.push(val['startTaskTotal'])
            errorList.push(val['failTaskTotal'])
          })
        }
      } else {
        this.showProjectNoData = true
        return
      }
      let myChartT = this.$echarts.init(document.getElementById('show-list-total'))
      let option = {
        dataZoom: [{
          startValue: startTime,
          // backgroundColor: 'red', // 组件的背景颜色
          left: '5%',
          right: '5%',
          bottom: '30px'
        }, {
          type: 'inside'
        }],
        grid: [{bottom: '95px', right: '35px', top: '10px', left: '45px'}],
        legend: {
          data: ['异常', '已完成', '运行中'],
          bottom: '1px',
          icon: 'rect',
          itemWidth: 12, // 设置宽度
          itemHeight: 10 // 设置高度
        },
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
        xAxis: [{
          data: timeList,
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
          }
        }],
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
            name: '异常',
            type: 'line',
            data: errorList,
            smooth: true,
            showSymbol: false,
            itemStyle: { // 图形的形状
              color: '#ED6E7E'
            }
          },
          {
            name: '已完成',
            type: 'line',
            data: successList,
            smooth: true,
            showSymbol: false,
            itemStyle: { // 图形的形状
              color: '#1890FF'
            }
          }
        ]
      }
      myChartT.setOption(option, true)
    },
    // 随机颜色
    randomColor: function () {
      let r = Math.floor(Math.random() * 255)
      let g = Math.floor(Math.random() * 255)
      let b = Math.floor(Math.random() * 255)
      return 'rgb(' + r + ',' + g + ',' + b + ')'
    },
    dispatchNumber: function (params, type) {
      let colorList = ['#4A90E2', '#B8E986', '#7C6AF2', '#E8A010', '#60BECA']
      if (JSON.stringify(params) === '{}' || JSON.stringify(params) === '[]') {
        this.showNumberNoData = true
        return false
      }
      this.showNumberNoData = false
      let seriesData = []
      let legendData = []
      let timeData = []
      let data = []
      params.forEach((ele, index) => {
        data = []
        if (index > colorList.length && this.colorList.indexOf(this.randomColor()) === -1) {
          this.colorList.push(this.randomColor())
        }
        ele.info.forEach((elechild) => {
          if (type === 'JOB调度次数') {
            data.push(elechild.job_call_count)
          } else {
            data.push(elechild.task_call_count)
          }
          if (timeData.indexOf(elechild.times) === -1) {
            timeData.push(elechild.times)
          }
        })
        legendData.push(ele.scheduler)
        seriesData.push({
          name: ele.scheduler,
          type: 'line',
          showAllSymbol: true,
          data: JSON.stringify(data) === '[]' ? [] : data,
          smooth: true,
          showSymbol: false,
          itemStyle: { // 图形的形状
            color: colorList[index]
          }
        })
      })
      this.eachartsWidth = document.getElementsByClassName('show-run-status')[0].clientWidth
      this.showDispatchInfo = false
      let myChartN = this.$echarts.init(document.getElementById('show-list-number'))
      myChartN.clear()
      let option = {
        dataZoom: [{
          startValue: '2018-07-03 10:00:00',
          // backgroundColor: 'red', // 组件的背景颜色
          left: '5%',
          right: '5%',
          bottom: '30px'
        }, {
          type: 'inside'
        }],
        grid: [{bottom: '95px', right: '35px', top: '10px', left: '45px'}],
        legend: {
          data: legendData,
          bottom: '1px',
          icon: 'rect',
          itemWidth: 12, // 设置宽度
          itemHeight: 10 // 设置高度
        },
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
        toolbox: {
          show: true
        },
        xAxis: {
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
          data: timeData
        },
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
        series: seriesData
      }
      myChartN.setOption(option)
    },
    dispatchInfo: function (params) {
      this.eachartsWidth = document.getElementsByClassName('show-run-status')[0].clientWidth
      let parmasList = Object.keys(params)
      let seriesData1 = []
      let seriesData2 = []
      let seriesData3 = []
      if (parmasList.length !== 0) {
        this.showNoData = false
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
        this.showNoData = true
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
        xAxis: [
          {
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
              color: '#D7F59A'
            },
            data: seriesData1
          },
          {
            name: 'JOB运行数量',
            type: 'bar',
            barWidth: 13,
            tiled: 'JOB信息',
            itemStyle: { // 图形的形状
              color: '#5FD7D0'
            },
            data: seriesData2
          },
          {
            name: 'JOB预警值',
            type: 'bar',
            barWidth: 13,
            tiled: 'JOB信息',
            itemStyle: { // 图形的形状
              color: '#3878CB'
            },
            data: seriesData3
          }
        ]
      }
      myChart.setOption(option)
    },
    getPandectdata: function () {
      let self = this
      self.$http.get(self.$api.getApiAddress('/monitor/schedulers', 'CESHI_API_HOST')).then((res) => {
        if (res.data.code === 0) {
          self.dispatchInfoData = res.data.data.length
        } else {
          self.$message({message: res.data.message, type: 'error'})
        }
      }).catch(() => {
        self.$message({message: '服务器未响应！', type: 'error'})
      })
      self.$http.get(self.$api.getApiAddress('/monitor/actuators', 'CESHI_API_HOST')).then((res) => {
        if (res.data.code === 0) {
          self.diapatchTotal = res.data.data
        } else {
          self.$message({message: res.data.message, type: 'error'})
        }
      }).catch(() => {
        self.$message({message: '服务器未响应！', type: 'error'})
      })
      self.$http.get(self.$api.getApiAddress('/monitor/jobcallcount', 'CESHI_API_HOST')).then((res) => {
        if (res.data.code === 0) {
          self.diapatchNumber = res.data.data.jobCallCount
        } else {
          self.$message({message: res.data.message, type: 'error'})
        }
      }).catch(() => {
        self.$message({message: '服务器未响应！', type: 'error'})
      })
      // 对接项目总数详情接口
      self.$http.get(self.$api.getApiAddress('/monitor/jobGroupDetails', 'CESHI_API_HOST')).then((res) => {
        if (res.data.code === 0) {
          self.projectCountData = res.data.data
        } else {
          self.$message({message: res.data.message, type: 'error'})
        }
      }).catch(() => {
        self.$message({message: '服务器未响应！', type: 'error'})
      })
    }
  }
}
</script>
<style lang='less' scoped>
@import '../styles/home.page.less';
</style>
<style lang='less'>
@import '../styles/home.page.reset.less';
</style>

