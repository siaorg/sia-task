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

package com.sia.task.collector.log;

import com.sia.task.collector.log.consume.LogConsumeService;
import com.sia.task.core.entity.JobLog;
import com.sia.task.core.entity.LogMessage;
import com.sia.task.core.entity.TaskLog;
import com.sia.task.core.log.LogMessageConstant;
import com.sia.task.core.log.LogStatusEnum;
import com.sia.task.core.task.DagTask;
import com.sia.task.core.util.Constant;
import com.sia.task.core.util.JsonHelper;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.text.MessageFormat;

/**
 * @author maozhengwei
 * @version V1.0.0
 * @data 2020/4/25 12:38 下午
 * @see LogMessage
 **/
@Slf4j
@NoArgsConstructor
public class LogMessageShell extends LogMessage implements Runnable {

    /**
     * When an object implementing interface <code>Runnable</code> is used
     * to create a thread, starting the thread causes the object's
     * <code>run</code> method to be called in that separately executing
     * thread.
     * <p>
     * The general contract of the method <code>run</code> is that it may
     * take any action whatsoever.
     * <p>
     * LOG_JOB_HANDLE_FAIL_STOP and LOG_JOB_EXECUTION_VETOED require early warning notification when these two states appear.
     * Among them, the former will change the execution status of the Job, so it will provide unified early warning notification through the aspect mechanism when the state changes.
     * The latter is a running error and cannot change the status of the job, it needs to be supplemented with early warning notice
     * <p>
     *
     * @see Thread#run()
     */
    @Override
    public void run() {
        long logStarted = System.currentTimeMillis();
        try {
            switch (statusEnum) {
                case LOG_STATUS_JOB_SCHEDULING:
                    jobStartScheduling(statusEnum);
                    break;
                case LOG_JOB_HANDLE_FAIL_STOP:
                    jobStartScheduling(statusEnum);
                    break;
                case LOG_JOB_EXECUTION_VETOED:
                    jobStartScheduling(statusEnum);
                    break;
                case LOG_JOB_EXECUTION_MISFIRE:
                    jobStartScheduling(statusEnum);
                case LOG_STATUS_TASK_HANDLE_FINISHED:
                    recardTaskLogs(statusEnum);
                    recardJobFinishedLogs(statusEnum);
                    break;
                default:
                    recardTaskLogs(statusEnum);
                    recardJobFinishedLogs(statusEnum);
            }
        } catch (Exception e) {
            log.error(Constant.LOG_EX_PREFIX + "LogMessage run : ", e);
        }
        log.info(Constant.LOG_PREFIX + " logLoad执行共计耗时【{}】: 【{}】", mTask.getJobKey(), System.currentTimeMillis() - logStarted);
    }

    private void recardJobFinishedLogs(LogStatusEnum statusEnum) throws Exception {
        log.info(Constant.LOG_PREFIX + " log - recardJobFinishedLogs : statusEnum[{}]", statusEnum);

        if (Constant.ENDTASK.equals(mTask.getTaskKey())) {
            LogConsumeService.recardJobFinishedLogs(generateUpdateJoblog(statusEnum), statusEnum);
        }

        if (statusEnum.isFail()) {
            LogConsumeService.recardJobFinishedLogs(generateUpdateJoblog(statusEnum), statusEnum);
        }
    }

    private void recardTaskLogs(LogStatusEnum statusEnum) throws Exception {
        log.info(Constant.LOG_PREFIX + " log - recardTaskLogs : statusEnum[{}]", statusEnum);
        LogConsumeService.recardTaskLogs(buildTaskLog(mTask, statusEnum, message));
    }

    private void jobStartScheduling(LogStatusEnum statusEnum) throws Exception {
        log.info(Constant.LOG_PREFIX + " log - jobStartScheduling :  statusEnum[{}]", statusEnum);
        LogConsumeService.jobStartScheduling(generateJobLog(statusEnum));
    }

    private JobLog generateJobLog(LogStatusEnum statusEnum) {
        JobLog jobLog = new JobLog();
        jobLog.setJobKey(mTask.getJobKey());
        jobLog.setJobGroup(mTask.getJobGroup());
        jobLog.setTraceId(mTask.getTraceId());
        jobLog.setJobHandleCode(statusEnum.toString());
        jobLog.setJobHandleMsg(message);
        jobLog.setJobTriggerCode(LogMessageConstant.LOG_SUCCESS);
        jobLog.setJobTriggerTime(timer);
        jobLog.setJobHandleTime(timer);
        jobLog.setJobTriggerMsg(Constant.LOCALHOST);
        return jobLog;
    }

    private JobLog generateUpdateJoblog(LogStatusEnum status) {
        String url = null;
        if (!mTask.getTaskKey().equals(Constant.ENDTASK)) {
            url = Constant.HTTP_PREFIX + mTask.getCurrentHandler() + mTask.getTaskKey().split(Constant.REGEX_COLON)[1] + LogMessageConstant.REGEX_EX;
        }
        JobLog jobLog = new JobLog();
        switch (status) {
            case LOG_STATUS_TASK_HANDLE_FINISHED:
                jobLog.setJobHandleCode(LogStatusEnum.LOG_STATUS_JOB_FINISHED.toString());
                jobLog.setJobHandleFinishedTime(timer);
                jobLog.setJobHandleMsg(LogMessageConstant.LOG_FINISHED);
                break;
            case LOG_STATUS_TASK_HANDLE_FAIL_STOP:
                jobLog.setJobHandleCode(LogStatusEnum.LOG_JOB_HANDLE_FAIL_STOP.toString());
                jobLog.setJobHandleFinishedTime(timer);
                jobLog.setJobHandleMsg("调度task " + url + LogMessageConstant.LOG_TASK_MSG_FAIL_STOP);
                break;
            case LOG_STATUS_TASK_HANDLE_IGNORE:
                jobLog.setJobHandleCode(status.toString());
                jobLog.setJobHandleTime(timer);
                jobLog.setJobHandleMsg("调度task " + url + LogMessageConstant.LOG_TASK_MSG_FAIL_IGNORE);
                break;
            default:
                jobLog.setJobHandleCode(status.toString());
                jobLog.setJobHandleTime(timer);
                jobLog.setJobHandleMsg("调度task " + url + status.toString());
                break;
        }
        jobLog.setTraceId(mTask.getTraceId());
        return jobLog;
    }

