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

import com.sia.task.admin.service.Log4Service;
import com.sia.task.admin.vo.SiaResponseBody;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * 日志管理界面 - API
 *
 * @author maozhengwei
 * @version V1.0.0
 * @data 2018/4/19 10:50 下午
 **/
@RestController
@RequestMapping("/logapi")
public class LogController {

    @Resource
    Log4Service log4Service;

    /**
     * 日志管理页面-日志分页查询接口
     *
     * @param jobGroupName
     * @param jobKey
     * @param currentPage
     * @param pageSize
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/jobAndTaskLogVos", method = RequestMethod.GET)
    @ResponseBody
    public String selectLog4Page(@RequestParam String jobGroupName, @RequestParam String jobKey, @RequestParam int currentPage, @RequestParam int pageSize) throws Exception {
        return SiaResponseBody.success(log4Service.selectLogs4Page(jobGroupName, jobKey, currentPage, pageSize));
    }

    /**
     * 获取每个group所包含的日志数目
     * 保留group 的原因是下一个版本需要进行页面分离，到时候传递参数group,只需要单独查询改组的信息即可
     *
     * @param jobGroupName
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/countGroupsJobLogs", method = RequestMethod.GET)
    @ResponseBody
    public String selectCountGroupsJobLogs(@RequestParam String jobGroupName) throws Exception {
        return SiaResponseBody.success(log4Service.selectLogCount4AuthGroups(jobGroupName));
    }
}
