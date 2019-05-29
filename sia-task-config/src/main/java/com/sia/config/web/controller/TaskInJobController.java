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
import com.sia.core.entity.JobMTask;
import com.sia.core.helper.JSONHelper;
import com.sia.core.web.vo.JobMTaskVO;
import com.sia.core.web.vo.ResultBody;
import com.sia.config.web.constants.Constants;
import com.sia.config.web.filter.AuthInterceptor;
import com.sia.config.web.service.JobMTaskService;
import com.sia.config.web.util.OnlineJobDagCheck;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * The TASK arranging
 *
 * @author maozhengwei
 * @version V1.0.0
 * @date 2019-04-28 15:40
 * @see
 **/
@RestController
@RequestMapping("/taskinjobapi")
public class TaskInJobController {

    private final static Logger LOGGER = LoggerFactory.getLogger(TaskInJobController.class);

    @Autowired
    private JobMTaskService jobMTaskService;

    @Autowired
    protected AuthInterceptor userService;

    @Autowired
    protected Curator4Scheduler curator4Scheduler;

    @RequestMapping(value = "/selectTaskByJobKey", method = {RequestMethod.POST, RequestMethod.GET})
    @ResponseBody
    public String selectTaskByJobKey(@RequestParam String jobGroup, @RequestParam String jobKey) {
        List<JobMTaskVO> jobMTasks = jobMTaskService.selectTaskMJobAndIPListByJobGroupAndKey(jobGroup, jobKey);
        return ResultBody.success(jobMTasks);
    }

    /**
     * Task dependency graph of Job
     *
     * @param jobGroup
     * @param jobKey
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/selectTaskDependencyByJobKey", method = {RequestMethod.POST, RequestMethod.GET})
    @ResponseBody
    public String selectTaskDependencyByJobKey(@RequestParam String jobGroup, @RequestParam String jobKey) {
        List<JobMTaskVO> jobMTasks = jobMTaskService.selectTaskMJobAndIPListByJobGroupAndKey(jobGroup, jobKey);
        List<JobMTaskVO> jobMTaskVOList = null;
        if (jobMTasks.size() > 0) {
            jobMTaskVOList = jobMTaskService.analyticalTask(jobMTasks);
        }
        return ResultBody.success(jobMTaskVOList);
    }


    /**
     * insert or update task in a job
     *
     * @param jobMTaskList
     * @return String : success or fail
     */
    @PostMapping("/inserttaskinjob")
    @ResponseBody
    public String insertTaskInJob(@RequestBody List<JobMTask> jobMTaskList) {
        int activeCount;
        String jobGroupName = jobMTaskList.get(0).getJobGroup();
        String jobKey = jobMTaskList.get(0).getJobKey();
        try {
            //DAG Check
            List<String> list = OnlineJobDagCheck.doDagCheck(jobMTaskList);
            if (list != null) {
                LOGGER.info("dag check fail：error task {}", JSONHelper.toString(list));
                return ResultBody.failed(ResultBody.ResultEnum.DAG_CHECK.getCode(), ResultBody.ResultEnum.DAG_CHECK.getMessage());
            }

            String userName = userService.getCurrentUser();
            List<JobMTask> jobMTasks = jobMTaskService.selectByJobGroupAndKey(jobGroupName, jobKey);
            if (jobMTasks.size() > 0) {
                //  delete the orchestration relationship
                jobMTaskService.deleteByJobKeyAndJobGroup(jobGroupName, jobKey);
            }

            activeCount = jobMTaskService.insertSelective(jobMTaskList);
            LOGGER.info(Constants.OPERATION_LOG_PREFIX + "username is: " + userName + "; operation is: insert task in job,jobKey is: " + jobKey);

            if (activeCount != jobMTaskList.size()) {
                LOGGER.info(Constants.LOG_PREFIX + " task relationship insert fail,task list {}", JSONHelper.toString(jobMTaskList));
                return ResultBody.failed();
            }
        } catch (Exception e) {
            LOGGER.error(Constants.LOG_PREFIX + " task relationship insert fail,Exception : ", e);
            return ResultBody.error();
        }
        return jobMTaskList.size() == activeCount ? ResultBody.success() : ResultBody.failed();
    }

}
