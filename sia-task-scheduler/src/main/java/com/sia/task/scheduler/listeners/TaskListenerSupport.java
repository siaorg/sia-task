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

package com.sia.task.scheduler.listeners;

import com.sia.task.core.exceptions.SchedulerBaseException;
import com.sia.task.core.task.DagTask;
import com.sia.task.scheduler.core.TaskRunShell;
import com.sia.task.scheduler.task.TriggerOnlineTaskBundle;


/**
 * @author maozhengwei
 * @version V1.0.0
 * @description
 * @data 2019-10-11 10:37
 * @see
 **/
public abstract class TaskListenerSupport implements TaskListener {

    /**
     * <p>
     * Get the name of the <code>TaskListener</code>.
     * </p>
     */
    @Override
    public String getName() {
        return null;
    }

    /**
     * <p>
     * Called by the <code>{@link TaskRunShell}</code> when a <code>{@link DagTask}</code>
     * is executeStarted.
     * </p>
     *
     * @param taskBundle
     */
    @Override
    public void executeStarted(TriggerOnlineTaskBundle taskBundle) {

    }

    /**
     * <p>
     * Called by the <code>{@link TaskRunShell}</code> when a <code>{@link DagTask}</code>
     * is executed.
     * </p>
     *
     * @param taskBundle
     */
    @Override
    public void taskExecuted(TriggerOnlineTaskBundle taskBundle) {

    }

    /**
     * <p>
     * Called by the <code>{@link TaskRunShell}</code> when a <code>{@link DagTask}</code>
     * is unExecuted.
     * </p>
     *
     * @param jobMTask
     */
    @Override
    public void taskunExecuted(TriggerOnlineTaskBundle jobMTask) {

    }


    /**
     * <p>
     * Called by the <code>{@link TaskRunShell}</code> when a <code>{@link DagTask}</code>
     * is executedError.
     * </p>
     *
     * @param jobMTask
     * @param ose
     */
    @Override
    public void executedError(TriggerOnlineTaskBundle jobMTask, SchedulerBaseException ose) {

    }
}
