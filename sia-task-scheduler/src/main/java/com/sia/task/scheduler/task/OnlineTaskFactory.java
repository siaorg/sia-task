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


import com.sia.task.core.exceptions.SchedulerBaseException;

/**
 * Called by the TaskRunShell at the time of the <code>OnlineJob</code> firing, in order to
 * produce a <code>OnlineTask</code> instance on which to call execute.
 *
 * @author maozhengwei
 * @version V1.0.0
 * @description
 * @data 2019-10-11 21:05
 * @see
 **/
public interface OnlineTaskFactory {

    /**
     * Build a running instance of OnlineTask
     *
     * @param taskBundle
     * @return OnlineTask
     * @throws SchedulerBaseException
     */
    OnlineTask newOnlineTask(TriggerOnlineTaskBundle taskBundle) throws SchedulerBaseException;
}
