<template>
  <div class="mask-add-task-config" id="mask">
      <div class="mask-content" id="mask-content">
        <div class="mask-main-title">
          <span>{{addTaskTagValue.valTaget.taskKey}} 参数配置</span>
          <i class="close-icon" @click="showHiddenAddJob(1)"></i>
        </div>
        <div class="info scroll-bar">
          <el-form :model="taskCreateViewModel" :rules="jobAddViewModelRules" ref="taskAddConfigViewForm" label-width="165px" class="taskAddConfigViewForm" auto-complete="off">
              <el-form-item label="Task_参数类型" v-if="showParamsCount" prop="taskParamsType">
                <el-select v-model="taskCreateViewModel.taskParamsType" placeholder="Task_参数类型">
                    <el-option v-for="(item,index) in taskParamsTypeList" :key="index" :label="item" :value="item"></el-option>
                </el-select>
                <el-tooltip placement="top">
                  <div slot="content">
                    选择Task参数类型 </br>
                    FROM_UI（来自前端，需要输入Task能解析的JSON串）； </br>
                    FROM_TASK（来自任务，需要配置参数来源的任务）
                  </div>
                  <span class="el-icon-question info-icon"></span>
                </el-tooltip>
              </el-form-item>

              <el-form-item label="Task_参数值" v-if="showParamsCount">
                <el-input v-if="taskCreateViewModel.taskParamsType !== 'FROM_TASK'" type="text" auto-complete="off" placeholder="请填写Task参数" v-model="taskCreateViewModel.taskParamsValue"></el-input>
                <el-select v-if="taskCreateViewModel.taskParamsType === 'FROM_TASK'" v-model="taskCreateViewModel.taskParamsValue" placeholder="请选择Task参数">
                    <el-option v-for="(item,index) in preTaskKeyArr" :key="index" :label="item" :value="item"></el-option>
                </el-select>
                <el-tooltip class="item" effect="dark" content="Task参数类型为来自前端，输入Task能解析的JSON串（最多255字符）" placement="top">
                  <span class="el-icon-question info-icon"></span>
                </el-tooltip>
              </el-form-item>

              <el-form-item label="过期时间(s)" prop="readTimeout">
                <el-input-number v-model="taskCreateViewModel.readTimeout" controls-position="right" :min="1" uto-complete="off" placeholder="请输入过期时间"></el-input-number>
                <el-tooltip class="item" effect="dark" content="Job任务的过期时间" placement="top">
                  <span class="el-icon-question info-icon"></span>
                </el-tooltip>
              </el-form-item>

              <el-form-item label="Task_选取实例策略" prop="taskSelectCase" required>
                <el-select v-model="taskCreateViewModel.taskSelectCase" placeholder="Task_选取实例策略">
                    <el-option v-for="(item,index) in taskSelectCaseList" :key="index" :label="item" :value="item"></el-option>
                </el-select>
                <el-tooltip placement="top">
                  <div slot="content">
                    选取实例策略： </br>
                    随机（从可选的列表中，随机选择实例，即IP+端口）； </br>
                    固定IP（指定实例，随后需要从可选列表中人工指定实例）
                  </div>
                  <span class="el-icon-question info-icon"></span>
                </el-tooltip>
              </el-form-item>

              <el-form-item label="选取实例" prop="ipSelectTag" required v-if="taskCreateViewModel.taskSelectCase === '固定IP'">
                <el-select v-model="taskCreateViewModel.ipSelectTag" placeholder="选取实例">
                    <el-option v-for="(item,index) in ipDataList" :key="index" :label="item" :value="item"></el-option>
                </el-select>
                <el-tooltip placement="top">
                  <div slot="content">选择相应的实例</div>
                  <span class="el-icon-question info-icon"></span>
                </el-tooltip>
              </el-form-item>

              <el-form-item label="Task_调用失败策略" prop="tasktransferFail" required>
                <el-select v-model="taskCreateViewModel.tasktransferFail" placeholder="请选择Task_调用失败策略">
                    <el-option v-for="(item,index) in tasktransferFailList" :key="index" :label="item" :value="item"></el-option>
                </el-select>
                <el-tooltip placement="top">
                  <div slot="content">
                    调用失败策略: </br>
                    STOP（停止策略，调用失败则整个Job停止，不再执行后续Task）； </br>
                    IGNORE（忽略策略，调用失败则跳过该Task，继续执行后续Task）； </br>
                    TRANSFER（转移策略，选取该Task的其他实例执行，如果依然失败，则使用停止策略）； </br>
                    MULTI_CALLS_TRANSFER（多次调用再转移策略，重复调用该Task多次，如果依然失败，则使用转移策略）
                  </div>
                  <span class="el-icon-question info-icon"></span>
                </el-tooltip>
              </el-form-item>
            </el-form>
          <div class="alert-button">
            <el-button @click="showHiddenAddJob(1)">取消</el-button>
            <el-button @click="showHiddenAddJob(2)">添加</el-button>
          </div>
        </div>
      </div>
  </div>
</template>

