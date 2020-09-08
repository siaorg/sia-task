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

package com.sia.task.register.zookeeper.impl;

import com.sia.task.core.ModifyOnlineJobStatus;
import com.sia.task.core.exceptions.TaskBaseExecutionException;
import com.sia.task.core.task.DagTask;
import com.sia.task.core.task.SiaJobStatus;
import com.sia.task.core.util.Constant;
import com.sia.task.integration.curator.Curator4Scheduler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 * @author maozhengwei
 * @version V1.0.0
 * @description
 * @data 2019-10-11 15:36
 * @see
 **/
@Slf4j
public class ModifyOnlineJobStatusByZookeeperImpl implements ModifyOnlineJobStatus {

    @Autowired
    private Curator4Scheduler curator4Scheduler;


    @Override
    public String getJobScheduler(DagTask onlineTask) {

        List<String> jobScheduler = curator4Scheduler.getJobScheduler(onlineTask.getJobGroup(), onlineTask.getJobKey());
        return (jobScheduler != null && jobScheduler.size() > 0) ? jobScheduler.get(0) : null;
    }

    @Override
    public String getJobStatus(DagTask onlineTask) {
        return curator4Scheduler.getJobStatus(onlineTask.getJobGroup(), onlineTask.getJobKey());
    }

    @Override
    public boolean isJobOwner(DagTask onlineTask, String schedulerInstance) {
        return schedulerInstance.equals(getJobScheduler(onlineTask));
    }

    @Override
    public boolean stopJobStatus(DagTask onlineTask, String message) {
        log.info(Constant.LOG_PREFIX + "stopJobStatus message {}", message);
        return curator4Scheduler.casJobStatus4Scheduler(onlineTask.getJobGroup(), onlineTask.getJobKey(), Constant.LOCALHOST, curator4Scheduler.getJobStatus(onlineTask.getJobGroup(), onlineTask.getJobKey()), SiaJobStatus.STOP.getStatus());
    }

    @Override
    public boolean startJobStatus(DagTask onlineTask) {
        return curator4Scheduler.casJobStatus4Scheduler(onlineTask.getJobGroup(), onlineTask.getJobKey(), Constant.LOCALHOST, SiaJobStatus.READY.getStatus(), SiaJobStatus.RUNNING.getStatus());
    }

    @Override
    public boolean completedJobStatus(DagTask onlineTask) {
        return curator4Scheduler.casJobStatus4Scheduler(onlineTask.getJobGroup(), onlineTask.getJobKey(), Constant.LOCALHOST, SiaJobStatus.RUNNING.getStatus(), SiaJobStatus.READY.getStatus());
    }

    @Override
    public boolean runningJobStatus(DagTask onlineTask) {
        return curator4Scheduler.casJobStatus4Scheduler(onlineTask.getJobGroup(), onlineTask.getJobKey(), Constant.LOCALHOST, SiaJobStatus.READY.getStatus(), SiaJobStatus.RUNNING.getStatus());
    }

    @Override
    public boolean pauseJobStatus(DagTask onlineTask) {
        return false;
    }

    @Override
    public boolean releaseJob(DagTask onlineTask) throws TaskBaseExecutionException {
        return curator4Scheduler.releaseJob(onlineTask.getJobGroup(), onlineTask.getJobKey(), Constant.LOCALHOST);
    }

}
