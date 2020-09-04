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

package com.sia.task.core;


import com.sia.task.core.entity.BasicJob;
import com.sia.task.core.task.DagTask;

import java.util.List;

/**
 * Scheduler API
 * <p>Keys are composed of both a jobKey and jobGroup, and the jobKey must be unique
 * within the jobGroup.  If only a jobKey is specified then the default jobGroup
 * jobKey will be used.</p>
 *
 * @author maozhengwei
 * @version V1.0.0
 * @data 2019-10-24 10:28
 **/
public interface INotifyScheduler<T> {

    /**
     * @param job
     * @param mTasks
     * @param taskClass
     * @return
     */
    boolean addJob(BasicJob job, List<DagTask> mTasks, Class<? extends T> taskClass);

    /**
     * Starts the <code>Scheduler</code>'s threads that fire Triggers.
     * When a scheduler is first created it is in "stand-by" mode, and will not
     * fire triggers.  The scheduler can also be put into stand-by mode by
     * calling the <code>standby()</code> method.
     *
     * @param jobGroup group schedulers through jobGroup
     */
    void startJob(String jobGroup);

    /**
     * Pause the <code>JobDetail</code> with the given
     * key - by pausing all of its current <code>Trigger</code>s.
     *
     * @param jobGroup
     * @param jobKey
     * @return
     */
    boolean pauseJob(String jobGroup, String jobKey);

    /**
     * Resume (un-pause) the <code>{JobDetail}</code> with
     * the given key.
     *
     * <p>
     * If any of the <code>Job</code>'s<code>Trigger</code> s missed one
     * or more fire-times, then the <code>Trigger</code>'s misfire
     * instruction will be applied.
     * </p>
     *
     * @param jobGroup
     * @param jobKey
     * @return
     */
    boolean resumeJob(String jobGroup, String jobKey);

    /**
     * Delete the identified <code>Job</code> from the Scheduler - and any
     * associated <code>Trigger</code>s.
     *
     * @param jobGroup
     * @param jobKey
     * @return
     */
    boolean removeJob(String jobGroup, String jobKey);

    /**
     * Request the interruption, within this Scheduler instance, of all
     * currently executing instances of the identified <code>Job</code>, which
     * must be an implementor of the <code>InterruptableJob</code> interface.
     *
     * <p>
     * This method is not cluster aware.  That is, it will only interrupt
     * instances of the identified InterruptableJob currently executing in this
     * Scheduler instance, not across the entire cluster.
     * </p>
     *
     * @param jobKey
     * @param jobGroup
     * @return true if at least one instance of the identified job was found and interrupted.
     */
    boolean interrupt(String jobKey, String jobGroup);

    /**
     * Determine whether a { Job} with the given identifier already
     * exists within the scheduler.
     *
     * @param jobGroup
     * @param jobKey
     * @return
     */
    boolean checkExists(String jobGroup, String jobKey);

    /**
     * Trigger the identified <code>{Job}</code> (execute it
     * now) - with a non-volatile trigger.
     *
     * @param jobGroup
     * @param jobKey
     * @return
     */
    boolean triggerJob(String jobGroup, String jobKey);

    /**
     *
     * @return
     */
    Class getOnlineTaskClass();

}
