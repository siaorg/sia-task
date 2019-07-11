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

import com.sia.config.web.constants.Constants;
import com.sia.config.web.filter.AuthInterceptor;
import com.sia.config.web.service.BasicJobService;
import com.sia.config.web.service.JobMTaskService;
import com.sia.config.web.service.RegistryService;
import com.sia.config.web.util.PageBean;
import com.sia.config.web.vo.JobPortrait;
import com.sia.core.curator.Curator4Scheduler;
import com.sia.core.entity.BasicJob;
import com.sia.core.entity.JobMTask;
import com.sia.core.helper.JSONHelper;
import com.sia.core.helper.StringHelper;
import com.sia.core.web.vo.ResultBody;
import org.quartz.CronExpression;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

/**
 * Job management interface API,
 * Provides various operations on jobs
 *
 * @author maozhengwei
 * @version V1.0.0
 * @date 2019-04-28 15:40
 * @see
 **/
@RestController
@RequestMapping("/jobapi")
public class JobController {

    private final static Logger LOGGER = LoggerFactory.getLogger(JobController.class);

    @Autowired
    private BasicJobService basicJobService;

    @Autowired
    private JobMTaskService jobMTaskService;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    protected AuthInterceptor userService;

    @Autowired
    protected Curator4Scheduler curator4Scheduler;

    @Autowired
    protected RegistryService registryService;

    /**
     * Perform cron expression validation
     *
     * @param cron cronExpression
     * @return
     */

    @RequestMapping(value = "/cronexpression", method = {RequestMethod.POST, RequestMethod.GET})
    @ResponseBody
    public String cronExpression(@RequestParam String cron) {
        if (cron == null) {
            return ResultBody.failed(ResultBody.ResultEnum.REQUEST_FAIL_PARAM.getCode(), ResultBody.ResultEnum.REQUEST_FAIL_PARAM.getMessage());
        }
        boolean valid = CronExpression.isValidExpression(cron);

        return valid ? ResultBody.success() : ResultBody.failed();
    }

    /**
     * The front end calls the interface
     * Job Status
     *
     * @param jobGroupName
     * @param jobKey
     * @return
     */
    @RequestMapping(value = "/selectJobStatus/{jobGroupName}/{jobKey}", method = {RequestMethod.POST, RequestMethod.GET})
    @ResponseBody
    public String selectJobStatus(@PathVariable String jobGroupName, @PathVariable String jobKey) {
        String jobStatus = registryService.getJobStatus(jobGroupName, jobKey);
        LOGGER.info(Constants.LOG_PREFIX + " selectJobStatus : jobStatus is {}, jobGroupName is{}, jobKey is {}", jobStatus, jobGroupName, jobKey);
        return ResultBody.success(jobStatus, ResultBody.ResultEnum.SUCCESS.getMessage());
    }

    /**
     * The front end calls the interface
     * Run the Job once
     *
     * @param jobGroupName
     * @param jobKey
     * @return
     */
    @RequestMapping(value = "/runOnceforweb/{jobGroupName}/{jobKey}", method = {RequestMethod.POST, RequestMethod.GET})
    @ResponseBody
    public String runOnce(@PathVariable String jobGroupName, @PathVariable String jobKey) {
        LOGGER.info(Constants.OPERATION_LOG_PREFIX + " runOnce : {}，{}", jobGroupName, jobKey);

        String userName = userService.getCurrentUser();
        List<String> jobScheduler = curator4Scheduler.getJobScheduler(jobGroupName, jobKey);
        if (!StringHelper.isEmpty(jobGroupName) && jobScheduler.size() == 1) {
            try {
                String urlStr = "http://" + jobScheduler.get(0) + "/jobapi/runOnce/" + jobGroupName + "/" + jobKey;
                ResponseEntity<String> responseEntity = restTemplate.getForEntity(urlStr, String.class);
                String result = responseEntity.getBody();
                LOGGER.info(Constants.OPERATION_LOG_PREFIX + "username is: " + userName + "; operation is: runOnce job,jobKey is： " + jobKey + "; jobScheduler is: " + jobScheduler.get(0));
                return "true".equals(result) ? ResultBody.success() : ResultBody.failed();
            } catch (Exception e) {
                LOGGER.error(Constants.LOG_PREFIX + " runOnce Job Exception : ", e);
                return ResultBody.error();
            }
        }
        LOGGER.info(Constants.OPERATION_LOG_PREFIX + "username is: " + userName + "operation is: runOnce job;" + " curator4Scheduler.getJobScheduler({},{}) --> List<String> jobScheduler is null or size != 1", jobGroupName, jobKey);
        return ResultBody.failed();
    }

