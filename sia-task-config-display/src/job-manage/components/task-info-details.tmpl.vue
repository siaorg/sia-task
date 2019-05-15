
<template>
  <div class="mask-task-info-details" id="mask">
      <div class="mask-content" id="mask-content">
        <div class="mask-main-title">
          <span>Task配置信息详情</span>
          <i class="close-icon" @click="showHiddenTaskInfoDetail"></i>
        </div>
        <div class="info">
          <el-tabs>
            <el-tab-pane label="TASK 配置信息图">
              <div class="diagra-box">
                <div v-show="!isShowConfigBox" class="jsPlumbBox" id="jsPlumbBox">
                </div>
                <p class="no-data" v-show="isShowConfigBox">
                  <img src="../../common/images/no-data.png" alt="">
                  <span>暂无数据！</span>
                </p>
                <div class="mask-box">
                </div>
              </div>
            </el-tab-pane>
            <el-tab-pane label="TASK 配置信息详情">
              <el-table :data="viewTaskDataList" style="width: 100%" class="task-info-table">
                <el-table-column prop="jobGroup" label="Job_Group" width="120">
                </el-table-column>
                <el-table-column prop="jobKey" show-overflow-tooltip width="140" label="Job_Key">
                </el-table-column>
                <el-table-column prop="taskKey"  show-overflow-tooltip width="160" label="Task_Key">
                </el-table-column>
                <el-table-column prop="preTaskKey"  show-overflow-tooltip min-width="160" label="前置task">
                  <template slot-scope="scope">
                    <p v-for="(item,index) in scope.row.preTaskKey.split(',')" :key="index" :label="item">{{item}}</p> 
                  </template>
                </el-table-column>
                <el-table-column prop="inputType"  show-overflow-tooltip min-width="120" label="Task_参数类型">
                </el-table-column>
                <el-table-column prop="inputValue"  show-overflow-tooltip min-width="130" label="Task_参数值">
                  <template slot-scope="scope">
                    {{scope.row.inputValue === '{}' ? '' : scope.row.inputValue}}
                  </template>
                </el-table-column>
                <el-table-column prop="routeStrategy"  show-overflow-tooltip width="150" label="Task_选取实例策略">
                  <template slot-scope="scope">
                    {{scope.row.routeStrategy | routeStrategyFilter}}
                  </template>
                </el-table-column>
                <el-table-column prop="failover"  show-overflow-tooltip width="145" label="Task_调用失败策略">
                </el-table-column>
                <template slot="empty">
                  <p class="no-data">
                    <img src="../../common/images/no-data.png" alt="">
                    <span>暂无数据！</span>
                  </p>
                </template>
              </el-table>
            </el-tab-pane>
          </el-tabs>
        </div>
      </div>
  </div>
</template>

