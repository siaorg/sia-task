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

package com.sia.task.scheduler.job.triggers;

import com.sia.task.core.util.Constant;
import com.sia.task.core.util.DateFormatHelper;
import com.sia.task.quartz.core.SimpleScheduleBuilder;
import com.sia.task.quartz.job.trigger.Trigger;
import com.sia.task.quartz.job.trigger.TriggerBuilder;

import java.text.ParseException;
import java.util.Date;

/**
 * @author: MAOZW
 * @Description: FixRepeatTriggerImpl
 * @date 2018/4/1910:21
 */
public class RepeatTriggerImpl extends AbstractTrigger {

    private int repeatInterval;
    private int setRepeatCount = 0;
    private Date startTime;

    private RepeatTriggerImpl(Date startTime, int setRepeatCount, int repeatInterval) {

        this.repeatInterval = repeatInterval;
        this.setRepeatCount = setRepeatCount;
        this.startTime = startTime;
    }

    public RepeatTriggerImpl() {

    }

    /**
     * triggerBuild
     *
     * @param jobName
     * @param jobGroup
     * @return
     */
    public Trigger triggerBuild(String jobName, String jobGroup) {

        Trigger trigger;
        SimpleScheduleBuilder simpleScheduleBuilder = SimpleScheduleBuilder.simpleSchedule();
        simpleScheduleBuilder.withIntervalInSeconds(repeatInterval);
        if (setRepeatCount > 0) {
            simpleScheduleBuilder.withMisfireHandlingInstructionIgnoreMisfires();
            simpleScheduleBuilder.withRepeatCount(setRepeatCount);
        } else {
            simpleScheduleBuilder.repeatForever();
        }
        trigger = TriggerBuilder.newTrigger().withIdentity(jobName, jobGroup).startAt(startTime).withSchedule(simpleScheduleBuilder).build();
        return trigger;
    }


    private static Trigger buildFixRepeatTrigger(String jobKey, String jobGroup, String triggerValue) {
        String[] triggerValues = triggerValue.split(Constant.REGEX_COMMA);
        if (triggerValues.length == 0) {
            throw new IndexOutOfBoundsException(Constant.LOG_PREFIX
                    + "The trigger type of the current job is : TRIGGER_TYPE_FIXRATE ,but trigerValue.split(,).length is zero");
        }
        Date startTime;
        try {
            startTime = DateFormatHelper.parse(triggerValues[0]);
        } catch (ParseException e) {
            throw new IllegalArgumentException("The time format must be : yyyy-MM-dd HH:mm:ss");
        }
        int repeatCount = Integer.valueOf(triggerValues[1]);
        int repeatInterval = Integer.valueOf(triggerValues[2]);
        return new RepeatTriggerImpl(startTime, repeatCount, repeatInterval).triggerBuild(jobKey, jobGroup);
    }

    /**
     *
     * @param jobKey
     * @param jobGroup
     * @param triggerType
     * @param triggerValue
     * @return
     */
    @Override
    public Trigger build(String jobKey, String jobGroup, String triggerType, String triggerValue) {
        return buildFixRepeatTrigger(jobKey, jobGroup, triggerValue);
    }
}
