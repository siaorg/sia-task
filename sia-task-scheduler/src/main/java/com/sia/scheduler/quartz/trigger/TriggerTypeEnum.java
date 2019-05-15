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

/**
 *
 * TriggerTypeEnum
 * @description
 * @see
 * @author maozhengwei
 * @date 2018-04-28 19:53
 * @version V1.0.0
 **/
public enum TriggerTypeEnum {

    /**
     * Trigger Type
     */
    TRIGGER_TYPE_CRON("TRIGGER_TYPE_CRON", new CronTriggerImpl()),
    TRIGGER_TYPE_FIXRATE("TRIGGER_TYPE_FIXRATE", new FixRepeatTriggerImpl()),
    TRIGGER_TYPE_PLAN("TRIGGER_TYPE_PLAN", new PlanTriggerImpl());
    //TRIGGER_TYPE_FIXDEALY("TRIGGER_TYPE_FIXDELAY", new FixRepeatTriggerImpl());
    //TRIGGER_TYPE_CALENDARINTERVAL("TRIGGER_TYPE_CALENDARINTERVAL");

    private String triggerType;

    private AbstractTrigger trigger;

    TriggerTypeEnum(String value, AbstractTrigger abstractTriggerBuild) {
        this.triggerType = value;
        this.trigger = abstractTriggerBuild;
    }

    TriggerTypeEnum(String value) {
        this.triggerType = value;
    }

    @Override
    public String toString() {
        return triggerType;
    }


    public static TriggerTypeEnum getByValue(String value) {
        for (TriggerTypeEnum triggerTypeEnum : values()) {
            if (triggerTypeEnum.toString().equals(value)) {
                return triggerTypeEnum;
            }
        }
        return null;
    }

    /**
     * 选择 TriggerType
     *
     * @param triggerType
     */
    public static TriggerTypeEnum choose(String triggerType) {
        if (null != triggerType) {
            for (TriggerTypeEnum typeEnum : TriggerTypeEnum.values()) {
                if (typeEnum.triggerType.equals(triggerType)) {
                    return typeEnum;
                }
            }
        }
        return null;
    }

    public String getTriggerType() {
        return triggerType;
    }

    public AbstractTrigger getTrigger() {
        return trigger;
    }
}
