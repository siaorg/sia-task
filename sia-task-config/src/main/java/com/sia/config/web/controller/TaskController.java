/*-
 * <<
 * task
 * ==
 * Copyright (C) 2019 sia
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

package com.sia.config.web.controller;

import com.sia.core.curator.Curator4Scheduler;
import com.sia.core.entity.BasicTask;
import com.sia.core.entity.JobMTask;
import com.sia.core.helper.NetHelper;
import com.sia.core.helper.StringHelper;
import com.sia.core.web.vo.ResultBody;
import com.sia.config.web.constants.Constants;
import com.sia.config.web.filter.AuthInterceptor;
import com.sia.config.web.service.BasicTaskService;
import com.sia.config.web.service.JobMTaskService;
import com.sia.config.web.util.PageBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * The orchestration center calls TASK's API class
 *
 * @author maozhengwei
 * @version V1.0.0
 * @date 2019-04-28 15:40
 * @see
 **/
@RestController
@RequestMapping("/taskapi")
public class TaskController {

    private final static Logger LOGGER = LoggerFactory.getLogger(TaskController.class);

    @Autowired
    private BasicTaskService basicTaskService;

    @Autowired
    private JobMTaskService jobMTaskService;

    @Autowired
    protected AuthInterceptor userService;

    @Autowired
    protected Curator4Scheduler curator4Scheduler;


    /**
     * Permissions are filtered based on the role's corresponding group name
     *
     * @return
     */
    @RequestMapping(value = "/selectAuth", method = {RequestMethod.POST, RequestMethod.GET})
    @ResponseBody
    public String selectAuth() {
        List<String> groupList;
        try {
            List<String> roleNames = userService.getCurrentUserRoles();
            groupList = basicTaskService.selectGroupsByAuth(roleNames);
        } catch (Exception e) {
            LOGGER.error(Constants.LOG_PREFIX + "selectAuth Error", e);
            return ResultBody.error();
        }
        return ResultBody.success(groupList);
    }

    /**
     * Get the corresponding App name according to the group and conduct permission filtering
     *
     * @param groupName
     * @return
     */
    @RequestMapping(value = "/selectappsbygroup", method = {RequestMethod.POST, RequestMethod.GET})
    @ResponseBody
    public String selectAppsByGroup(String groupName) {
        List<String> apps;
        try {
            apps = basicTaskService.selectAppsByGroup(groupName);
        } catch (Exception e) {
            LOGGER.error(Constants.LOG_PREFIX + "selectAppsByGroup ", e);
            return ResultBody.error();
        }
        return ResultBody.success(apps);
    }

    /**
     * According to the group, the App gets the corresponding TaskKey name for permission filtering
     *
     * @param groupName
     * @param appName
     * @return
     */
    @RequestMapping(value = "/selecttaskkeys", method = {RequestMethod.POST, RequestMethod.GET})
    @ResponseBody
    public String selectTaskKeys(String groupName, String appName) {
        if (StringHelper.isEmpty(groupName)) {
            return ResultBody.failed(ResultBody.ResultEnum.REQUEST_FAIL_PARAM.getCode(), ResultBody.ResultEnum.REQUEST_FAIL_PARAM.getMessage());
        }
        List<String> taskKeys;
        try {
            taskKeys = basicTaskService.selectTaskKeysByGroupAndApp(groupName, appName);
        } catch (Exception e) {
            LOGGER.error(Constants.LOG_PREFIX + "selectTaskKeys ", e);
            return ResultBody.error();
        }
        return ResultBody.success(taskKeys);
    }

    /**
     * check Ping Telnet
     *
     * @param host
     * @return
     */
    @RequestMapping(value = "/checkPingTelnet", method = {RequestMethod.POST, RequestMethod.GET})
    @ResponseBody
    public String checkPingTelnet(@RequestParam String host) {
        boolean result;
        String[] split = host.split(Constants.REGEX_COLON);
        switch (split.length) {
            case 1:
                result = NetHelper.ping(split[0]);
                break;
            case 2:
                result = NetHelper.ping(split[0]) && NetHelper.telnet(split[0], Integer.valueOf(split[1]));
                break;
            default:
                result = false;
        }
        return result ? ResultBody.success() : ResultBody.failed();
    }

