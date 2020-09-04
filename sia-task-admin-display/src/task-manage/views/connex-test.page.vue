<template>
    <div class="dispatch-system-default connex-test-page">
      <div class="section-container">
        <div class="section-header">
          <span>{{taskInfo.taskKey}} ~ 连通性测试</span>
          <el-button class="btn-large refresh-btn btn-ml-auto" icon="el-icon-refresh" :loading="loadingRefresh" @click="showHiddenRefreshTaskList">{{loadingRefresh?'加载中':'刷新'}}</el-button>
          <el-button class="btn-large edit-btn" @click="handleClickBack"> 返回 </el-button>
        </div>
        <div class="search-box">
          <el-form :model="connectTestViewModel" :rules="connectTestModelRules" ref="connectTestViewForm" label-width="80px" class="connectTestViewForm" auto-complete="off">
            <el-form-item label="测试地址" prop="requestAddress">
              <!--<el-input type="text" auto-complete="off" placeholder="请输入测试地址" v-model="connectTestViewModel.requestAddress"></el-input>-->
              <el-select v-if="taskInfo.taskAppIpPort !== null && taskInfo.taskAppIpPort !== ''" v-model="connectTestViewModel.requestAddress" placeholder="请输入测试地址" allow-create filterable>
                <el-option v-for="(item,index) in taskInfo.taskAppIpPort.split(',')" :key="index" :label="'http://'+item + taskInfo.taskKey.split(':')[1]" :value="'http://'+ item + taskInfo.taskKey.split(':')[1]"></el-option>
              </el-select>
              <el-input v-if="taskInfo.taskAppIpPort === null || taskInfo.taskAppIpPort === ''" type="text" auto-complete="off" placeholder="请输入测试地址" v-model="connectTestViewModel.requestAddress"></el-input>
            </el-form-item>
            <el-form-item label="测试参数">
              <textarea type="text" auto-complete="off" placeholder="请输入测试参数"  v-model="connectTestViewModel.requestParams"></textarea>
            </el-form-item>
          </el-form>
          <div class="alert-button">
            <el-button class="blue-button" @click="handleClickCancel">取消</el-button>
            <el-button class="blue-button" @click="handleClickConnexTest">测试</el-button>
          </div>
        </div>
        <div class="respones-info">
          <div class="title"><i><img src="../images/home-title-icon.png" alt=""></i>response</div>
          <textarea name="" :disabled="true" class="respones-info-text" cols="30" rows="10" v-model="responseInfoFormatJson"></textarea>
        </div>
      </div>
    </div>
</template>

<script>
export default {
  name: 'ConnextestPage',
  data () {
    return {
      connectTestViewModel: {
        requestAddress: '',
        requestParams: ''
      },
      responseInfoFormatJson: '',
      connectTestModelRules: {
        requestAddress: [this.$validator.required('请输入测试地址')]
      },
      taskInfo: {},
      loadingRefresh: false // 刷新按钮动画
    }
  },
  created () {
    this.taskInfo = this.$store.state.TaskManage.taskMsg
  },
  methods: {
    handleClickBack: function () {
      this.$router.push({path: '/task-manage-list'})
    },
    // 刷新按钮事件
    showHiddenRefreshTaskList: function () {
      let self = this
      self.loadingRefresh = true
      this.handleClickCancel()
      setTimeout(function () {
        self.loadingRefresh = false
      }, 2000)
    },
    handleClickCancel: function () {
      this.connectTestViewModel = {
        requestAddress: '',
        requestParams: ''
      }
      this.responseInfoFormatJson = ''
      this.$refs['connectTestViewForm'].resetFields()
    },
    handleClickConnexTest: function () {
      let self = this
      let paramsObj = {
        'url': self.connectTestViewModel.requestAddress,
        'param': self.connectTestViewModel.requestParams
      }
      this.$refs['connectTestViewForm'].validate((valid) => {
        if (valid) {
          self.$http.post(self.$api.getApiAddress('/taskapi/connextest', 'CESHI_API_HOST'), paramsObj).then(res => {
            if (res.data.code === 0) {
              let data = Object.keys(res.data.data).length === 0 ? '' : res.data.data
              this.formatJson(data)
            } else {
              self.$message({ message: res.data.message, type: 'error' })
            }
          }).catch((err) => {
            self.$message({ message: this.$helper.handleLoginErrorMsg(err), type: 'error' })
          })
        }
      })
    },
    formatJson: function (params) {
      this.responseInfoFormatJson = ''
      let k = 0
      let j = 0
      let ii = null
      let ele = null
      for (let i = 0; i < params.length; i++) {
        ele = params.charAt(i)
        if (j % 2 === 0 && ele === '}') {
          k--
          for (ii = 0; ii < k; ii++) {
            ele = '    ' + ele
          }
          ele = '\n' + ele
        } else if (j % 2 === 0 && ele === '{') {
          ele += '\n'
          k++
          for (ii = 0; ii < k; ii++) {
            ele += '    '
          }
        } else if (j % 2 === 0 && ele === ',') {
          ele += '\n'
          for (ii = 0; ii < k; ii++) {
            ele += '    '
          }
        } else if (ele === '"') {
          j++
        }
        this.responseInfoFormatJson += ele
      }
    }
  }
}
</script>
<style lang="less" scoped>
@import "../styles/connex-test.page.less";
</style>
<style lang="less">
@import "../styles/connex-test.page.reset.less";
</style>