    /**
     * The front end calls the interface
     * Activate the Job
     *
     * @param jobGroupName
     * @param jobKey
     * @return
     */
    @RequestMapping(value = "/activateJob/{jobGroupName}/{jobKey}", method = {RequestMethod.POST, RequestMethod.GET})
    @ResponseBody
    public String activateJob(@PathVariable String jobGroupName, @PathVariable String jobKey) {
        if (StringHelper.isEmpty(jobGroupName) || StringHelper.isEmpty(jobKey)) {
            LOGGER.info(Constants.LOG_PREFIX + " activate JOB IS FAIL : jobGroupName or jobKey , jobGroupName is{}, jobKey is {}", jobGroupName, jobKey);
            return ResultBody.failed(ResultBody.ResultEnum.REQUEST_FAIL_PARAM.getCode(), ResultBody.ResultEnum.REQUEST_FAIL_PARAM.getMessage());
        }
        //Check that Task is not configured to activate without configuration
        try {
            List<JobMTask> jobMTasks = jobMTaskService.selectByJobGroupAndKey(jobGroupName, jobKey);
            if (jobMTasks == null || jobMTasks.size() == 0) {
                return ResultBody.failed(ResultBody.ResultEnum.JOB_NO_TASK_CONFIG.getCode(), ResultBody.ResultEnum.JOB_NO_TASK_CONFIG.getMessage());
            }
            return basicJobService.activateJob(jobGroupName, jobKey) ? ResultBody.success() : ResultBody.failed();

        } catch (Exception e) {
            LOGGER.error(Constants.LOG_PREFIX + " activateJob Exception : ", e);
            return ResultBody.error();
        }


    }

    /**
     * The front end calls the interface
     * stop the Job
     *
     * @param jobGroupName
     * @param jobKey
     * @return
     */
    @RequestMapping(value = "/stopJob/{jobGroupName}/{jobKey}", method = {RequestMethod.POST, RequestMethod.GET})
    @ResponseBody
    public String stopJob(@PathVariable String jobGroupName, @PathVariable String jobKey) {

        String userName = userService.getCurrentUser();
        boolean deleteJobKey = registryService.deleteJobKey(jobGroupName, jobKey);
        LOGGER.info(Constants.OPERATION_LOG_PREFIX + "username is: " + userName + "; operation is: stop job,jobKey is：" + jobKey);

        return deleteJobKey ? ResultBody.success() : ResultBody.failed();
    }

    /**
     * Permissions are filtered based on the role's corresponding group name
     *
     * @return
     */
    @RequestMapping(value = "/selectAuth", method = {RequestMethod.POST, RequestMethod.GET})
    @ResponseBody
    public String selectAuth() {
        Map<String, List<String>> groupMap;
        try {
            List<String> roleNames = userService.getCurrentUserRoles();
            groupMap = basicJobService.selectAuth(roleNames);
        } catch (Exception e) {
            LOGGER.error(Constants.LOG_PREFIX + " select job auth Exception : ", e);
            return ResultBody.error();
        }
        return ResultBody.success(groupMap);
    }

