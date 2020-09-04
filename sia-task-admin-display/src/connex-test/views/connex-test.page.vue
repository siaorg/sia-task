<template>
    <div class="dispatch-system-default connex-test-page">
      <div class="section-container">
        <div class="search-box">
          <el-form :model="connectTestViewModel" :rules="connectTestModelRules" ref="connectTestViewForm" label-width="80px" class="connectTestViewForm" auto-complete="off">
            <el-form-item label="测试地址" prop="requestAddress">
              <el-input type="text" auto-complete="off" placeholder="请输入测试地址" v-model="connectTestViewModel.requestAddress"></el-input>
            </el-form-item>
            <el-form-item label="测试参数" prop="requestParams">
              <el-input type="text" auto-complete="off" placeholder="请输入测试参数"  v-model="connectTestViewModel.requestParams"></el-input>
            </el-form-item>
          </el-form>
          <div class="alert-button">
            <el-button class="blue-button">取消</el-button>
            <el-button class="blue-button" @click="handleClickConnexTest">测试</el-button>
          </div>
        </div>
        <div class="respones-info">
          <div class="title"><i><img src="../images/home-title-icon.png" alt=""></i>respones</div>
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
        requestAddress: 'http://localhost:10086/success-noparam',
        requestParams: 'lll'
      },
      responseInfoFormatJson: '',
      connectTestModelRules: {
        requestAddress: [this.$validator.required('请输入测试地址')],
        requestParams: [this.$validator.required('请输入测试参数')]
      }
    }
  },
  created () {
  },
  methods: {
    handleClickConnexTest: function () {
      let self = this
      // if (self.connectTestViewModel.requestAddress === '') {
      //   self.$message({ message: '请输入测试路由！', type: 'error' })
      //   return
      // }
      let paramsObj = {
        'url': self.connectTestViewModel.requestAddress,
        'param': self.connectTestViewModel.requestParams
      }
      self.$http.post(self.$api.getApiAddress('/taskapi/connextest', 'CESHI_API_HOST'), paramsObj).then(res => {
        if (res.data.code === 0) {
          let data = Object.keys(res).length === 0 ? '' : res
          this.formatJson(JSON.stringify(data))
        } else {
          // self.$message({ message: this.$helper.handleLoginErrorMsg(res), type: 'error' })
        }
      }).catch((err) => {
        self.$message({ message: this.$helper.handleLoginErrorMsg(err), type: 'error' })
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