    /**
     * check Task In Job
     *
     * @param taskKey
     * @return
     */
    @RequestMapping(value = "/selectTaskInJob", method = {RequestMethod.POST, RequestMethod.GET})
    @ResponseBody
    public String selectTaskInJob(@RequestParam String taskAppName, @RequestParam String taskGroupName, @RequestParam String taskKey) {
        List<JobMTask> jobMTasks = basicTaskService.selectTaskInJob(taskAppName, taskGroupName, taskKey);
        return ResultBody.success(jobMTasks);
    }

    /**
     * Front-end paging interface
     *
     * @param taskAppName
     * @param taskGroupName
     * @param taskKey
     * @param currentPage
     * @param pageSize
     * @return
     */
    @RequestMapping(value = "/selectTasksByPage", method = {RequestMethod.POST, RequestMethod.GET})
    @ResponseBody
    public String selectTasks(@RequestParam String taskAppName, @RequestParam String taskGroupName, @RequestParam String taskKey, @RequestParam int currentPage, @RequestParam int pageSize) {
        PageBean<?> pageData;
        try {
            List<String> roleNames = userService.getCurrentUserRoles();
            pageData = basicTaskService.selectTasksByAppNameOrGroupName(taskAppName, taskGroupName, taskKey, currentPage, pageSize, roleNames);
        } catch (Exception e) {
            LOGGER.error(Constants.LOG_PREFIX + " selectTasksByPage Exception", e);
            return ResultBody.error();
        }
        return ResultBody.success(pageData);
    }

    /**
     * get task List
     *
     * @param taskAppName
     * @param taskGroupName
     * @param taskKey
     * @return
     */
    @RequestMapping(value = "/selectTasks", method = {RequestMethod.POST, RequestMethod.GET})
    @ResponseBody
    public String selectTasks(@RequestParam String taskAppName, @RequestParam String taskGroupName, @RequestParam String taskKey) {
        List<BasicTask> basicTaskList;
        try {
            List<String> roleNames = userService.getCurrentUserRoles();
            basicTaskList = basicTaskService.selectTasksByAppNameOrGroupNameNoIpPort(taskAppName, taskGroupName, taskKey, roleNames);
        } catch (Exception e) {
            LOGGER.error(Constants.LOG_PREFIX + " selectTasksByPage Exception", e);
            return ResultBody.error();
        }
        return ResultBody.success(basicTaskList);
    }

    /**
     * The task management interface gets the project name and number of tasks owned
     *
     * @param taskGroupName
     */
    @RequestMapping(value = "/selectGroupAndCount", method = {RequestMethod.POST,RequestMethod.GET})
    @ResponseBody
    public String selectGroupCount(@RequestParam String taskGroupName) {
        List<Map<String, Integer>> groupAndCount;
        try {
            List<String> roleNames = userService.getCurrentUserRoles();
            groupAndCount = basicTaskService.selectGroupAndCount(roleNames, taskGroupName);
        } catch (Exception e) {
            LOGGER.error(Constants.LOG_PREFIX + "selectGroupAndCount error");
            return ResultBody.error();
        }

        return ResultBody.success(groupAndCount);
    }

