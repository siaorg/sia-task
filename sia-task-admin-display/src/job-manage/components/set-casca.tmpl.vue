<template>
  <div class="set-casca" id="mask">
      <div class="mask-content" id="mask-content">
        <div class="mask-main-title">
          <span>{{jobKeyListCasca.jobMsg.jobKey}} ~ 级联关系设置</span>
          <i class="close-icon" @click="showHiddenSetCasca"></i>
        </div>
        <div class="info" :class="{'active':jobPlan}">
          <el-form :model="setCasca" :rules="setCascaRules" ref="setCascaForm" label-width="120px" class="dispose-Subgroup-form" auto-complete="off">
            <el-form-item label="后置Job：" prop="jobKey">
              <el-select v-model="setCasca.jobKey" placeholder="项目名称" value-key="jobKey">
                <el-option v-for="(item,index) in jobKeyList" :key="index" :label="item.jobKey" :value="item"></el-option>
              </el-select>
              <el-button @click="showHiddenSave('detete')" class="btn-small">删除</el-button>
            </el-form-item>
            <el-form-item label="前置Job：" v-show="jobPlan!==''">
              <span class="pre-job">{{jobPlan}}</span>
              <!--<el-input type="text" auto-complete="off" :disabled="true" v-model="jobPlan"></el-input>-->
            </el-form-item>
          </el-form>
          <div class="alert-button">
            <el-button class="blue-button" @click="showHiddenSetCasca">取消</el-button>
            <el-button class="blue-button" @click="showHiddenSave('add')">{{jobKeyListCasca.jobMsg.jobPlan === null ? '添加' : '修改'}}</el-button>
          </div>
        </div>
      </div>
  </div>
</template>

<script>
export default {
  name: 'AddMaageTmpl',
  props: ['jobKeyListCasca'],
  data () {
    return {
      jobKeyList: [],
      setCasca: {
        jobKey: {}
      },
      setCascaRules: {
        jobKey: [this.$validator.required('请输入调度器！')]
      },
      jobPlan: ''
    }
  },
  created () {
    this.jobPlan = this.jobKeyListCasca.jobMsg.jobPlan === null ? '' : this.jobKeyListCasca.jobMsg.jobPlan
    this.setCasca.jobKey = this.jobKeyListCasca.jobMsg
    this.jobKeyList = this.jobKeyListCasca.jobKeyList
    this.setCasca.jobKey = this.jobKeyListCasca.jobMsg.jobChild === null ? {} : this.jobKeyListCasca.jobMsg.jobChild
  },
  methods: {
    showHiddenDeleteCasca: function () {
    },
    showHiddenSave: function (val) {
      let self = this
      if (this.setCasca.jobKey.jobKey === this.jobKeyListCasca.jobMsg.jobKey) {
        self.$message({message: '不能关联自己为后置Job !', type: 'error'})
        return false
      }
      let params = {
        jobAlarmEmail: this.jobKeyListCasca.jobMsg.jobAlarmEmail,
        jobCreateTime: this.jobKeyListCasca.jobMsg.jobCreateTime,
        jobDesc: this.jobKeyListCasca.jobMsg.jobDesc,
        jobGroup: this.jobKeyListCasca.jobMsg.jobGroup,
        jobId: this.jobKeyListCasca.jobMsg.jobId,
        jobKey: this.jobKeyListCasca.jobMsg.jobKey,
        jobParentKey: this.jobKeyListCasca.jobMsg.jobParentKey,
        jobTrigerType: this.jobKeyListCasca.jobMsg.jobTrigerType,
        jobTrigerValue: this.jobKeyListCasca.jobMsg.jobTrigerValue,
        jobUpdateTime: this.jobKeyListCasca.jobMsg.jobUpdateTime,
        triggerInstance: this.jobKeyListCasca.jobMsg.triggerInstance
      }
      params.jobPlan = val === 'add' ? this.jobKeyListCasca.jobMsg.jobPlan : null
      params.jobChild = val === 'add' ? this.setCasca.jobKey : null
      this.$refs.setCascaForm.validate(valid => {
        if (valid) {
          self.$http.post(self.$api.getApiAddress('/jobapi/updatejobplan', 'CESHI_API_HOST'), params).then((res) => {
            if (res.data.code === 0) {
              this.setCasca.jobKey = res.data.data
              this.$emit('showHiddenSetCasca', false)
              self.$message({message: val === 'add' ? '设置成功！' : '删除成功', type: 'success'})
            } else {
              self.$message({message: res.data.message, type: 'error'})
            }
          }).catch((err) => {
            self.$message({message: '服务未响应！', type: 'error'})
          })
        }
      })
    },
    showHiddenSetCasca: function () {
      this.$emit('showHiddenSetCasca', false)
    }
  }
}
</script>
<style lang="less">
@import '../styles/common/set-casca.tmpl.less';
</style>
