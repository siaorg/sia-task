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


import com.sia.task.quartz.core.CronScheduleBuilder;
import com.sia.task.quartz.job.trigger.Trigger;
import com.sia.task.quartz.job.trigger.TriggerBuilder;
import com.sia.task.quartz.job.trigger.TriggerKey;

/**
 * @author: MAOZW
 * @Description: CronTriggerImpl
 * @date 2018/4/1910:20
 */
public class CronTriggerImpl extends AbstractTrigger {

    TriggerKey triggerKey;

    String cronExpression;

    public CronTriggerImpl() {
    }

    public CronTriggerImpl(TriggerKey triggerKey, String cronExpression) {

        this.triggerKey = triggerKey;
        this.cronExpression = cronExpression;
    }

    private Trigger getTrigger() {
        /**
         * quartz-misfire 错失、补偿执行
         * withMisfireHandlingInstructionFireAndProceed（默认） : 以当前时间为触发频率立刻触发一次执行,然后按照Cron频率依次执行.
         * withMisfireHandlingInstructionDoNothing : 不触发立即执行;等待下次Cron触发频率到达时刻开始按照Cron频率依次执行;
         * withMisfireHandlingInstructionIgnoreMisfires : 以错过的第一个频率时间立刻开始执行,重做错过的所有频率周期后,当下一次触发频率发生时间大于当前时间后，再按照正常的Cron频率依次执行;
         *
         */
        CronScheduleBuilder cronScheduleBuilder = CronScheduleBuilder.cronSchedule(cronExpression).withMisfireHandlingInstructionDoNothing();
        Trigger cronTrigger = TriggerBuilder.newTrigger().withIdentity(triggerKey).withSchedule(cronScheduleBuilder).build();
        return cronTrigger;
    }

    /**
     * @param jobKey
     * @param jobGroup
     * @param trigerValue
     * @return
     */
    @Override
    Trigger build(String jobKey, String jobGroup, String trigerType, String trigerValue) {
        return new CronTriggerImpl(TriggerKey.triggerKey(jobKey, jobGroup), trigerValue).getTrigger();
    }
}
