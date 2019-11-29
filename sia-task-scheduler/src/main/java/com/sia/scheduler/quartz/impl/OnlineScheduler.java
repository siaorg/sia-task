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

import com.sia.core.helper.StringHelper;
import com.sia.scheduler.log.jobfile.LoggerBuilder;
import com.sia.scheduler.quartz.BaseScheduler;
import com.sia.scheduler.quartz.trigger.TriggerBuildHandler;
import com.sia.scheduler.thread.ExecutorPoolService;
import com.sia.scheduler.util.constant.Constants;
import org.quartz.*;
import org.quartz.impl.matchers.GroupMatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * The class used to define the method that operates the quartz schedule
 *
 * @see
 * @author maozhengwei
 * @date 2018-04-19 19:59
 * @version V1.0.0
 **/
public class OnlineScheduler extends BaseScheduler {

    private final static Logger LOGGER = LoggerFactory.getLogger(OnlineScheduler.class);

    /**
     * scheduleJob
     *
     * @param jobKey
     * @param jobGroup
     * @param trigerType
     * @param trigerValue
     * @param clazz
     */
    public void scheduleJob(String jobKey, String jobGroup, String trigerType, String trigerValue, Class<? extends Job> clazz) {
        try {
            Trigger trigger = TriggerBuildHandler.build(jobKey, jobGroup, trigerType, trigerValue);
            JobDetail jobDetail = JobBuilder.newJob(clazz)
                    .withIdentity(jobKey, trigger.getKey().getGroup()).build();
            scheduler.scheduleJob(jobDetail, trigger);
            startJob();
        } catch (SchedulerException e) {
            LOGGER.error(Constants.LOG_PREFIX + "scheduleJob fail:", e);
        }
    }

    /**
     * Start scheduler
     */
    public void startJob() {

        try {
            if (!scheduler.isShutdown()) {
                scheduler.start();
            }
        } catch (Exception e) {
            LOGGER.error(Constants.LOG_PREFIX + "scheduler start Exception:", e);
        }
    }

    /**
     * Mission suspension
     *
     * @param jobKey
     * @param jobGroup
     * @return
     * @throws SchedulerException
     */
    @Deprecated
    public boolean pauseJob(String jobGroup, String jobKey) throws SchedulerException {

        boolean pauseResult = false;
        TriggerKey triggerKey = TriggerKey.triggerKey(jobKey, jobGroup);
        if (checkExists(jobKey, jobGroup)) {
            scheduler.pauseTrigger(triggerKey);
            pauseResult = true;
        }
        LOGGER.info(Constants.LOG_PREFIX + " pauseJob is {}, jobKey:{}", pauseResult, jobKey);
        return pauseResult;
    }

    /**
     * Task recovery
     *
     * @param jobKey
     * @param jobGroup
     * @return
     * @throws SchedulerException
     */
    @Deprecated
    public boolean resumeJob(String jobGroup, String jobKey) throws SchedulerException {

        boolean resumeResult = false;
        TriggerKey triggerKey = TriggerKey.triggerKey(jobKey, jobGroup);
        if (checkExists(jobKey, jobGroup)) {
            scheduler.resumeTrigger(triggerKey);
            resumeResult = true;
        }
        LOGGER.info(Constants.LOG_PREFIX + " resumeJob is {}, this job is not exists, jobKey:{}", resumeResult, jobKey);
        return resumeResult;
    }

    /**
     * Remove task
     *
     * @param jobKey
     * @param jobGroup
     * @return
     * @throws SchedulerException
     */
    public boolean removeJob(String jobGroup, String jobKey) throws SchedulerException {

        boolean removeResult = false;
        try {
            interrupt(jobKey, jobGroup);
            removeResult = scheduler.deleteJob(JobKey.jobKey(jobKey, jobGroup));
            if(scheduler.getJobKeys(GroupMatcher.groupEquals(jobGroup)).size() == 0) {
                ExecutorPoolService.releaseExecutorService(jobGroup);
            }
            if(removeResult) {
                LoggerBuilder.releaseLogger(jobKey);
            }

        } catch (Exception e) {
            LOGGER.info(Constants.LOG_PREFIX + " removeJob Exception : ", e);
        }
        LOGGER.info(Constants.LOG_PREFIX + " removeJob is : {}, jobKey:{}", removeResult, jobKey);
        return removeResult;
    }

    /**
     * Interrupt task
     *
     * @param jobKey
     * @param jobGroup
     * @return
     * @throws SchedulerException
     */
    public boolean interrupt(String jobKey, String jobGroup) throws SchedulerException {

        boolean removeResult = false;
        if (StringHelper.isEmpty(jobGroup) || StringHelper.isEmpty(jobKey)) {
            return removeResult;
        }
        removeResult = scheduler.interrupt(JobKey.jobKey(jobKey, jobGroup));
        return removeResult;
    }

    /**
     * check if exists
     *
     * @param jobKey
     * @param jobGroup
     * @return
     * @throws SchedulerException
     */
    public boolean checkExists(String jobKey, String jobGroup) throws SchedulerException {

        return  scheduler.checkExists(JobKey.jobKey(jobKey, jobGroup)) || scheduler.checkExists(TriggerKey.triggerKey(jobKey, jobGroup));
    }
}
