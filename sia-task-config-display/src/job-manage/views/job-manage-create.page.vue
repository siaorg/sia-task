<template>
  <div class='dispatch-system-default job-manage-create-page'>
    <div class="section-container">
      <div class="section-header">
        <span>Task信息配置</span>
        <!--<el-button class="btn-large refresh-btn btn-ml-auto" icon="el-icon-refresh" :loading="loadingRefresh" @click="showHiddenRefreshTaskList">{{loadingRefresh?'加载中':'刷新'}}</el-button>-->
        <el-button class="btn-large edit-btn btn-ml-auto" @click="clickSaveTaskConfig"> 提交 </el-button>
      </div>
      <div class="section-content" id="section-content">
        <div class="section-left">
          <div class="title"><i><img src="../images/home-title-icon.png" alt=""></i>TASK信息</div>
          <div class="leftIconCopy" id="leftIconCopy">
          </div>
          <div class="left-content">
            <div class="search-box">
              <el-input type="text" @input="changeSearchTakkeyVlaue" v-model="searchTakkeyVlaue" auto-complete="off" placeholder="请输入TASK名称"></el-input>
            </div>
            <div class="left-box" id="left-box">
              <ul class="left-list">
                <li class="addJsPlumb" v-for="item in leftMenuList" :style="{'background': item.color}" :title="item.taskKey" :key="item.taskKey" @mousedown="addLeftIconEvent($event, item)" :data-taskId="item.taskId">{{item.taskKey}}</li>
                <!--<li class="addJsPlumb" v-for="item in leftMenuList" :style="{'background': item.bgColor}" :title="item.taskKey" :key="item.taskKey" :data-taskId="item.taskId">{{item.taskKey}}</li>-->
              </ul>
            </div>
          </div>
        </div>
        <div class="center-bg"></div>
        <div class="section-right">
          <div class="title"><i><img src="../images/home-title-icon.png" alt=""></i>TASK信息配置图</div>
          <div class="jsPlumbBox" id="jsPlumbBox"></div>
        </div>
      </div>
    </div>
    <!-- add task-->
    <add-task-tmpl v-if="addTaskShowConfig" :addTaskTagValue="addTaskTagValue" v-on:showHiddenAddtask="showHiddenAddtask"></add-task-tmpl>
    <!-- add task end -->
  </div>
</template>

