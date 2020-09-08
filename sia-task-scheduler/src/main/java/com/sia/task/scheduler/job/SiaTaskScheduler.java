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

package com.sia.task.scheduler.job;

import com.sia.task.core.IExecutorSelector;
import com.sia.task.core.INotifyScheduler;
import com.sia.task.core.ModifyOnlineJobStatus;
import com.sia.task.core.entity.BasicJob;
import com.sia.task.core.exceptions.TaskBaseExecutionException;
import com.sia.task.core.log.LogMessageConstant;
import com.sia.task.core.log.LogStatusEnum;
import com.sia.task.core.task.DagTask;
import com.sia.task.core.task.SiaJobStatus;
import com.sia.task.core.util.Constant;
import com.sia.task.core.util.ExecuteTaskThreadPool;
import com.sia.task.core.util.LoggerBackBuilder;
import com.sia.task.core.util.StringHelper;
import com.sia.task.quartz.core.CronExpression;
import com.sia.task.quartz.core.QuartzScheduler;
import com.sia.task.quartz.core.Scheduler;
import com.sia.task.quartz.exception.SchedulerException;
import com.sia.task.quartz.exception.UnableToInterruptJobException;
import com.sia.task.quartz.job.JobBuilder;
import com.sia.task.quartz.job.JobDataMap;
import com.sia.task.quartz.job.JobDetail;
import com.sia.task.quartz.job.JobKey;
import com.sia.task.quartz.job.matchers.GroupMatcher;
import com.sia.task.quartz.job.trigger.Trigger;
import com.sia.task.quartz.job.trigger.TriggerKey;
import com.sia.task.register.zookeeper.core.LoadBalanceHelper;
import com.sia.task.scheduler.core.OnlineScheduler;
import com.sia.task.scheduler.core.TaskRunShellContext;
import com.sia.task.scheduler.job.triggers.TriggerBuildHandler;
import com.sia.task.scheduler.job.triggers.TriggerTypeEnum;
import com.sia.task.scheduler.log.LogService;
import com.sia.task.scheduler.task.OnlineTask;
import com.sia.task.scheduler.task.impl.SimpleOnlineTask;
import com.sia.task.scheduler.util.LogSummary;
import com.sia.task.scheduler.util.TaskMessageUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * base quartz scheduler
 * 内置分布式锁实现
 *
 * @author maozhengwei
 * @version V1.0.0
 * @date 2018/4/19 11:42 上午
 **/
public class SiaTaskScheduler implements INotifyScheduler<OnlineTask> {

    private ModifyOnlineJobStatus modifyOnlineJobStatus;

    private IExecutorSelector executorSelector;

    public SiaTaskScheduler(ModifyOnlineJobStatus modifyOnlineJobStatus, IExecutorSelector executorSelector) {
        this.modifyOnlineJobStatus = modifyOnlineJobStatus;
        this.executorSelector = executorSelector;
    }

    private final static Logger log = LoggerFactory.getLogger(SiaTaskScheduler.class);

