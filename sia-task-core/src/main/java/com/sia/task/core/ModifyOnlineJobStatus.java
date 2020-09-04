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

import com.sia.task.core.exceptions.TaskBaseExecutionException;
import com.sia.task.core.task.DagTask;

/**
 * <p>
 * Provides job-related operations in cluster mode.
 * Job-related operations must be consistent, and all node information must be synchronized in real time,
 * so the implementation of this interface must ensure that changes to it are made available to all nodes. status。
 * </p>
 *
 * <p>
 * Here only the job-related operations are declared, and no specific implementation is given.
 * The specific implementation should be implemented by components that provide distributed consistency, such as like: zookeeper, redis, mysql, etc.
 * </p>
 *
 * @author maozhengwei
 * @version V1.0.0
 * @data 2019-10-11 15:43
 * @see
 **/
public interface ModifyOnlineJobStatus {

    /**
     * <p>
     * Get the scheduler that hosts the current job. The scheduler should be unique. A job can only be owned by one scheduler.
     * </p>
     *
     * @param onlineTask
     * @return Returns instance information of a specific scheduler: ip + port
     * @throws TaskBaseExecutionException
     */
    String getJobScheduler(DagTask onlineTask) throws TaskBaseExecutionException;

    /**
     * Gets the runtime status of the specified job.
     *
     * @param onlineTask
     * @return
     * @throws TaskBaseExecutionException
     */
    String getJobStatus(DagTask onlineTask) throws TaskBaseExecutionException;

    /**
     * Determine whether the job belongs to the specified scheduler
     *
     * @param onlineTask
     * @param schedulerInstance
     * @return
     * @throws TaskBaseExecutionException
     */
    boolean isJobOwner(DagTask onlineTask, String schedulerInstance) throws TaskBaseExecutionException;

    /**
     * Change the status of the job to stop
     *
     * @param onlineTask
     * @param message
     * @return
     * @throws TaskBaseExecutionException
     */
    boolean stopJobStatus(DagTask onlineTask, String message) throws TaskBaseExecutionException;

    /**
     * Change the status of the job to start
     *
     * @param onlineTask
     * @return
     * @throws TaskBaseExecutionException
     */
    boolean startJobStatus(DagTask onlineTask) throws TaskBaseExecutionException;

    /**
     * Change the status of the job to completed
     *
     * @param onlineTask
     * @return
     * @throws TaskBaseExecutionException
     */
    boolean completedJobStatus(DagTask onlineTask) throws TaskBaseExecutionException;

    /**
     * Change the status of the job to running
     *
     * @param onlineTask
     * @return
     * @throws TaskBaseExecutionException
     */
    boolean runningJobStatus(DagTask onlineTask) throws TaskBaseExecutionException;

    /**
     * Change the status of the job to pause
     *
     * @param onlineTask
     * @return
     * @throws TaskBaseExecutionException
     */
    boolean pauseJobStatus(DagTask onlineTask) throws TaskBaseExecutionException;

    /**
     * releaseJob
     * @param onlineTask
     * @return
     * @throws TaskBaseExecutionException
     */
    boolean releaseJob(DagTask onlineTask) throws TaskBaseExecutionException;
}
