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

package com.sia.task.scheduler.impl;

import com.sia.task.core.ModifyOnlineJobStatus;
import com.sia.task.core.exceptions.TaskBaseExecutionException;
import com.sia.task.core.log.LogStatusEnum;
import com.sia.task.core.task.DagTask;
import com.sia.task.core.util.Constant;
import com.sia.task.quartz.core.Scheduler;
import com.sia.task.quartz.exception.JobExecutionException;
import com.sia.task.quartz.exception.SchedulerException;
import com.sia.task.quartz.job.JobDataMap;
import com.sia.task.quartz.job.JobDetail;
import com.sia.task.quartz.job.JobExecutionContext;
import com.sia.task.quartz.job.trigger.Trigger;
import com.sia.task.quartz.listeners.JobListener;
import com.sia.task.quartz.listeners.TriggerListener;
import com.sia.task.register.zookeeper.core.LoadBalanceHelper;
import com.sia.task.scheduler.core.OnlineScheduler;
import com.sia.task.scheduler.core.TaskRunShellContext;
import com.sia.task.scheduler.log.LogService;
import com.sia.task.scheduler.log.LogTraceIdBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * @author maozhengwei
 * @version V1.0.0
 * @description
 * @data 2019-10-28 14:33
 * @see
 **/
public class InnerJobListener implements JobListener {

    private static final Logger log = LoggerFactory.getLogger(InnerJobListener.class);

    /**
     * <p>
     * Get the name of the <code>JobListener</code>.
     * </p>
     */
    @Override
    public String getName() {
        return InnerJobListener.class.getName();
    }

    /**
     * <p>
     * Called by the <code>{@link Scheduler}</code> when a <code>{@link JobDetail}</code>
     * is about to be executed (an associated <code>{@link Trigger}</code>
     * has occurred).
     * </p>
     *
     * <p>
     * This method will not be invoked if the execution of the Job was vetoed
     * by a <code>{@link TriggerListener}</code>.
     * </p>
     *
     * @param context
     * @see #jobExecutionVetoed(JobExecutionContext)
     */
    @Override
    public void jobToBeExecuted(JobExecutionContext context) {
        log.info(Constant.LOG_PREFIX + " jobToBeExecuted ... ");
        JobDataMap dataMap = context.getJobDetail().getJobDataMap();
        TaskRunShellContext runShellContext = (TaskRunShellContext) dataMap.get(TaskRunShellContext.class.getName());
        List<DagTask> mTasks = runShellContext.getFirstTasks();

        String logTraceId = LogTraceIdBuilder.buildLogTraceId(mTasks.get(0).getJobKey());
        setTraceId(mTasks, logTraceId);
        LogService.produceLog(mTasks.get(0), "开始任务调度...", LogStatusEnum.LOG_STATUS_JOB_SCHEDULING);
    }

    /**
     * <p>
     * Called by the <code>{@link Scheduler}</code> when a <code>{@link JobDetail}</code>
     * was about to be executed (an associated <code>{@link Trigger}</code>
     * has occurred), but a <code>{@link TriggerListener}</code> vetoed it's
     * execution.
     * </p>
     *
     * @param context
     * @see #jobToBeExecuted(JobExecutionContext)
     */
    @Override
    public void jobExecutionVetoed(JobExecutionContext context) {
        JobDataMap dataMap = context.getJobDetail().getJobDataMap();
        TaskRunShellContext runShellContext = (TaskRunShellContext) dataMap.get(TaskRunShellContext.class.getName());
        List<DagTask> mTasks = runShellContext.getFirstTasks();
        DagTask siaDagTask = mTasks.get(0);
        siaDagTask.setTraceId(null);
        LogService.produceLog(mTasks.get(0), " 作业执行被否决: 可能原因：上次执行周期未结束、任务时钟参数错误、任务已下线或者发生转移，不隶属当前调度器...", LogStatusEnum.LOG_JOB_EXECUTION_VETOED);
        log.info(Constant.LOG_EX_PREFIX + " Job Execution vetoed, [{}]", context.getJobDetail().getKey().getName());
    }

    /**
     * <p>
     * Called by the <code>{@link Scheduler}</code> after a <code>{@link JobDetail}</code>
     * has been executed, and be for the associated <code>Trigger</code>'s
     * <code>triggered(xx)</code> method has been called.
     * </p>
     *
     * @param context
     * @param jobException
     */
    @Override
    public void jobWasExecuted(JobExecutionContext context, JobExecutionException jobException) {
        if (jobException != null) {
            String jobKey = context.getJobDetail().getKey().getName();
            log.info(Constant.LOG_EX_PREFIX + " Job Was Executed [jobKey : {}], jobException is not null, delete Job : ", jobKey, jobException);
            try {
                if (context.getScheduler().deleteJob(context.getJobDetail().getKey())) {
                    LoadBalanceHelper.updateScheduler(-1, "jobWasExecuted-jobException is NotNull : " + jobKey);
                }

                JobDataMap dataMap = context.getJobDetail().getJobDataMap();
                TaskRunShellContext runShellContext = (TaskRunShellContext) dataMap.get(TaskRunShellContext.class.getName());
                OnlineScheduler onlineScheduler = runShellContext.getOnlineScheduler();
                ModifyOnlineJobStatus modifyOnlineJobStatus = onlineScheduler.getModifyOnlineJobStatus();
                DagTask siaDagTask = runShellContext.getFirstTasks().get(0);
                LogService.produceLog(siaDagTask, "job execute fail ", LogStatusEnum.LOG_JOB_HANDLE_FAIL_STOP);
                modifyOnlineJobStatus.stopJobStatus(siaDagTask,"job execute fail ");

            } catch (SchedulerException | TaskBaseExecutionException e) {
                log.info(Constant.LOG_EX_PREFIX + " Job Was Executed ['{}'], jobException is not null,delete Job was exception : ", context.getJobDetail().getKey().getName(), e);
            }
        }
        log.info(Constant.LOG_PREFIX + " Job execution completed, ['{}']", context.getJobDetail().getKey().getName());
    }

    private void setTraceId(List<DagTask> mTasks, String logTraceId) {
        mTasks.forEach(mTask -> {
            List<DagTask> postTask = mTask.getPostTask();
            if (null != postTask && postTask.size() > 0) {
                setTraceId(postTask, logTraceId);
            }
            mTask.setTraceId(logTraceId);
        });
    }
}
