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

import com.sia.task.admin.service.Index4UserService;
import com.sia.task.admin.service.Index4managerService;
import com.sia.task.admin.vo.SiaResponseBody;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * Index4ManagerController
 *
 * @author maozhengwei
 * @version V1.0.0
 * @date 2020/6/12 11:22 上午
 **/
@Slf4j
@RestController
@RequestMapping("/index4user")
public class Index4UserController {

    @Autowired
    Index4managerService index4managerService;

    @Autowired
    Index4UserService index4UserService;


    /**
     * 获取大屏展示数据接口
     *
     * @return
     */
    @RequestMapping(value = "/homePageStatistics", method = RequestMethod.GET)
    @ResponseBody
    public String homePageStatistics(@RequestParam String group) {
        return SiaResponseBody.success(index4UserService.homePageStatistics(group));
    }

    /**
     * 健康度
     *
     * @return
     */
    @RequestMapping(value = "/taskTrackerHealthStatus", method = RequestMethod.GET)
    @ResponseBody
    public String taskTrackerHealthStatus(@RequestParam String group) {
        return SiaResponseBody.success(index4UserService.taskTrackerHealthStatus(group));
    }

    /**
     * 项目组任务分配情况
     *
     * @return
     */
    @RequestMapping(value = "/jobLoadInfo", method = RequestMethod.GET)
    @ResponseBody
    public String schedulerLoadInfo(@RequestParam String group) {
        return SiaResponseBody.success(index4UserService.jobLoadInfo(group));
    }

    /**
     * WarningLog（Top-10）
     *
     * @return
     */
    @RequestMapping(value = "/warningLog", method = RequestMethod.GET)
    @ResponseBody
    public String warningLogTop(@RequestParam String group) {
        return SiaResponseBody.success(index4UserService.warningLogTop(group));
    }

    /**
     * jobSchedulingStatistics
     *
     * @return
     */
    @RequestMapping(value = "/jobSchedulingStatistics", method = RequestMethod.GET)
    @ResponseBody
    public String jobSchedulingStatistics(@RequestParam String group) {
        return SiaResponseBody.success(index4UserService.jobSchedulingStatistics(group));
    }

    /**
     * taskExecutionStatistics
     *
     * @return
     */
    @RequestMapping(value = "/taskExecutionStatistics", method = RequestMethod.GET)
    @ResponseBody
    public String taskExecutionStatistics(@RequestParam String group) {
        return SiaResponseBody.success(index4UserService.taskExecutionStatistics(group));
    }


}
