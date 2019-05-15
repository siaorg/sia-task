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

import com.sia.core.web.vo.ResultBody;
import com.sia.config.web.constants.Constants;
import com.sia.config.web.service.LogService;
import com.sia.config.web.util.PageBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 *
 * Various query operations for joblog and tasklog
 * @see
 * @author maozhengwei
 * @date 2019-04-28 15:40
 * @version V1.0.0
 **/
@RestController
@RequestMapping("/logapi")
public class LogController {

    private final static Logger LOGGER = LoggerFactory.getLogger(LogController.class);

    @Autowired
    private LogService logService;


    /**
     * The front end calls the interface
     * Gets the JobLog and associated TaskLog
     * @return JobLog and associated TaskLog pageData
     */
    @RequestMapping(value = "/jobAndTaskLogVos", method = RequestMethod.GET)
    @ResponseBody
    public String selectJobLogAndTaskLogList(@RequestParam String jobGroupName, @RequestParam String jobKey, @RequestParam int currentPage, @RequestParam int pageSize) {
        PageBean<?> pageData;
        try {
            pageData = logService.selectJobLogAndTaskLogList(jobGroupName, jobKey, currentPage, pageSize);
        } catch (Exception e) {
            LOGGER.error(Constants.LOG_PREFIX + " selectJobLogAndTaskLogList TASK Exception : ", e);
            return ResultBody.error();
        }
        return ResultBody.success(pageData);
    }

    /**
     * Gets the number of logs contained in each group
      * @return  the number of logs contained in each group
     */
    @RequestMapping(value = "/countGroupsJobLogs",method = RequestMethod.GET)
    @ResponseBody
    public String selectCountGroupsJobLogs(@RequestParam String jobGroupName) {
        List<Map<String, Integer>> jobLogsByGroup;
        try {
            jobLogsByGroup = logService.selectCountGroupsJobLogs(jobGroupName);
        } catch (Exception e) {
            LOGGER.error(Constants.LOG_PREFIX + " selectCountGroupsJobLogs TASK Exception : ", e);
            return ResultBody.error();
        }
        return ResultBody.success(jobLogsByGroup);

    }


}
