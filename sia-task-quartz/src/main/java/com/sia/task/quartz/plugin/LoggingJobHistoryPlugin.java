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

package com.sia.task.quartz.plugin;

import com.sia.task.quartz.ClassLoadHelper;
import com.sia.task.quartz.core.Scheduler;
import com.sia.task.quartz.exception.JobExecutionException;
import com.sia.task.quartz.exception.SchedulerConfigException;
import com.sia.task.quartz.exception.SchedulerException;
import com.sia.task.quartz.job.JobExecutionContext;
import com.sia.task.quartz.job.matchers.EverythingMatcher;
import com.sia.task.quartz.job.trigger.Trigger;
import com.sia.task.quartz.listeners.JobListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.MessageFormat;

/**
 * Logs a history of all job executions (and execution vetos) via the 
 * Jakarta Commons-Logging framework.
 * 
 * <p>
 * The logged message is customizable by setting one of the following message
 * properties to a String that conforms to the syntax of <code>java.util.MessageFormat</code>.
 * </p>
 *
 *
 * @description
 * @see org.quartz
 * @author @see Quartz
 * @data 2019-07-18 14:29
 * @version V1.0.0
 **/
public class LoggingJobHistoryPlugin implements SchedulerPlugin, JobListener {

    /*
     * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
     * 
     * Data members.
     * 
     * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
     */

    private String name;

    private String jobToBeFiredMessage = "Job {1}.{0} fired (by trigger {4}.{3}) at: {2, date, HH:mm:ss MM/dd/yyyy}";
    
    private String jobSuccessMessage = "Job {1}.{0} execution complete at {2, date, HH:mm:ss MM/dd/yyyy} and reports: {8}";

    private String jobFailedMessage = "Job {1}.{0} execution failed at {2, date, HH:mm:ss MM/dd/yyyy} and reports: {8}";

    private String jobWasVetoedMessage = "Job {1}.{0} was vetoed.  It was to be fired (by trigger {4}.{3}) at: {2, date, HH:mm:ss MM/dd/yyyy}";

    private final Logger log = LoggerFactory.getLogger(getClass());

    /*
     * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
     * 
     * Constructors.
     * 
     * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
     */

    public LoggingJobHistoryPlugin() {
    }

    /*
     * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
     * 
     * Interface.
     * 
     * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
     */

    protected Logger getLog() {
        return log;
    }

    /**
     * Get the message that is logged when a Job successfully completes its 
     * execution.
     */
    public String getJobSuccessMessage() {
        return jobSuccessMessage;
    }

    /**
     * Get the message that is logged when a Job fails its 
     * execution.
     */
    public String getJobFailedMessage() {
        return jobFailedMessage;
    }

    /**
     * Get the message that is logged when a Job is about to execute.
     */
    public String getJobToBeFiredMessage() {
        return jobToBeFiredMessage;
    }

    /**
     * Set the message that is logged when a Job successfully completes its 
     * execution.
     * 
     * @param jobSuccessMessage
     *          String in java.text.MessageFormat syntax.
     */
    public void setJobSuccessMessage(String jobSuccessMessage) {
        this.jobSuccessMessage = jobSuccessMessage;
    }

    /**
     * Set the message that is logged when a Job fails its 
     * execution.
     * 
     * @param jobFailedMessage
     *          String in java.text.MessageFormat syntax.
     */
    public void setJobFailedMessage(String jobFailedMessage) {
        this.jobFailedMessage = jobFailedMessage;
    }

    /**
     * Set the message that is logged when a Job is about to execute.
     * 
     * @param jobToBeFiredMessage
     *          String in java.text.MessageFormat syntax.
     */
    public void setJobToBeFiredMessage(String jobToBeFiredMessage) {
        this.jobToBeFiredMessage = jobToBeFiredMessage;
    }

    /**
     * Get the message that is logged when a Job execution is vetoed by a
     * trigger listener.
     */
    public String getJobWasVetoedMessage() {
        return jobWasVetoedMessage;
    }

    /**
     * Set the message that is logged when a Job execution is vetoed by a
     * trigger listener.
     * 
     * @param jobWasVetoedMessage
     *          String in java.text.MessageFormat syntax.
     */
    public void setJobWasVetoedMessage(String jobWasVetoedMessage) {
        this.jobWasVetoedMessage = jobWasVetoedMessage;
    }

