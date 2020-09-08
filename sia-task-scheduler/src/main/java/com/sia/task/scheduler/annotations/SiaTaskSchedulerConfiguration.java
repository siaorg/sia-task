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

package com.sia.task.scheduler.annotations;

import com.sia.task.scheduler.log.LogService;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;


/**
 * SiaTaskSchedulerConfiguration
 *
 * @author maozhengwei
 * @version V1.0.0
 * @date 2020/8/18 10:19 上午
 **/
@Configuration
public class SiaTaskSchedulerConfiguration implements InitializingBean {

    @Value("${sia.task.log.collector.enabled}")
    private boolean logCollectorEnabled;

    @Override
    public void afterPropertiesSet() throws Exception {
        LogService.setLogCollectorEnabled(logCollectorEnabled);
    }
}
