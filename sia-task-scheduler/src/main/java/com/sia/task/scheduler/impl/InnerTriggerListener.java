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
import com.sia.task.core.exceptions.SchedulerBaseException;
import com.sia.task.core.log.LogStatusEnum;
import com.sia.task.core.task.DagTask;
import com.sia.task.core.task.SiaJobStatus;
import com.sia.task.core.util.Constant;
import com.sia.task.quartz.core.Scheduler;
import com.sia.task.quartz.exception.SchedulerException;
import com.sia.task.quartz.job.JobDataMap;
import com.sia.task.quartz.job.JobDetail;
import com.sia.task.quartz.job.JobExecutionContext;
import com.sia.task.quartz.job.JobKey;
import com.sia.task.quartz.job.trigger.SimpleTrigger;
import com.sia.task.quartz.job.trigger.Trigger;
import com.sia.task.quartz.job.trigger.impl.SimpleTriggerImpl;
import com.sia.task.quartz.listeners.TriggerListener;
import com.sia.task.register.zookeeper.core.LoadBalanceHelper;
import com.sia.task.scheduler.core.OnlineScheduler;
import com.sia.task.scheduler.core.TaskRunShellContext;
import com.sia.task.scheduler.log.LogService;
import com.sia.task.scheduler.util.LogSummary;
import com.sia.task.scheduler.util.SchedulerMisFireDataMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>A built-in TriggerListener is used to determine whether the Job has the ability to continue execution when using remote scheduling,
 * and perform corresponding operations. </p>
 *
 *
 * <p>The listener may block the execution of subsequent jobs. Please refer to the <code>vetoJobExecution(..)</code> </p>
 *
 * @author maozhengwei
 * @version V1.0.0
 * @date 2019-10-28 14:34
 **/
public class InnerTriggerListener implements TriggerListener {
    private static final Logger log = LoggerFactory.getLogger(InnerTriggerListener.class);

    private static final String timerPre = "timerPre";

    /**
     * <p>
     * Get the name of the <code>TriggerListener</code>.
     * </p>
     */
    @Override
    public String getName() {
        return InnerTriggerListener.class.getSimpleName();
    }

    /**
     * <p>
     * Called by the <code>{@link Scheduler}</code> when a <code>{@link Trigger}</code>
     * has fired, and it's associated <code>{@link JobDetail}</code>
     * is about to be executed.
     * </p>
     *
     * <p>
     * It is called before the <code>vetoJobExecution(..)</code> method of this
     * interface.
     * </p>
     *
     * @param trigger The <code>Trigger</code> that has fired.
     * @param context The <code>JobExecutionContext</code> that will be passed to
     */
    @Override
    public void triggerFired(Trigger trigger, JobExecutionContext context) {
        trigger.getJobDataMap().put(timerPre + trigger.getJobKey().getName(), System.currentTimeMillis());
        log.info(Constant.LOG_PREFIX + " triggerFired " + trigger.getJobKey().getName() + " >> " + context.getFireTime());
    }

