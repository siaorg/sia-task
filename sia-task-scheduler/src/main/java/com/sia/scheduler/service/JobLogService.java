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

import com.sia.core.entity.BasicJob;
import com.sia.core.entity.JobLog;
import com.sia.core.entity.JobMTask;
import com.sia.core.helper.DateFormatHelper;
import com.sia.core.helper.StringHelper;
import com.sia.core.mapper.JobLogMapper;
import com.sia.scheduler.log.enums.JobLogEnum;
import com.sia.scheduler.util.constant.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * JobLogService
 *
 * @description
 * @see
 * @author maozhengwei
 * @date 2018-04-18 11:21
 * @version V1.0.0
 **/
@Service
public class JobLogService {

    private static final Logger LOGGER = LoggerFactory.getLogger(JobLogService.class);

    @Autowired
    private JobLogMapper jobLogMapper;

    @Autowired
    private BasicJobService basicJobService;

    public int insertSelective(JobLog jobLog) {
        if (jobLog == null) {
            LOGGER.warn("insert JobLog fail, jobLog invalid, jobLog={}", jobLog);
            return 0;
        }
        return jobLogMapper.insertSelective(jobLog);
    }

    /**
     * Insert jobLog
     * @param jobGroup
     * @param jobKey
     * @param jobMTaskList
     * @return
     */
    @Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.DEFAULT, timeout = 36000, rollbackFor = Exception.class)
    public int insertJobLogAndTaskLog(String jobGroup, String jobKey, List<JobMTask> jobMTaskList) {
        try {
            BasicJob basicJob = basicJobService.getJob(jobGroup, jobKey);
            // insert JobLog
            JobLog jobLog = new JobLog();
            jobLog.setJobId(basicJob.getJobId());
            jobLog.setJobHandleCode(JobLogEnum.LOG_JOB_HANDLE_BEGIN.toString());
            jobLog.setJobTriggerCode(Constants.SUCCESS);
            jobLog.setJobTriggerTime(new Date());
            jobLog.setJobHandleTime(new Date());
            jobLog.setJobTriggerMsg(Constants.LOCALHOST);
            insertSelective(jobLog);

            setJobId(jobMTaskList, jobLog.getJobLogId());
        } catch (Exception e) {
            LOGGER.error("",e);
        }
        return 0;
    }

    /**
     * setJobId
     *
     * @param jobMTaskList
     * @param jobLogId
     */
    private void setJobId(List<JobMTask> jobMTaskList, int jobLogId) {
        for (JobMTask jobMTask : jobMTaskList) {
            jobMTask.setJobLogId(jobLogId);
            List<JobMTask> postTask = jobMTask.getPostTask();
            if (postTask != null && postTask.size() > 0) {
                setJobId(postTask, jobLogId);
            }
        }
    }

    /**
     * Update jobLog log
     *
     * @param onlineTask
     * @param status
     */
    public void updateJobLog(JobMTask onlineTask, String status) {
        if (onlineTask == null || StringHelper.isEmpty(status)) {
            LOGGER.warn("update JobLog fail, jobLog invalid, jobLog={}", onlineTask);
            return;
        }
        String url = null;
        if (!onlineTask.getTaskKey().equals(Constants.ENDTASK)) {
            url = Constants.HTTP_PREFIX + onlineTask.getCurrentHandler() + onlineTask.getTaskKey().split(Constants.REGEX_COLON)[1] + Constants.REGEX_ARROW;
        }
        JobLog jobLog = new JobLog();
        if (JobLogEnum.LOG_ENDTASK_FINISHED.toString().equals(status)) {
            jobLog.setJobHandleCode(Constants.LOG_SUCCESS);
            jobLog.setJobHandleFinishedTime(new Date());
            jobLog.setJobHandleMsg(Constants.LOG_FINISHED);
        }
        if (JobLogEnum.LOG_JOB_HANDLE_FAIL_STOP.toString().equals(status)) {
            jobLog.setJobHandleCode(Constants.LOG_FAIL);
            jobLog.setJobHandleFinishedTime(new Date());
            jobLog.setJobHandleMsg("调度task "  + url + Constants.LOG_TASK_MSG_FAIL_STOP);
        }
        if (JobLogEnum.LOG_JOB_FAIL_MULTI_CALLS_TRANSFER.toString().equals(status)) {
            jobLog.setJobHandleCode(Constants.LOG_FAIL);
            jobLog.setJobHandleTime(new Date());
            jobLog.setJobHandleMsg("调度task "  + url + Constants.LOG_TASK_MSG_FAIL_MULTI_CALLS_TRANSFER);
        }
        if (JobLogEnum.LOG_JOB_FAIL_TRANSFER.toString().equals(status)) {
            jobLog.setJobHandleCode(Constants.LOG_FAIL);
            jobLog.setJobHandleTime(new Date());
            jobLog.setJobHandleMsg("调度task "  + url + Constants.LOG_TASK_MSG_FAIL_TRANSFER);
        }
        if (JobLogEnum.LOG_JOB_FAIL_IGNORE.toString().equals(status)){
            jobLog.setJobHandleCode(Constants.LOG_FAIL);
            jobLog.setJobHandleTime(new Date());
            jobLog.setJobHandleMsg("调度task "  + url + Constants.LOG_TASK_MSG_FAIL_IGNORE);
        }
        //TODO  重构日志1
        jobLog.setJobId(onlineTask.getJobId());
        jobLog.setJobLogId(onlineTask.getJobLogId());
        jobLogMapper.updateByPrimaryKeySelective(jobLog);

    }

    /**
     * Delete log forward N days
     * @param displacement Number of days
     */
    public int deleteJobLogByDate(int displacement) {
        Date date = new Date();
        Map<String, String> taskMap = new HashMap<>(2);
        taskMap.put("create_time", DateFormatHelper.getFormatByDay(date,displacement));
        return jobLogMapper.deleteJobLogByDate(taskMap);
    }

}
