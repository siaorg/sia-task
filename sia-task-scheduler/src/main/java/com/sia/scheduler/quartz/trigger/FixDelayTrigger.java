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

import org.quartz.SimpleScheduleBuilder;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;

import java.util.Date;

/**
 *
 * 固定延时的
 * 固定延时是指当前Job结束后，过固定的时间再执行下一次任务。可惜的是，Quartz本身并不提供固定延时机制。所以只能根据情况hack。
 *
 * @description
 * @see
 * @author maozhengwei
 * @date 2018/4/1910:20
 * @version V1.0.0
 **/
@Deprecated
public class FixDelayTrigger {

    int repeatInterval;

    int setRepeatCount;

    Date startTime;

    long delay;

    /**
     * FixDelayTrigger
     * @param repeatInterval
     * @param setRepeatCount
     * @param startTime
     * @param delay
     */
    public FixDelayTrigger(int repeatInterval, int setRepeatCount, Date startTime, long delay) {

        this.repeatInterval = repeatInterval;
        this.setRepeatCount = setRepeatCount;
        this.startTime = startTime;
        this.delay = delay;
    }

    /**
     * getTrigger
     * @param jobName
     * @param jobGroup
     * @return
     */
    public Trigger getTrigger(String jobName, String jobGroup) {

        // SimpleScheduleBuilder 是简单调用触发器，它只能指定触发的间隔时间和执行次数；
        Trigger trigger = TriggerBuilder.newTrigger().withIdentity(jobName, jobGroup).startAt(startTime)
                .withSchedule(SimpleScheduleBuilder
                        .simpleSchedule()
                        .withIntervalInSeconds(repeatInterval)
                        .withRepeatCount(setRepeatCount))
                .build();
        return trigger;
    }

}
