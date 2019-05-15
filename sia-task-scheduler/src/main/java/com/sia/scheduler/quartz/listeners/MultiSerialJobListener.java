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
import org.quartz.*;
import org.quartz.impl.matchers.EverythingMatcher;
import org.quartz.spi.ClassLoadHelper;
import org.quartz.spi.SchedulerPlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * Serial Job
 *
 * This class is the processing logic class of the cascaded job.
 *
 * @see
 * @author maozhengwei
 * @date 2019-03-05 15:47
 * @version V1.0.0
 **/
public class MultiSerialJobListener extends CommonService implements SchedulerPlugin, JobListener {

    private final static Logger LOGGER = LoggerFactory.getLogger(MultiSerialJobListener.class);

    private String name;

    /**
     * <p>
     * Get the name of the <code>JobListener</code>.
     * </p>
     */
    @Override
    public String getName() {
        return name;
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
        LOGGER.info("this is method MultiSerialJobListener jobToBeExecuted " + context.getJobDetail().getKey());
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
        LOGGER.info("this is method MultiSerialJobListener jobWasExecuted " + context.getJobDetail().getKey());

        String jobKey = context.getJobDetail().getKey().getName();
        String group = context.getJobDetail().getKey().getGroup();
        BasicJob basicJob = basicJobService.getJob(group, jobKey);
        String jobParentKey = basicJob.getJobParentKey();
        String plan = basicJob.getJobPlan();

        if (!StringHelper.isEmpty(plan)) {
            //JOB运行完成
            String jobStatus = curator4Scheduler.getJobStatus(basicJob.getJobGroup(), basicJob.getJobKey());
            if (JobStatus.READY.toString().equals(jobStatus)) {

                shouldStartPostTask(basicJob);
            }

            //是否是plan后置 则关闭自己；
            if (plan.equals(jobParentKey) && JobStatus.READY.toString().equals(jobStatus)) {

                shouldStopPostTask(basicJob);
            }
        }


    }

    /**
     * <p>
     * Called during creation of the <code>Scheduler</code> in order to give
     * the <code>SchedulerPlugin</code> a chance to initialize.
     * </p>
     *
     * <p>
     * At this point, the Scheduler's <code>JobStore</code> is not yet
     * initialized.
     * </p>
     *
     * <p>
     * If you need direct access your plugin, for example during <code>Job</code>
     * execution, you can have this method explicitly put a
     * reference to this plugin in the <code>Scheduler</code>'s
     * <code>SchedulerContext</code>.
     * </p>
     *
     * @param name       The name by which the plugin is identified.
     * @param scheduler  The scheduler to which the plugin is registered.
     * @param loadHelper The classLoadHelper the <code>SchedulerFactory</code> is
     *                   actually using
     * @throws SchedulerConfigException if there is an error initializing.
     */
    @Override
    public void initialize(String name, Scheduler scheduler, ClassLoadHelper loadHelper) throws SchedulerException {
        LOGGER.info("this is method MultiSerialJobListener initialize " + scheduler.getSchedulerName());

        //init
        this.name = name;
        scheduler.getListenerManager().addJobListener(this, EverythingMatcher.allJobs());
        //scheduler.getListenerManager().addJobListener(this, NameMatcher.jobNameContains("TRIGGER_TYPE_PLAN"));

    }

    /**
     * <p>
     * Called when the associated <code>Scheduler</code> is started, in order
     * to let the plug-in know it can now make calls into the scheduler if it
     * needs to.
     * </p>
     */
    @Override
    public void start() {
        LOGGER.info("this is method MultiSerialJobListener start ");
    }

    /**
     * <p>
     * Called in order to inform the <code>SchedulerPlugin</code> that it
     * should free up all of it's resources because the scheduler is shutting
     * down.
     * </p>
     */
    @Override
    public void shutdown() {

    }
}