    /**
     * front-end calls the API
     * insert Task
     *
     * @param basicTask
     * @return
     */
    @PostMapping("/insertTask")
    @ResponseBody
    public String insertTask(@RequestBody BasicTask basicTask) {
        int result = 0;
        if (basicTask == null || StringHelper.isEmpty(basicTask.getTaskAppName())
                || StringHelper.isEmpty(basicTask.getTaskAppHttpPath())
                || StringHelper.isEmpty(basicTask.getTaskGroupName())) {
            LOGGER.warn(Constants.LOG_PREFIX + " insert task fail :  basicTask invalid, basicTask={}", basicTask);
            return ResultBody.failed(ResultBody.ResultEnum.REQUEST_FAIL_PARAM.getCode(), ResultBody.ResultEnum.REQUEST_FAIL_PARAM.getMessage());
        } else if (!StringHelper.isGrammatical(basicTask.getTaskGroupName(), Constants.REGEX) || !StringHelper.isGrammatical(basicTask.getTaskAppName(), Constants.REGEX)) {
            return ResultBody.failed("输入的名称不符合规则，仅能包含数字、字母、下划线和中划线");
        } else {
            try {
                String userName = userService.getCurrentUser();
                basicTask.setTaskSource(Constants.TASK_SOURCE_UI);
                basicTask.setTaskKey(basicTask.getTaskAppName() + Constants.REGEX_COLON + basicTask.getTaskAppHttpPath());
                result = basicTaskService.insertSelective(basicTask);
                LOGGER.info(Constants.OPERATION_LOG_PREFIX + "username is: " + userName + "; operation is: insert task,taskKey is " + basicTask.getTaskKey());
            } catch (Exception e) {
                LOGGER.error(Constants.LOG_PREFIX + " insertTask Exception", e);
                return ResultBody.error();
            }

        }

        return result == 1 ? ResultBody.success() : ResultBody.failed(ResultBody.ResultEnum.TASK_ALREADY_EXISTED.getCode(), ResultBody.ResultEnum.TASK_ALREADY_EXISTED.getMessage());
    }

    /**
     * front-end calls the API
     * updateTask
     *
     * @param basicTask
     * @return
     */
    @PostMapping("/updateTask")
    @ResponseBody
    public String updateTaskByPrimaryKey(@RequestBody BasicTask basicTask) {
        int result = 0;
        if (basicTask == null || StringHelper.isEmpty(basicTask.getTaskAppName())
                || StringHelper.isEmpty(basicTask.getTaskAppHttpPath())
                || StringHelper.isEmpty(basicTask.getTaskGroupName())) {
            LOGGER.warn(Constants.LOG_PREFIX + " update task fail :  basicTask={}", basicTask);
            return ResultBody.failed(ResultBody.ResultEnum.REQUEST_FAIL_PARAM.getCode(), ResultBody.ResultEnum.REQUEST_FAIL_PARAM.getMessage());
        } else {
            try {
                String userName = userService.getCurrentUser();
                basicTask.setTaskSource(Constants.TASK_SOURCE_UI);
                basicTask.setTaskKey(basicTask.getTaskAppName() + Constants.REGEX_COLON + basicTask.getTaskAppHttpPath());
                result = basicTaskService.updateByPrimaryKeySelective(basicTask);
                LOGGER.info(Constants.OPERATION_LOG_PREFIX + "username is: " + userName + "; operation is: update task,taskKey is " + basicTask.getTaskKey());
            } catch (Exception e) {
                LOGGER.error(Constants.LOG_PREFIX + " updateTaskByPrimaryKey Exception : ", e);
                return ResultBody.error();
            }
        }
        return result == 1 ? ResultBody.success() : ResultBody.failed();
    }

    /**
     * front-end calls the API
     * delete Task
     *
     * @param taskKey
     * @return
     */
    @RequestMapping(value = "/deleteTaskByPrimaryKey", method = {RequestMethod.POST,RequestMethod.GET})
    @ResponseBody
    public String deleteTaskByPrimaryKey(@RequestParam String taskAppName, @RequestParam String taskGroupName, @RequestParam String taskKey) {
        int result = 0;
        if (StringHelper.isEmpty(taskAppName) && StringHelper.isEmpty(taskGroupName) && StringHelper.isEmpty(taskKey)) {
            LOGGER.error(Constants.LOG_PREFIX + " delete task fail : taskKey or taskGroupName or taskKey invalid, taskAppName={},taskGroupName={},taskKey={}", taskAppName, taskGroupName, taskKey);
        } else {
            String userName;
            try {
                userName = userService.getCurrentUser();
                //判断是否存在引用
                List<JobMTask> jobMTasks = jobMTaskService.selectJobMTaskTaskKey(taskKey);
                if (jobMTasks != null && jobMTasks.size() > 0) {
                    LOGGER.error(Constants.LOG_PREFIX + "delete task fail : task_referenced_job. taskAppName={},taskGroupName={},taskKey={}", taskAppName, taskGroupName, taskKey);
                    return ResultBody.failed(ResultBody.ResultEnum.TASK_CHECK.getCode(), ResultBody.ResultEnum.TASK_CHECK.getMessage());
                }
                result = basicTaskService.deleteByPrimaryKey(taskAppName, taskGroupName, taskKey);
                curator4Scheduler.deleteTaskKey(taskKey);
                LOGGER.info(Constants.OPERATION_LOG_PREFIX + "username is: " + userName + "; operation is: delete task,taskKey is " + taskKey);

            } catch (Exception e) {
                LOGGER.error(Constants.LOG_PREFIX + " deleteTaskByPrimaryKey Exception : ", e);
                return ResultBody.error();
            }
        }
        return result == 1 ? ResultBody.success() : ResultBody.failed();
    }

