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
 *
 * @author maozhengwei
 * @version V1.0.0
 * @data 2019-10-24 10:28
 **/
public interface INotifyScheduler<T> {

    boolean addJob(BasicJob job, List<DagTask> mTasks, Class<? extends T> taskClass);

    void startJob(String jobGroup);

    boolean pauseJob(String jobGroup, String jobKey);

    boolean resumeJob(String jobGroup, String jobKey);

    boolean removeJob(String jobGroup, String jobKey);

    boolean interrupt(String jobKey, String jobGroup);

    boolean checkExists(String jobGroup, String jobKey);

    boolean triggerJob(String jobGroup, String jobKey);

    Class getOnlineTaskClass();

}