    @Override
    public boolean addJob(BasicJob job, List<DagTask> dagTasks, Class<? extends OnlineTask> taskClass) {
        String group = job.getJobGroup();
        String jobKey = job.getJobKey();
        String triggerType = job.getJobTrigerType();
        String triggerValue = job.getJobTrigerValue();

        try {
            if (dagTasks.isEmpty()) {
                DagTask task = new DagTask();
                task.setJobKey(jobKey);
                task.setJobGroup(group);
                task.setJobAlarmEmail(job.getJobAlarmEmail());
                boolean stopJobStatus = modifyOnlineJobStatus.stopJobStatus(task, TaskMessageUtil.mapToMessage(task, null, LogMessageConstant.NO_DAG_TASK));
                log.info(Constant.LOG_PREFIX + " Job 没有进行task编排，请检查task编排关系, stopJobStatus {}", stopJobStatus);
                // send log
                LogService.produceLog(task, "Job 没有进行task编排，请检查task编排关系", LogStatusEnum.LOG_JOB_HANDLE_FAIL_STOP);
                return false;
            }
        } catch (Exception e) {
            log.error(Constant.LOG_PREFIX + "addJobToQuartz fail:", e);
            return false;
        }

        DagTask task = dagTasks.get(0);
        try {
            // 如果是Cron类型，检查CronExpression的合法性
            if (TriggerTypeEnum.TRIGGER_TYPE_CRON.toString().equals(triggerType) && !CronExpression.isValidExpression(triggerValue)) {
                boolean stopJobStatus = modifyOnlineJobStatus.stopJobStatus(task, TaskMessageUtil.mapToMessage(task, null, LogMessageConstant.ERROR_CRON_EXPRESSION));
                log.info(Constant.LOG_PREFIX + "addJobToQuartz fail: CronExpression.isValidExpression [false], stopJobStatus {}", stopJobStatus);
                // send log
                LogService.produceLog(task, "CronExpression.isValidExpression [false]", LogStatusEnum.LOG_JOB_HANDLE_FAIL_STOP);
                return false;
            }
            // 运行中发生转移，提供重新激活补偿措施: 出现情况-可能是调度器down机；
            if (SiaJobStatus.RUNNING.getStatus().equals(modifyOnlineJobStatus.getJobStatus(task))) {
                log.info(Constant.LOG_PREFIX + " 任务【{}】在运行中发生转移，提供重新激活补偿措施", task.getJobKey());
                modifyOnlineJobStatus.completedJobStatus(task);
            }

            Trigger trigger = TriggerBuildHandler.build(jobKey, group, triggerType, triggerValue);
            log.info(Constant.LOG_PREFIX + " add Job[{}],triggerType[{}], trigger[{}] ", jobKey, triggerType, trigger);
            JobDetail jobDetail = JobBuilder.newJob(SiaJob.class).withIdentity(jobKey, group).build();
            JobDataMap jobDataMap = jobDetail.getJobDataMap();


            //OnlineTaskDetail taskDetail = OnlineTaskBuild.newOnlineTaskBuild(taskClass).build();
            OnlineScheduler onlineScheduler = new OnlineScheduler();
            onlineScheduler.setModifyOnlineJobStatus(modifyOnlineJobStatus);
            onlineScheduler.setIExecutorSelector(executorSelector);

            //TODO TaskRunShellContext
            //TaskRunShellContext runShellContext = new TaskRunShellContext(dagTasks, taskDetail, onlineScheduler);
            TaskRunShellContext runShellContext = new TaskRunShellContext(dagTasks, onlineScheduler);
            runShellContext.setOnlineTaskClass(taskClass);
            jobDataMap.put(TaskRunShellContext.class.getName(), runShellContext);
            trigger.getJobDataMap().put(TaskRunShellContext.class.getName(), runShellContext);
            trigger.getJobDataMap().put("Job_RunOnce_Disable", jobKey);
            Scheduler scheduler = SiaTaskSchedulerFactory.getScheduler(group);
            scheduler.scheduleJob(jobDetail, trigger);
            LoadBalanceHelper.updateScheduler(1, "addJob - " + jobKey);

            Map<String, String> metaData = new HashMap<>(16);
            metaData.put("Scheduler Information", scheduler.getSchedulerName() + "(v" + QuartzScheduler.getVersionMajor() + "." + QuartzScheduler.getVersionMinor() + "."
                    + QuartzScheduler.getVersionIteration() + ")" + " - " + scheduler.getSchedulerInstanceId());
            metaData.put("Job information", group + "@" + jobKey);
            metaData.put("Trigger class", trigger.getClass().toString());
            metaData.put("Trigger type", triggerType);
            metaData.put("Trigger value", triggerValue);
            metaData.put("Next fire time", String.valueOf(trigger.getNextFireTime()));
            metaData.put("Scheduler total jobs registered", String.valueOf(scheduler.getJobKeys(GroupMatcher.anyGroup()).size()));
            log.info(Constant.LOG_PREFIX + "Sia-Task-Scheduler -- Register Job" + new LogSummary("Register Job Information", null, metaData).toString());
        } catch (Exception e) {
            log.error(Constant.LOG_PREFIX + "addJobToQuartz fail:", e);
            try {
                modifyOnlineJobStatus.stopJobStatus(task, TaskMessageUtil.mapToMessage(task, null, LogMessageConstant.ERROR_ADD_JOB));
                LogService.produceLog(task, " addJobToQuartz fail", LogStatusEnum.LOG_JOB_HANDLE_FAIL_STOP);
            } catch (TaskBaseExecutionException taskBaseExecutionException) {
                log.error(Constant.LOG_EX_PREFIX + "addJobToQuartz fail, - stopJobStatus fail -", taskBaseExecutionException);
            }

            return false;
        }
        startJob(group);
        return true;
    }

    /**
     * 启动scheduler
     */
    @Override
    public void startJob(String jobGroup) {
        try {
            Scheduler scheduler = SiaTaskSchedulerFactory.getScheduler(jobGroup);
            if (!scheduler.isShutdown()) {
                scheduler.start();
            }
        } catch (Exception e) {
            log.error(Constant.LOG_PREFIX + "scheduler start Exception:", e);
        }
    }

    /**
     * 任务暂停
     *
     * @param jobKey
     * @param jobGroup
     * @return
     */
    @Deprecated
    @Override
    public boolean pauseJob(String jobGroup, String jobKey) {
        Scheduler scheduler = SiaTaskSchedulerFactory.getScheduler(jobGroup);
        boolean pauseResult = false;
        TriggerKey triggerKey = TriggerKey.triggerKey(jobKey, jobGroup);
        if (checkExists(jobKey, jobGroup)) {
            try {
                scheduler.pauseTrigger(triggerKey);
                pauseResult = true;
            } catch (SchedulerException e) {
                log.error(Constant.LOG_PREFIX + "pauseJob fail:", e);
            }
        }
        log.info(Constant.LOG_PREFIX + " pauseJob is {}, jobKey:{}", pauseResult, jobKey);
        return pauseResult;
    }

