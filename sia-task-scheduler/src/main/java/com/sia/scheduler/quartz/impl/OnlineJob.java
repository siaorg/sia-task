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

package com.sia.scheduler.quartz.impl;

import com.sia.core.entity.BasicJob;
import com.sia.core.entity.JobMTask;
import com.sia.scheduler.context.SpringContext;
import com.sia.scheduler.service.JobLogService;
import com.sia.scheduler.service.common.CommonService;
import com.sia.scheduler.thread.execute.TaskCommit;
import com.sia.scheduler.util.constant.Constants;
import org.quartz.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.CountDownLatch;

/**
 *
 * OnlineJob class
 * Rewrite the {@see execute(JobExecutionContext context)} in the Job class to implement the execution logic of the job.
 *
 * If you use the scheduler's interrupt method {@Link interrupt()} you must inherit InterruptableJob, override the interrupt() method,
 * and set the variable private boolean interrupted and set it to true.
 *
 * Provide CountDownLatch as a judgment to extend the life cycle of the job, which is equivalent to the life cycle of the task contained in the Job.
 *
 * @PersistJobDataAfterExecution Used to store related data when the job is executed, for example, a Job counter.
 * @DisallowConcurrentExecution Setting a quartz job will not execute in parallel
 * @description
 * @see
 * @author maozhengwei
 * @date 2018-04-28 17:25
 * @version V1.0.0
 **/
@PersistJobDataAfterExecution
@DisallowConcurrentExecution
public class OnlineJob extends CommonService implements Job, InterruptableJob {

    private static final Logger LOGGER = LoggerFactory.getLogger(OnlineJob.class);
    private boolean interrupted = false;

    CountDownLatch countDownLatch = new CountDownLatch(1);

    /**
     * @param context
     * @throws JobExecutionException
     */
    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {

        String jobGroup = context.getTrigger().getJobKey().getGroup();
        String jobKey = context.getTrigger().getJobKey().getName();
        BasicJob job = SpringContext.getRunningJob().get(jobKey);
        if (job != null) {
            job.setTriggerInstance(Constants.LOCALHOST);
        }

        List<JobMTask> onlineTaskList = analyticalJob(jobGroup, jobKey);

        if (onlineTaskList == null || onlineTaskList.size() <= 0) {
            LOGGER.error(Constants.LOG_PREFIX + "The job failed to run and is empty. jobGroup is {}, The jobKey is {} ", jobGroup, jobKey);
            return;
        }

        LOGGER.info(Constants.LOG_PREFIX + "JOB execution begins， The jobGroup is {}, The jobKey is {}", jobGroup, jobKey);
        JobLogService jobLogService = SpringContext.getJobLogService();

        jobLogService.insertJobLogAndTaskLog(jobGroup, jobKey, onlineTaskList);
        onlineTaskList.forEach(jobMTask -> {
            jobMTask.setCountDownLatch(countDownLatch);
            TaskCommit.commit(jobMTask);
        });
        try {
            countDownLatch.await();
            //ExecutorRouteSharding.clean(jobKey);
        } catch (InterruptedException e) {
            LOGGER.error(Constants.LOG_EX_PREFIX + " JOB execution is complete，countDownLatch InterruptedException", e);
        }
    }

    /**
     * <p>
     * Called by the <code>{@link Scheduler}</code> when a user
     * interrupts the <code>Job</code>.
     * </p>
     *
     * @throws UnableToInterruptJobException if there is an exception while interrupting the job.
     */
    @Override
    public void interrupt() throws UnableToInterruptJobException {
        LOGGER.warn(Constants.LOG_PREFIX + "Job interrupt >>>>>> countDownLatch.countDown()");
        countDownLatch.countDown();
        interrupted = true;
    }
}