    private TaskLog buildTaskLog(DagTask mTask, LogStatusEnum statusEnum, String result) {
        TaskLog taskLog = new TaskLog();
        String target;
        if (mTask.getTaskKey().equals(Constant.ENDTASK)) {
            target = Constant.ENDTASK;
        } else {
            target = Constant.HTTP_PREFIX + mTask.getCurrentHandler() + mTask.getTaskKey().split(Constant.REGEX_COLON)[1];
        }

        switch (statusEnum) {
            case LOG_STATUS_TASK_HANDLE_BEGIN:
                taskLog.setTaskStatus(LogMessageConstant.LOG_START);
                taskLog.setTaskMsg(logMessFormat(target));
                break;
            case LOG_STATUS_TASK_HANDLE_FINISHED:
                taskLog.setTaskStatus(LogMessageConstant.LOG_SUCCESS);
                taskLog.setTaskMsg(logMessFormat(target, result));
                break;
            case LOG_STATUS_TASK_HANDLE_FAIL_STOP:
                taskLog.setTaskStatus(LogMessageConstant.LOG_FAIL);
                taskLog.setTaskMsg(logMessFormat(target, LogMessageConstant.LOG_TASK_MSG_FAIL_STOP + LogMessageConstant.REGEX_EX + result));
                break;
            case LOG_STATUS_TASK_HANDLE_IGNORE:
                taskLog.setTaskStatus(LogMessageConstant.LOG_FAIL);
                taskLog.setTaskMsg(logMessFormat(target, LogMessageConstant.LOG_TASK_MSG_FAIL_IGNORE + LogMessageConstant.REGEX_EX + result));
                break;
            case LOG_STATUS_TASK_HANDLE_TRANSFER:
                taskLog.setTaskStatus(LogMessageConstant.LOG_FAIL);
                taskLog.setTaskMsg(logMessFormat(target, LogMessageConstant.LOG_TASK_MSG_FAIL_TRANSFER + LogMessageConstant.REGEX_EX + result));
                break;
            case LOG_STATUS_TASK_HANDLE_TRANSFER_STOP:
                taskLog.setTaskStatus(LogMessageConstant.LOG_FAIL);
                taskLog.setTaskMsg(logMessFormat(target, LogMessageConstant.LOG_TASK_MSG_FAIL_TRANSFER_STOP + LogMessageConstant.REGEX_EX + result));
                break;
            case LOG_STATUS_TASK_HANDLE_MULTI_CALLS:
                taskLog.setTaskStatus(LogMessageConstant.LOG_FAIL);
                taskLog.setTaskMsg(logMessFormat(target, LogMessageConstant.LOG_STATUS_TASK_HANDLE_MULTI_CALLS + LogMessageConstant.REGEX_EX + result));
                break;
            case LOG_STATUS_TASK_HANDLE_MULTI_CALLS_STOP:
                taskLog.setTaskStatus(LogMessageConstant.LOG_FAIL);
                taskLog.setTaskMsg(logMessFormat(target, LogMessageConstant.LOG_STATUS_TASK_HANDLE_MULTI_CALLS_STOP + LogMessageConstant.REGEX_EX + result));
                break;
            case LOG_STATUS_TASK_HANDLE_MULTI_CALLS_TRANSFER:
                taskLog.setTaskStatus(LogMessageConstant.LOG_FAIL);
                taskLog.setTaskMsg(logMessFormat(target, LogMessageConstant.LOG_STATUS_TASK_HANDLE_MULTI_CALLS_TRANSFER + LogMessageConstant.REGEX_EX + result));
                break;
            case LOG_STATUS_TASK_HANDLE_MULTI_CALLS_TRANSFER_STOP:
                taskLog.setTaskStatus(LogMessageConstant.LOG_FAIL);
                taskLog.setTaskMsg(logMessFormat(target, LogMessageConstant.LOG_STATUS_TASK_HANDLE_MULTI_CALLS_TRANSFER_STOP + LogMessageConstant.REGEX_EX + result));
                break;
            default:
                break;
        }

        String traceId = mTask.getTraceId();
        taskLog.setTaskHandleTime(timer);
        taskLog.setJobKey(mTask.getJobKey());
        taskLog.setTaskKey(mTask.getTaskKey());
        taskLog.setTraceId(traceId);
        return taskLog;
    }

    public static String logMessFormat(String... mess) {
        if (mess.length == 1) {
            return MessageFormat.format(" Scheduling task :[{0}]", mess);
        }
        if (mess.length == 2) {
            return MessageFormat.format("Scheduling tasks :[{0}], Scheduling information : [{1}]", mess);
        }
        return MessageFormat.format("Scheduling information: {0}", JsonHelper.toString(mess));
    }

    @Override
    public String toString() {
        return super.toString();
    }
}
