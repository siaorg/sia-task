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

import com.sia.hunter.annotation.OnlineTask;
import com.sia.core.helper.JSONHelper;
import com.sia.core.helper.StringHelper;
import com.sia.scheduler.service.JobLogService;
import com.sia.scheduler.service.PortalStatisticsService;
import com.sia.scheduler.service.TaskLogService;
import com.sia.scheduler.util.constant.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.regex.Pattern.compile;

/**
 * SchedulerInnerJob
 *
 * @description
 * @see
 * @author maozhengwei
 * @date 2018-04-28 20:12
 * @version V1.0.0
 **/
@RestController
public class SchedulerInnerJob {

    private static final Logger LOGGER = LoggerFactory.getLogger(SchedulerInnerJob.class);

    @Autowired
    private TaskLogService taskLogService;

    @Autowired
    private JobLogService jobLogService;

    @Autowired
    private PortalStatisticsService portalStatisticsService;


    /**
     * 运行统计数据 定时任务
     *
     * @return
     */
    @OnlineTask(description = "清除日志数据定时任务")
    @RequestMapping(path = "/cleanLog", method = {RequestMethod.POST}, produces = "application/json;charset=UTF-8")
    @CrossOrigin(methods = {RequestMethod.POST}, origins = "*")
    @ResponseBody
    public String cleanLog(@RequestBody String request) {
        Map<String, Object> info = new HashMap<String, Object>(4);
        try {
            Pattern compile = compile("[0-9]*");
            Matcher matcher = compile.matcher(request);
            if (matcher.matches()) {
                LOGGER.info(Constants.LOG_PREFIX + "运行统计数据定时任务");
                int deleteJobLog = jobLogService.deleteJobLogByDate(-Integer.valueOf(request));
                int deleteTaskLog = taskLogService.deleteTaskLogByDate(-Integer.valueOf(request));
                info.put("status", "success");
                info.put("result", deleteJobLog + " ：" + deleteTaskLog);
                info.put("desc", "清除日志数据定时任务，最低按天计算");
                return JSONHelper.toString(info);
            }
            info.put("status", "fail");
            info.put("result", "入参格式不正确");
            info.put("desc", "清除日志数据定时任务，最低按天计算");
        } catch (Exception e) {

        }
        return JSONHelper.toString(info);
    }

    /**
     * 首页数据统计，定时任务
     */
    @OnlineTask(description = "首页数据统计定时任务")
    @RequestMapping(path = "/runPortalStatistics", method = {RequestMethod.POST}, produces = "application/json;charset=UTF-8")
    @CrossOrigin(methods = {RequestMethod.POST}, origins = "*")
    @ResponseBody
    public String runPortalStatistics(@RequestBody String request) throws ParseException {
        String nowTime =  portalStatisticsService.getDbTime();
        String lastTime = portalStatisticsService.getNearestTime();
        if (StringHelper.isEmpty(lastTime)){
            Calendar now = Calendar.getInstance();
            Date nowTimeDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(nowTime);
            now.setTime(nowTimeDate);
            now.add(Calendar.MINUTE, -30);
            String nowTimeBefore = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(now.getTime());
            lastTime = nowTimeBefore;
        }

        boolean flag = true;
        List<Map<String, Object>> lm = portalStatisticsService.selectCallExpFinFromJobLog(lastTime, nowTime);
        for(Map<String, Object> item : lm){
            Map<String, Object> param = new HashMap<>(item);
            param.put("last_time", nowTime);
            int result = portalStatisticsService.insertPortalStatistics(param);
            if(result != 1){
                LOGGER.error(Constants.LOG_PREFIX + "insert runPortalStatistics error,scheduler: " + param);
                flag = false;
            }
        }

        Map<String, Object> info = new HashMap<String, Object>(4);
        if (flag){
            info.put("status", "success");
            info.put("result", "Execution success");
            info.put("desc", "insert DB success");
        } else {
            info.put("status", "fail");
            info.put("result", "Execution failed");
            info.put("desc", "insert DB error");
        }

        return JSONHelper.toString(info);
    }
}
