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

package com.sia.scheduler.web.controller;

import com.sia.core.helper.JSONHelper;
import com.sia.core.helper.StringHelper;
import com.sia.scheduler.service.BasicJobService;
import com.sia.scheduler.util.constant.Constants;
import org.quartz.SchedulerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 *
 *
 * WebJobController
 * @see
 * @author maozhengwei
 * @date 2018-04-17 11:13
 * @version V1.0.0
 **/
@RestController
@RequestMapping("/jobapi")
public class WebJobController {

    private static final Logger LOGGER = LoggerFactory.getLogger(WebJobController.class);

    @Autowired
    private BasicJobService basicJobService;

    /**
     * 前端调用接口
     * runOnce
     *
     * @param jobGroupName
     * @param jobKey
     * @return
     */
    @RequestMapping(value = "/runOnce/{jobGroupName}/{jobKey}",method = {RequestMethod.POST,RequestMethod.GET})
    @ResponseBody
    public String runOnce(@PathVariable String jobGroupName, @PathVariable String jobKey) {
        if (StringHelper.isEmpty(jobGroupName) || StringHelper.isEmpty(jobKey)) {
            return JSONHelper.toString(Constants.FAIL);
        }
        try {
            boolean status = basicJobService.runOnce(jobGroupName, jobKey);
            return JSONHelper.toString(status);
        } catch (SchedulerException e) {
            LOGGER.error(Constants.LOG_PREFIX + " runOnce jobs Exception : ", e);
        }
        return JSONHelper.toString(Constants.FAIL);
    }

}