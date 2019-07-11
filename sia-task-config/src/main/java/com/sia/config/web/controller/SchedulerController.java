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

import com.sia.config.web.service.RegistryService;
import com.sia.core.curator.Curator4Scheduler;
import com.sia.core.helper.StringHelper;
import com.sia.core.web.vo.ResultBody;
import com.sia.config.web.constants.Constants;
import com.sia.config.web.filter.AuthInterceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;

/**
 * Scheduler operation
 *
 * @author maozhengwei
 * @version V1.0.0
 * @date 2019-04-28 15:40
 * @see
 **/
@RestController
@RequestMapping("/scheduler")
public class SchedulerController {

    private final static Logger LOGGER = LoggerFactory.getLogger(SchedulerController.class);

    @Autowired
    protected AuthInterceptor userService;

    @Autowired
    protected Curator4Scheduler curator4Scheduler;

    @Autowired
    protected RegistryService registryService;

    /**
     * Front-end call interface
     * Work scheduler list
     * Gets the list of all online schedulers to remove the scheduler from the blacklist
     *
     * @return
     */
    @RequestMapping(value = "/workinglist", method = {RequestMethod.POST, RequestMethod.GET})
    @ResponseBody
    public String getSchedulersOfWorking() {
        List<String> blackList = registryService.getBlackList();
        List<String> schedulers = registryService.getSchedulers();
        schedulers.removeAll(blackList);
        LOGGER.info(Constants.LOG_PREFIX + " get SchedulersOfWorking >>> success : schedulerInstance is {}", schedulers);
        return ResultBody.success(schedulers);
    }

    /**
     * Front-end call interface
     * Blacklist scheduler
     * offlineList = blacklist - whitelist
     *
     * @return
     */
    @RequestMapping(value = "/blacklist", method = {RequestMethod.POST, RequestMethod.GET})
    @ResponseBody
    public String getSchedulerInfoOfBlacklist() {
        List<String> blackList = registryService.getBlackList();
        List<String> schedulers = registryService.getSchedulers();
        blackList.retainAll(schedulers);
        LOGGER.info(Constants.LOG_PREFIX + " get SchedulerInfoOfBlacklist >>> success : schedulerInstance is {}", blackList);
        return ResultBody.success(blackList);
    }

    /**
     * Front-end call interface
     * offline scheduler List
     * offlineList = blacklist - whitelist
     *
     * @return
     */
    @RequestMapping(value = "/offline", method = {RequestMethod.POST, RequestMethod.GET})
    @ResponseBody
    public String getSchedulersOffLine() {
        List<String> blackList = registryService.getBlackList();
        List<String> schedulers = registryService.getSchedulers();
        blackList.removeAll(schedulers);
        LOGGER.info(Constants.LOG_PREFIX + " get SchedulersOffLine >>> success : schedulerInstance is {}", blackList);
        return ResultBody.success(blackList);
    }

    /**
     * Front-end call interface
     * Remove the blacklist scheduler
     *
     * @param schedulerInstance
     * @return
     */
    @RequestMapping(value = "/openScheduler", method = {RequestMethod.POST, RequestMethod.GET})
    @ResponseBody
    public String upLineScheduler(@RequestBody String schedulerInstance) {
        if (StringHelper.isEmpty(schedulerInstance)) {
            LOGGER.info(Constants.LOG_PREFIX + " upLineScheduler >>> fail : schedulerInstance is {}", schedulerInstance);
            return ResultBody.failed();
        }
        try {
            String userName = userService.getCurrentUser();
            List<String> schedulerInstances = Arrays.asList(schedulerInstance.split(Constants.REGEX_COMMA));
            for (String instance : schedulerInstances) {
                boolean openScheduler = registryService.openScheduler(instance);
                if (!openScheduler) {
                    return ResultBody.failed();
                }
            }
            LOGGER.info(Constants.OPERATION_LOG_PREFIX + "userName : " + userName + "; operation is: upline scheduler, scheduler is [ " + schedulerInstance + " ]");
            return ResultBody.success();
        } catch (Exception e) {
            LOGGER.error(Constants.LOG_PREFIX + " upLineScheduler Error ： ", e);
        }
        return ResultBody.failed();
    }

