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


import com.sia.task.quartz.job.trigger.Trigger;


/**
 * AbstractTrigger
 *
 * @description
 * @see
 * @author maozhengwei
 * @data 2018/9/26 17:05
 * @version V1.0.0
 **/
public abstract class AbstractTrigger {


    /**
     * build Trigger
     * @param jobKey
     * @param jobGroup
     * @param trigerType
     * @param trigerValue
     * @return
     */
    abstract Trigger build(String jobKey, String jobGroup, String trigerType, String trigerValue);

}
