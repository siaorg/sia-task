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

import com.sia.task.core.task.DagTask;
import com.sia.task.core.util.DirectedCycleHelper;
import com.sia.task.scheduler.task.OnlineTask;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * TaskRunShellContext
 * 提供DagTask 运行时所必须的上下文环境
 *
 *
 * @author maozhengwei
 * @version V1.0.0
 * @description
 * @data 2019-10-28 14:49
 * @see
 **/

@Setter
public class TaskRunShellContext {

    /**
     * Task 缓存，未进行排版的原始任务集
     */
    private List<DagTask> firstTasks;

    /**
     * 运行时的任务集
     */
    private List<DagTask> dagTasks;

//    @Getter
//    private OnlineTaskDetail onlineTaskDetail;
    @Getter
    private OnlineScheduler onlineScheduler;

    @Getter
    private Class<? extends OnlineTask> onlineTaskClass;

//    public TaskRunShellContext(List<DagTask> mTasks, OnlineTaskDetail onlineTaskDetail, OnlineScheduler onlineScheduler) {
//        this.firstTasks = mTasks;
//        this.onlineTaskDetail = onlineTaskDetail;
//        this.onlineScheduler = onlineScheduler;
//        this.dagTasks = DirectedCycleHelper.layoutTask(firstTasks);
//    }

    public TaskRunShellContext(List<DagTask> mTasks, OnlineScheduler onlineScheduler) {
        this.firstTasks = mTasks;
        this.onlineScheduler = onlineScheduler;
        this.dagTasks = DirectedCycleHelper.layoutTask(firstTasks);
    }

    public List<DagTask> getFirstTasks() {
        return firstTasks;
    }

    public List<DagTask> getDagTasks() {
        dagTasks = new ArrayList<>();
        firstTasks.forEach(jobMTask -> {
            DagTask jobMTaskClone = jobMTask.deepClone();
            dagTasks.add(jobMTaskClone);
        });
        return DirectedCycleHelper.layoutTask(dagTasks);
    }

}