    /**
     * Front-end call interface
     * Add the blacklist scheduler
     *
     * @param schedulerInstance
     * @return
     */
    @RequestMapping(value = "/closeScheduler", method = {RequestMethod.POST, RequestMethod.GET})
    @ResponseBody
    public String downLineScheduler(@RequestBody String schedulerInstance) {
        if (StringHelper.isEmpty(schedulerInstance)) {
            LOGGER.info(Constants.LOG_PREFIX + " downLineScheduler >>> fail : schedulerInstance is {}", schedulerInstance);
            return ResultBody.failed();
        }
        try {
            String userName = userService.getCurrentUser();
            List<String> schedulerInstances = Arrays.asList(schedulerInstance.split(Constants.REGEX_COMMA));
            for (String instance : schedulerInstances) {
                boolean openScheduler = registryService.closeScheduler(instance);
                if (!openScheduler) {
                    return ResultBody.failed();
                }
            }
            LOGGER.info(Constants.OPERATION_LOG_PREFIX + "username is: " + userName + "; operation is: downline scheduler, scheduler is [ " + schedulerInstance + " ]");
            return ResultBody.success();
        } catch (Exception e) {
            LOGGER.error(Constants.LOG_PREFIX + " downLineScheduler Error :", e);
        }
        return ResultBody.failed();
    }

    /**
     * Get the IP whitelist
     */
    @RequestMapping(value = "/getAuthList", method = {RequestMethod.POST, RequestMethod.GET})
    @ResponseBody
    public String getAuthList() {
        List<String> authList = registryService.getAuthList();
        return ResultBody.success(authList);
    }

    /**
     * Add IP whitelist
     */
    @RequestMapping(value = "/addAuthList", method = {RequestMethod.POST, RequestMethod.GET})
    @ResponseBody
    public String addAuthList(@RequestBody String ip) {

        if (!StringHelper.isGrammatical(ip, Constants.IP_REGEX) && !"localhost".equals(ip.toLowerCase())) {
            return ResultBody.failed("IP地址格式不正确");
        }

        boolean flag = false;
        try {
            String userName = userService.getCurrentUser();
            if (!StringHelper.isEmpty(ip)) {
                LOGGER.info(Constants.LOG_PREFIX + "username is: " + userName + " add IP whiteList,IP :{}", ip);
                flag = registryService.addToAuth(ip);
            }
        } catch (Exception e) {
            LOGGER.error(Constants.LOG_PREFIX + " add IP whiteList Error ： ", e);
            return ResultBody.error();
        }

        return flag ? ResultBody.success() : ResultBody.failed();
    }

    /**
     * Remove IP whitelist
     */
    @RequestMapping(value = "/removeAuthList", method = {RequestMethod.POST, RequestMethod.GET})
    @ResponseBody
    public String removeAuthList(@RequestBody String ipList) {
        if (StringHelper.isEmpty(ipList)) {
            LOGGER.info(Constants.LOG_PREFIX + " removeAuthList >>> fail : ipList is {}", ipList);
            return ResultBody.failed();
        }
        try {
            String userName = userService.getCurrentUser();
            List<String> ips = Arrays.asList(ipList.split(Constants.REGEX_COMMA));
            for (String ip : ips) {
                boolean flag = registryService.removeFromAuth(ip);
                if (!flag) {
                    LOGGER.info(Constants.LOG_PREFIX + " removeAuthList >>> fail : ip is {}", ip);
                    return ResultBody.failed();
                }
            }
            LOGGER.info(Constants.OPERATION_LOG_PREFIX + "username is: " + userName + "; operation is: removeAuthList, ipList is [ " + ipList + " ]");
            return ResultBody.success();
        } catch (Exception e) {
            LOGGER.error(Constants.LOG_PREFIX + " remove IP whiteList Error ：", e);
            return ResultBody.error();
        }
    }
}