    /**
     * Get the job list and support paging.
     * if jobAppName and jobGroupName = null ? return All : return filtration
     *
     * @param jobGroupName can be null
     * @param jobKey       can be null
     * @return
     */
    @RequestMapping(value = "/selectjobs", method = {RequestMethod.POST, RequestMethod.GET})
    @ResponseBody
    public String selectJobs(@RequestParam String jobGroupName, @RequestParam String jobKey, @RequestParam int currentPage, @RequestParam int pageSize) {
        PageBean<?> pageData;
        try {
            List<String> roleNames = userService.getCurrentUserRoles();
            pageData = basicJobService.selectByJobKeyAndJobGroup(jobGroupName, jobKey, currentPage, pageSize, roleNames);
        } catch (Exception e) {
            LOGGER.error(Constants.LOG_PREFIX + " select jobs Exception : ", e);
            return ResultBody.error();
        }
        return ResultBody.success(pageData);
    }

    /**
     * Job management gets the project name and the number of jobs
     */
    @RequestMapping(value = "/selectGroupAndJobCount", method = {RequestMethod.POST, RequestMethod.GET})
    @ResponseBody
    public String selectGroupAndJobCount(@RequestParam String jobGroupName) {
        List<Map<String, Integer>> groupAndCount;
        try {
            List<String> roleNames = userService.getCurrentUserRoles();
            groupAndCount = basicJobService.selectGroupAndJobCount(roleNames, jobGroupName);
        } catch (Exception e) {
            LOGGER.error(Constants.LOG_PREFIX + "selectGroupAndJobCount error");
            return ResultBody.error();
        }

        return ResultBody.success(groupAndCount);
    }

    /**
     * Save the Job according to the jobKey
     *
     * @param basicJob
     * @return
     */
    @PostMapping(value = "/insertJob")
    @ResponseBody
    public String insertJobByPrimaryKey(@RequestBody BasicJob basicJob) {
        int result;
        try {
            if (StringHelper.isEmpty(basicJob.getJobKey()) || StringHelper.isEmpty(basicJob.getJobGroup())) {
                return ResultBody.failed();
            } else if (!StringHelper.isGrammatical(basicJob.getJobKey(), Constants.REGEX) || !StringHelper.isGrammatical(basicJob.getJobGroup(), Constants.REGEX)) {
                return ResultBody.failed("输入的名称不符合规则，仅能包含数字、字母、下划线和中划线");
            }
            String userName = userService.getCurrentUser();
            result = basicJobService.insertSelective(basicJob);
            LOGGER.info(Constants.OPERATION_LOG_PREFIX + "username is: " + userName + "; operation is: insert job,jobKey is： " + basicJob.getJobKey());
        } catch (Exception e) {
            LOGGER.error(Constants.LOG_PREFIX + " insert job Exception : ", e);
            return ResultBody.error();
        }
        return result == 1 ? ResultBody.success() : ResultBody.failed(ResultBody.ResultEnum.JOB_ALREADY_EXISTED.getCode(), ResultBody.ResultEnum.JOB_ALREADY_EXISTED.getMessage());
    }

    /**
     * update Job
     * Modifying the Job's own attributes does not associate the Task
     *
     * @param basicJob
     * @return
     */
    @PostMapping("/updateJob")
    @ResponseBody
    public String updateJobByPrimaryKey(@RequestBody BasicJob basicJob) {
        int result = 0;
        String userName = userService.getCurrentUser();
        String jobStatus = registryService.getJobStatus(basicJob.getJobGroup(), basicJob.getJobKey());
        if (StringHelper.isEmpty(jobStatus)) {
            try {
                result = basicJobService.updateByPrimaryKey(basicJob);
                LOGGER.info(Constants.OPERATION_LOG_PREFIX + "username is: " + userName + "; operation is: update job,jobKey is; " + basicJob.getJobKey());
            } catch (Exception e) {
                LOGGER.error(Constants.LOG_PREFIX + " update job fail, job is {}", JSONHelper.toString(basicJob));
                LOGGER.error(" updateJobByPrimaryKey Exception:", e);
                return ResultBody.error();
            }
        }
        return result == 1 ? ResultBody.success() : ResultBody.failed();
    }

