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

import org.quartz.Trigger;


/**
 *
 * TriggerBuildHandler
 * @description
 * @see
 * @author maozhengwei
 * @date 2018-094-28 19:52
 * @version V1.0.0
 **/
public class TriggerBuildHandler {

    /**
     * 创建不同类型的触发器，目前提供Crontrigger，FixRepeatTriggerImpl。FixDelayTrigger 暂时不提供（需要自己实现逻辑）
     *
     * @param jobKey
     * @param jobGroup
     * @param trigerType
     * @return
     */
    public static Trigger build(String jobKey, String jobGroup, String trigerType, String trigerValue) {
        TriggerTypeEnum triggerTypeEnum = TriggerTypeEnum.choose(trigerType);
        return triggerTypeEnum.getTrigger().build(jobKey, jobGroup, trigerType, trigerValue);
    }

}