<script>
import $ from 'jquery'
const jsPlumb = require('jsplumb').jsPlumb
const addTaskTmpl = resolve => require(['../components/task-add-config'], resolve)
export default {
  components: {addTaskTmpl},
  name: 'JobManageCreatePage',
  data () {
    return {
      taskGrounpList: [],
      colorBgList: ['#4A90E2', '#E8A010', '#60BECA', '#7C6AF2', '#B8E986'],
      loadingRefresh: false,
      addTaskShowConfig: false,
      addTaskTagValue: {},
      // 左侧菜单点击状态
      mouseDownState: false,
      // 节点Icon点击状态
      iconMouseDownState: {
        state: false, // 点击状态
        num: null, // 点击Icon对应的jsPlumbJson数组序号
        mouseup: true // Icon鼠标mouseup点击状态标记
      },
      // 记录点击状态li Icon的位置
      leftIconOffset: false,
      // 记录点击li Icon的鼠标位置
      mousedownClient: false,
      // jsPlumbBox的边界范围
      jsPlumbBoxBorders: {},
      // 连接线数组（用于删除连接线）
      connectArray: [],
      // 端点数组（用于删除端点）
      endpointSourceArray: [],
      targetSourceArray: [],
      // 存放初始化jsPlumb实例
      instanceArray: [],
      // 记录节点Icon对应的连接端点数组编号
      iconSourceArray: [],
      // 左侧列表数据
      leftMenuList: [],
      // 左侧列表总数据
      leftMenuTotalList: [],
      // 左侧列表搜素值
      searchTakkeyVlaue: '',
      taskIdObj: {}, // 通过taskID 存储taskkeyName
      taskConfigTagObj: {}, // 储存左侧菜单  ip  taskId  paramCount
      // jsPlumbBox容器
      jsPlumbBox: '#jsPlumbBox',
      // jsPlumbJson数据
      jsPlumbJson: [],
      // 节点Icon模版
      iconTemplate: [
        '<div class=\'{class}\' id=\'{id}\' title=\'{taskKey}\'>',
        '<h5 title=\'{taskKey}\'>{taskKey}</h5>',
        '<span class=\'delete-dom iconfont icon-delete\' id=\'{id}\'></span>',
        '<span class=\'edit-dom iconfont icon-edit\' id=\'{id}\'></span>',
        '</div>'
      ].join(''),
      // 出发端点配置
      targetEndpoint: {
        endpoint: 'Dot',
        paintStyle: {fill: '#A9B4DA', radius: 6},
        hoverPaintStyle: {
          fill: 'red',
          stroke: 'red'
        },
        maxConnections: -1,
        dropOptions: {hoverClass: 'hover', activeClass: 'active'},
        isTarget: true,
        overlays: [
          ['Label', {location: [0.5, -0.5], label: 'Drop', cssClass: 'endpointTargetLabel', visible: false}]
        ]
      },
      // 到达端点配置
      sourceEndpoint: {
        endpoint: 'Dot',
        paintStyle: {
          stroke: '#A9B4DA',
          fill: 'transparent',
          radius: 5,
          lineWidth: 5
        },
        isSource: true,
        connector: ['Flowchart', {stub: [10, 10], gap: 8, cornerRadius: 3, alwaysRespectStubs: true}],
        connectorStyle: {
          lineWidth: 5,
          stroke: '#A9B4DA',
          joinstyle: 'round',
          outlineColor: 'white',
          outlineWidth: 2
        },
        hoverPaintStyle: {
          fill: 'red',
          stroke: 'red'
        },
        maxConnections: -1,
        connectorHoverStyle: {
          lineWidth: 8,
          stroke: 'red',
          outlineWidth: 2,
          outlineColor: 'white'
        },
        dragOptions: {},
        overlays: [
          ['Label', {
            location: [0.5, 1.5],
            label: 'Drag',
            cssClass: 'endpointSourceLabel',
            visible: false
          }]
        ]
      }
    }
  },
  created () {
    // 初始化jsPlumb实例
    this.getTaskKeyList()
  },
  mounted () {
    // window.onload()
    // 读取jsPlumbBox的边界值
    this.readJsPlumbBoxBorders()
    this.initTaskConfigData()
  },
  methods: {
    changeSearchTakkeyVlaue: function () {
      this.leftMenuList = this.leftMenuTotalList
      if (this.searchTakkeyVlaue !== '') {
        console.log(this.leftMenuTotalList)
        this.leftMenuList = this.leftMenuTotalList.filter((ele) => ele.taskKey.indexOf(this.searchTakkeyVlaue) !== -1)
      }
    },
    clickSaveTaskConfig () {
      let self = this
      let params = self.jsPlumbJson
      if (params.length === 0) {
        self.$message({message: '信息为空，不能保存', type: 'error'})
        return false
      }
      // job_group
      // job_key
      // task_key
      params.forEach(function (val) {
        let preTask = []
        val.jobId = self.$route.query.jobId
        val.jobKey = self.$route.query.jobKey
        val.jobGroup = self.$route.query.jobGroup
        val.taskId = val.taskId.split('_')[1]
        val.preTaskKey.forEach((pre) => {
          if (preTask.indexOf(self.taskIdObj[pre]) === -1) {
            preTask.push(self.taskIdObj[pre])
          }
        })
        val.preTaskKey = preTask.join(',')
        if (val.inputType === 'FROM_TASK' && val.inputValue === '{}') {
          val.inputValue = ''
          self.$message({message: 'TASK参数不能为空', type: 'error'})
          return false
        }
      })
      // return false
      self.$http.post(self.$api.getApiAddress('/taskinjobapi/inserttaskinjob', 'CESHI_API_HOST'), params).then((res) => {
        switch (res.data.code) {
          case 0:
            self.$message({message: '添加成功', type: 'success'})
            self.$router.push({path: '/job-manage-list'})
            break
          // case 4003:
          //   self.$message({message: 'task配置中存在环，请检查配置信息', type: 'error'})
          //   break
          default:
            self.jsPlumbJson.forEach((val) => {
              val.taskId = 'jsPlumb_' + val.taskId
              val.depth = Number(val.depth + 1)
              let preTaskKeyArr = []
              if (val.preTaskKey !== '') {
                let oldpreTaskKey = val.preTaskKey.split(',')
                oldpreTaskKey.forEach((perele) => {
                  if (preTaskKeyArr.indexOf('jsPlumb_' + this.taskConfigTagObj[perele].taskId) === -1) {
                    preTaskKeyArr.push('jsPlumb_' + this.taskConfigTagObj[perele].taskId)
                  }
                })
                val.preTaskKey = preTaskKeyArr
              } else {
                val.preTaskKey = []
              }
            })
            // self.jsPlumbJson delete jsessionid
            console.log(self.jsPlumbJson, 'oooooooooooooooooooo')
            // self.removeAllJsPlumb()
            // self.jsPlumbInstance()
            self.$message({message: res.data.message, type: 'error'})
            break
        }
      }).catch(() => {
        self.$message({message: '添加失败', type: 'error'})
      })
    },
    showHiddenRefreshTaskList: function () {
      let self = this
      self.loadingRefresh = true
      setTimeout(function () {
        self.loadingRefresh = false
      }, 2000)
    },
    readJsPlumbBoxBorders: function () { // 读取jsPlumbBox的边界值
      var jsPlumbBoxOffset = $(this.jsPlumbBox).offset()
      this.jsPlumbBoxBorders = {
        top: jsPlumbBoxOffset.top,
        right: jsPlumbBoxOffset.left + $(this.jsPlumbBox).width(),
        bottom: jsPlumbBoxOffset.top + $(this.jsPlumbBox).height(),
        left: jsPlumbBoxOffset.left
      }
    },
    // 获取配置关系数据
    initTaskConfigData: function () {
      let self = this
      self.$http.get(self.$api.getApiAddress('/taskinjobapi/selectTaskDependencyByJobKey', 'CESHI_API_HOST'), {
        jobGroup: this.$route.query.jobGroup,
        jobKey: this.$route.query.jobKey
      }).then((res) => {
        if (res.data.code === 0) {
          this.getTaskKeyList().then((taskConfigTagObj) => {
            if (res.data.data !== null && res.data.data !== undefined) {
              res.data.data.forEach((val) => {
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
              })
            }
            self.jsPlumbInit(res.data.data)
          })
        } else {
          self.$message({message: res.data.message, type: 'error'})
        }
      }).catch(() => {
        self.$message({message: '服务未响应！', type: 'error'})
      })
    },
    // 随机颜色
    randomColor: function () {
      let r = Math.floor(Math.random() * 255)
      let g = Math.floor(Math.random() * 255)
      let b = Math.floor(Math.random() * 255)
      return 'rgb(' + r + ',' + g + ',' + b + ')'
    },
    // 获取左侧task列表
    getTaskKeyList: function () {
      let self = this
      return new Promise((resolve) => {
        self.$http.get(self.$api.getApiAddress('/taskapi/selectAuth', 'CESHI_API_HOST')).then((res) => {
          if (res.data.code === 0) {
            this.taskGrounpList = res.data.data
            this.taskGrounpList.forEach((ele, index) => {
              if (index > 5 && this.colorBgList.indexOf(this.randomColor()) === -1) {
                this.colorBgList.push(this.randomColor())
              }
            })
          } else {
            self.$message({message: res.data.message, type: 'error'})
          }
        })
        self.$http.get(self.$api.getApiAddress('/taskapi/selectTasks', 'CESHI_API_HOST'), {
          taskGroupName: '',
          taskAppName: '',
          taskKey: ''
        }).then((res) => {
          if (res.data.code === 0) {
            res.data.data.forEach((ele) => {
              ele.color = this.colorBgList[this.taskGrounpList.indexOf(ele.taskKey.split('-')[0])]
              this.taskConfigTagObj[ele.taskKey] = {
                taskGroupName: ele.taskGroupName,
                taskAppName: ele.taskAppName,
                taskSource: ele.taskSource,
                taskId: ele.taskId,
                paramCount: ele.paramCount
              }
              this.taskIdObj['jsPlumb_' + ele.taskId] = ele.taskKey
            })
            self.leftMenuList = res.data.data
            self.leftMenuTotalList = res.data.data
            resolve(this.taskConfigTagObj)
          } else {
            self.$message({message: res.data.message, type: 'error'})
          }
        }).catch(() => {
          self.$message({message: '服务未响应！', type: 'error'})
        })
      })
    },
    // 获取初始化数据后  处理数据  设置各个数据left top 值
    jsPlumbInit: function (configParams) {
      // this.removeAllJsPlumb();
      var _slef = this
      // 拖拽图关系赋值
      _slef.jsPlumbJson = configParams
      if (configParams === null || configParams === undefined) {
        _slef.jsPlumbJson = []
      }
      // 获取初始化Json数据
      let maxValArr = [] // 定义拖拽图关系层级变量，用于找出最大值
      let indexObj = {} // 定义各个层级  task个数   用于计算统一行  各个task之间的间距
      for (let maxVal in _slef.jsPlumbJson) {
        // 根据关系数据来追加层级
        maxValArr.push(_slef.jsPlumbJson[maxVal].depth)
      }
      let getMaxVal = Math.max.apply(null, maxValArr) // 求出最大层级
      for (let i = 0; i < getMaxVal; i++) {
        // 计算各个层级之间拥有的task个数
        indexObj[i + 1] = {
          num: 0,
          list: []
        }
      }
      // 计算各个task  top以及left值
      for (var i = 0; i < _slef.jsPlumbJson.length; i++) {
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
        let number = $(_slef.jsPlumbBox).width() / (indexObj[editX].num + 1)
        indexObj[editX].list.forEach(function (item, i) {
          _slef.jsPlumbJson.forEach(function (ele, index) {
            if (ele.taskKey === item) {
              ele.left = number * (i + 1) - 80
              ele.top = ele.depth === 1 ? 30 : xStep * (_slef.jsPlumbJson[index].depth - 1) + 50
            }
          })
        })
      }
      _slef.jsPlumbInstance()
    },
    // 初始化jsPlumb
    jsPlumbInstance: function () {
      if (this.instanceArray.length > 0) {
        this.instanceArray[this.instanceArray.length - 1] = null
      }
      // 初始化jsPlumb实例
      this.instanceArray.push(jsPlumb.getInstance({
        DragOptions: {cursor: 'pointer', zIndex: 200},
        ConnectionOverlays: [ // 箭头样式配置
          ['Arrow', {
            width: 10,
            length: 10,
            location: 1
          }],
          ['Label', {
            width: 10,
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
        Container: 'jsPlumbBox'
      }))
      var instance = this.instanceArray[this.instanceArray.length - 1]
      // 添加Icon
      this.endpointSourceArray = []
      this.targetSourceArray = []
      for (var k = 0; k < this.jsPlumbJson.length; k++) {
        this.addNewChart(instance, k)
        // 添加节点Icon事件绑定
        this.addJsPlumbIconBind($('#' + this.jsPlumbJson[k].taskId), this.jsPlumbJson[k])
      }
      // 添加连接线
      var num = 0
      this.connectArray = []
      for (var i = 0; i < this.jsPlumbJson.length; i++) {
        if (this.jsPlumbJson[i].preTaskKey.length > 0) {
          for (var j = 0; j < this.jsPlumbJson[i].preTaskKey.length; j++) {
            this.connectArray[num] = instance.connect({uuids: [this.jsPlumbJson[i].preTaskKey[j] + 'BottomCenter', this.jsPlumbJson[i].taskId + 'TopCenter'], editable: true})
            num++
          }
        }
      }
      // 添加JsPlumb绑定事件
      this.addJsPlumbBind(instance)
    },
    addNewChart: function (instance, i, clientObj) {
      var _slef = this
      var jsPlumbObj = {}
      if (i < this.jsPlumbJson.length) { // 原初始化数据
        jsPlumbObj = this.jsPlumbJson[i]
      } else {
        jsPlumbObj = {
          'taskId': clientObj.taskId,
          'taskKey': clientObj.taskKey,
          'preTaskKey': [],
          'left': clientObj.clientX + $(window).scrollLeft(),
          'top': clientObj.clientY + $(window).scrollTop()
        }
        jsPlumbObj.left = Math.round(Math.round(jsPlumbObj.left) / 10) * 10
        jsPlumbObj.top = Math.round(Math.round(jsPlumbObj.top) / 10) * 10
        this.jsPlumbJson.push(jsPlumbObj)
      }
      var chartID = jsPlumbObj.taskId
      var obj = {}
      obj.class = 'jsPlumbIcon'
      obj.id = chartID
      obj.taskKey = jsPlumbObj.taskKey
      let colorBg = this.colorBgList[this.taskGrounpList.indexOf(jsPlumbObj.taskKey.split('-')[0])]
      $(this.jsPlumbBox).append(this.substitute(this.iconTemplate, obj))
      $('#' + chartID).css('left', jsPlumbObj.left + 'px').css('top', jsPlumbObj.top + 'px').css('position', 'absolute').css('background', colorBg)
      // 绑定移动动作
      instance.draggable(chartID)
      instance.batch(function () {
        _slef._addEndpoints(instance, chartID, 'BottomCenter', 'TopCenter', i)
      })
    },
    // 添加jsPlumb块
    _addEndpoints: function (instance, toId, sourceAnchors, targetAnchors, m) {
      let _slef = this
      var sourceUUID = toId + sourceAnchors
      this.endpointSourceArray[m] = instance.addEndpoint(toId, _slef.sourceEndpoint, {
        anchor: sourceAnchors, uuid: sourceUUID
      })
      var targetUUID = toId + targetAnchors
      this.targetSourceArray[m] = instance.addEndpoint(toId, _slef.targetEndpoint, { anchor: targetAnchors, uuid: targetUUID })
      this.iconSourceArray[toId] = m
    },
    // 节点Icon 点击事件绑定
    addJsPlumbIconBind: function (ID, valTaget) {
      var _slef = this
      $(ID).mousedown(function (e) {
        if (e.which === 1) { // 鼠标左键事件
          _slef.mousedownClient = {
            'mousedownX': e.clientX - $(this).offset().left,
            'mousedownY': e.clientY - $(this).offset().top,
            'left': $(this).css('left'),
            'top': $(this).css('top')
          }
          $(_slef.jsPlumbBox).children('.jsPlumbIcon').css({'z-index': 20})
          $(this).css({'z-index': 21})
          // 获取jsPlumbJson中对应的数组序号
          var num
          for (var i = 0; i < _slef.jsPlumbJson.length; i++) {
            if ($(this).attr('id') === _slef.jsPlumbJson[i].taskId) {
              num = i
              break
            }
          }
          _slef.iconMouseDownState.state = true
          _slef.iconMouseDownState.num = num
          // $(this).css({cursor: 'move'})
        }
      })
      $(ID).mouseup(function (e) {
        if (e.which === 1) { // 鼠标左键事件，用于捕捉Icon位移数据
          // 获取jsPlumbJson中对应的数组序号
          var num
          for (var i = 0; i < _slef.jsPlumbJson.length; i++) {
            if ($(this).attr('id') === _slef.jsPlumbJson[i].taskId) {
              num = i
              break
            }
          }
          var data = {
            'icon_ID': _slef.jsPlumbJson[num].taskId, // 节点IconID
            'icon_left': e.clientX - $(_slef.jsPlumbBox).offset().left - _slef.mousedownClient.mousedownX + $(_slef.jsPlumbBox).scrollLeft(),
            'icon_top': e.clientY - $(_slef.jsPlumbBox).offset().top - _slef.mousedownClient.mousedownY + $(_slef.jsPlumbBox).scrollTop()
          }
          if (_slef.mousedownClient.left === $(this).css('left') && _slef.mousedownClient.top === $(this).css('top')) {
            // 节点Icon没有位移不做处理
          } else {
            if (!isNaN(data.icon_left)) {
              // 自动对齐网格
              data.icon_left = Math.round(Math.round(data.icon_left) / 10) * 10
              data.icon_top = Math.round(Math.round(data.icon_top) / 10) * 10
              $(this).css({left: data.icon_left + 'px', top: data.icon_top + 'px'})
              _slef.jsPlumbJson[num].left = data.icon_left
              _slef.jsPlumbJson[num].top = data.icon_top
              _slef.editIconAjax(data, num)
            }
          }
          _slef.mousedownClient = false
          $(this).css({cursor: 'pointer'})
          _slef.iconMouseDownState.state = false
          _slef.iconMouseDownState.num = null
          // TODO
          // $('#nodeIconDelMenu').bind('contextmenu', function(e) {
          //   return false;
          // })
          // valTaget为点击项的数据
          console.log($(e.target).attr('id'), '---------------------------eeeicon')
          if (e.target.tagName === 'SPAN') {
            if ($(e.target).attr('class').indexOf('delete-dom') !== -1) {
              console.log('删除节点:', $(e.target).attr('id'))
              for (var k = 0; k < _slef.jsPlumbJson.length; k++) {
                if (_slef.jsPlumbJson[k].taskId === $(e.target).attr('id')) {
                  _slef.jsPlumbJson.splice(k, 1)
                  console.log(_slef.jsPlumbJson, '_slef.jsPlumbJsonshanchu--------删除')
                  break
                }
              }
              _slef.removeJsPlumbIcon($(e.target).attr('id'))
            } else if ($(e.target).attr('class').indexOf('edit-dom') !== -1) {
              for (var z = 0; z < _slef.jsPlumbJson.length; z++) {
                if (_slef.jsPlumbJson[z].taskId === $(e.target).attr('id')) {
                  let params = valTaget === undefined ? _slef.jsPlumbJson[z] : valTaget
                  _slef.editTaskConfig(params)
                  break
                }
              }
            }
          }
        }
        // else if(e.which == 3) {//e.which = 3 为鼠标右键事件（用于打开删除按钮）
        //   e.preventDefault();
        //   e.stopPropagation();
        //   $('#nodeIconDelMenu').show();
        //   $('#nodeIconDelMenu').css({left:Math.round(e.clientX + $(window).scrollLeft())+'px', top:Math.round(e.clientY + $(window).scrollTop())+'px'});
        //   $('#nodeIconDelMenu .delIcon').attr({
        //     'data-id':$(this).attr('id')
        //   });
        // }
      })
      $(ID).mousemove(function () {
        if (_slef.iconMouseDownState.state) {
          $(this).css({cursor: 'move'})
        }
      })
      $(ID).bind('contextmenu', function (e) {
        return false
      })
    },
    editTaskConfig (valTaget) {
      this.addTaskShowConfig = true
      this.addTaskTagValue = {
        valTaget: valTaget,
        taskListTag: this.taskConfigTagObj,
        taskIdObj: this.taskIdObj
      }
    },
    addJsPlumbBind: function (instance) { // 添加JsPlumb绑定事件
      var _slef = this
      // 连接线双击事件绑定
      instance.bind('click', function (conn, e) {
        e.preventDefault()
        e.stopPropagation()
        // _slef.delConnection(conn);
        // console.log(jsPlumb)
        jsPlumb.deleteConnection(conn)
        // _slef.delConnection(conn)
      })
      // 监听新建连接事件
      instance.bind('connection', function (connInfo, originalEvent) {
        /*
        * 添加新连接线流程说明：
        * 1,前台新连接线事件触发后，添加连接线数据至connectArray，用于删除连接线
        * 2,更新jsPlumbJson中的对应连接线数据
        * 3,向API提交更新数据
        */
        // 判断是新建连接还是移动端点的连接
        if (connInfo.connection.suspendedElementId === undefined || connInfo.connection.suspendedElementId === null) {
          var num
          // 添加对应新连接(用于删除连接)
          _slef.connectArray.push(connInfo.connection)
          // 更新新连接线至jsPlumbJson
          for (var k = 0; k < _slef.jsPlumbJson.length; k++) {
            if (_slef.jsPlumbJson[k].taskId === connInfo.connection.targetId) {
              _slef.jsPlumbJson[k].preTaskKey.push(connInfo.connection.sourceId)
              num = k
              break
            }
          }
          var data = {
            'icon_ID': _slef.jsPlumbJson[num].taskId, // 节点IconID
            'icon_preTaskKey': _slef.jsPlumbJson[num].preTaskKey
          }
          _slef.editIconAjax(data, num)
        }
      })
      // 监听连接线移除事件(注意：此监听事件并不监听连接线两头的端点变更)
      instance.bind('connectionDetached', function (conn) {
        console.log('删除连线')
        _slef.delConnection(conn)
      })
      // 监听连接线移动端点事件
      instance.bind('connectionMoved', function (conn) {
        // 同一端点断开再连接不做处理
        if (conn.connection.suspendedElementId === conn.connection.preTaskKey) {
          return false
        }
        // 更新新连接线至jsPlumbJson
        for (var i = 0; i < _slef.jsPlumbJson.length; i++) {
          if (_slef.jsPlumbJson[i].taskId === conn.connection.sourceId) {
            for (var j = 0; j < _slef.jsPlumbJson[i].preTaskKey.length; j++) {
              if (_slef.jsPlumbJson[i].preTaskKey[j] === conn.connection.suspendedElementId) {
                _slef.jsPlumbJson[i].preTaskKey.splice(j, 1)
                _slef.jsPlumbJson[i].preTaskKey.push(conn.connection.preTaskKey)
                var data = {
                  'icon_ID': _slef.jsPlumbJson[i].taskId, // 节点IconID
                  'icon_preTaskKey': _slef.jsPlumbJson[i].preTaskKey
                }
                console.log(data, '==================data连线')
                _slef.editIconAjax(data, i)
                break
              }
            }
            break
          }
        }
      })
    },
    addNewJsPlumbIcon: function (clientObj) { // 添加新sPlumb Icon
      var num = this.jsPlumbJson.length
      // 添加新Icon
      this.addNewChart(this.instanceArray[this.instanceArray.length - 1], num, clientObj)
      // 这里的num，经过addNewChart之后数字会加1，所以下面的num不用-1了
      // 删除原绑定事件
      this.instanceArray[this.instanceArray.length - 1].unbind()
      // 添加JsPlumb绑定事件
      this.addJsPlumbBind(this.instanceArray[this.instanceArray.length - 1])
      // 添加节点Icon事件绑定
      this.addJsPlumbIconBind($('#' + this.jsPlumbJson[num].taskId))
      /*
      * 添加新Icon后，向API接口添加新Icon数据
      * API获取新节点Icon数据后，返回taskID（此节点的任务ID）
      * 将taskID更新至jsPlumbJson数组及页面Dom元素上
      */
    },
    // 删除连接线处理
    delConnection: function (conn) {
      // 循环查找jsPlumbJson中对应的数据，删除对应数据
      console.log(conn.sourceId, '----------------------conn.sourceId', this.jsPlumbJson)
      for (var i = 0; i < this.jsPlumbJson.length; i++) {
        if (conn.targetId === this.jsPlumbJson[i].taskId) {
          // var num = null
          for (var j = 0; j < this.jsPlumbJson[i].preTaskKey.length; j++) {
            if (this.jsPlumbJson[i].preTaskKey[j] === conn.sourceId) {
              this.jsPlumbJson[i].preTaskKey.splice(j, 1)
              var data = {
                'icon_ID': this.jsPlumbJson[i].taskId, // 节点IconID
                'icon_preTaskKey': this.jsPlumbJson[i].preTaskKey.length > 0 ? this.jsPlumbJson[i].preTaskKey : null
              }
              this.editIconAjax(data, i)
              break
            }
          }
          break
        }
      }
    },
    // 清空画布jsPlumb实例
    removeAllJsPlumb: function () {
      if (this.instanceArray.length > 0) {
        // 释放事件
        this.instanceArray[this.instanceArray.length - 1].unbind()
        for (var i = 0; i < this.jsPlumbJson.length; i++) {
          $('#' + this.jsPlumbJson[i].taskId).unbind()
        }
        // 删除所有端点
        this.instanceArray[this.instanceArray.length - 1].deleteEveryEndpoint()
        // 删除所有Dom
        jsPlumb.empty(this.jsPlumbBox)
      }
    },
    // 删除节点Icon
    removeJsPlumbIcon: function (id) {
      // 释放事件
      $('#' + id).unbind()
      console.log(id, this.instanceArray, this.instanceArray, '-------------------this.instanceArray')
      console.log(this.iconSourceArray, this.targetSourceArray[this.iconSourceArray[id]], '-------------this.targetSourceArray[this.iconSourceArray[id]]')
      // 删除对应的端点
      // if (this.instanceArray.length !== this.instanceArray.length) {
      this.instanceArray[this.instanceArray.length - 1].deleteEndpoint(this.endpointSourceArray[this.iconSourceArray[id]])
      this.instanceArray[this.instanceArray.length - 1].deleteEndpoint(this.targetSourceArray[this.iconSourceArray[id]])
      this.instanceArray[this.instanceArray.length - 1].remove(id)
      // }
    },
    editIconAjax: function (data, num) {
      console.log(data, '----------------data')
    },
    showHiddenAddtask (type, val, isShow) {
      this.addTaskShowConfig = isShow
      if (type === 2) {
        console.log(val, '------------------------val')
        this.jsPlumbJson.forEach((ele) => {
          if (ele.taskKey === val.taskKey) {
            ele.inputType = val.taskParamsType
            ele.readTimeout = val.readTimeout
            ele.inputValue = val.taskParamsValue
            ele.routeStrategy = (val.taskSelectCase === '分片' ? 'ROUTE_TYPE_SHARDING' : (val.taskSelectCase === '随机' ? 'ROUTE_TYPE_RANDOM' : 'ROUTE_TYPE_SPECIFY'))
            ele.fixIp = val.taskSelectCase === '固定IP' ? val.ipSelectTag : ''
            ele.failover = val.tasktransferFail
          }
        })
        // job_group
        // job_key
        // task_key
        // task_参数类型
        // task_参数值
        // 过期时间
        // task——选取实例策略
        // task_调用失败策略
        // 选取实例
        console.log(this.jsPlumbJson, 'ppppppppppppppppppppppppppppppp')
      }
    },
    // 左侧列表添加点击事件
    addLeftIconEvent: function (e, val) {
      let odiv = e.target
      let _slef = this
      e.preventDefault()
      e.stopPropagation()
      _slef.mouseDownState = true
      // 在原li Icon处生成外观一样的可拖动Copy Icon
      var iconOffset = _slef.leftIconOffset = {
        left: odiv.offsetLeft,
        top: e.pageY - $('#left-box')[0].offsetTop - 55
      }
      var top = 'top:' + iconOffset.top + 'px;'
      var left = 'left:' + iconOffset.left + 'px;'
      _slef.mousedownClient = {
        clientX: e.clientX,
        clientY: e.clientY
      }
      // taskKey 跟着鼠标移动盒子
      $('#leftIconCopy').html(val.taskKey).css({background: val.color, left: left, top: top, zIndex: 2, height: '30px', display: 'none'}).attr('taskId', val.taskId).attr('taskKey', val.taskKey)
      $(window).mousemove(function (e) {
        e.preventDefault()
        e.stopPropagation()
        if (_slef.mouseDownState) {
          // Icon跟随鼠标移动
          $('#leftIconCopy').css({display: 'block'})
          var top = e.clientY - (_slef.mousedownClient.clientY - _slef.leftIconOffset.top)
          var left = e.clientX - (_slef.mousedownClient.clientX - _slef.leftIconOffset.left)
          $('#leftIconCopy').css({left: left + 'px', top: top + 'px', zIndex: 2})
        }
      })
      $(window).mouseup(function (e) {
        e.preventDefault()
        e.stopPropagation()
        // 只检测鼠标左键事件
        if (e.which === 1) {
          // 判断是否是左侧菜单拖动事件
          if (_slef.mouseDownState) {
            // 拖动Icon边界碰撞检测
            if (e.clientX > _slef.jsPlumbBoxBorders.left && e.clientX < _slef.jsPlumbBoxBorders.right) {
              if (e.clientY > _slef.jsPlumbBoxBorders.top && e.clientY < _slef.jsPlumbBoxBorders.bottom) {
                var clientObj = {
                  clientX: e.clientX - $(this.jsPlumbBox).offset().left - 25,
                  clientY: e.clientY - $(this.jsPlumbBox).offset().top - 25,
                  taskKey: $('#leftIconCopy').attr('taskKey'),
                  taskId: 'jsPlumb_' + $('#leftIconCopy').attr('taskId')
                }
                let isAddRightData = false
                _slef.jsPlumbJson.forEach((val) => {
                  if (val.taskId === clientObj.taskId) {
                    isAddRightData = true
                  }
                })
                _slef.$http.get(_slef.$api.getApiAddress('/taskapi/selectTaskInJob', 'CESHI_API_HOST'), {
                  taskAppName: _slef.taskConfigTagObj[$('#leftIconCopy').attr('taskKey')].taskAppName,
                  taskGroupName: _slef.taskConfigTagObj[$('#leftIconCopy').attr('taskKey')].taskGroupName,
                  taskKey: $('#leftIconCopy').attr('taskKey')
                }).then((res) => {
                  if (res.data.data.length !== 0) {
                    _slef.$confirm($('#leftIconCopy').attr('taskKey') + '已经被Job引用，你确定再次引用该Task么?', '', {
                      confirmButtonText: '确定',
                      cancelButtonText: '取消',
                      showClose: false
                    }).then(() => {
                      if (!isAddRightData) {
                        _slef.addNewJsPlumbIcon(clientObj)
                      } else {
                        _slef.$message({message: '右侧关系图已存在，请勿重复添加！', type: 'error'})
                      }
                    })
                  } else {
                    if (!isAddRightData) {
                      _slef.addNewJsPlumbIcon(clientObj)
                    } else {
                      _slef.$message({message: '右侧关系图已存在，请勿重复添加！', type: 'error'})
                    }
                  }
                })
              }
            }
          } else if (_slef.iconMouseDownState.state) {
            var icon = $('#' + _slef.jsPlumbJson[_slef.iconMouseDownState.num].taskId)
            // 节点Icon超出编辑区后触发
            if (_slef.iconMouseDownState.mouseup) {
              if (e.clientX < _slef.jsPlumbBoxBorders.left || e.clientY < _slef.jsPlumbBoxBorders.top) {
                _slef.removeAllJsPlumb()
                _slef.jsPlumbInstance()
              } else {
                var num = _slef.iconMouseDownState.num
                var data = {
                  'icon_ID': _slef.jsPlumbJson[num].taskId, // 节点IconID
                  'icon_left': e.clientX - $(_slef.jsPlumbBox).offset().left - _slef.mousedownClient.mousedownX + $(_slef.jsPlumbBox).scrollLeft(),
                  'icon_top': e.clientY - $(_slef.jsPlumbBox).offset().top - _slef.mousedownClient.mousedownY + $(_slef.jsPlumbBox).scrollTop()
                }
                if (!isNaN(data.icon_left)) {
                  // 自动对齐网格
                  data.icon_left = Math.round(Math.round(data.icon_left) / 10) * 10
                  data.icon_top = Math.round(Math.round(data.icon_top) / 10) * 10
                  _slef.jsPlumbJson[num].left = data.icon_left
                  _slef.jsPlumbJson[num].top = data.icon_top
                  _slef.editIconAjax(data, num)
                }
              }
            }
            _slef.mousedownClient = false
            $(icon).css({cursor: 'pointer'})
            _slef.iconMouseDownState.state = false
            _slef.iconMouseDownState.num = null
            _slef.iconMouseDownState.mouseup = true
          }
        }
        _slef.mouseDownState = false
        _slef.leftIconOffset = false
        _slef.mousedownClient = false
        $('#leftIconCopy').html('')
        $('#leftIconCopy').css({left: '0px', top: '0px', zIndex: 2, height: '0px'})
        // $('#nodeIconDelMenu').hide()
      })
    },
    substitute: function (str, object) {
      return str.replace(/\\?\{([^}]+)\}/g, function (match, name) {
        if (match.charAt(0) === '\\') return match.slice(1)
        return (object[name] !== undefined) ? object[name] : ''
      })
    }
  }
}
</script>
<style lang='less' scoped>
@import '../styles/job-manage-create.page.less';
</style>
<style lang='less'>
@import '../styles/job-manage-create.page.reset.less';
</style>
