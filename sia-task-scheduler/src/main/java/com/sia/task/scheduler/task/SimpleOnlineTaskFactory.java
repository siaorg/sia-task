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
import lombok.extern.slf4j.Slf4j;

/**
 * The default OnlineTaskFactory used by OnlineSchedule - simply calls
 * <code>newInstance()</code> on the OnlineTask class.
 *
 * @author maozhengwei
 * @version V1.0.0
 * @description
 * @data 2019-10-12 10:43
 * @see
 **/
@Slf4j
public class SimpleOnlineTaskFactory implements OnlineTaskFactory {

    /**
     * @return
     */
    @Override
    public OnlineTask newOnlineTask(TriggerOnlineTaskBundle taskBundle) throws SchedulerBaseException {

        SiaTaskDetail detail = taskBundle.getOnlineTaskDetail();

        Class<? extends OnlineTask> taskClass = detail.getOnlineTaskClass();

        try {
            if (log.isDebugEnabled()) {
                log.debug("Producing instance of OnlineTask '" + detail.getDagTask() + "', class= " + taskClass.getName());
            }
            log.info("Producing instance of OnlineTask '" + detail.getDagTask() + "', class= " + taskClass.getName());
            return taskClass.newInstance();

        } catch (Exception e) {
            throw new SchedulerBaseException("Problem instantiating class '" + detail.getOnlineTaskClass().getName() + "'", e);
        }
    }
}
