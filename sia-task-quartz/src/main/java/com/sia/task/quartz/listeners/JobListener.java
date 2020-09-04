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

package com.sia.task.quartz.listeners;

import com.sia.task.quartz.core.Scheduler;
import com.sia.task.quartz.exception.JobExecutionException;
import com.sia.task.quartz.job.Job;
import com.sia.task.quartz.job.JobDetail;
import com.sia.task.quartz.job.JobExecutionContext;
import com.sia.task.quartz.job.matchers.Matcher;
import com.sia.task.quartz.job.trigger.Trigger;

/**
 * The interface to be implemented by classes that want to be informed when a
 * <code>{@link JobDetail}</code> executes. In general,
 * applications that use a <code>Scheduler</code> will not have use for this
 * mechanism.
 * 
 * @see ListenerManager#addJobListener(JobListener, Matcher)
 * @see Matcher
 * @see Job
 * @see JobExecutionContext
 * @see JobExecutionException
 * @see TriggerListener
 *
 * @author @see Quartz
 * @data 2019-06-23 14:51
 * @version V1.0.0
 **/
public interface JobListener {

    /*
     * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
     * 
     * Interface.
     * 
     * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
     */

    /**
     * <p>
     * Get the name of the <code>JobListener</code>.
     * </p>
     */
    String getName();

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
     * @see #jobExecutionVetoed(JobExecutionContext)
     */
    void jobToBeExecuted(JobExecutionContext context);

    /**
     * <p>
     * Called by the <code>{@link Scheduler}</code> when a <code>{@link JobDetail}</code>
     * was about to be executed (an associated <code>{@link Trigger}</code>
     * has occurred), but a <code>{@link TriggerListener}</code> vetoed it's
     * execution.
     * </p>
     * 
     * @see #jobToBeExecuted(JobExecutionContext)
     */
    void jobExecutionVetoed(JobExecutionContext context);

    
    /**
     * <p>
     * Called by the <code>{@link Scheduler}</code> after a <code>{@link JobDetail}</code>
     * has been executed, and be for the associated <code>Trigger</code>'s
     * <code>triggered(xx)</code> method has been called.
     * </p>
     */
    void jobWasExecuted(JobExecutionContext context,
                        JobExecutionException jobException);

}
