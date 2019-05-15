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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * AbstractTrigger
 *
 * @description
 * @see
 * @author maozhengwei
 * @date 2018-09-25 19:47
 * @version V1.0.0
 **/
public abstract class AbstractTrigger {

    protected final static Logger LOGGER = LoggerFactory.getLogger(AbstractTrigger.class);


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