    /**
     * front-end calls the API
     * insert or update by taskKey
     *
     * @param basicTask
     * @return
     */
    @PostMapping("/insertOrUpdateByTaskKey")
    @ResponseBody
    public String insertOrUpdateByTaskKey(@RequestBody BasicTask basicTask) {
        int result = 0;
        if (basicTask == null) {
            LOGGER.warn(Constants.LOG_PREFIX + " update task fail :  basicTask={}", basicTask);
        } else {
            try {
                String userName = userService.getCurrentUser();
                basicTask.setTaskSource(Constants.TASK_SOURCE_UI);
                basicTask.setTaskKey(basicTask.getTaskAppName() + Constants.REGEX_COLON + basicTask.getTaskAppHttpPath());
                result = basicTaskService.insertOrUpdateByTaskKey(basicTask);
                LOGGER.info(Constants.OPERATION_LOG_PREFIX + "username is: " + userName + "; operation is: insert or update task,taskKey is " + basicTask.getTaskKey());
            } catch (Exception e) {
                LOGGER.error(Constants.LOG_PREFIX + " insertOrUpdateByTaskKey Exception : ", e);
                return ResultBody.error();
            }

        }

        return result == 1 ? ResultBody.success() : ResultBody.failed();
    }

    /**
     * front-end calls the API
     * Get the list of executor instances based on basicTask
     *
     * @param basicTask
     * @return
     */
    @PostMapping("/getExecutorList")
    @ResponseBody
    public String getExecutorList(@RequestBody BasicTask basicTask) {
        String executorList = null;
        if (basicTask == null) {
            LOGGER.warn(Constants.LOG_PREFIX + " getExecutorList fail :  basicTask={}", basicTask);
        } else {
            try {
                executorList = basicTaskService.getExecutorList(basicTask);
            } catch (Exception e) {
                LOGGER.error(Constants.LOG_PREFIX + " getExecutorList Exception : ", e);
                return ResultBody.error();
            }
        }
        return ResultBody.success((Object) executorList);
    }

    /**
     * front-end calls the API
     * Task connectivity test
     * For tasks that use the sia-task-hunter component and achieve automatic fetching
     * through standard annotations,sia-task-hunter adds permission control.
     * IP that is not within this permission cannot call the task.
     * If it is necessary to test whether the task can work normally (POST only)
     *
     * @param request
     * @return
     */
    @RequestMapping(value = "/connextest", produces = "application/json;charset=UTF-8", method = {RequestMethod.POST,RequestMethod.GET})
    @ResponseBody
    public String connexTest(@RequestBody Map<String, String> request) {
        if (request == null) {
            LOGGER.info(Constants.LOG_PREFIX + "connextest by user:" + userService.getCurrentUser());
            return ResultBody.failed();
        }
        String param = request.get("param");
        String result;
        try {
            String url = request.get("url");
            LOGGER.info(Constants.LOG_PREFIX + "connextest by user:" + userService.getCurrentUser());
            LOGGER.info(Constants.LOG_PREFIX + "url：" + url);
            LOGGER.info(Constants.LOG_PREFIX + "param: " + param);
            result = basicTaskService.testTask(url, param);
        } catch (Exception e) {
            LOGGER.error(Constants.LOG_PREFIX + " connexTest Error ：", e);
            return ResultBody.error();
        }
        return ResultBody.success((Object) result);
    }

}
