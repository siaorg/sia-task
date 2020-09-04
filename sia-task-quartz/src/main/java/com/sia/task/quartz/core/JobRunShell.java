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

import com.sia.task.quartz.exception.JobExecutionException;
import com.sia.task.quartz.exception.SchedulerException;
import com.sia.task.quartz.job.Job;
import com.sia.task.quartz.job.JobDetail;
import com.sia.task.quartz.job.JobExecutionContext;
import com.sia.task.quartz.job.impl.JobExecutionContextImpl;
import com.sia.task.quartz.job.trigger.OperableTrigger;
import com.sia.task.quartz.job.trigger.Trigger;
import com.sia.task.quartz.job.trigger.TriggerFiredBundle;
import com.sia.task.quartz.listeners.SchedulerListenerSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>
 * JobRunShell instances are responsible for providing the 'safe' environment
 * for <code>Job</code> s to run in, and for performing all of the work of
 * executing the <code>Job</code>, catching ANY thrown exceptions, updating
 * the <code>Trigger</code> with the <code>Job</code>'s completion code,
 * etc.
 * </p>
 *
 * <p>
 * A <code>JobRunShell</code> instance is created by a <code>JobRunShellFactory</code>
 * on behalf of the <code>QuartzSchedulerThread</code> which then runs the
 * shell in a thread from the configured <code>ThreadPool</code> when the
 * scheduler determines that a <code>Job</code> has been triggered.
 * </p>
 *
 * @see JobRunShellFactory
 * @see QuartzSchedulerThread
 * @see Job
 * @see Trigger
 *
 * @author @see Quartz
 * @data 2019-06-24 15:24
 * @version V1.0.0
 **/
public class JobRunShell extends SchedulerListenerSupport implements Runnable {


    protected JobExecutionContextImpl jec = null;

    protected QuartzScheduler qs = null;
    
    protected TriggerFiredBundle firedTriggerBundle = null;

    protected Scheduler scheduler = null;

    protected volatile boolean shutdownRequested = false;

    private final Logger log = LoggerFactory.getLogger(getClass());


    /**
     * <p>
     * Create a JobRunShell instance with the given settings.
     * </p>
     *
     * @param scheduler
     *          The <code>Scheduler</code> instance that should be made
     *          available within the <code>JobExecutionContext</code>.
     */
    public JobRunShell(Scheduler scheduler, TriggerFiredBundle bndle) {
        this.scheduler = scheduler;
        this.firedTriggerBundle = bndle;
    }


    @Override
    public void schedulerShuttingdown() {
        requestShutdown();
    }

    @Override
    protected Logger getLog() {
        return log;
    }

    public void initialize(QuartzScheduler sched)
        throws SchedulerException {
        this.qs = sched;

        Job job;
        JobDetail jobDetail = firedTriggerBundle.getJobDetail();

        try {
            job = sched.getJobFactory().newJob(firedTriggerBundle, scheduler);
        } catch (SchedulerException se) {
            sched.notifySchedulerListenersError(
                    "An error occured instantiating job to be executed. job= '"
                            + jobDetail.getKey() + "'", se);
            throw se;
        } catch (Throwable ncdfe) { // such as NoClassDefFoundError
            SchedulerException se = new SchedulerException(
                    "Problem instantiating class '"
                            + jobDetail.getJobClass().getName() + "' - ", ncdfe);
            sched.notifySchedulerListenersError(
                    "An error occured instantiating job to be executed. job= '"
                            + jobDetail.getKey() + "'", se);
            throw se;
        }

        this.jec = new JobExecutionContextImpl(scheduler, firedTriggerBundle, job);
    }

    public void requestShutdown() {
        shutdownRequested = true;
    }