    /**
     * delete Job by primary jobKey
     *
     * @param jobKey
     * @return
     */
    @RequestMapping(value = "/deleteJobByJobKeyAndGroup", method = {RequestMethod.POST, RequestMethod.GET})
    @ResponseBody
    public String deleteJobByPrimaryKey(@RequestParam String jobGroupName, @RequestParam String jobKey) {
        int result;
        String userName = userService.getCurrentUser();
        if (StringHelper.isEmpty(jobGroupName) || StringHelper.isEmpty(jobKey)) {
            return ResultBody.failed();
        }
        try {
            //Delete the Job from ZK
            registryService.deleteJobKey(jobGroupName, jobKey);
            //Delete the Job from DB
            result = basicJobService.deleteJobByJobKeyAndJobGroup(jobGroupName, jobKey);
            LOGGER.info(Constants.OPERATION_LOG_PREFIX + "username is: " + userName + "; operation is: delete job,jobKey is: " + jobKey);

        } catch (Exception e) {
            LOGGER.error(Constants.LOG_PREFIX + " remove job Exception : ", e);
            return ResultBody.error();
        }
        return result == 1 ? ResultBody.success() : ResultBody.failed();
    }

    /**
     * Gets an overview of the tasks in scheduling monitoring.
     *
     * @return taskView
     */
    @RequestMapping(value = "/selectTaskView", method = {RequestMethod.POST, RequestMethod.GET})
    @ResponseBody
    public String selectTaskView() {
        List<String> roleNames = userService.getCurrentUserRoles();
        Map<String, Integer> taskView = basicJobService.selectTaskView(roleNames);
        return ResultBody.success(taskView);
    }

    /**
     * Get the number of scheduling monitoring projects, tasks, and jobs.
     *
     * @return summary
     */
    @RequestMapping(value = "/selectSummary", method = {RequestMethod.POST, RequestMethod.GET})
    @ResponseBody
    public String selectSummary() {
        List<String> roleNames = userService.getCurrentUserRoles();
        Map<String, Integer> summary = basicJobService.selectSummary(roleNames);
        return ResultBody.success(summary);
    }

    /**
     * The scheduling monitoring page filters by project name and gets the scheduler running details.
     *
     * @param jobGroupName
     * @return job and scheduler running details
     */
    @RequestMapping(value = "/jobGroupPortrait", method = {RequestMethod.POST, RequestMethod.GET})
    @ResponseBody
    public String selectJobGroupPortrait(@RequestParam String jobGroupName) {
        List<JobPortrait> jobPortraitList;
        try {
            List<String> roleNames = userService.getCurrentUserRoles();
            jobPortraitList = basicJobService.selectJobGroupPortrait(jobGroupName, roleNames);
        } catch (Exception e) {
            LOGGER.error(Constants.LOG_PREFIX + " selectJobGroupPortrait Exception : ", e);
            return ResultBody.error();
        }
        return ResultBody.success(jobPortraitList);
    }

    /**
     * Gets the list of jobs belong to the scheduler
     *
     * @param scheduler
     * @return
     */
    @RequestMapping(value = "/getJobList", method = {RequestMethod.POST, RequestMethod.GET})
    @ResponseBody
    public String getJobListByScheduler(@RequestParam String scheduler) {
        if (StringHelper.isEmpty(scheduler)) {
            return ResultBody.failed();
        }
        List jobList;
        try {
            jobList = basicJobService.getJobListByScheduler(scheduler);
        } catch (Exception e) {
            LOGGER.error(Constants.LOG_PREFIX + " getJobListByScheduler Exception : ", e);
            return ResultBody.error();
        }
        return ResultBody.success(jobList);
    }

