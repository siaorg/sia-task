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

package com.sia.scheduler.quartz.listeners;

import com.sia.core.entity.BasicJob;
import com.sia.core.helper.StringHelper;
import com.sia.core.status.JobStatus;
import com.sia.scheduler.service.common.CommonService;
import com.sia.scheduler.util.constant.Constants;
import com.sia.scheduler.zk.monitor.LoadBalanceHelper;
import org.quartz.JobExecutionContext;
import org.quartz.SchedulerException;
import org.quartz.Trigger;

import java.util.List;


/**
 *
 * OnlineJobListeners
 *
 * @see
 * @author maozhengwei
 * @date 2018-10-10 16:08
 * @version V1.0.0
 **/
public class OnlineTriggerListener extends CommonService implements AbstractTriggerListener {
    @Override
    public String getName() {
        return OnlineTriggerListener.class.getSimpleName();
    }

    @Override
    public void triggerFired(Trigger trigger, JobExecutionContext context) {
        LOGGER.info("triggerFired " + trigger.getJobKey().getName() + " > > > > " + context.getFireTime());
    }

    /**
     * veto Job Execution
     *
     * @param trigger
     * @param context
     * @return false 执行； true 不执行
     */
    @Override
    public boolean vetoJobExecution(Trigger trigger, JobExecutionContext context) {
        LOGGER.info("vetoJobExecution " + context.getFireTime());
        String jobGroup = context.getTrigger().getJobKey().getGroup();
        String jobKey = context.getTrigger().getJobKey().getName();
        // READY >>> RUNNING
        BasicJob basicJob = basicJobService.getJob(jobGroup, jobKey);
        //Plan Job
        if (!StringHelper.isEmpty(basicJob.getJobPlan())) {
            BasicJob jobChild = basicJob.getJobChild();
            if (null != jobChild) {
                String jobStatus = curator4Scheduler.getJobStatus(jobChild.getJobGroup(), jobChild.getJobKey());
                //后置任务是否运行正常
                if (!StringHelper.isEmpty(jobStatus)) {
                    return true;
                }

            }
        }

        String jobStatus = curator4Scheduler.getJobStatus(jobGroup, jobKey);
        boolean casJobStatus = curator4Scheduler.casJobStatus4Scheduler(jobGroup, jobKey, Constants.LOCALHOST,
                JobStatus.READY.toString(), JobStatus.RUNNING.toString());

        if (!casJobStatus) {
            // TODO 是否需要预警（可能上次没有运行完成 导致本次不运行）
            LOGGER.error(Constants.LOG_PREFIX
                            + "The job failed to run and change Job status is failed, job is {},schedulerIPAndPort is {},job old status is {},expected job status is {}",
                    jobKey, Constants.LOCALHOST, jobStatus, JobStatus.RUNNING.toString());
            try {
                List<String> jobScheduler = curator4Scheduler.getJobScheduler(jobGroup, jobKey);
                // 重新连接 判断任务还是否属于自己，不是则进行释放
                if (!jobScheduler.isEmpty() && !jobScheduler.contains(Constants.LOCALHOST)) {
                    LOGGER.info(Constants.LOG_PREFIX + " remove job , currentScheduler is {},job own scheduler is {}", Constants.LOCALHOST, jobScheduler.get(0));
                    boolean b = removeJob(jobGroup, jobKey);
                    if (b) {
                        LoadBalanceHelper.updateScheduler(-1);
                    }
                }
            } catch (SchedulerException e) {
                LOGGER.error("vetoJobExecution remove job is error : ", e);
            }
            return true;
        }

        return false;
    }

    /**
     * TODO 后续添加 Misfired 是否进行邮件告知
     *
     * @param trigger
     */
    @Override
    public void triggerMisfired(Trigger trigger) {
        LOGGER.info("triggerMisfired " + trigger.getJobKey().getName() + " > > > > " + trigger.getStartTime());
    }

    @Override
    public void triggerComplete(Trigger trigger, JobExecutionContext context, Trigger.CompletedExecutionInstruction triggerInstructionCode) {

    }
}
