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

package com.sia.task.scheduler.core;

import com.sia.task.core.IExecutorSelector;
import com.sia.task.core.ModifyOnlineJobStatus;
import com.sia.task.core.exceptions.SchedulerBaseException;
import com.sia.task.core.exceptions.TaskBaseExecutionException;
import com.sia.task.core.util.Constant;
import com.sia.task.scheduler.exception.OnlineTaskExecutionException;
import com.sia.task.scheduler.listeners.OnlineListenerManager;
import com.sia.task.scheduler.listeners.OnlineListenerManagerImpl;
import com.sia.task.scheduler.listeners.TaskListener;
import com.sia.task.scheduler.task.OnlineTaskFactory;
import com.sia.task.scheduler.task.SimpleOnlineTaskFactory;
import com.sia.task.scheduler.task.TriggerOnlineTaskBundle;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author maozhengwei
 * @version V1.0.0
 * @description
 * @data 2019-10-10 20:31
 * @see
 **/
@Slf4j
public final class OnlineScheduler {

    private OnlineListenerManager listenerManager = new OnlineListenerManagerImpl();

    private Map<String, TaskListener> internalTaskListeners = new ConcurrentHashMap<>();

    private ModifyOnlineJobStatus modifyOnlineJobStatus;

    private IExecutorSelector executorSelector;

    private OnlineTaskFactory onlineTaskFactory = new SimpleOnlineTaskFactory();

    public OnlineListenerManager getListenerManager() {
        return listenerManager;
    }

    private Collection<? extends TaskListener> getInternalTaskListeners() {
        return Collections.unmodifiableList(new LinkedList<>(internalTaskListeners.values()));
    }

    public void setModifyOnlineJobStatus(ModifyOnlineJobStatus modifyOnlineJobStatus) {
        this.modifyOnlineJobStatus = modifyOnlineJobStatus;
    }

    public ModifyOnlineJobStatus getModifyOnlineJobStatus() {
        return this.modifyOnlineJobStatus;
    }

    public void setIExecutorSelector(IExecutorSelector executorSelector) {
        this.executorSelector = executorSelector;
    }

    public IExecutorSelector getIExecutorSelector() {
        return this.executorSelector;
    }

    public void setOnlineTaskFactory(OnlineTaskFactory onlineTaskFactory) {
        if (onlineTaskFactory == null) {
            throw new IllegalArgumentException("OnlineTaskFactory cannot be set to null!");
        }
        log.info(Constant.LOG_PREFIX + "OnlineTaskFactory set to: {}", onlineTaskFactory);
        this.onlineTaskFactory = onlineTaskFactory;
    }

    public OnlineTaskFactory getOnlineTaskFactory() {
        return onlineTaskFactory;
    }

    /**
     * build a list of all task listeners that are to be notified...
     *
     * @return
     */
    private List<TaskListener> buildTaskListenerList() {
        List<TaskListener> taskListeners = new LinkedList<>();
        taskListeners.addAll(getListenerManager().getTaskListeners());
        taskListeners.addAll(getInternalTaskListeners());
        return taskListeners;
    }


    public void notifyTaskListenersExecuteStarted(TriggerOnlineTaskBundle taskBundle) throws TaskBaseExecutionException {

        List<TaskListener> taskListeners = buildTaskListenerList();

        for (TaskListener taskListener : taskListeners) {
            try {
                taskListener.executeStarted(taskBundle);
            } catch (Exception e) {
                throw new OnlineTaskExecutionException("TaskListener '" + taskListener.getName() + "' threw exception :" + e.getMessage(), e);
            }
        }
    }

    public void notifyTaskListenersExecuted(TriggerOnlineTaskBundle taskBundle) throws TaskBaseExecutionException {

        List<TaskListener> taskListeners = buildTaskListenerList();
        //notify all task listeners
        for (TaskListener taskListener : taskListeners) {
            try {
                taskListener.taskExecuted(taskBundle);
            } catch (Exception e) {
                throw new OnlineTaskExecutionException("TaskListener '" + taskListener.getName() + "' threw exception :" + e.getMessage(), e);
            }
        }
    }

    public void notifyTaskListenersUnExecuted(TriggerOnlineTaskBundle taskBundle) throws TaskBaseExecutionException {

        List<TaskListener> taskListeners = buildTaskListenerList();
        //notify all task listeners
        for (TaskListener taskListener : taskListeners) {
            try {
                taskListener.taskunExecuted(taskBundle);
            } catch (Exception e) {
                throw new OnlineTaskExecutionException("TaskListener '" + taskListener.getName() + "' threw exception :" + e.getMessage(), e);
            }
        }
    }

    public void notifyTaskListenersExecutedError(TriggerOnlineTaskBundle taskBundle, SchedulerBaseException ose) {

        List<TaskListener> taskListeners = buildTaskListenerList();
        //notify all task listeners
        for (TaskListener taskListener : taskListeners) {
            try {
                taskListener.executedError(taskBundle, ose);
            } catch (Exception e) {
                log.error("An error occurred, the task execution failed to trigger the notification to be abnormal. [{}]", taskListener.getName(), e);
            }
        }
    }
}