    /*
     * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
     * 
     * SchedulerPlugin Interface.
     * 
     * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
     */

    /**
     * <p>
     * Called during creation of the <code>Scheduler</code> in order to give
     * the <code>SchedulerPlugin</code> a chance to initialize.
     * </p>
     * 
     * @throws SchedulerConfigException
     *           if there is an error initializing.
     */
    public void initialize(String pname, Scheduler scheduler, ClassLoadHelper classLoadHelper)
        throws SchedulerException {
        this.name = pname;
        scheduler.getListenerManager().addJobListener(this, EverythingMatcher.allJobs());
    }

    public void start() {
        // do nothing...
    }

    /**
     * <p>
     * Called in order to inform the <code>SchedulerPlugin</code> that it
     * should free up all of it's resources because the scheduler is shutting
     * down.
     * </p>
     */
    public void shutdown() {
        // nothing to do...
    }

    /*
     * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
     * 
     * JobListener Interface.
     * 
     * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
     */

    /*
     * Object[] arguments = { new Integer(7), new
     * Date(System.currentTimeMillis()), "a disturbance in the Force" };
     * 
     * String result = MessageFormat.format( "At {1,time} on {1,date}, there
     * was {2} on planet {0,number,integer}.", arguments);
     */

    public String getName() {
        return name;
    }

    /** 
     * @see JobListener#jobToBeExecuted(JobExecutionContext)
     */
    public void jobToBeExecuted(JobExecutionContext context) {
        if (!getLog().isInfoEnabled()) {
            return;
        } 
        
        Trigger trigger = context.getTrigger();

        Object[] args = {
            context.getJobDetail().getKey().getName(),
            context.getJobDetail().getKey().getGroup(), new java.util.Date(),
            trigger.getKey().getName(), trigger.getKey().getGroup(),
            trigger.getPreviousFireTime(), trigger.getNextFireTime(),
            Integer.valueOf(context.getRefireCount())
        };

        getLog().info(MessageFormat.format(getJobToBeFiredMessage(), args));
    }
    
    /** 
     * @see JobListener#jobWasExecuted(JobExecutionContext, JobExecutionException)
     */
    public void jobWasExecuted(JobExecutionContext context,
                               JobExecutionException jobException) {

        Trigger trigger = context.getTrigger();
        
        Object[] args = null;
        
        if (jobException != null) {
            if (!getLog().isWarnEnabled()) {
                return;
            } 
            
            String errMsg = jobException.getMessage();
            args = 
                new Object[] {
                    context.getJobDetail().getKey().getName(),
                    context.getJobDetail().getKey().getGroup(), new java.util.Date(),
                    trigger.getKey().getName(), trigger.getKey().getGroup(),
                    trigger.getPreviousFireTime(), trigger.getNextFireTime(),
                    Integer.valueOf(context.getRefireCount()), errMsg
                };
            
            getLog().warn(MessageFormat.format(getJobFailedMessage(), args), jobException); 
        } else {
            if (!getLog().isInfoEnabled()) {
                return;
            } 
            
            String result = String.valueOf(context.getResult());
            args =
                new Object[] {
                    context.getJobDetail().getKey().getName(),
                    context.getJobDetail().getKey().getGroup(), new java.util.Date(),
                    trigger.getKey().getName(), trigger.getKey().getGroup(),
                    trigger.getPreviousFireTime(), trigger.getNextFireTime(),
                    Integer.valueOf(context.getRefireCount()), result
                };
            
            getLog().info(MessageFormat.format(getJobSuccessMessage(), args));
        }
    }

    /** 
     * @see JobListener#jobExecutionVetoed(JobExecutionContext)
     */
    public void jobExecutionVetoed(JobExecutionContext context) {
        
        if (!getLog().isInfoEnabled()) {
            return;
        } 
        
        Trigger trigger = context.getTrigger();

        Object[] args = {
            context.getJobDetail().getKey().getName(),
            context.getJobDetail().getKey().getGroup(), new java.util.Date(),
            trigger.getKey().getName(), trigger.getKey().getGroup(),
            trigger.getPreviousFireTime(), trigger.getNextFireTime(),
            Integer.valueOf(context.getRefireCount())
        };

        getLog().info(MessageFormat.format(getJobWasVetoedMessage(), args));
    }

}

// EOF
