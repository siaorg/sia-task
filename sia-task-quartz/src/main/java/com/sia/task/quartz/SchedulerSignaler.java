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

package com.sia.task.quartz;

import com.sia.task.quartz.exception.SchedulerException;
import com.sia.task.quartz.job.JobKey;
import com.sia.task.quartz.job.trigger.Trigger;

/**
 * An interface to be used by <code>JobStore</code> instances in order to
 * communicate signals back to the <code>QuartzScheduler</code>.
 * 
 * @author jhouse
 */
public interface SchedulerSignaler {

    void notifyTriggerListenersMisfired(Trigger trigger);

    void notifySchedulerListenersFinalized(Trigger trigger);

    void notifySchedulerListenersJobDeleted(JobKey jobKey);

    void signalSchedulingChange(long candidateNewNextFireTime);

    void notifySchedulerListenersError(String string, SchedulerException jpe);
}
