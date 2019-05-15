/*-
 * <<
 * task
 * ==
 * Copyright (C) 2019 sia
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

package com.sia.scheduler.quartz.trigger;

import org.quartz.CronScheduleBuilder;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.TriggerKey;

/**
 *
 *
 * @description
 * @see
 * @author maozhengwei
 * @date 2019-03-06 15:52
 * @version V1.0.0
 **/
public class PlanTriggerImpl extends AbstractTrigger {

    TriggerKey triggerKey;

    String cronExpression;


    public PlanTriggerImpl(TriggerKey triggerKey, String cronExpression) {

        this.triggerKey = triggerKey;
        this.cronExpression = cronExpression;
    }

    public PlanTriggerImpl() {

    }

    private Trigger getTrigger() {
        CronScheduleBuilder cronScheduleBuilder = CronScheduleBuilder.cronSchedule(cronExpression).withMisfireHandlingInstructionDoNothing();
        Trigger planTrigger = TriggerBuilder.newTrigger().withIdentity(triggerKey).withSchedule(cronScheduleBuilder).build();
        return planTrigger;
    }

    private static Trigger buildCronTrigger(String jobKey, String jobGroup, String trigerValue) {
        return new PlanTriggerImpl(TriggerKey.triggerKey(jobKey, jobGroup), trigerValue).getTrigger();
    }

    /**
     * @param jobKey
     * @param jobGroup
     * @param trigerValue
     * @return
     */
    @Override
    Trigger build(String jobKey, String jobGroup, String trigerType, String trigerValue) {
        return buildCronTrigger(jobKey, jobGroup, trigerValue);
    }
}
