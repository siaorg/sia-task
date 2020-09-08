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

import java.util.List;

public interface OnlineListenerManager {


    /**
     * Add the given <code>{@link TaskListener}</code> to the <code>Scheduler</code>,
     * and register it to receive events for all Tasks.
     *
     * Because no matchers are provided, the <code>EverythingMatcher</code> will be used.
     *
     */
    void addTaskListener(TaskListener taskListener);


    /**
     * Remove the identified <code>{@link TaskListener}</code> from the <code>Scheduler</code>.
     *
     * @return true if the identified listener was found in the list, and
     *         removed.
     */
    boolean removeTaskListener(String name);


    /**
     * Register the given <code>{@link TaskListener}</code> with the
     * <code>Scheduler</code>.
     */
    List<TaskListener> getTaskListeners();
}
