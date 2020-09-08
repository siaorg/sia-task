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

import com.sia.task.scheduler.listeners.impl.InnerTaskListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


/**
 * 
 * 
 * @description 
 * @see 
 * @author maozhengwei
 * @data 2019-10-10 20:46
 * @version V1.0.0
 **/
public class OnlineListenerManagerImpl implements OnlineListenerManager {


    public OnlineListenerManagerImpl(){
        //初始化内部监听器
        Initialize();
    }

    private void Initialize() {
        addTaskListener(new InnerTaskListener());
    }

    private Map<String, TaskListener> globalTaskListeners = new ConcurrentHashMap<>(10);

    /**
     * Add the given <code>{@link TaskListener}</code> to the <code>Scheduler</code>,
     * and register it to receive events for all Tasks.
     * <p>
     * Because no matchers are provided, the <code>EverythingMatcher</code> will be used.
     *
     * @param taskListener
     */
    @Override
    public void addTaskListener(TaskListener taskListener) {
        if (taskListener.getName() == null || taskListener.getName().length()==0) {
            throw new IllegalArgumentException("TaskListener name cannot be empty.");
        }
        globalTaskListeners.put(taskListener.getName(), taskListener);
    }

    /**
     * Remove the identified <code>{@link TaskListener}</code> from the <code>Scheduler</code>.
     *
     * @param name
     * @return true if the identified listener was found in the list, and
     * removed.
     */
    @Override
    public boolean removeTaskListener(String name) {
        return (globalTaskListeners.remove(name) != null);
    }

    /**
     * Register the given <code>{@link TaskListener}</code> with the
     * <code>Scheduler</code>.
     */
    @Override
    public List<TaskListener> getTaskListeners() {
        return Collections.unmodifiableList(new ArrayList<>(globalTaskListeners.values())) ;
    }
}