    /**
     * <p>
     * Called by the <code>{@link Scheduler}</code> when a <code>{@link Trigger}</code>
     * has fired, and it's associated <code>{@link JobDetail}</code>
     * is about to be executed.  If the implementation vetos the execution (via
     * returning <code>true</code>), the job's execute method will not be called.
     * </p>
     *
     * <p>
     * It is called after the <code>triggerFired(..)</code> method of this
     * interface.
     * </p>
     *
     * @param trigger The <code>Trigger</code> that has fired.
     * @param context The <code>JobExecutionContext</code> that will be passed to
     */
    @Override
    public boolean vetoJobExecution(Trigger trigger, JobExecutionContext context) {
        long timeMillis = System.currentTimeMillis();
        boolean vetoJobExecution;
        JobKey jobKey = trigger.getJobKey();
        JobDataMap dataMap = context.getJobDetail().getJobDataMap();
        try {
            TaskRunShellContext runShellContext = (TaskRunShellContext) dataMap.get(TaskRunShellContext.class.getName());
            OnlineScheduler onlineScheduler = runShellContext.getOnlineScheduler();
            ModifyOnlineJobStatus modifyOnlineJobStatus = onlineScheduler.getModifyOnlineJobStatus();
            DagTask dagTask = runShellContext.getFirstTasks().get(0);

            vetoJobExecution = modifyOnlineJobStatus.runningJobStatus(dagTask);
            if (!vetoJobExecution) {
                log.info(Constant.LOG_PREFIX + " veto job execution : casJobStatus is failed, job [{}]", dagTask.getJobKey());
                if (!modifyOnlineJobStatus.isJobOwner(dagTask, Constant.LOCALHOST)) {
                    Scheduler scheduler = context.getScheduler();
                    if (scheduler.deleteJob(JobKey.jobKey(dagTask.getJobKey(), dagTask.getJobGroup()))) {
                        log.info(Constant.LOG_PREFIX + " veto job execution : remove job [{}], Job has been preempted by other schedulers, the current scheduler should remove this job", dagTask.getJobKey());
                        LoadBalanceHelper.updateScheduler(-1, " vetoJobExecution is true - deleteJob : " + jobKey.getName());
                    }
                }
                if (SiaJobStatus.STOP.getStatus().equals(modifyOnlineJobStatus.getJobStatus(dagTask))) {
                    Scheduler scheduler = context.getScheduler();
                    if (scheduler.deleteJob(JobKey.jobKey(dagTask.getJobKey(), dagTask.getJobGroup()))) {
                        log.info(Constant.LOG_PREFIX + " veto job execution : remove job [{}],  task execution exception has stopped, the current scheduler should remove this job", dagTask.getJobKey());
                        LoadBalanceHelper.updateScheduler(-1, " vetoJobExecution is true - deleteJob : " + jobKey.getName());
                    }
                }

                synchronousCounter4SimpleTrigger(trigger, context, jobKey.getName(), jobKey.getGroup());
            }
        } catch (SchedulerBaseException | SchedulerException e) {
            log.error(Constant.LOG_EX_PREFIX + " jobKey:[{}] vetoJobExecution >>>>  ", jobKey.getName(), e);
            vetoJobExecution = false;
        }
        log.info(Constant.LOG_PREFIX + " vetoJobExecution - veto : [{}], JobKey : [{}]", !vetoJobExecution, trigger.getJobKey().getName());
        log.info(Constant.LOG_PREFIX + " 否决Job执行共计耗时【{}】: 【{}】", jobKey.getName(), System.currentTimeMillis() - timeMillis);
        return !vetoJobExecution;
    }

    /**
     * <p>
     * Called by the <code>{@link Scheduler}</code> when a <code>{@link Trigger}</code>
     * has misfired.
     * </p>
     *
     * <p>
     * Consideration should be given to how much time is spent in this method,
     * as it will affect all triggers that are misfiring.  If you have lots
     * of triggers misfiring at once, it could be an issue it this method
     * does a lot.
     * </p>
     *
     * @param trigger The <code>Trigger</code> that has misfired.
     */
    @Override
    public void triggerMisfired(Trigger trigger) {
        JobDataMap dataMap = trigger.getJobDataMap();
        TaskRunShellContext runShellContext = (TaskRunShellContext) dataMap.get(TaskRunShellContext.class.getName());
        List<DagTask> mTasks = runShellContext.getFirstTasks();
        DagTask siaDagTask = mTasks.get(0);
        siaDagTask.setTraceId(null);
        SchedulerMisFireDataMap.putMisfireCount4Job(siaDagTask.getJobKey(), System.currentTimeMillis());

        Map<String, String> metaData = new HashMap<>(16);
        JobKey jobKey = trigger.getJobKey();
        metaData.put("Job information", jobKey.getGroup() + "@" + jobKey.getName());
        metaData.put("Misfire instruction", String.valueOf(trigger.getMisfireInstruction()));
        metaData.put("Misfire count", String.valueOf(SchedulerMisFireDataMap.getMisfireCount(jobKey.getName())));
        metaData.put("Misfire count for all job", String.valueOf(SchedulerMisFireDataMap.getMisfireCount4AllJob()));
        LogService.produceLog(siaDagTask, " Job-Trigger-Misfired：" + metaData, LogStatusEnum.LOG_JOB_EXECUTION_MISFIRE);
        log.info(Constant.LOG_PREFIX + "TriggerMisfired " + new LogSummary("Scheduler trigger misfired information", "如果【Misfire count for all job】数据太大，那就跪了...", metaData));
    }