    public void run() {
        qs.addInternalSchedulerListener(this);

        try {
            OperableTrigger trigger = (OperableTrigger) jec.getTrigger();
            JobDetail jobDetail = jec.getJobDetail();

            do {

                JobExecutionException jobExEx = null;
                Job job = jec.getJobInstance();

                try {
                    begin();
                } catch (SchedulerException se) {
                    qs.notifySchedulerListenersError("Error executing Job ("
                            + jec.getJobDetail().getKey()
                            + ": couldn't begin execution.", se);
                    break;
                }

                // notify job & trigger listeners...
                try {
                    if (!notifyListenersBeginning(jec)) {
                        break;
                    }
                } catch(VetoedException ve) {
                    try {
                        Trigger.CompletedExecutionInstruction instCode = trigger.executionComplete(jec, null);
                        qs.notifyJobStoreJobVetoed(trigger, jobDetail, instCode);
                        
                        // QTZ-205
                        // Even if trigger got vetoed, we still needs to check to see if it's the trigger's finalized run or not.
                        if (jec.getTrigger().getNextFireTime() == null) {
                            qs.notifySchedulerListenersFinalized(jec.getTrigger());
                        }

                        complete(true);
                    } catch (SchedulerException se) {
                        qs.notifySchedulerListenersError("Error during veto of Job ("
                                + jec.getJobDetail().getKey()
                                + ": couldn't finalize execution.", se);
                    }
                    break;
                }

                long startTime = System.currentTimeMillis();
                long endTime = startTime;

                // execute the job
                try {
                    log.debug("Calling execute on job " + jobDetail.getKey());
                    job.execute(jec);
                    endTime = System.currentTimeMillis();
                } catch (JobExecutionException jee) {
                    endTime = System.currentTimeMillis();
                    jobExEx = jee;
                    getLog().info("Job " + jobDetail.getKey() +
                            " threw a JobExecutionException: ", jobExEx);
                } catch (Throwable e) {
                    endTime = System.currentTimeMillis();
                    getLog().error("Job " + jobDetail.getKey() +
                            " threw an unhandled Exception: ", e);
                    SchedulerException se = new SchedulerException(
                            "Job threw an unhandled exception.", e);
                    qs.notifySchedulerListenersError("Job ("
                            + jec.getJobDetail().getKey()
                            + " threw an exception.", se);
                    jobExEx = new JobExecutionException(se, false);
                }

                jec.setJobRunTime(endTime - startTime);

                // notify all job listeners
                if (!notifyJobListenersComplete(jec, jobExEx)) {
                    break;
                }

                Trigger.CompletedExecutionInstruction instCode = Trigger.CompletedExecutionInstruction.NOOP;

                // update the trigger
                try {
                    instCode = trigger.executionComplete(jec, jobExEx);
                } catch (Exception e) {
                    // If this happens, there's a bug in the trigger...
                    SchedulerException se = new SchedulerException(
                            "Trigger threw an unhandled exception.", e);
                    qs.notifySchedulerListenersError(
                            "Please report this error to the Quartz developers.",
                            se);
                }

                // notify all trigger listeners
                if (!notifyTriggerListenersComplete(jec, instCode)) {
                    break;
                }

                // update job/trigger or re-execute job
                if (instCode == Trigger.CompletedExecutionInstruction.RE_EXECUTE_JOB) {
                    jec.incrementRefireCount();
                    try {
                        complete(false);
                    } catch (SchedulerException se) {
                        qs.notifySchedulerListenersError("Error executing Job ("
                                + jec.getJobDetail().getKey()
                                + ": couldn't finalize execution.", se);
                    }
                    continue;
                }

                try {
                    complete(true);
                } catch (SchedulerException se) {
                    qs.notifySchedulerListenersError("Error executing Job ("
                            + jec.getJobDetail().getKey()
                            + ": couldn't finalize execution.", se);
                    continue;
                }

                qs.notifyJobStoreJobComplete(trigger, jobDetail, instCode);
                break;
            } while (true);

        } finally {
            qs.removeInternalSchedulerListener(this);
        }
    }

    protected void begin() throws SchedulerException {
    }

    protected void complete(boolean successfulExecution)
        throws SchedulerException {
    }

    public void passivate() {
        jec = null;
        qs = null;
    }

    private boolean notifyListenersBeginning(JobExecutionContext jobExCtxt) throws VetoedException {

        boolean vetoed = false;

        // notify all trigger listeners
        try {
            vetoed = qs.notifyTriggerListenersFired(jobExCtxt);
        } catch (SchedulerException se) {
            qs.notifySchedulerListenersError(
                    "Unable to notify TriggerListener(s) while firing trigger "
                            + "(Trigger and Job will NOT be fired!). trigger= "
                            + jobExCtxt.getTrigger().getKey() + " job= "
                            + jobExCtxt.getJobDetail().getKey(), se);

            return false;
        }

        if(vetoed) {
            try {
                qs.notifyJobListenersWasVetoed(jobExCtxt);
            } catch (SchedulerException se) {
                qs.notifySchedulerListenersError(
                        "Unable to notify JobListener(s) of vetoed execution " +
                        "while firing trigger (Trigger and Job will NOT be " +
                        "fired!). trigger= "
                        + jobExCtxt.getTrigger().getKey() + " job= "
                        + jobExCtxt.getJobDetail().getKey(), se);

            }
            throw new VetoedException();
        }

        // notify all job listeners
        try {
            qs.notifyJobListenersToBeExecuted(jobExCtxt);
        } catch (SchedulerException se) {
            qs.notifySchedulerListenersError(
                    "Unable to notify JobListener(s) of Job to be executed: "
                            + "(Job will NOT be executed!). trigger= "
                            + jobExCtxt.getTrigger().getKey() + " job= "
                            + jobExCtxt.getJobDetail().getKey(), se);

            return false;
        }

        return true;
    }

    private boolean notifyJobListenersComplete(JobExecutionContext jobExCtxt, JobExecutionException jobExEx) {
        try {
            qs.notifyJobListenersWasExecuted(jobExCtxt, jobExEx);
        } catch (SchedulerException se) {
            qs.notifySchedulerListenersError(
                    "Unable to notify JobListener(s) of Job that was executed: "
                            + "(error will be ignored). trigger= "
                            + jobExCtxt.getTrigger().getKey() + " job= "
                            + jobExCtxt.getJobDetail().getKey(), se);

            return false;
        }

        return true;
    }

    private boolean notifyTriggerListenersComplete(JobExecutionContext jobExCtxt, Trigger.CompletedExecutionInstruction instCode) {
        try {
            qs.notifyTriggerListenersComplete(jobExCtxt, instCode);

        } catch (SchedulerException se) {
            qs.notifySchedulerListenersError(
                    "Unable to notify TriggerListener(s) of Job that was executed: "
                            + "(error will be ignored). trigger= "
                            + jobExCtxt.getTrigger().getKey() + " job= "
                            + jobExCtxt.getJobDetail().getKey(), se);

            return false;
        }
        if (jobExCtxt.getTrigger().getNextFireTime() == null) {
            qs.notifySchedulerListenersFinalized(jobExCtxt.getTrigger());
        }

        return true;
    }

    static class VetoedException extends Exception {

        private static final long serialVersionUID = 1539955697495918463L;

        public VetoedException() {
        }
    }

}
