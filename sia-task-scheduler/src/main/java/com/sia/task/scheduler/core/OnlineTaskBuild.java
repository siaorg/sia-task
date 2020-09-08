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

import com.sia.task.scheduler.task.OnlineTask;
import com.sia.task.scheduler.task.SiaTaskDetail;
import com.sia.task.scheduler.task.SiaTaskDetailImpl;

public class OnlineTaskBuild {


    private Class<? extends OnlineTask> onlineTaskClass;


    public static OnlineTaskBuild newOnlineTaskBuild(Class<? extends OnlineTask> onlineTaskClass){
        OnlineTaskBuild taskBuild = new OnlineTaskBuild();
        taskBuild.ofType(onlineTaskClass);
        return taskBuild;
    }


    /**
     * Produce the <code>JobDetail</code> instance defined by this
     * <code>JobBuilder</code>.
     *
     * @return the defined JobDetail.
     */
    public SiaTaskDetail build() {

        SiaTaskDetailImpl taskDetail = new SiaTaskDetailImpl();

        taskDetail.setOnlineTaskClass(onlineTaskClass);

        return taskDetail;
    }


    /**
     * Set the class which will be instantiated and executed when a
     * Trigger fires that is associated with this OnlineTaskDetail.
     *
     * @param onlineTaskClass a class implementing the OnlineTask interface.
     * @return the updated OnlineTaskBuild
     * @see SiaTaskDetail#getOnlineTaskClass()
     */
    public OnlineTaskBuild ofType(Class <? extends OnlineTask> onlineTaskClass) {
        this.onlineTaskClass = onlineTaskClass;
        return this;
    }
}