<script>
import $ from 'jquery'
const jsPlumb=require('jsplumb').jsPlumb
export default {
  name: 'AddJobTmpl',
  props: ['taskInfoDetailsParams'],
  data () {
    return {
      isShowConfigBox: false,
      viewTaskDataList: [],
      viewTaskConfigDataList: [],
      //jsPlumbJson数据
      jsPlumbJson: [],
      //jsPlumbBox容器
      jsPlumbBox: "#jsPlumbBox",
      //连接线数组（用于删除连接线）
		  connectArray: new Array(),
      //端点数组（用于删除端点）
      endpointSourceArray: new Array(),
      targetSourceArray: new Array(),
      //存放初始化jsPlumb实例
		  instanceArray: new Array(),
      //记录节点Icon对应的连接端点数组编号
		  iconSourceArray: new Array(),
      //节点Icon模版
      iconTemplate: [
        "<h5 class=\"{class}\" id=\"{id}\" title=\"{taskKey}\">{taskKey}</h5>",
      ].join(""),
      //出发端点配置
      sourceEndpoint: {
        endpoint: "Dot",
        paintStyle: {
          stroke: "transparent",
          fill: "transparent",
          radius: 6
        },
        maxConnections: -1,
        // dropOptions: { hoverClass: "hover", activeClass: "active" },
        // isTarget: false,
        connector: [ "Flowchart", { stub: [10, 10], gap: 0, cornerRadius: 3, alwaysRespectStubs: false }],
        ConnectionsDetachable: false
      },
      //到达端点配置
      targetEndpoint: {
        endpoint: "Dot",
        paintStyle: {
          fill: "#A9B4DA",
          radius: 5,
          lineWidth: 2
        },
        // isSource: false,
        connector: [ "Flowchart", { stub: [10, 10], gap: 0, cornerRadius: 3, alwaysRespectStubs: false }],
        connectorStyle: {
          lineWidth: 1,
          stroke: "#A9B4DA",
          joinstyle: "round",
          outlineColor: "white",
          outlineWidth: 4
        },
        maxConnections: -1,
        ConnectionsDetachable: false
      }
    }
  },
  filters: {
    routeStrategyFilter: function (val) {
      switch (val) {
        case 'ROUTE_TYPE_RANDOM':
          return '随机'
        case 'ROUTE_TYPE_FIRST':
          return '第一个'
        case 'ROUTE_TYPE_LAST':
          return '最后一个'
        case 'ROUTE_TYPE_ROUND':
          return '轮询'
        case 'ROUTE_TYPE_SPECIFY':
          return '固定IP'
      }
    }
  },
  created () {
    this.getTaskList()
  },
  mounted () {
    this.getTaskConfigList()
  },
  methods: {
    showHiddenTaskInfoDetail: function () {
      this.$emit('showHiddenTaskInfoDetail', false)
    },
    getTaskList: function () {
      let self = this
      self.$http.get(self.$api.getApiAddress('/taskinjobapi/selectTaskByJobKey', 'CESHI_API_HOST'), {
        jobGroup: this.taskInfoDetailsParams.jobGroup,
        jobKey: this.taskInfoDetailsParams.jobKey
      }).then((res) => {
        if (res.data.code === 0) {
          self.viewTaskDataList = res.data.data
        } else {
          self.$message({message: res.data.message, type: 'error'})
        }
      }).catch(() => {
        self.$message({message: '服务未响应！', type: 'error'})
      })
    },
    getTaskConfigList: function () {
      let self = this
      self.$http.get(self.$api.getApiAddress('/taskinjobapi/selectTaskDependencyByJobKey', 'CESHI_API_HOST'), {
        jobGroup: this.taskInfoDetailsParams.jobGroup,
        jobKey: this.taskInfoDetailsParams.jobKey
      }).then((res) => {
        if (res.data.code === 0) {
          this.viewTaskConfigDataList = res.data.data
          this.getTaskKeyList(res.data.data).then((data) => {
            this.jsPlumbInit(data)
          })
        } else {
          self.$message({message: res.data.message, type: 'error'})
        }
      }).catch(() => {
        self.$message({message: '服务未响应！', type: 'error'})
      })
    },
    getTaskKeyList: function (res) {
      return new Promise((resolve) => {
        let taskConfigTagObj = {}
        res.forEach((ele) => {
          taskConfigTagObj[ele.taskKey] = {
            taskId: ele.taskId
          }
        })
        let changeTaskIdList = []
        res.forEach((val) => {
          val.taskId = 'jsPlumb_' + val.taskId
          val.depth = Number(val.depth + 1)
          let preTaskKeyArr = []
          if (val.preTaskKey !== '') {
            let oldpreTaskKey = val.preTaskKey.split(',')
            oldpreTaskKey.forEach((perele) => {
              if (preTaskKeyArr.indexOf('jsPlumb_' + taskConfigTagObj[perele].taskId) === -1) {
                preTaskKeyArr.push('jsPlumb_' + taskConfigTagObj[perele].taskId)
              }
            })
            val.preTaskKey = preTaskKeyArr
          } else {
            val.preTaskKey = []
          }
          changeTaskIdList.push(val)
        })
        resolve(changeTaskIdList)
      })
    },
    //Ajax获取初始化数据后初始化jsPlumb
    jsPlumbInit: function(configParams){
      // this.removeAllJsPlumb();
      var _slef = this;
      _slef.jsPlumbJson = configParams
      if (configParams === null || configParams === undefined) {
        _slef.jsPlumbJson = []
      }
      if (JSON.stringify(_slef.jsPlumbJson) === '[]') {
        _slef.isShowConfigBox = true
        return false
      }
      // 获取初始化Json数据
      let maxValArr = []
      let indexObj = {}
      for (let maxVal in _slef.jsPlumbJson) {
        maxValArr.push(_slef.jsPlumbJson[maxVal].depth)
      }
      let getMaxVal = Math.max.apply(null, maxValArr)
      for (let i = 0; i < getMaxVal; i++) {
        indexObj[i + 1] = {
          num: 0,
          list: []
        }
      }
      //如果有超出边界值的Icon,则修正Icon位置
      for(var i=0; i<_slef.jsPlumbJson.length; i++){
        for (let k in indexObj) {
          if (Number(k) === Number(_slef.jsPlumbJson[i].depth)) {
            indexObj[_slef.jsPlumbJson[i].depth].num += 1
            if (indexObj[_slef.jsPlumbJson[i].depth].list.indexOf(_slef.jsPlumbJson[i].taskKey) === -1) {
              indexObj[_slef.jsPlumbJson[i].depth].list.push(_slef.jsPlumbJson[i].taskKey)
            }
          }
        }
      }
      let xStep = $(_slef.jsPlumbBox).height() / (getMaxVal)
      for (let editX in indexObj) {
        let number = $(_slef.jsPlumbBox).width() / (indexObj[editX].num+1)
        indexObj[editX].list.forEach(function (item, i) {
          _slef.jsPlumbJson.forEach(function (ele,index) {
            if (ele.taskKey === item) {
              ele.left = number * (i+1) - 80
              ele.top = ele.depth === 1 ? 30 : xStep * (_slef.jsPlumbJson[index].depth - 1) + 50
            }
          })
        })
      }
      _slef.jsPlumbInstance();
    },
    //初始化jsPlumb
    jsPlumbInstance: function(){
      if(this.instanceArray.length > 0){
        this.instanceArray[this.instanceArray.length-1] == null;
      }
      //初始化jsPlumb实例
      this.instanceArray.push(jsPlumb.getInstance({
          DragOptions: { cursor: 'pointer', zIndex: 200 },
          ConnectionOverlays: [//箭头样式配置
              ['Arrow', {
                width: 10,
                length: 10,
                location: 1
              }],
              ['Label', {
                label: '',
                cssClass: '',
                labelStyle: {
                  color: 'red'
                },
                events: {
                  click: function (labelOverlay, originalEvent) {
                  console.log('click on label overlay for :' + labelOverlay.component)
                  }
                }
              }]
          ],
          Container: "jsPlumbBox"
      }))
      var instance = this.instanceArray[this.instanceArray.length-1];
      //添加Icon
      this.endpointSourceArray = [];
      this.targetSourceArray = [];
      let bgColorList = []
      for(var i=0; i<this.jsPlumbJson.length; i++){
        let preTask = this.jsPlumbJson[i].taskKey.split('-')[0]
        if (bgColorList.indexOf(preTask) === -1) {
          bgColorList.push(preTask)
        }
        this.addNewChart(instance, i, bgColorList);
      }
      //添加连接线
      var num = 0;
      this.connectArray = [];
      for(var i=0; i<this.jsPlumbJson.length; i++){
        if(this.jsPlumbJson[i].preTaskKey.length > 0){
          for(var j=0; j<this.jsPlumbJson[i].preTaskKey.length; j++){
            console.log(this.jsPlumbJson[i].taskId, this.jsPlumbJson[i].preTaskKey[j])
            this.connectArray[num] = instance.connect({uuids: [this.jsPlumbJson[i].preTaskKey[j]+"BottomCenter", this.jsPlumbJson[i].taskId+"TopCenter"], editable: true});
            num++;
          }
        }
      }
    },
    addNewChart: function (instance, i, bgColorList){
      var _slef = this;
      var jsPlumbObj = this.jsPlumbJson[i];//原初始化数据
      var chartID = jsPlumbObj.taskId;
	    var obj = {}
      let addClassNum = bgColorList.indexOf(jsPlumbObj.taskKey.split('-')[0])
	    obj.class = "jsPlumbIcon" + addClassNum !== -1 ? (' box_' + addClassNum) : '';
	    obj.id = chartID;
	    obj.taskKey = jsPlumbObj.taskKey;

	    $(this.jsPlumbBox).append(this.substitute(this.iconTemplate,obj));
	    $("#"+chartID).css("left",jsPlumbObj.left+"px").css("top",jsPlumbObj.top+"px");
	    // 绑定移动动作
	    instance.draggable(chartID);
	    instance.batch(function () {
	      _slef._addEndpoints(instance, chartID, "BottomCenter", "TopCenter", i);
	    })
    },
    //添加jsPlumb块
    _addEndpoints : function (instance, toId, sourceAnchors, targetAnchors, m) {
      let _slef = this
      var sourceUUID = toId + sourceAnchors;
      this.endpointSourceArray[m] = instance.addEndpoint(toId, _slef.sourceEndpoint,{
          anchor: sourceAnchors, uuid: sourceUUID
      });
      var targetUUID = toId + targetAnchors;
      this.targetSourceArray[m] = instance.addEndpoint(toId, _slef.targetEndpoint, { anchor: targetAnchors, uuid: targetUUID });
      this.iconSourceArray[toId] = m;
    },
    substitute : function(str,object){
      return str.replace(/\\?\{([^}]+)\}/g, function(match, name){
          if (match.charAt(0) == '\\') return match.slice(1);
          return (object[name] != undefined) ? object[name] : '';
      })
    }
  }
}
</script>
<style lang="less">
@import '../styles/common/task-info-details.tmpl.less';
</style>
