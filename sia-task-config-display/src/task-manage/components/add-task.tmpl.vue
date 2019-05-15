<template>
  <div class="mask-add-task-manage" id="mask">
      <div class="mask-content" id="mask-content">
        <div class="mask-main-title">
          <span>添加 Task</span>
          <i class="close-icon" @click="showHiddenAddTask"></i>
        </div>
        <div class="info">
          <el-form :model="taskAddViewModel" :rules="taskAddViewModelRules" ref="taskManageViewForm" label-width="120px" class="taskManageViewForm" auto-complete="off">
            <el-form-item label="项目组名称" prop="projectName">
              <!--<el-input type="text" auto-complete="off" v-model="taskAddViewModel.projectName"></el-input>-->
              <el-select placeholder="项目组名称" v-model="taskAddViewModel.projectName" :allow-create="isEnterText" :filterable="isEnterText">
                <el-option v-for="(item,index) in projectDataList" :key="index" :label="item" :value="item"></el-option>
              </el-select>
              <el-tooltip class="item" effect="dark" content="选择Task归属的项目组，可选列表中一般包含多个项目组" placement="right">
                <span class="el-icon-question info-icon"></span>
              </el-tooltip>
            </el-form-item>
            <el-form-item label="应用名称" prop="applyName">
              <!--<el-input type="text" auto-complete="off" placeholder="" v-model="taskAddViewModel.applyName"></el-input>-->
              <span class="pre-job-key-text">{{taskAddViewModel.projectName}}</span><span class="center-line">－</span>
              <el-select class="pre-task-key" placeholder="应用名称" v-model="taskAddViewModel.applyName" :allow-create="isEnterText" :filterable="isEnterText">
                <el-option v-for="(item,index) in applyDataList" :key="index" :label="item" :value="item"></el-option>
              </el-select>
              <el-tooltip class="item" effect="dark" content="选择Task归属的应用，可选列表中一般包含多个应用" placement="right">
                <span class="el-icon-question info-icon"></span>
              </el-tooltip>
            </el-form-item>
            <el-form-item label="HTTP_PATH" prop="httpPath">
              <el-input type="text" auto-complete="off" placeholder="" v-model="taskAddViewModel.httpPath"></el-input>
              <el-tooltip class="item" effect="dark" content="添加Task的HTTP接口请求路径，示例：/example" placement="right">
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
              <el-input type="textarea" auto-complete="off" placeholder="描述" v-model="taskAddViewModel.taskdesc"></el-input>
              <el-tooltip class="item" effect="dark" content="描述Task的功能" placement="right">
                <span class="el-icon-question info-icon"></span>
              </el-tooltip>
            </el-form-item>
            <el-form-item label="ip：port" class="add-ip-post">
              <el-input :class="{'active':showIpPortError}" type="text" auto-complete="off" placeholder="" v-model="addIpPostVal"></el-input>
              <el-tooltip class="item" effect="dark" content="添加Task所在应用的IP地址与端口" placement="right">
                <span class="el-icon-question info-icon"></span>
              </el-tooltip>
              <el-button @click="showHiddenAddIpPost" class="btn-small" :icon="loadingRefresh?'el-icon-refresh':''" :loading="loadingRefresh">添加</el-button>
              <div class="error-msg" v-show="showIpPortError">{{ipPortErrorText}}</div>
            </el-form-item>
          </el-form>
          <div class="ip-post">
            <span v-for="(item, index) in taskAddViewModel.ipPost" :key="index">{{item}}<i @click="handleClickDeleteIp(item)">X</i></span>
          </div>
          <div class="alert-button">
            <el-button @click="showHiddenAddTask">取消</el-button>
            <el-button @click="showHiddenSave">添加</el-button>
          </div>
        </div>
      </div>
  </div>
</template>

