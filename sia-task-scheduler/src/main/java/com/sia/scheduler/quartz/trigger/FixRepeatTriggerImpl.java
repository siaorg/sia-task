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

import com.sia.core.helper.DateFormatHelper;
import com.sia.scheduler.util.constant.Constants;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;

import java.text.ParseException;
import java.util.Date;

/**
 *
 * Use SimpleScheduleBuilder to achieve fixed interval, different number of timing tasks.
 * @see
 * @author maozhengwei
 * @date 2018-04-19 19:51
 * @version V1.0.0
 **/
public class FixRepeatTriggerImpl  extends AbstractTrigger {

    private int repeatInterval;
    private int setRepeatCount = 0;
    private Date startTime;

    public FixRepeatTriggerImpl(Date startTime, int setRepeatCount, int repeatInterval) {

        this.repeatInterval = repeatInterval;
        this.setRepeatCount = setRepeatCount;
        this.startTime = startTime;
    }

    public FixRepeatTriggerImpl() {

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


    private static Trigger buildFixRepeatTrigger(String jobKey, String jobGroup, String trigerValue) {
        String[] triggerValues = trigerValue.split(Constants.REGEX_COMMA);
        if (triggerValues.length == 0) {
            throw new IndexOutOfBoundsException(Constants.LOG_PREFIX
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
        return new FixRepeatTriggerImpl(startTime, repeatCount, repeatInterval).triggerBuild(jobKey, jobGroup);
    }

    /**
     * @param jobKey
     * @param jobGroup
     * @param trigerValue
     * @return
     */
    @Override
    public Trigger build(String jobKey, String jobGroup, String trigerType, String trigerValue) {
        return buildFixRepeatTrigger(jobKey, jobGroup, trigerValue);
    }
}
