/*-
 * <<
 * task
 * ==
 * Copyright (C) 2019 - 2020 sia
 * ==
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * >>
 */

package com.sia.task.admin.controller;

import com.sia.task.admin.service.BasicTask4Service;
import com.sia.task.admin.vo.SiaResponseBody;
import com.sia.task.core.entity.BasicTask;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.Map;

/**
 * TASK 管理
 *
 * @author maozhengwei
 * @version V1.0.0
 * @data 2018/4/19 12:39 下午
 **/
@RestController
@RequestMapping("/taskapi")
public class BasicTaskController {

    @Resource
    private BasicTask4Service basicTask4Service;

    /**
     * 查询项目组
     * 返回当前用户权限内的项目组
     *
     * @return
     */
    @RequestMapping(value = "/selectAuth", method = RequestMethod.GET)
    @Deprecated
    public String selectGroups4Auth() {
        return SiaResponseBody.success(basicTask4Service.selectGroupsByAuth());
    }

    /**
     * 根据项目组查询指定项目内的全部应用名称
     *
     * @param groupName
     * @return
     */
    @RequestMapping(value = "/selectappsbygroup", method = RequestMethod.GET)
    public String selectAppsByGroup(String groupName) {
        return SiaResponseBody.success(basicTask4Service.selectAppsByGroup(groupName));
    }

    /**
     * 查询taskKey通过appName和groupName
     *
     * @param groupName
     * @param appName
     * @return
     */
    @RequestMapping(value = "/selecttaskkeys", method = RequestMethod.GET)
    public String selectTaskKeys(String groupName, String appName) {
        return SiaResponseBody.success(basicTask4Service.selectTaskKeysByGroupAndApp(groupName, appName));
    }

    /**
     * 检查网络连接
     *
     * @param host
     * @return
     */
    @RequestMapping(value = "/checkPingTelnet", method = RequestMethod.GET)
    public String checkNetworkConnectivity(@RequestParam String host) {
        return SiaResponseBody.isOk(basicTask4Service.checkNetworkConnectivity(host));
    }

    /**
     * 查看Task被关联的Job
     *
     * @param taskKey
     * @return
     */
    @RequestMapping(value = "/selectTaskInJob", method = RequestMethod.GET)
    public String selectTaskInJob(@RequestParam String taskAppName, @RequestParam String taskGroupName, @RequestParam String taskKey) {
        return SiaResponseBody.success(basicTask4Service.selectTask4Job(taskAppName, taskGroupName, taskKey));
    }

    /**
     * 获取task列表 包含执行器信息
     * 分页查询接口
     *
     * @param taskAppName
     * @param taskGroupName
     * @param taskKey
     * @param currentPage
     * @param pageSize
     * @return
     */
    @RequestMapping(value = "/selectTasksByPage", method = RequestMethod.GET)
    public String selectTasks4Page(@RequestParam String taskAppName,
                                   @RequestParam String taskGroupName,
                                   @RequestParam String taskKey,
                                   String taskDesc,
                                   @RequestParam int currentPage,
                                   @RequestParam int pageSize) throws Exception {
        return SiaResponseBody.success(basicTask4Service.selectTasks4Page(taskAppName, taskGroupName, taskKey, taskDesc, currentPage, pageSize));
    }

    /**
     * 获取task列表 不包含执行器信息
     *
     * @param taskAppName
     * @param taskGroupName
     * @param taskKey
     * @return
     */
    @RequestMapping(value = "/selectTasks", method = RequestMethod.GET)
    public String selectTasks4WithoutActuators(@RequestParam String taskAppName, @RequestParam String taskGroupName, @RequestParam String taskKey) throws Exception {
        return SiaResponseBody.success(basicTask4Service.selectTasks4WithoutActuators(taskAppName, taskGroupName, taskKey));
    }

    /**
     * 任务管理界面得到项目名称和拥有的task数量
     * TODO 逻辑存在问题，虽然不影响结果，但是语义不正确
     */
    @RequestMapping(value = "/selectGroupAndCount", method = RequestMethod.GET)
    @ResponseBody
    public String selectTaskCount4Group(@RequestParam String taskGroupName) {
        return SiaResponseBody.success(basicTask4Service.selectTaskCount4Group(taskGroupName));
    }

    /**
     * 保存BasicTask
     *
     * @param basicTask
     * @return
     */
    @PostMapping("/insertTask")
    @ResponseBody
    public String insertTask(@RequestBody BasicTask basicTask) {
        return SiaResponseBody.isOk(basicTask4Service.saveBasicTask(basicTask));
    }

    /**
     * 前端API updateTask
     *
     * @param basicTask
     * @return
     */
    @PostMapping("/updateTask")
    @ResponseBody
    public String updateBasicTask(@RequestBody BasicTask basicTask) {
        return SiaResponseBody.isOk(basicTask4Service.updateBasicTask(basicTask));
    }

    /**
     * 删除Task
     *
     * @param taskKey
     * @return
     */
    @RequestMapping(value = "/deleteTaskByPrimaryKey", method = RequestMethod.GET)
    public String deleteBasicTask(@RequestParam String taskAppName, @RequestParam String taskGroupName, @RequestParam String taskKey) {
        return SiaResponseBody.isOk(basicTask4Service.deleteBasicTask(taskAppName, taskGroupName, taskKey));
    }

    /**
     * 获取指定Task的执行器实例列表
     *
     * @param basicTask
     * @return
     */
    @PostMapping("/getExecutorList")
    @ResponseBody
    public String getExecutors4Task(@RequestBody BasicTask basicTask) {
        return SiaResponseBody.success(basicTask4Service.getExecutors4Task(basicTask));
    }

    /**
     * Task联通性测试
     */
    @RequestMapping(value = "/connextest", produces = "application/json;charset=UTF-8", method = RequestMethod.POST)
    @ResponseBody
    public String connectivityTest4Task(@RequestBody Map<String, String> request) {
        return SiaResponseBody.success(basicTask4Service.connectivityTest4Task(request), "Task联通性测试");
    }

}