    /**
     * 任务恢复
     *
     * @param jobKey
     * @param jobGroup
     * @return
     */
    @Deprecated
    @Override
    public boolean resumeJob(String jobGroup, String jobKey) {
        Scheduler scheduler = SiaTaskSchedulerFactory.getScheduler(jobGroup);
        boolean resumeResult = false;
        TriggerKey triggerKey = TriggerKey.triggerKey(jobKey, jobGroup);
        if (checkExists(jobKey, jobGroup)) {
            try {
                scheduler.resumeTrigger(triggerKey);
                resumeResult = true;
            } catch (SchedulerException e) {
                log.error(Constant.LOG_PREFIX + " resumeJob fail:", e);
            }
        }
        log.info(Constant.LOG_PREFIX + " resumeJob is {}, this job is not exists, jobKey:{}", resumeResult, jobKey);
        return resumeResult;
    }

    /**
     * 移除任务
     * <p>
     * 修改关闭线程资源的逻辑，由于线程池的创建逻辑有job级别更变为group级别，所以关闭Job不在释放线程资源；而是销毁整个group是才会销毁；
     *
     * @param jobKey
     * @param jobGroup
     * @return
     * @throws SchedulerException
     */
    @Override
    public boolean removeJob(String jobGroup, String jobKey) {
        boolean removeResult = false;
        try {
            Scheduler scheduler = SiaTaskSchedulerFactory.getScheduler(jobGroup);
            interrupt(jobKey, jobGroup);
            removeResult = scheduler.deleteJob(JobKey.jobKey(jobKey, jobGroup));
            //移除关闭逻辑
            //ExecutorPoolService. releaseExecutorService(jobKey);
            try {
                if (scheduler.getJobKeys(GroupMatcher.anyGroup()).isEmpty()) {
                    log.error(Constant.LOG_PREFIX + "shut down the scheduler and release thread resources...");
                    ExecuteTaskThreadPool.releaseExecutorService(jobGroup);
                    //scheduler.shutdown();
                }
//                if (SiaTaskSchedulerFactory.isEmpty4Scheduler(jobGroup)) {
//                    log.error(Constant.LOG_PREFIX + "shut down the scheduler and release thread resources...");
//                    ExecuteTaskThreadPool.releaseExecutorService(jobGroup);
//                    scheduler.shutdown();
//                }
            } catch (SchedulerException e) {
                log.error(Constant.LOG_EX_PREFIX + " An error occurred while shutting down the scheduler: ", e);
            }
            //主动移除避免造成内存泄漏
            LoggerBackBuilder.removeLogger(jobKey);
        } catch (Exception e) {
            log.info(Constant.LOG_PREFIX + " removeJob Exception : ", e);
        }
        log.info(Constant.LOG_PREFIX + " removeJob is : {}, jobKey:{}", removeResult, jobKey);
        return removeResult;
    }

    /**
     * 中断任务
     *
     * @param jobKey
     * @param jobGroup
     * @return
     */
    @Override
    public boolean interrupt(String jobKey, String jobGroup) {
        Scheduler scheduler = SiaTaskSchedulerFactory.getScheduler(jobGroup);
        boolean removeResult = false;
        if (StringHelper.isEmpty(jobGroup) || StringHelper.isEmpty(jobKey)) {
            return removeResult;
        }
        try {
            removeResult = scheduler.interrupt(JobKey.jobKey(jobKey, jobGroup));
        } catch (UnableToInterruptJobException e) {
            log.error(Constant.LOG_PREFIX + " interrupt fail:", e);
        }
        return removeResult;
    }

    /**
     * check if exists
     *
     * @param jobKey
     * @param jobGroup
     * @return
     */
    @Override
    public boolean checkExists(String jobGroup, String jobKey) {
        Scheduler scheduler = SiaTaskSchedulerFactory.getScheduler(jobGroup);
        TriggerKey triggerKey = TriggerKey.triggerKey(jobKey, jobGroup);
        try {
            return scheduler.checkExists(triggerKey);
        } catch (SchedulerException e) {
            log.error(Constant.LOG_PREFIX + " checkExists fail:", e);
        }
        return false;
    }

    @Override
    public boolean triggerJob(String jobGroup, String jobKey) {
        Scheduler scheduler = SiaTaskSchedulerFactory.getScheduler(jobGroup);
        try {
            scheduler.triggerJob(JobKey.jobKey(jobKey, jobGroup));
        } catch (SchedulerException e) {
            log.error(Constant.LOG_PREFIX + " triggerJob fail:", e);
        }
        return false;
    }

    @Override
    public Class getOnlineTaskClass() {
        return SimpleOnlineTask.class;
    }


}
