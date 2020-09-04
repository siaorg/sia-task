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

import com.sia.task.admin.service.DagTaskService;
import com.sia.task.admin.service.Index4managerService;
import com.sia.task.admin.vo.SiaResponseBody;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * Index4ManagerController
 *
 * @author maozhengwei
 * @version V1.0.0
 * @date 2020/6/12 11:22 上午
 **/
@Slf4j
@RestController
@RequestMapping("/index4manager")
public class Index4ManagerController {

    @Autowired
    Index4managerService index4managerService;

    @Resource
    private DagTaskService dagTaskService;

    /**
     * 获取大屏展示数据接口
     *
     * @return
     */
    @RequestMapping(value = "/homePageStatistics", method = RequestMethod.GET)
    @ResponseBody
    public String homePageStatistics() {
        return SiaResponseBody.success(index4managerService.homePageStatistics());
    }

    /**
     * 健康度
     *
     * @return
     */
    @RequestMapping(value = "/taskTrackerHealthStatus", method = RequestMethod.GET)
    @ResponseBody
    public String taskTrackerHealthStatus() {
        return SiaResponseBody.success(index4managerService.taskTrackerHealthStatus());
    }

    /**
     * 调度器任务分配情况
     *
     * @return
     */
    @RequestMapping(value = "/schedulerLoadInfo", method = RequestMethod.GET)
    @ResponseBody
    public String schedulerLoadInfo() {
        return SiaResponseBody.success(index4managerService.schedulerLoadInfo());
    }

    /**
     * 任务分配情况汇总ß
     *
     * @return
     */
    @RequestMapping(value = "/jobLoadInfo", method = RequestMethod.GET)
    @ResponseBody
    public String jobLoadInfo() throws Exception {
        return SiaResponseBody.success(index4managerService.jobLoadInfo());
    }

    /**
     * 首页管理-获取各个JobGroup下的job与task及联系人列表
     *
     * @return
     */
    @RequestMapping(value = "/jobGroupDetails", method = RequestMethod.GET)
    @ResponseBody
    public String getJobDetails4Group() {
        return SiaResponseBody.success(dagTaskService.selectJobGroupDetails());
    }
}