<script>
export default {
  name: 'AddTaskTmpl',
  props: ['addParamsSearch'],
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
      showApplyNamePre: false,
      isEnterText: true,
      taskAddViewModel: {
        projectName: '',
        applyName: '',
        httpPath: '',
        isConfigParams: '',
        taskdesc: '',
        ipPost: []
      },
      projectDataList: [],
      applyDataList: [],
      viewSearchDateList: {
        'gantry': {
          'gantry': []
        }
      },
      addIpPostVal: '',
      showIpPortError: false,
      ipPortErrorText: '',
      taskAddViewModelRules: {
        projectName: [this.$validator.required('请输入项目名称')],
        applyName: [this.$validator.required('请输入应用名称')],
        isConfigParams: [this.$validator.required('请选择是否配置参数')],
        taskdesc: [this.$validator.required('请输入描述内容'), { validator: checkBlur, trigger: 'blur' }],
        httpPath: [this.$validator.required('请输入HTTP_PATH'), { validator: checkBlur, trigger: 'blur' }]
      }
    }
  },
  watch: {
    'taskAddViewModel.projectName': function (newVal, oldVal) {
      let self = this
      self.getSearchApplyList()
    },
    'taskAddViewModel.applyName': function (newVal, oldVal) {
      this.showApplyNamePre = false
      if (newVal === undefined || newVal === '') {
        return false
      }
      // if (newVal.indexOf(this.taskAddViewModel.projectName) === -1) {
      //   this.showApplyNamePre = true
      // }
    }
  },
  created () {
    this.getSearchList()
  },
  methods: {
    getSearchList: function () {
      let self = this
      let roleList = sessionStorage.getItem('selectAuth')
      if (roleList !== null) {
        if (JSON.stringify(roleList).indexOf('admin') !== -1) {
          self.isEnterText = true
        } else {
          self.isEnterText = false
        }
      }
      self.$http.get(self.$api.getApiAddress('/taskapi/selectAuth', 'CESHI_API_HOST')).then((res) => {
        self.projectDataList = res.data.data
        self.taskAddViewModel.projectName = self.projectDataList[0]
      }).catch(() => {
        self.$message({message: '服务未响应！', type: 'error'})
      })
    },
    getSearchApplyList: function () {
      let self = this
      self.applyDataList = []
      self.$http.get(self.$api.getApiAddress('/taskapi/selectappsbygroup', 'CESHI_API_HOST'), {
        'groupName': self.taskAddViewModel.projectName
      }).then((res) => {
        if (res.data.code !== 0) {
          self.$message({message: res.data.message, type: 'error'})
        } else {
          res.data.data.forEach((ele) => {
            self.applyDataList.push(ele.split('-')[1])
          })
          self.taskAddViewModel.applyName = self.applyDataList[0]
        }
      }).catch(() => {
        self.$message({message: '服务未响应！', type: 'error'})
      })
    },
    handleClickDeleteIp: function (val) {
      let self = this
      let index = self.taskAddViewModel.ipPost.indexOf(val)
      if (index !== -1) {
        self.taskAddViewModel.ipPost.splice(index, index + 1)
      }
    },
    showHiddenAddIpPost: function () {
      let self = this
      this.loadingRefresh = true
      self.$http.get(self.$api.getApiAddress('/taskapi/checkPingTelnet', 'CESHI_API_HOST'), {
        host: self.addIpPostVal
      }).then((res) => {
        switch (res.data.code) {
          case 0:
            if (self.taskAddViewModel.ipPost.indexOf(self.addIpPostVal) === -1 && self.addIpPostVal !== '') {
              self.$refs.taskManageViewForm.validate('ipPost')
              self.taskAddViewModel.ipPost.push(self.addIpPostVal)
              this.showIpPortError = false
              self.addIpPostVal = ''
              this.ipPortErrorText = ''
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
            // self.$message({message: '添加失败，输入ip：port不可用', type: 'error'})
            // self.$message({message: '连通性失败', type: 'error'})
            break
        }
        this.loadingRefresh = false
      }).catch(() => {
        this.loadingRefresh = false
        self.$message({message: '服务未响应！', type: 'error'})
      })
    },
    showHiddenAddTask: function () {
      this.$emit('showHiddenAddtask', false, this.addParamsSearch)
    },
    showHiddenSave: function () {
      let self = this
      let modalObj = {
        taskKey: self.taskAddViewModel.applyName + self.taskAddViewModel.httpPath,
        taskGroupName: self.taskAddViewModel.projectName,
        taskAppName: this.taskAddViewModel.projectName + '-' + self.taskAddViewModel.applyName,
        taskAppHttpPath: self.taskAddViewModel.httpPath,
        taskAppIpPort: self.taskAddViewModel.ipPost.join(','),
        taskDesc: self.taskAddViewModel.taskdesc,
        paramCount: self.taskAddViewModel.isConfigParams
      }
      let positionTask = {
        searchProjectName: self.taskAddViewModel.projectName,
        searchApplyName: self.taskAddViewModel.applyName,
        searchTaskName: ''
      }
      if (self.taskAddViewModel.ipPost.length === 0) {
        // self.$message({message: '请添加ip：port', type: 'error'})
        this.$refs.taskManageViewForm.validate()
        this.showIpPortError = true
        this.ipPortErrorText = 'ip：port不能为空'
        return false
      }
      this.$refs.taskManageViewForm.validate(valid => {
        if (valid) {
          self.$http.post(
            self.$api.getApiAddress('/taskapi/insertTask', 'CESHI_API_HOST'), modalObj)
            .then((res) => {
              switch (res.data.code) {
                case 0:
                  self.$message({message: '创建成功', type: 'success'})
                  self.$emit('showHiddenAddtask', false, positionTask)
                  break
                default:
                  self.$message({message: res.data.message, type: 'error'})
                  // self.$emit('showHiddenAddtask', false, positionTask)
              }
            })
            .catch(() => {
              self.$message({message: '创建失败', type: 'error'})
              // self.$emit('showHiddenAddtask', false, self.addParamsSearch)
            })
        }
      })
    }
  }
}
</script>
<style lang="less">
@import '../styles/common/add-task.tmpl.less';
</style>
