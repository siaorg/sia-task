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

package com.sia.task.quartz.job;

import com.sia.task.quartz.core.Scheduler;
import com.sia.task.quartz.exception.SchedulerException;
import com.sia.task.quartz.job.trigger.TriggerFiredBundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The default JobFactory used by Quartz - simply calls 
 * <code>newInstance()</code> on the job class.
 * 
 * @see JobFactory
 * @see PropertySettingJobFactory
 *
 * @author @see Quartz
 * @data 2019-06-24 18:20
 * @version V1.0.0
 **/
public class SimpleJobFactory implements JobFactory {

    private final Logger log = LoggerFactory.getLogger(getClass());
    
    protected Logger getLog() {
        return log;
    }
    
    public Job newJob(TriggerFiredBundle bundle, Scheduler Scheduler) throws SchedulerException {

        JobDetail jobDetail = bundle.getJobDetail();
        Class<? extends Job> jobClass = jobDetail.getJobClass();
        try {
            if(log.isDebugEnabled()) {
                log.debug(
                    "Producing instance of Job '" + jobDetail.getKey() + 
                    "', class=" + jobClass.getName());
            }
            
            return jobClass.newInstance();
        } catch (Exception e) {
            SchedulerException se = new SchedulerException(
                    "Problem instantiating class '"
                            + jobDetail.getJobClass().getName() + "'", e);
            throw se;
        }
    }

}
