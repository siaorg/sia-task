<template>
  <div class="mask-edit-task-manage" id="mask">
      <div class="mask-content" id="mask-content">
        <div class="mask-main-title">
          <span>修改 Task</span>
          <i class="close-icon" @click="showHiddenEdittask"></i>
        </div>
        <div class="info">
          <el-form :model="taskAddViewModel" :rules="taskEditViewModelRules" ref="taskManageViewForm"label-width="120px" class="taskManageViewForm" auto-complete="off">
            <el-form-item label="项目组名称" prop="projectName">
              <el-input :disabled="true" type="text" auto-complete="off" v-model="taskAddViewModel.projectName"></el-input>
              <el-tooltip class="item" effect="dark" content="Task归属的项目组，可选列表中一般包含多个项目组" placement="right">
                <span class="el-icon-question info-icon"></span>
              </el-tooltip>
            </el-form-item>
            <el-form-item label="应用名称" prop="applyName">
              <el-input :disabled="true" type="text" auto-complete="off" placeholder="" v-model="taskAddViewModel.applyName"></el-input>
              <el-tooltip class="item" effect="dark" content="Task归属的应用，可选列表中一般包含多个应用" placement="right">
                <span class="el-icon-question info-icon"></span>
              </el-tooltip>
            </el-form-item>
            <el-form-item label="HTTP_PATH" prop="httpPath">
              <el-input :disabled="true" type="text" auto-complete="off" placeholder="请输入HTTP_PATH" v-model="taskAddViewModel.httpPath"></el-input>
              <el-tooltip class="item" effect="dark" content="Task的HTTP接口请求路径，示例：/example" placement="right">
                <span class="el-icon-question info-icon"></span>
              </el-tooltip>
            </el-form-item>
            <el-form-item label="是否配置参数" prop="isConfigParams"  class="radio-text">
              <el-radio-group v-model="taskAddViewModel.isConfigParams">
                  <el-radio :label="1">是</el-radio>
                  <el-radio :label="0">否</el-radio>
              </el-radio-group>
              <el-tooltip class="item" effect="dark" content="选择HTTP接口是否有入参" placement="right">
                <span class="el-icon-question info-icon"></span>
              </el-tooltip>
            </el-form-item>
            <el-form-item label="描述" prop="taskdesc">
              <el-input type="textarea" auto-complete="off" placeholder="" v-model="taskAddViewModel.taskdesc"></el-input>
              <el-tooltip class="item" effect="dark" content="描述Task的功能" placement="right">
                <span class="el-icon-question info-icon"></span>
              </el-tooltip>
            </el-form-item>
            <el-form-item label="ip：port" prop="ipPost" class="add-ip-post">
              <el-input :class="{'active':showIpPortError}" type="text" auto-complete="off" placeholder="" v-model="addIpPostVal"></el-input>
              <el-tooltip class="item" effect="dark" content="添加Task所在应用的IP地址与端口" placement="right">
                <span class="el-icon-question info-icon"></span>
              </el-tooltip>
              <div class="error-msg" v-show="showIpPortError">{{ipPortErrorText}}</div>
            </el-form-item>
            <el-button @click="showHiddenEditIpPost" class="btn-small" :loading="loadingRefresh">添加</el-button>
          </el-form>
          <div class="ip-post">
            <span v-for="(item, index) in taskAddViewModel.ipPost" :key="index">{{item}}<i class="el-icon-error" @click="handleClickDeleteIp(item)"></i></span>
          </div>
          <div class="alert-button">
            <el-button @click="showHiddenEdittask">取消</el-button>
            <el-button @click="showHiddenSave">修改</el-button>
          </div>
        </div>
      </div>
  </div>
</template>

