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

import com.sia.task.quartz.SchedulerSignaler;
import com.sia.task.quartz.exception.SchedulerException;
import com.sia.task.quartz.job.JobKey;
import com.sia.task.quartz.job.trigger.Trigger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An interface to be used by <code>JobStore</code> instances in order to
 * communicate signals back to the <code>QuartzScheduler</code>.
 * 
 *
 * @author @see Quartz
 * @data 2019-06-24 18:20
 * @version V1.0.0
 **/
public class SchedulerSignalerImpl implements SchedulerSignaler {

    Logger log = LoggerFactory.getLogger(SchedulerSignalerImpl.class);
    
    /*
     * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
     * 
     * Data members.
     * 
     * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
     */

    protected QuartzScheduler sched;
    protected QuartzSchedulerThread schedThread;

    /*
     * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
     * 
     * Constructors.
     * 
     * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
     */

    public SchedulerSignalerImpl(QuartzScheduler sched, QuartzSchedulerThread schedThread) {
        this.sched = sched;
        this.schedThread = schedThread;
        
        log.info("Initialized Scheduler Signaller of type: " + getClass());
    }

    /*
     * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
     * 
     * Interface.
     * 
     * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
     */

    @Override
    public void notifyTriggerListenersMisfired(Trigger trigger) {
        try {
            sched.notifyTriggerListenersMisfired(trigger);
        } catch (SchedulerException se) {
            sched.getLog().error(
                    "Error notifying listeners of trigger misfire.", se);
            sched.notifySchedulerListenersError(
                    "Error notifying listeners of trigger misfire.", se);
        }
    }

    @Override
    public void notifySchedulerListenersFinalized(Trigger trigger) {
        sched.notifySchedulerListenersFinalized(trigger);
    }

    @Override
    public void signalSchedulingChange(long candidateNewNextFireTime) {
        schedThread.signalSchedulingChange(candidateNewNextFireTime);
    }

    @Override
    public void notifySchedulerListenersJobDeleted(JobKey jobKey) {
        sched.notifySchedulerListenersJobDeleted(jobKey);
    }

    @Override
    public void notifySchedulerListenersError(String string, SchedulerException jpe) {
        sched.notifySchedulerListenersError(string, jpe);
    }
}