    /**
     * <p>
     * Called by the <code>{@link Scheduler}</code> when a <code>{@link Trigger}</code>
     * has fired, it's associated <code>{@link JobDetail}</code>
     * has been executed, and it's <code>triggered(xx)</code> method has been
     * called.
     * </p>
     *
     * @param trigger                The <code>Trigger</code> that was fired.
     * @param context                The <code>JobExecutionContext</code> that was passed to the
     *                               <code>Job</code>'s<code>execute(xx)</code> method.
     * @param triggerInstructionCode the result of the call on the <code>Trigger</code>'s<code>triggered(xx)</code>
     */
    @Override
    public void triggerComplete(Trigger trigger, JobExecutionContext context, Trigger.CompletedExecutionInstruction triggerInstructionCode) {
        JobKey jobKey = trigger.getJobKey();
        String jobKeyName = jobKey.getName();
        String jobKeyGroup = jobKey.getGroup();
        log.info(Constant.LOG_PREFIX + " triggerComplete, JobKey : [{}]  nextFireTime : [{}]", jobKeyName);
        long endTime = System.currentTimeMillis();
        long startTime = (long) trigger.getJobDataMap().get(timerPre + jobKeyName);
        log.info(Constant.LOG_PREFIX + " 触发任务共计耗时【{}】: 【{}】", jobKeyName, endTime - startTime);

        try {
            String runOnceJobKey = (String) trigger.getJobDataMap().get("Job_RunOnce_Disable");
            if (jobKeyName.equals(runOnceJobKey)) {
                synchronousCounter4SimpleTrigger(trigger, context, jobKeyName, jobKeyGroup);
            }
        } catch (SchedulerException e) {
            log.error(Constant.LOG_EX_PREFIX + " synchronousCounter4SimpleTrigger is error [jobKey:{}]", jobKeyName, e);
        }
    }


    /**
     * constructor
     * Constructor
     * @param trigger
     * @param context
     * @param jobKeyName
     * @param jobKeyGroup
     * @throws SchedulerException
     */
    private void synchronousCounter4SimpleTrigger(Trigger trigger, JobExecutionContext context, String jobKeyName, String jobKeyGroup) throws SchedulerException {


        if (trigger instanceof SimpleTriggerImpl) {
            log.info(Constant.LOG_PREFIX + " triggerComplete -- synchronousCounter4SimpleTrigger - 【jobKey :{}】", jobKeyName);
            SimpleTriggerImpl simpleTrigger = (SimpleTriggerImpl) trigger;
            Scheduler scheduler = context.getScheduler();
            int timesTriggered = simpleTrigger.getTimesTriggered();
            int repeatCount = simpleTrigger.getRepeatCount();
            if (repeatCount == SimpleTrigger.REPEAT_INDEFINITELY) {
                return;
            }
            if (timesTriggered >= repeatCount + 1) {
                if (scheduler.deleteJob(JobKey.jobKey(jobKeyName, jobKeyGroup))) {
                    LoadBalanceHelper.updateScheduler(-1, " synchronousCounter4SimpleTrigger(timesTriggered >= repeatCount) - deleteJob : " + jobKeyName);
                }
            }
        }
    }

}