<script>
import moment from 'moment'
export default {
  name: 'AddJobTmpl',
  props: ['addTaskTagValue'],
  data () {
    var checkBlur = (rule, value, callback) => {
      switch (rule.field) {
        case 'taskParamsValue':
          if (value.replace(/(^\s*)|(\s*$)/g, '') !== '' && !new RegExp(/^[^]{1,255}$/).test(value)) {
            return callback(new Error('输入内容必须是1到255个字符!'))
          }
          break
        case 'readTimeout':
          if (value !== '' && !new RegExp(/^[1-9]{1}[0-9]{0,}$/).test(value)) {
            return callback(new Error('输入内容必须是大于1的数字!'))
          }
          break
      }
      callback()
    }
    return {
      showParamsCount: true, // 是否显示参数配置
      ipDataList: [],   // ip地址list
      taskParamsTypeList: ['FROM_UI', 'FROM_TASK'],
      taskSelectCaseList: ['随机', '固定IP'], // , '分片'
      tasktransferFailList: ['STOP', 'IGNORE', 'MULTI_CALLS_TRANSFER', 'TRANSFER'],
      taskParamsValue: '',
      taskCreateViewModel: {
        taskParamsType: '',  // Task_参数类型
        taskParamsValue: '',  // Task_参数值
        readTimeout: '',  // 过期时间
        taskSelectCase: '', // 实例策略
        ipSelectTag: '', // 选取实例
        tasktransferFail: ''  // 失败策略
      },
      jobAddViewModelRules: {
        taskParamsType: [this.$validator.required('请输入Task_参数类型')],
        taskParamsValue: [this.$validator.required('请输入Task_参数值'), { validator: checkBlur, trigger: 'blur' }],
        readTimeout: [{ validator: checkBlur, trigger: 'blur' }],
        taskSelectCase: [this.$validator.required('请选择Task_选取实例策略')],
        tasktransferFail: [this.$validator.required('请选择Task_调用失败策略')],
        ipSelectTag: [this.$validator.required('请选择实例信息')]
      },
      preTaskKeyArr: [] // 前置task
    }
  },
  created () {
    this.getIpPortList()
    this.getPreTaskKey()
    this.showParamsCount = this.addTaskTagValue.taskListTag[this.addTaskTagValue.valTaget.taskKey]['paramCount']
  },
  methods: {
    getPreTaskKey: function () {
      let self = this
      let arr = []
      this.addTaskTagValue.valTaget.preTaskKey.forEach((ele) => {
        arr.push(this.addTaskTagValue.taskIdObj[ele])
      })
      this.preTaskKeyArr = arr
      self.taskCreateViewModel.taskKey = self.addTaskTagValue.valTaget.taskKey
      self.taskCreateViewModel.taskParamsType = self.addTaskTagValue.valTaget.inputType || ''
      self.taskCreateViewModel.readTimeout = self.addTaskTagValue.valTaget.readTimeout || ''
      self.taskCreateViewModel.taskParamsValue = self.addTaskTagValue.valTaget.inputValue || ''
      self.taskCreateViewModel.taskSelectCase = self.addTaskTagValue.valTaget.routeStrategy !== undefined ? (self.addTaskTagValue.valTaget.routeStrategy === 'ROUTE_TYPE_RANDOM' ? '随机' : (self.addTaskTagValue.valTaget.routeStrategy === 'ROUTE_TYPE_SHARDING' ? '分片' : '固定IP')) : ''
      self.taskCreateViewModel.ipSelectTag = self.addTaskTagValue.valTaget.fixIp || ''
      self.taskCreateViewModel.tasktransferFail = self.addTaskTagValue.valTaget.failover || ''
      self.taskCreateViewModel.preTaskKey = self.addTaskTagValue.valTaget.preTaskKey || ''
      self.taskCreateViewModel.taskId = self.addTaskTagValue.valTaget.taskId
    },
    getIpPortList: function () {
      let self = this
      let params = {
        taskKey: self.addTaskTagValue.valTaget.taskKey,
        taskSource: this.addTaskTagValue.taskListTag[this.addTaskTagValue.valTaget.taskKey]['taskSource'] || '',
        taskGroupName: this.addTaskTagValue.taskListTag[this.addTaskTagValue.valTaget.taskKey]['taskGroupName'] || '',
        taskAppName: this.addTaskTagValue.taskListTag[this.addTaskTagValue.valTaget.taskKey]['taskAppName'] || '',
        ipSelectTag: self.addTaskTagValue.valTaget.fixIp || '',
        taskId: this.addTaskTagValue.taskListTag[this.addTaskTagValue.valTaget.taskKey]['taskId'] || ''
      }
      self.$http.post(self.$api.getApiAddress('/taskapi/getExecutorList', 'CESHI_API_HOST'), params).then((res) => {
        if (res.data.code === 0) {
          this.ipDataList = (res.data.data !== '' && res.data.data !== null) ? res.data.data.split(',') : []
        } else {
          self.$message({message: res.data.message, type: 'error'})
        }
      }).catch(() => {
        self.$message({message: 'IpPort获取失败!', type: 'error'})
      })
    },
    changeInputValue: function (val) {
    },
    showHiddenAddJob: function (type) {
      if (type === 1) {
        this.$emit('showHiddenAddtask', type, {}, false)
      } else {
        let params = this.taskCreateViewModel
        this.$refs.taskAddConfigViewForm.validate(valid => {
          if (valid) {
            this.$emit('showHiddenAddtask', type, params, false)
          }
        })
      }
    },
    showHiddenSave: function () {
      // job_group
      // job_key
      // task_key
      // task_参数类型
      // task_参数值
      // 过期时间
      // task——选取实例策略
      // task_调用失败策略
      // 选取实例
    }
  }
}
</script>
<style lang="less">
@import '../styles/common/task-add-config.tmpl.less';
</style>
