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

import com.sia.task.core.util.Constant;
import com.sia.task.quartz.exception.SchedulerException;
import com.sia.task.quartz.job.JobDetail;
import com.sia.task.quartz.job.JobKey;
import com.sia.task.quartz.job.trigger.Trigger;
import com.sia.task.quartz.job.trigger.TriggerKey;
import com.sia.task.quartz.listeners.SchedulerListenerSupport;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class InnerSchedulerListener extends SchedulerListenerSupport {

    @Override
    public void jobAdded(JobDetail jobDetail) {
        JobKey job = jobDetail.getKey();
        log.info(Constant.LOG_PREFIX + "Job registration is successful, Job information is [{}]", job);
    }

    @Override
    public void jobDeleted(JobKey jobKey) {
        log.info(Constant.LOG_PREFIX + "Job deleted is successful, Job information is [{}]", jobKey);
    }

    @Override
    public void jobPaused(JobKey jobKey) {
        super.jobPaused(jobKey);
    }

    @Override
    public void jobResumed(JobKey jobKey) {
        super.jobResumed(jobKey);
    }

    @Override
    public void jobScheduled(Trigger trigger) {
        super.jobScheduled(trigger);
    }

    @Override
    public void jobsPaused(String jobGroup) {
        super.jobsPaused(jobGroup);
    }

    @Override
    public void jobsResumed(String jobGroup) {
        super.jobsResumed(jobGroup);
    }

    @Override
    public void jobUnscheduled(TriggerKey triggerKey) {
        super.jobUnscheduled(triggerKey);
    }

    @Override
    public void schedulerError(String msg, SchedulerException cause) {
        super.schedulerError(msg, cause);
    }

    @Override
    public void schedulerShutdown() {
        super.schedulerShutdown();
    }

    @Override
    public void schedulerStarted() {
        super.schedulerStarted();
    }

    @Override
    public void schedulerStarting() {
        super.schedulerStarting();
    }
}
