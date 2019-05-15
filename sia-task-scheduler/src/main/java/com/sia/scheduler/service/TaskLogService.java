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

package com.sia.scheduler.service;

import com.sia.core.entity.JobMTask;
import com.sia.core.entity.TaskLog;
import com.sia.core.helper.DateFormatHelper;
import com.sia.core.mapper.TaskLogMapper;
import com.sia.scheduler.log.enums.TaskLogEnum;
import com.sia.scheduler.util.constant.Constants;
import com.sia.scheduler.util.tools.StringFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * PortalStatisticsService
 * Home information statistics
 *
 * @see
 * @author maozhengwei
 * @date 2018-04-18 11:27
 * @version V1.0.0
 **/
@Service
public class TaskLogService {

    private static final Logger LOGGER = LoggerFactory.getLogger(TaskLogService.class);

    @Autowired
    @SuppressWarnings("all")
    private TaskLogMapper taskLogMapper;

    /**
     * Insert the Task log
     * @param taskLog
     * @return
     */
    public int insertSelective(TaskLog taskLog) {
        if (taskLog == null) {
            LOGGER.warn("insert TaskLog fail, taskLog invalid, taskLog={}", taskLog);
            return 0;
        }
        return taskLogMapper.insertSelective(taskLog);
    }

    /**
     * Record task scheduling process log
     * @param jobMTask
     * @param status
     * @param result
     * @throws Exception
     */
    public void recordTaskLog(JobMTask jobMTask, String status, String result) {
        TaskLog taskLog = new TaskLog();
        String url = null;
        if (!jobMTask.getTaskKey().equals(Constants.ENDTASK)) {
            url = Constants.HTTP_PREFIX + jobMTask.getCurrentHandler() + jobMTask.getTaskKey().split(Constants.REGEX_COLON)[1];
        }

        switch (TaskLogEnum.getByValue(status)) {
            case LOG_TASK_HANDLE_BEGIN:
                taskLog.setTaskStatus(Constants.LOG_START);
                taskLog.setTaskMsg(StringFormat.logMessFormat(url));
                break;
            case LOG_TASK_FINISHED:
                taskLog.setTaskStatus(Constants.LOG_SUCCESS);
                taskLog.setTaskMsg(StringFormat.logMessFormat(url,result));
                break;
            case LOG_ENDTASK_FINISHED:
                taskLog.setTaskStatus(Constants.LOG_SUCCESS);
                taskLog.setTaskMsg(StringFormat.logMessFormat(url,Constants.LOG_TASK_MSG_END));
                break;
            case LOG_TASK_FAIL_STOP:
                taskLog.setTaskStatus(Constants.LOG_FAIL);
                taskLog.setTaskMsg(StringFormat.logMessFormat(url,Constants.LOG_TASK_MSG_FAIL_STOP + Constants.REGEX_ARROW + result));
                break;
            case LOG_TASK_FAIL_IGNORE:
                taskLog.setTaskStatus(Constants.LOG_FAIL);
                taskLog.setTaskMsg(StringFormat.logMessFormat(url,Constants.LOG_TASK_MSG_FAIL_IGNORE + Constants.REGEX_ARROW + result));
                break;
            case LOG_TASK_FAIL_TRANSFER:
                taskLog.setTaskStatus(Constants.LOG_FAIL);
                taskLog.setTaskMsg(StringFormat.logMessFormat(url,Constants.LOG_TASK_MSG_FAIL_TRANSFER + Constants.REGEX_ARROW + result));
                break;
            case LOG_TASK_FAIL_MULTI_CALLS_TRANSFER:
                taskLog.setTaskStatus(Constants.LOG_FAIL);
                taskLog.setTaskMsg(StringFormat.logMessFormat(url,Constants.LOG_TASK_MSG_FAIL_MULTI_CALLS_TRANSFER + Constants.REGEX_ARROW + result));
                break;
            case LOG_TASK_FAIL_DETAIL:
                taskLog.setTaskStatus(Constants.LOG_FAIL);
                taskLog.setTaskMsg(StringFormat.logMessFormat(url,Constants.LOG_TASK_MSG_FAIL_DETAIL + Constants.REGEX_ARROW + result));
                break;
            case LOG_TASK_CALLBACKERROR:
                taskLog.setTaskStatus(Constants.LOG_FAIL);
                taskLog.setTaskMsg(StringFormat.logMessFormat(url,Constants.LOG_TASK_CALLBACKERROR + Constants.REGEX_ARROW + result));
                break;
            default:
                break;
        }
        taskLog.setTaskHandleTime( new Date());
        taskLog.setJobLogId(jobMTask.getJobLogId());
        taskLog.setJobKey(jobMTask.getJobKey());
        taskLog.setTaskKey(jobMTask.getTaskKey());
        insertSelective(taskLog);
    }

    /**
     * Delete log forward N days
     *
     * @param displacement Number of days
     */
    public int deleteTaskLogByDate(int displacement) {
        Date date = new Date();
        Map<String, String> taskMap = new HashMap<>(2);
        taskMap.put("create_time", DateFormatHelper.getFormatByDay(date, displacement));
        return taskLogMapper.deleteTaskLogByDate(taskMap);
    }
}
