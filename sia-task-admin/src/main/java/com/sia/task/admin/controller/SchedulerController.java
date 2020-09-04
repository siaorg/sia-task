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

import com.sia.task.admin.service.Scheduler4Service;
import com.sia.task.admin.vo.SiaResponseBody;
import com.sia.task.core.util.Constant;
import com.sia.task.core.util.StringHelper;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * 调度器管理
 *
 * @author maozhengwei
 * @version V1.0.0
 * @data 2018/4/19 4:29 下午
 **/
@RestController
@RequestMapping("/scheduler")
public class SchedulerController {

    @Resource
    Scheduler4Service scheduler4Service;

    /**
     * 获取工作调度器列表
     *
     * @return
     */
    @RequestMapping(value = "/workinglist", method = RequestMethod.GET)
    public String getSchedulers4Working() {
        return SiaResponseBody.success(scheduler4Service.getSchedulers4Working());
    }

    /**
     * 获取非工作状态的调度器列表
     *
     * @return
     */
    @RequestMapping(value = "/blacklist", method = RequestMethod.GET)
    public String getScheduler4OutOfServices() {
        return SiaResponseBody.success(scheduler4Service.getScheduler4OutOfServices());
    }

    /**
     * 获取已经离线调度器列表
     *
     * @return
     */
    @RequestMapping(value = "/offline", method = RequestMethod.GET)
    public String getSchedulers4OffLine() {
        return SiaResponseBody.success(scheduler4Service.getSchedulers4OffLine());
    }

    /**
     * 调度器上线
     * 注销调度器在<code>ZK_OFFLINE_SCHEDULER</code>路径
     *
     * @return
     */
    @RequestMapping(value = "/openScheduler", method = RequestMethod.POST)
    public String unregisterScheduler4Offline(@RequestBody String schedulerInstance) {
        if (StringHelper.isEmpty(schedulerInstance)) {
            return SiaResponseBody.failure(null, "Request parameter cannot be empty");
        }
        return SiaResponseBody.isOk(scheduler4Service.unregisterScheduler4Offline(schedulerInstance));
    }

    /**
     * 前端调用接口-添加黑名单调度器
     *
     * @return
     */
    @RequestMapping(value = "/closeScheduler", method = RequestMethod.POST)
    public String downLineScheduler(@RequestBody String schedulerInstance) {
        if (StringHelper.isEmpty(schedulerInstance)) {
            return SiaResponseBody.failure(null, "Request parameter cannot be empty");
        }
        return SiaResponseBody.isOk(scheduler4Service.registerScheduler4Offline(schedulerInstance));
    }

    /**
     * 获取白名单列表中注册的调度器
     *
     * @return
     */
    @RequestMapping(value = "/getAuthList", method = RequestMethod.GET)
    public String getScheduler4OnlineAuth() {
        return SiaResponseBody.success(scheduler4Service.getScheduler4OnlineAuth());
    }

    /**
     * 添加IP白名单
     *
     * @param ip
     * @return
     */
    @RequestMapping(value = "/addAuthList", method = RequestMethod.POST)
    public String addAuthList(@RequestBody String ip) {
        if (!StringHelper.isGrammatical(ip, Constant.IP_REGEX) && !"localhost".equals(ip.toLowerCase())) {
            return SiaResponseBody.failure(null, "IP地址格式不正确");
        }
        return SiaResponseBody.isOk(scheduler4Service.registerScheduler4OnlineAuth(ip));
    }

    /**
     * 移除IP白名单
     *
     * @param ipList
     * @return
     */
    @RequestMapping(value = "/removeAuthList", method = RequestMethod.POST)
    public String unregisterScheduler4OnlineAuth(@RequestBody String ipList) {
        if (StringHelper.isEmpty(ipList)) {
            return SiaResponseBody.failure(null, "Request parameter cannot be empty");
        }
        return SiaResponseBody.isOk(scheduler4Service.unregisterScheduler4OnlineAuth(ipList));
    }
}
