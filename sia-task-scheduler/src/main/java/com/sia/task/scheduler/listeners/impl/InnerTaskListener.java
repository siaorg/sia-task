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

package com.sia.task.scheduler.listeners.impl;

import com.sia.task.core.ModifyOnlineJobStatus;
import com.sia.task.core.exceptions.SchedulerBaseException;
import com.sia.task.core.log.LogStatusEnum;
import com.sia.task.core.task.DagTask;
import com.sia.task.core.util.Constant;
import com.sia.task.core.util.StringHelper;
import com.sia.task.quartz.job.JobKey;
import com.sia.task.register.zookeeper.core.LoadBalanceHelper;
import com.sia.task.scheduler.core.TaskRunShell;
import com.sia.task.scheduler.failover.FailoverEnum;
import com.sia.task.scheduler.job.SiaTaskSchedulerFactory;
import com.sia.task.scheduler.listeners.TaskListener;
import com.sia.task.scheduler.log.LogService;
import com.sia.task.scheduler.task.TriggerOnlineTaskBundle;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;

/**
 * @author maozhengwei
 * @version V1.0.0
 * @description
 * @data 2019-10-15 11:20
 * @see
 **/
@Slf4j
public class InnerTaskListener implements TaskListener {

    /**
     * <p>
     * Get the name of the <code>TaskListener</code>.
     * </p>
     */
    @Override
    public String getName() {
        return InnerTaskListener.class.getSimpleName();
    }

    /**
     * <p>
     * Called by the <code>{@link TaskRunShell}</code> when a <code>{@link DagTask}</code>
     * is executeStarted.
     * </p>
     *
     * @param taskBundle
     */
    @Override
    public void executeStarted(TriggerOnlineTaskBundle taskBundle) {
        DagTask dagTask = taskBundle.getOnlineTaskDetail().getDagTask();
        log.info(Constant.LOG_PREFIX + "InnerTaskListener --- executeStarted {}", dagTask);
        // 需要详细日志可打开此配置
        //LogProduceService.produceLog(dagTask, null, LogStatusEnum.LOG_STATUS_TASK_HANDLE_BEGIN);
    }

    /**
     * <p>
     * Called by the <code>{@link TaskRunShell}</code> when a <code>{@link DagTask}</code>
     * is executed.
     * </p>
     *
     * @param taskBundle
     */
    @Override
    public void taskExecuted(TriggerOnlineTaskBundle taskBundle) {
        DagTask dagTask = taskBundle.getOnlineTaskDetail().getDagTask();
        log.info(Constant.LOG_PREFIX + "InnerTaskListener --- taskExecuted {}", dagTask);
        String mess = dagTask.getOutParam();
        if (Constant.ENDTASK.equals(dagTask.getTaskKey())) {
            mess = "completed";
            ModifyOnlineJobStatus modifyOnlineJobStatus = taskBundle.getModifyOnlineJobStatus();
            try {
                if (LoadBalanceHelper.isOffline() || LoadBalanceHelper.getMyJobNum() > LoadBalanceHelper.getJobThreshold()) {
                    log.info(Constant.LOG_PREFIX + "Job执行完成,  符合任务转移标记，进人任务转移 waitForAMoment: [{}]  key [{}]" , waitForAMoment(60000), dagTask.getJobKey());
                    boolean releaseJob = modifyOnlineJobStatus.releaseJob(dagTask);
                    if (releaseJob) {
                        log.info(Constant.LOG_PREFIX + " 进人任务转移： key [{}], releaseJob [{}]", dagTask.getJobKey(), releaseJob);
                        if (SiaTaskSchedulerFactory.getScheduler(dagTask.getJobGroup()).deleteJob(JobKey.jobKey(dagTask.getJobKey(), dagTask.getJobGroup()))) {
                            log.info(Constant.LOG_PREFIX + " 进人任务转移： key [{}], deleteJob [{true}]", dagTask.getJobKey());
                            boolean b = LoadBalanceHelper.updateScheduler(-1, "taskExecuted - releaseJob:" + dagTask.getJobKey());
                            log.info(Constant.LOG_PREFIX + " 进人任务转移： key [{}],  LoadBalanceHelper.updateScheduler [-1]", dagTask.getJobKey());
                        }
                    }
                    log.info(Constant.LOG_PREFIX + "释放任务： key [{}], releaseJob [{}]", dagTask.getJobKey(), releaseJob);
                }
            } catch (Exception ex) {
                log.info(Constant.LOG_PREFIX + "Job执行完成, 释放出现错误： key [{}]", dagTask.getJobKey());
            }
        }
        //  日志
        LogService.produceLog(dagTask, mess, LogStatusEnum.LOG_STATUS_TASK_HANDLE_FINISHED);
    }

