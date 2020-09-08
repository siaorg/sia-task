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

import java.io.Serializable;


/**
 * OnlineTaskDetail Task 运行时的状态承载实例
 *
 * @author maozhengwei
 * @version V1.0.0
 * @date 2019/10/12 11:02 上午
 **/
public interface SiaTaskDetail extends Serializable {

    /**
     * OnlineTask 执行Task的逻辑封装接口
     *
     * @return
     */
    Class<? extends OnlineTask> getOnlineTaskClass();

    /**
     * DagTask 当前要执行的Task
     *
     * @return
     */
    DagTask getDagTask();

    /**
     * The carrier that configures the task running parameters
     * @param mTask 当前要执行的Task
     */
    void setDagTask(DagTask mTask);
}
