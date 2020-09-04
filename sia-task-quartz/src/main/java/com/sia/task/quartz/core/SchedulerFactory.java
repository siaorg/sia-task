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

package com.sia.task.quartz.core;

import com.sia.task.quartz.exception.SchedulerException;

import java.util.Collection;

/**
 * Provides a mechanism for obtaining client-usable handles to <code>Scheduler</code>
 * instances.
 * 
 * @see Scheduler
 * @see StdSchedulerFactory
 *
 * @author @see Quartz
 * @data 2019-06-24 17:08
 * @version V1.0.0
 **/
public interface SchedulerFactory {

    /**
     * <p>
     * Returns a client-usable handle to a <code>Scheduler</code>.
     * </p>
     * 
     * @throws SchedulerException
     *           if there is a problem with the underlying <code>Scheduler</code>.
     */
    Scheduler getScheduler() throws SchedulerException;

    /**
     * <p>
     * Returns a handle to the Scheduler with the given name, if it exists.
     * </p>
     */
    Scheduler getScheduler(String schedName) throws SchedulerException;

    /**
     * <p>
     * Returns handles to all known Schedulers (made by any SchedulerFactory
     * within this jvm.).
     * </p>
     */
    Collection<Scheduler> getAllSchedulers() throws SchedulerException;

}
