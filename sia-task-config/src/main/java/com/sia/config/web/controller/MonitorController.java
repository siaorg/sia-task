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
import com.sia.core.helper.JSONHelper;
import com.sia.core.web.vo.ResultBody;
import com.sia.config.web.constants.Constants;
import com.sia.config.web.filter.AuthInterceptor;
import com.sia.config.web.service.JobMTaskService;
import com.sia.config.web.service.PortalStatisticsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author: MAOZW
 * @Description: MonitorController
 * @date 2018/4/1911:24
 */

/**
 * Monitoring interface,
 * the operation of some monitoring indicators
 * @see
 * @author maozhengwei
 * @date 2019-04-28 15:40
 * @version V1.0.0
 **/
@RestController
@RequestMapping("/monitor")

public class MonitorController {

    private Logger logger = LoggerFactory.getLogger(MonitorController.class);

    @Autowired
    private JobMTaskService jobMTaskService;

    @Autowired
    protected AuthInterceptor userService;

    @Autowired
    protected Curator4Scheduler curator4Scheduler;

    @Autowired
    PortalStatisticsService portalStatisticsService;


    @RequestMapping(value = "/",method = RequestMethod.GET)
    public String index() {
        return "index.html";
    }

    /**
     * Front-end call interface
     * number of schedulers
     * Through the interaction with ZK, get the information of the scheduler
     *
     * @return
     */
    @RequestMapping(value = "/schedulers",method = RequestMethod.GET)
    @ResponseBody
    public String getSchedulers() {

        List<String> schedulers = curator4Scheduler.getSchedulers();
        return ResultBody.success(schedulers);
    }

    /**
     * Front-end call interface
     * get the information that the scheduler has
     *
     * @return
     */
    @RequestMapping(value = "/schedulerInfo",method = RequestMethod.GET)
    @ResponseBody
    public String getSchedulerInfo() {

        Map<String, String> schedulerInfo = new HashMap<>();
        List<String> schedulers = curator4Scheduler.getSchedulers();

        for (String scheduler : schedulers) {
            String info = curator4Scheduler.getSchedulerInfo(scheduler);
            schedulerInfo.put(scheduler, info);
        }
        logger.info(Constants.LOG_PREFIX + " Gets the scheduler instance information >>> success :  schedulerInfo is{}", JSONHelper.toString(schedulerInfo));
        return ResultBody.success(schedulerInfo);

    }


    /**
     * Front-end call interface
     * Get the task information interface
     * Including the number of projects and the number of jobs
     * @return
     */
    @RequestMapping(value = "/actuators",method = RequestMethod.GET)
    @ResponseBody
    public String getActuators() {
        Map<String, String> actuatorsAndJobCount;
        try {
            actuatorsAndJobCount = jobMTaskService.getActuatorsAndJobCount();
        } catch (Exception e) {
            logger.error(Constants.LOG_PREFIX + " getActuators Error ：", e);
            return ResultBody.error();
        }
        return ResultBody.success(actuatorsAndJobCount);
    }


    /**
     * Front-end call interface
     * get Job statistics，
     * Real-time and historical job  data statistics
     * @return
     */
    @RequestMapping(value = "/jobstatistics",method = RequestMethod.GET)
    @ResponseBody
    public String getJobStatistics(@RequestParam String scheduler, @RequestParam String startTime, @RequestParam String endTime) {
        List<Map<String, String>> jobStatistics;
        try {
            jobStatistics = portalStatisticsService.getJobStatistics(scheduler, startTime, endTime);
        } catch (Exception e) {
            logger.error(Constants.LOG_PREFIX + " getJobStatistics Error ：", e);
            return ResultBody.error();
        }
        return ResultBody.success(jobStatistics);

    }

    /**
     * Front-end call interface
     * get Task statistics，
     * Real-time and historical Task  data statistics
     * @return
     */
    @RequestMapping(value = "/taskstatistics",method = RequestMethod.GET)
    @ResponseBody
    public String getTaskStatistics(@RequestParam String scheduler, @RequestParam String startTime, @RequestParam String endTime) {
        List<Map<String, String>> taskStatistics;
        try {
            taskStatistics = portalStatisticsService.getTaskStatistics(scheduler, startTime, endTime);
        } catch (Exception e) {
            logger.error(Constants.LOG_PREFIX + " getTaskStatistics Error ：", e);
            return ResultBody.error();
        }
        return ResultBody.success(taskStatistics);
    }

    /**
     * Front-end call interface
     * Get the job and task and contact list belong to each JobGroup
     *
     * @return
     */
    @RequestMapping(value = "/jobGroupDetails",method = RequestMethod.GET)
    @ResponseBody
    public String getJobGroupDetails() {
        //jobGroupDetails
        List<Map<String, Object>> jobGroupDetails;
        List<String> roleNames = userService.getCurrentUserRoles();
        try {
            jobGroupDetails = jobMTaskService.selectJobGroupDetails(roleNames);
        } catch (Exception e) {
            logger.error(Constants.LOG_PREFIX + " getJobGroupDetails Error ：", e);
            return ResultBody.error();
        }
        return ResultBody.success(jobGroupDetails);
    }

    /**
     * Front-end call interface
     * get scheduling statistics
     * @Return scheduling statistics
     */
    @RequestMapping(value = "/jobcallcount",method = RequestMethod.GET)
    @ResponseBody
    public String getJobCallCount() {
        Map<String, Integer> jobCnt;
        try {
            jobCnt = portalStatisticsService.getJobCallCount();
        } catch (Exception e) {
            logger.error(Constants.LOG_PREFIX + " getJobCallCount Error ：", e);
            return ResultBody.error();
        }

        return ResultBody.success(jobCnt);
    }

    /**
     * Front-end call interface
     * get the Job and Task call count details
     * @return
     */
    @RequestMapping(value = "/jobcallstatistics",method = RequestMethod.GET)
    @ResponseBody
    public String getJobCallStatistics(@RequestParam String scheduler, @RequestParam String startTime, @RequestParam String endTime) {
        List<Map<String, Object>> jobCallStatistics;
        try {
            jobCallStatistics = portalStatisticsService.getJobCallStatistics(scheduler, startTime, endTime);
        } catch (Exception e) {
            logger.error(Constants.LOG_PREFIX + " getJobCallStatistics Error ：", e);
            return ResultBody.error();
        }

        return ResultBody.success(jobCallStatistics);
    }


}
