<template>
  <div class="add-manage" id="mask">
      <div class="mask-content" id="mask-content">
        <div class="mask-main-title">
          <span>添加 ~ {{addManageType === '1' ? '下线调度器' : '白名单'}}</span>
          <i class="close-icon" @click="showHiddenAddManage"></i>
        </div>
        <div class="info">
          <el-form :model="addManageViewModel" :rules="addManageViewModelRules" ref="addManageForm" label-width="100px" class="dispose-Subgroup-form" auto-complete="off">
            <el-form-item :label="addManageType === '1' ? '下线调度器' : '白名单'" prop="manage">
              <el-input type="text" auto-complete="off" placeholder="" min='1' step="1" v-model="addManageViewModel.manage"></el-input>
            </el-form-item>
          </el-form>
          <div class="alert-button">
            <el-button class="blue-button" @click="showHiddenAddManage">取消</el-button>
            <el-button class="blue-button" @click="showHiddenSave">添加</el-button>
          </div>
        </div>
      </div>
  </div>
</template>

<script>
export default {
  name: 'AddMaageTmpl',
  props: ['addManageType'],
  data () {
    return {
      manageList: [],
      addManageViewModel: {
        manage: ''
      },
      addManageViewModelRules: {
        manage: [this.$validator.required('请输入调度器！')]
      }
    }
  },
  methods: {
    showHiddenSave: function () {
      let self = this
      let urlParams = this.addManageType === '1' ? '/scheduler/closeScheduler' : '/scheduler/addAuthList'
      this.$refs.addManageForm.validate(valid => {
        if (valid) {
          self.$http.postNoObj(self.$api.getApiAddress(urlParams, 'CESHI_API_HOST'), self.addManageViewModel.manage).then((res) => {
            this.$emit('showHiddenAddManage', false)
            if (res.data.code === 0) {
              self.$message({message: '添加成功！', type: 'success'})
            } else {
              self.$message({message: res.data.message, type: 'error'})
            }
          }).catch(() => {
            self.$message({message: '服务未响应！', type: 'error'})
          })
        }
      })
    },
    showHiddenAddManage: function () {
      this.$emit('showHiddenAddManage', false)
    }
  }
}
</script>
<style lang="less" scoped>
@import '../styles/common/add-manage.tmpl.less';
</style>
