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


import com.sia.task.core.task.DagTask;

import java.util.List;

/**
 * <p>
 * This is an interface that provides an executor selection for the <code>OnlineScheduler</code> to execute a running instance of the task.
 * </p>
 * <p>
 * The interface provides an interface for obtaining execution,
 * and an executor instance that conforms to its policy can be selected according to different strategies of the <code>JobMTask</code>;
 * </p>
 *
 * @author maozhengwei
 * @version V1.0.0
 * @description
 * @data 2019-10-18 14:26
 * @see
 **/
public interface IExecutorSelector {

    /**
     * get an executor instance
     *
     * @param task
     * @return
     * @throws Exception
     */
    List<String> getTaskExecutor(DagTask task) throws Exception;
}