    /**
     * Set cascade Job JobPlan,
     * set cascade jobs, and the dependencies between jobs can be realized with specific timing cycles,
     * which can be divided into pre and post jobs
     *
     * @param basicJob
     * @return
     */
    @RequestMapping(value = "/updatejobplan", method = {RequestMethod.POST, RequestMethod.GET})
    @ResponseBody
    public String updateJobPlan(@RequestBody BasicJob basicJob) {
        String userName = userService.getCurrentUser();
        String jobStatus = registryService.getJobStatus(basicJob.getJobGroup(), basicJob.getJobKey());
        if (!StringHelper.isEmpty(jobStatus)) {
            return ResultBody.failed("Job 已激活，请关闭Job 再进行配置！");
        }
        try {
            String jobPlan = basicJob.getJobPlan();
            //jobPlan = null ? delete/create : update
            if (StringHelper.isEmpty(jobPlan)) {

                BasicJob jobChild = basicJob.getJobChild();

                if (jobChild != null) {
                    if (!StringHelper.isEmpty(jobChild.getJobPlan())) {
                        return ResultBody.failed("后置Job[" + jobChild.getJobKey() + "] 已被其它Job关联！！");
                    }
                    //create
                    basicJobService.updateJobPlanByJobKey(basicJob);
                    LOGGER.info(Constants.OPERATION_LOG_PREFIX + "username is: " + userName + "; operation is:updateJobPlan [add],jobKey is; " + basicJob.getJobKey());

                } else {
                    //delete
                    basicJobService.removeJobPlan(basicJob);
                    LOGGER.info(Constants.OPERATION_LOG_PREFIX + "username is: " + userName + "; operation is:updateJobPlan [remove],jobKey is; " + basicJob.getJobKey());
                }

            } else {
                //update
                basicJobService.removeJobPlan(basicJob);
                basicJobService.updateJobPlanByJobKey(basicJob);
                LOGGER.info(Constants.OPERATION_LOG_PREFIX + "username is: " + userName + "; operation is:updateJobPlan [update],jobKey is; " + basicJob.getJobKey());

            }

            return ResultBody.success("Job 级联配置成功！");

        } catch (Exception e) {
            LOGGER.error(Constants.LOG_PREFIX + " updateJobPlan job fail, job is {}", JSONHelper.toString(basicJob));
            LOGGER.error(" updateJobPlan Exception:", e);
            return ResultBody.error();
        }
    }


    /**
     * Job batch transfer with one key
     * When the scheduler is switched,
     * the original scheduler needs to be offline,
     * the Job on the scheduler needs to be stopped,
     * then activated, and transferred to the newly online scheduler,
     * and the Job batch transfer result needs to be generated at the same time.
     *
     * @param scheduler
     * @return the Job batch transfer result
     */
    @RequestMapping(value = "/batchJobTransfer", method = {RequestMethod.POST, RequestMethod.GET})
    @ResponseBody
    public String batchJobTransfer(@RequestBody String scheduler) {
        if (StringHelper.isEmpty(scheduler)) {
            return ResultBody.failed(ResultBody.ResultEnum.REQUEST_FAIL_PARAM.getCode(), ResultBody.ResultEnum.REQUEST_FAIL_PARAM.getMessage());
        }
        Map<String, Object> resultMap;
        try {
            resultMap = basicJobService.batchJobTransfer(scheduler);
            if (resultMap.isEmpty()) {
                return ResultBody.failed(ResultBody.ResultEnum.ERROR.getCode(), scheduler + "调度器下线失败");
            }
        } catch (Exception e) {
            LOGGER.error(Constants.LOG_PREFIX + " batchJobTransfer Exception : ", e);
            return ResultBody.error();
        }
        return ResultBody.success(resultMap);
    }

    /**
     * The front end calls the interface
     * get batch transfer information with one key of Job
     *
     * @return
     */
    @RequestMapping(value = "/JobTransferInfo", method = {RequestMethod.POST, RequestMethod.GET})
    @ResponseBody
    public String getJobTransferInfo(@RequestParam String scheduler) {
        if (StringHelper.isEmpty(scheduler)) {
            return ResultBody.failed(ResultBody.ResultEnum.REQUEST_FAIL_PARAM.getCode(), ResultBody.ResultEnum.REQUEST_FAIL_PARAM.getMessage());
        }
        String JobTransferInfo;
        try {
            JobTransferInfo = curator4Scheduler.getJobTransferInfo(scheduler);
            LOGGER.info(Constants.LOG_PREFIX + " 获取一键Job转移信息 >>> 成功 :  schedulerInfo is{} ", JobTransferInfo);
        } catch (Exception e) {
            LOGGER.error(Constants.LOG_PREFIX + " getJobTransferInfo Error ：", e);
            return ResultBody.error();
        }
        return ResultBody.success((Object) JobTransferInfo);
    }

}
