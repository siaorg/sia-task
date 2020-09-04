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

package com.sia.task.quartz.core;

import com.sia.task.quartz.exception.SchedulerException;
import com.sia.task.quartz.exception.UnableToInterruptJobException;
import com.sia.task.quartz.job.JobDataMap;
import com.sia.task.quartz.job.JobDetail;
import com.sia.task.quartz.job.JobExecutionContext;
import com.sia.task.quartz.job.JobKey;
import com.sia.task.quartz.job.matchers.GroupMatcher;
import com.sia.task.quartz.job.trigger.OperableTrigger;
import com.sia.task.quartz.job.trigger.Trigger;
import com.sia.task.quartz.job.trigger.TriggerKey;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * <p>
 * The IQuartzScheduler interface is the core interface of MQuartz,
 * which stipulates all standard operations of Scheduler,
 * and discards the original <code> RemotableQuartzScheduler <code/> dependency on RMI components; making it simpler.
 * </p>
 *
 * @description
 * @see
 * @author @see Quartz
 * @data 2019-06-24 14:32
 * @version V1.0.0
 **/
public interface IQuartzScheduler {


    String getSchedulerName() ;

    String getSchedulerInstanceId() ;

    SchedulerContext getSchedulerContext() throws SchedulerException;

    void start() throws SchedulerException;

    void startDelayed(int seconds) throws SchedulerException;

    void standby() ;

    boolean isInStandbyMode() ;

    void shutdown() ;

    void shutdown(boolean waitForJobsToComplete) ;

    boolean isShutdown() ;

    Date runningSince() ;

    String getVersion() ;

    int numJobsExecuted() ;

    Class<?> getJobStoreClass() ;

    boolean supportsPersistence() ;

    boolean isClustered() ;

    Class<?> getThreadPoolClass() ;

    int getThreadPoolSize() ;

    void clear() throws SchedulerException;

    List<JobExecutionContext> getCurrentlyExecutingJobs() throws SchedulerException;

    Date scheduleJob(JobDetail jobDetail, Trigger trigger) throws SchedulerException;

    Date scheduleJob(Trigger trigger) throws SchedulerException;

    void addJob(JobDetail jobDetail, boolean replace) throws SchedulerException;

    void addJob(JobDetail jobDetail, boolean replace, boolean storeNonDurableWhileAwaitingScheduling) throws SchedulerException;

    boolean deleteJob(JobKey jobKey) throws SchedulerException;

    boolean unscheduleJob(TriggerKey triggerKey) throws SchedulerException;

    Date rescheduleJob(TriggerKey triggerKey, Trigger newTrigger) throws SchedulerException;

    void triggerJob(JobKey jobKey, JobDataMap data) throws SchedulerException;

    void triggerJob(OperableTrigger trig) throws SchedulerException;

    void pauseTrigger(TriggerKey triggerKey) throws SchedulerException;

    void pauseTriggers(GroupMatcher<TriggerKey> matcher) throws SchedulerException;

    void pauseJob(JobKey jobKey) throws SchedulerException;

    void pauseJobs(GroupMatcher<JobKey> matcher) throws SchedulerException;

    void resumeTrigger(TriggerKey triggerKey) throws SchedulerException;

    void resumeTriggers(GroupMatcher<TriggerKey> matcher) throws SchedulerException;

    Set<String> getPausedTriggerGroups() throws SchedulerException;

    void resumeJob(JobKey jobKey) throws SchedulerException;

    void resumeJobs(GroupMatcher<JobKey> matcher) throws SchedulerException;

    void pauseAll() throws SchedulerException;

    void resumeAll() throws SchedulerException;

    List<String> getJobGroupNames() throws SchedulerException;

    Set<JobKey> getJobKeys(GroupMatcher<JobKey> matcher) throws SchedulerException;

    List<? extends Trigger> getTriggersOfJob(JobKey jobKey) throws SchedulerException;

    List<String> getTriggerGroupNames() throws SchedulerException;

    Set<TriggerKey> getTriggerKeys(GroupMatcher<TriggerKey> matcher) throws SchedulerException;

    JobDetail getJobDetail(JobKey jobKey) throws SchedulerException;

    Trigger getTrigger(TriggerKey triggerKey) throws SchedulerException;

    Trigger.TriggerState getTriggerState(TriggerKey triggerKey) throws SchedulerException;

    void resetTriggerFromErrorState(TriggerKey triggerKey) throws SchedulerException;

    void addCalendar(String calName, Calendar calendar, boolean replace, boolean updateTriggers) throws SchedulerException;

    boolean deleteCalendar(String calName) throws SchedulerException;

    Calendar getCalendar(String calName) throws SchedulerException;

    List<String> getCalendarNames() throws SchedulerException;

    boolean interrupt(JobKey jobKey) throws UnableToInterruptJobException;

    boolean interrupt(String fireInstanceId) throws UnableToInterruptJobException;

    boolean checkExists(JobKey jobKey) throws SchedulerException;

    boolean checkExists(TriggerKey triggerKey) throws SchedulerException;

    boolean deleteJobs(List<JobKey> jobKeys) throws SchedulerException;

    void scheduleJobs(Map<JobDetail, Set<? extends Trigger>> triggersAndJobs, boolean replace) throws SchedulerException;

    void scheduleJob(JobDetail jobDetail, Set<? extends Trigger> triggersForJob, boolean replace) throws SchedulerException;

    boolean unscheduleJobs(List<TriggerKey> triggerKeys) throws SchedulerException;

}

