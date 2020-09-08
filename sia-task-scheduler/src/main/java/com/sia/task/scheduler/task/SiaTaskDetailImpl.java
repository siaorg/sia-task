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

package com.sia.task.scheduler.task;

import com.sia.task.core.task.DagTask;
import lombok.Data;

/**
 * OnlineTaskDetailImpl
 *
 * @author maozhengwei
 * @version V1.0.0
 * @date 2019-10-12 11:01 上午
 **/
public @Data
class SiaTaskDetailImpl implements SiaTaskDetail {

    private Class<? extends OnlineTask> onlineTaskClass;

    private DagTask mTask;

    @Override
    public Class<? extends OnlineTask> getOnlineTaskClass() {
        return onlineTaskClass;
    }

    @Override
    public DagTask getDagTask() {
        return mTask;
    }

    /**
     * The carrier that configures the task running parameters
     *
     * @param mTask
     */
    @Override
    public void setDagTask(DagTask mTask) {
        this.mTask = mTask;
    }

    /**
     * <p>
     * Set the instance of <code>Job</code> that will be executed.
     * </p>
     *
     * @throws IllegalArgumentException if jobClass is null or the class is not a <code>Job</code>.
     */
    public void setOnlineTaskClass(Class<? extends OnlineTask> onlineTaskClass) {
        if (onlineTaskClass == null) {
            throw new IllegalArgumentException("Job class cannot be null.");
        }

        if (!OnlineTask.class.isAssignableFrom(onlineTaskClass)) {
            throw new IllegalArgumentException(
                    "Job class must implement the Job interface.");
        }

        this.onlineTaskClass = onlineTaskClass;
    }
}