<script>
export default {
  name: 'EditTaskTmpl',
  props: ['editParamsSearch', 'editParamsInfo'],
  data () {
    var checkBlur = (rule, value, callback) => {
      switch (rule.field) {
        case 'httpPath':
          if (!new RegExp(/^\/([(a-zA-Z)+(0-9_)?]+\/?)+$/i).test(value)) {
            return callback(new Error('HTTP_PATH格式必须以 " / "开头，其余字符可为英文、数字、下划线'))
          }
          break
        case 'taskdesc':
          if (value.replace(/(^\s*)|(\s*$)/g, '') !== '' && !new RegExp(/^[^]{2,1000}$/).test(value)) {
            return callback(new Error('输入内容必须是2到1000个字符!'))
          }
          break
      }
      callback()
    }
    return {
      loadingRefresh: false,
      addIpPostVal: '',
      taskAddViewModel: {
        projectName: '',
        applyName: '',
        httpPath: '',
        isConfigParams: '',
        taskdesc: '',
        ipPost: []
      },
      showIpPortError: false,
      ipPortErrorText: '',
      taskEditViewModelRules: {
        isConfigParams: [this.$validator.required('请选择是否配置参数')],
        taskdesc: [this.$validator.required('请输入描述内容'), { validator: checkBlur, trigger: 'blur' }],
        httpPath: [this.$validator.required('请输入HTTP_PATH'), { validator: checkBlur, trigger: 'blur' }]
      }
    }
  },
  created () {
    this.initData()
  },
  methods: {
    initData: function () {
      this.taskAddViewModel = {
        projectName: this.editParamsInfo.taskGroupName,
        applyName: this.editParamsInfo.taskAppName,
        httpPath: this.editParamsInfo.taskAppHttpPath,
        isConfigParams: this.editParamsInfo.paramCount,
        taskdesc: this.editParamsInfo.taskDesc,
        ipPost: (this.editParamsInfo.taskAppIpPort !== null && this.editParamsInfo.taskAppIpPort !== undefined) ? this.editParamsInfo.taskAppIpPort.split(',') : []
      }
    },
    handleClickDeleteIp: function (val) {
      let self = this
      let index = self.taskAddViewModel.ipPost.indexOf(val)
      if (index !== -1) {
        self.taskAddViewModel.ipPost.splice(index, index + 1)
      }
    },
    showHiddenEditIpPost: function () {
      let self = this
      this.loadingRefresh = true
      self.$http.get(self.$api.getApiAddress('/taskapi/checkPingTelnet', 'CESHI_API_HOST'), {
        host: self.addIpPostVal
      }).then((res) => {
        switch (res.data.code) {
          case 0:
            if (self.taskAddViewModel.ipPost.indexOf(self.addIpPostVal) === -1 && self.addIpPostVal !== '') {
              self.taskAddViewModel.ipPost.push(self.addIpPostVal)
              self.addIpPostVal = ''
              this.ipPortErrorText = ''
              this.showIpPortError = false
            } else if (self.addIpPostVal === '') {
              this.showIpPortError = true
              this.ipPortErrorText = 'ip：port不能为空'
              // self.$message({ message: 'ip：port不能为空', type: 'error' })
            } else {
              this.showIpPortError = true
              this.ipPortErrorText = 'ip：port已添加，请勿重复'
              // self.$message({ message: 'ip：port已添加，请勿重复', type: 'error' })
            }
            break
          default:
            this.showIpPortError = true
            this.ipPortErrorText = '连通性失败'
            break
        }
        this.loadingRefresh = false
      }).catch(() => {
        this.loadingRefresh = false
        self.$message({message: '服务未响应！', type: 'error'})
      })
    },
    showHiddenEdittask: function () {
      this.$emit('showHiddenEdittask', false, this.editParamsSearch)
    },
    showHiddenSave: function () {
      let self = this
      let modalObj = {
        taskId: this.editParamsInfo.taskId,
        taskKey: this.editParamsInfo.taskKey,
        taskGroupName: this.editParamsInfo.taskGroupName,
        taskAppName: this.editParamsInfo.taskAppName,
        taskAppHttpPath: this.taskAddViewModel.httpPath,
        taskAppIpPort: self.taskAddViewModel.ipPost.join(','),
        taskDesc: self.taskAddViewModel.taskdesc,
        paramCount: this.taskAddViewModel.isConfigParams
      }
      if (self.taskAddViewModel.ipPost.length === 0) {
        self.$message({message: '请添加ip：port', type: 'error'})
        return
      }
      let positionTask = {
        searchProjectName: this.editParamsInfo.taskGroupName,
        searchApplyName: this.editParamsInfo.taskAppName,
        searchTaskName: ''
      }
      this.$refs.taskManageViewForm.validate(valid => {
        if (valid) {
          self.$http.post(
            self.$api.getApiAddress('/taskapi/updateTask', 'CESHI_API_HOST'), modalObj)
            .then((res) => {
              switch (res.data.code) {
                case 0:
                  self.$message({message: '修改成功', type: 'success'})
                  self.$emit('showHiddenEdittask', false, positionTask)
                  break
                default:
                  // self.$message({message: '修改失败', type: 'error'})
                  self.$message({message: res.data.message, type: 'error'})
                  self.$emit('showHiddenEdittask', false, positionTask)
              }
            })
            .catch(() => {
              self.$message({message: '修改失败', type: 'error'})
              self.$emit('showHiddenEdittask', false, self.editParamsSearch)
            })
        }
      })
    }
  }
}
</script>
<style lang="less">
@import '../styles/common/edit-task.tmpl.less';
</style>