    /**
     * <p>
     * Called by the <code>{@link TaskRunShell}</code> when a <code>{@link DagTask}</code>
     * is unExecuted.
     * </p>
     *
     * @param taskBundle
     */
    @Override
    public void taskunExecuted(TriggerOnlineTaskBundle taskBundle) {
        DagTask mTask = taskBundle.getOnlineTaskDetail().getDagTask();
        log.info(Constant.LOG_PREFIX + "InnerTaskListener --- task unExecuted {}", mTask);
    }

    /**
     * <p>
     * Called by the <code>{@link TaskRunShell}</code> when a <code>{@link DagTask}</code>
     * is executedError.
     * </p>
     *
     * @param taskBundle
     * @param ose
     */
    @Override
    public void executedError(TriggerOnlineTaskBundle taskBundle, SchedulerBaseException ose) {
        DagTask dagTask = taskBundle.getOnlineTaskDetail().getDagTask();
        log.info(Constant.LOG_EX_PREFIX + " InnerTaskListener --- executedError {}", dagTask, ose);
        try {
            if (FailoverEnum.IGNORE.getValue().equals(dagTask.getFailover())) {
                LogService.produceLog(dagTask, dagTask.getOutParam() + " - " + ose.getMessage(), LogStatusEnum.LOG_STATUS_TASK_HANDLE_IGNORE);
                return;
            }
            if (FailoverEnum.STOP.getValue().equals(dagTask.getFailover())) {
                LogService.produceLog(dagTask, dagTask.getOutParam() + " - " + ose.getMessage(), LogStatusEnum.LOG_STATUS_TASK_HANDLE_FAIL_STOP);
                return;
            }
            if (FailoverEnum.TRANSFER.getValue().equals(dagTask.getFailover())) {
                if (dagTask.isFailoverFlag()) {
                    LogService.produceLog(dagTask, dagTask.getOutParam() + " - " + ose.getMessage(), LogStatusEnum.LOG_STATUS_TASK_HANDLE_TRANSFER_STOP);
                    return;
                }
                LogService.produceLog(dagTask, dagTask.getOutParam() + " - " + ose.getMessage(), LogStatusEnum.LOG_STATUS_TASK_HANDLE_TRANSFER);
                return;
            }
            if (FailoverEnum.MULTI_CALLS.getValue().equals(dagTask.getFailover())) {
                if (dagTask.isFailoverFlag()) {
                    LogService.produceLog(dagTask, dagTask.getOutParam() + " - " + ose.getMessage(), LogStatusEnum.LOG_STATUS_TASK_HANDLE_MULTI_CALLS_STOP);
                    return;
                }
                LogService.produceLog(dagTask, dagTask.getOutParam() + " - " + ose.getMessage(), LogStatusEnum.LOG_STATUS_TASK_HANDLE_MULTI_CALLS);
                return;
            }
            if (FailoverEnum.MULTI_CALLS_TRANSFER.getValue().equals(dagTask.getFailover())) {
                if (dagTask.isFailoverFlag()) {
                    if (StringHelper.isEmpty(dagTask.getCurrentHandler())) {
                        LogService.produceLog(dagTask, ose.getMessage(), LogStatusEnum.LOG_STATUS_TASK_HANDLE_MULTI_CALLS_TRANSFER_STOP);
                    } else {
                        LogService.produceLog(dagTask, ose.getMessage(), LogStatusEnum.LOG_STATUS_TASK_HANDLE_MULTI_CALLS_TRANSFER);
                    }
                    return;
                }
                LogService.produceLog(dagTask, dagTask.getOutParam() + " - " + ose.getMessage(), LogStatusEnum.LOG_STATUS_TASK_HANDLE_MULTI_CALLS_TRANSFER);
                return;
            }
        } catch (Exception e) {
            log.error(Constant.LOG_EX_PREFIX + " InnerTaskListener --- executedError [{}]", dagTask, e);
        }
    }

    private long waitForAMoment(long mills) {
        try {
            TimeUnit.MILLISECONDS.sleep(mills);
            log.info(Constant.LOG_PREFIX + " releaseJob waitForAMoment...");
        } catch (InterruptedException e) {
            log.error(Constant.LOG_PREFIX + " releaseJob waitForAMoment - {}", e);
        }
        return mills;
    }

}
