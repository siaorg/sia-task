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

package com.sia.task.quartz.job;

import com.sia.task.quartz.core.Scheduler;
import com.sia.task.quartz.exception.JobExecutionException;
import com.sia.task.quartz.job.trigger.Trigger;
import com.sia.task.quartz.listeners.JobListener;
import com.sia.task.quartz.listeners.TriggerListener;

/**
 * <p>
 * The interface to be implemented by classes which represent a 'job' to be
 * performed.
 * </p>
 * 
 * <p>
 * Instances of <code>Job</code> must have a <code>public</code>
 * no-argument constructor.
 * </p>
 * 
 * <p>
 * <code>JobDataMap</code> provides a mechanism for 'instance member data'
 * that may be required by some implementations of this interface.
 * </p>
 *
 * @see
 * @author @see Quartz
 * @data 2019-06-22 23:59
 * @version V1.0.0
 **/
public interface Job {

    /**
     * <p>
     * Called by the <code>{@link Scheduler}</code> when a <code>{@link Trigger}</code>
     * fires that is associated with the <code>Job</code>.
     * </p>
     * 
     * <p>
     * The implementation may wish to set a 
     * {@link JobExecutionContext#setResult(Object) result} object on the
     * {@link JobExecutionContext} before this method exits.  The result itself
     * is meaningless to Quartz, but may be informative to 
     * <code>{@link JobListener}s</code> or
     * <code>{@link TriggerListener}s</code> that are watching the job's
     * execution.
     * </p>
     * 
     * @throws JobExecutionException
     *           if there is an exception while executing the job.
     */
    void execute(JobExecutionContext context)
        throws JobExecutionException;

}
