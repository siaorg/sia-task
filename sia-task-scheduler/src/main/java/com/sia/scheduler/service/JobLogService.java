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

import com.sia.core.constant.Constant;
import com.sia.core.entity.BasicJob;
import com.sia.core.entity.JobLog;
import com.sia.core.entity.JobMTask;
import com.sia.core.helper.DateFormatHelper;
import com.sia.core.mapper.JobLogMapper;
import com.sia.scheduler.log.enums.LogStatusEnum;
import com.sia.scheduler.log.jobfile.LoggerBuilder;
import com.sia.scheduler.log.worker.LogTraceIdBuilder;
import com.sia.scheduler.log.worker.service.LogProduceService;
import com.sia.scheduler.util.constant.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
        int result = 0;
        if (jobLog == null) {
            LOGGER.warn("insert JobLog fail, jobLog invalid, jobLog={}", jobLog);
            return 0;
        }
        try {
            result = jobLogMapper.insertSelective(jobLog);
        } catch (Exception e) {
            LOGGER.error("insertSelective Exception: ", e.getMessage());
        }
        return result;
    }

    private JobLog generateInsertJoblog(String jobGroup, String jobKey, String traceId){
        BasicJob basicJob = basicJobService.getJob(jobGroup, jobKey);
        JobLog jobLog = new JobLog();
        jobLog.setJobId(basicJob.getJobId());
        jobLog.setJobHandleCode(LogStatusEnum.LOG_JOB_HANDLE_BEGIN.toString());
        jobLog.setJobTriggerCode(Constants.SUCCESS);
        jobLog.setJobTriggerTime(new Date());
        jobLog.setJobHandleTime(new Date());
        jobLog.setJobTriggerMsg(Constants.LOCALHOST);
        jobLog.setTraceId(traceId);

        return jobLog;
    }

    /**
     * Insert jobLog
     * @param jobGroup
     * @param jobKey
     * @param jobMTaskList
     * @return
     */
    public int insertJobLogAndTaskLog(String jobGroup, String jobKey, List<JobMTask> jobMTaskList) {
        JobMTask jobMTask = new JobMTask();

        String traceId = LogTraceIdBuilder.buildLogTraceId(jobMTaskList.get(0));
        jobMTask.setTraceId(traceId);
        jobMTask.setJobGroup(jobGroup);
        jobMTask.setJobKey(jobKey);

        // 日志记录文件
        JobLog jobLog = generateInsertJoblog(jobGroup, jobKey, traceId);
        LoggerBuilder.getLogger(jobKey).info(jobLog.toString());
        // 日志异步提交
        setTraceId(jobMTaskList, traceId);
        LogProduceService.produceLogs(jobMTask, null, LogStatusEnum.LOG_JOB_HANDLE_BEGIN);
        return 0;
    }

    public int insertJobLogAndTaskLog4Consumer(JobMTask jobMTask) {
        try {
            JobLog jobLog = generateInsertJoblog(jobMTask.getJobGroup(), jobMTask.getJobKey(), jobMTask.getTraceId());
            insertSelective(jobLog);
        } catch (Exception e) {
            LOGGER.error(Constants.LOG_EX_PREFIX + "insertJobLogAndTaskLog 数据库插入数据操作异常", e.getMessage());
        }
        return 0;
    }

    /**
     * setJobId
     *
     * @param jobMTaskList
     * @param traceId
     */
    private void setTraceId(List<JobMTask> jobMTaskList, String traceId) {
        for (JobMTask jobMTask : jobMTaskList) {
            jobMTask.setTraceId(traceId);
            List<JobMTask> postTask = jobMTask.getPostTask();
            if (postTask != null && postTask.size() > 0) {
                setTraceId(postTask, traceId);
            }
        }
    }

    private JobLog generateUpdateJoblog(JobMTask onlineTask, LogStatusEnum status){
        String url = null;
        if (!onlineTask.getTaskKey().equals(Constants.ENDTASK)) {
            url = Constants.HTTP_PREFIX + onlineTask.getCurrentHandler() + onlineTask.getTaskKey().split(Constants.REGEX_COLON)[1] + Constants.REGEX_ARROW;
        }

        Integer jobLogId = jobLogMapper.selectJobLogIdByTraceId(onlineTask.getTraceId());
        if (jobLogId != null){
            onlineTask.setJobLogId(jobLogId);
        } else {
            LOGGER.error(Constant.LOG_PREFIX + "Not found jobLogId when update JobLog: {}", onlineTask);
            return null;
        }

        JobLog jobLog = new JobLog();
        if (LogStatusEnum.LOG_JOB_ENDTASK_FINISHED.equals(status)) {
            jobLog.setJobHandleCode(Constants.LOG_SUCCESS);
            jobLog.setJobHandleFinishedTime(new Date());
            jobLog.setJobHandleMsg(Constants.LOG_FINISHED);
        }
        if (LogStatusEnum.LOG_JOB_HANDLE_FAIL_STOP.equals(status)) {
            jobLog.setJobHandleCode(Constants.LOG_FAIL);
            jobLog.setJobHandleFinishedTime(new Date());
            jobLog.setJobHandleMsg("调度task " + url + Constants.LOG_TASK_MSG_FAIL_STOP);
        }
        if (LogStatusEnum.LOG_JOB_FAIL_MULTI_CALLS_TRANSFER.equals(status)) {
            jobLog.setJobHandleCode(Constants.LOG_FAIL);
            jobLog.setJobHandleTime(new Date());
            jobLog.setJobHandleMsg("调度task " + url + Constants.LOG_TASK_MSG_FAIL_MULTI_CALLS_TRANSFER);
        }
        if (LogStatusEnum.LOG_JOB_FAIL_TRANSFER.equals(status)) {
            jobLog.setJobHandleCode(Constants.LOG_FAIL);
            jobLog.setJobHandleTime(new Date());
            jobLog.setJobHandleMsg("调度task " + url + Constants.LOG_TASK_MSG_FAIL_TRANSFER);
        }
        if (LogStatusEnum.LOG_JOB_FAIL_IGNORE.equals(status)) {
            jobLog.setJobHandleCode(Constants.LOG_FAIL);
            jobLog.setJobHandleTime(new Date());
            jobLog.setJobHandleMsg("调度task " + url + Constants.LOG_TASK_MSG_FAIL_IGNORE);
        }
        //TODO  重构日志1
        jobLog.setJobId(onlineTask.getJobId());
        jobLog.setJobLogId(onlineTask.getJobLogId());
        jobLog.setTraceId(onlineTask.getTraceId());

        return jobLog;
    }

    /**
     * Update jobLog log
     *
     * @param onlineTask
     * @param status
     */
    public void updateJobLog(JobMTask onlineTask, LogStatusEnum status) {
        JobLog jobLog = generateUpdateJoblog(onlineTask, status);
        //落盘该Job日志
        LoggerBuilder.getLogger(onlineTask.getJobKey()).info(jobLog.toString());

        LogProduceService.produceLogs(onlineTask, null, status);
    }

    /**
     * 更新jobLog日志
     *
     * @param onlineTask
     * @param status
     */
    public void updateJobLog4Consumer(JobMTask onlineTask, LogStatusEnum status) {
        if (onlineTask == null) {
            LOGGER.warn("update JobLog fail, jobLog invalid, jobLog={}", onlineTask);
            return;
        }

        JobLog jobLog = generateUpdateJoblog(onlineTask, status);
        if (jobLog == null){
            return;
        }

        try {
            LOGGER.info(Constants.LOG_PREFIX + " log aspect >>> updateJobLog task:[{}], status:[{}]", onlineTask, status);
            jobLogMapper.updateByPrimaryKeySelective(jobLog);
        } catch (Exception e) {
            LOGGER.error(Constants.LOG_EX_PREFIX + "updateJobLog 数据库插入数据操作异常", e);
        }
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
