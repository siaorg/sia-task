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


import com.sia.task.collector.log.LogEnable;
import com.sia.task.register.zookeeper.annotations.EnableZkRegister4Scheduler;
import com.sia.task.scheduler.job.SiaTaskScheduler;
import org.springframework.context.annotation.Import;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * EnableCollectorZookeeper
 *
 * @author maozhengwei
 * @version V1.0.0
 * @data 2020/5/13 11:07 上午
 **/
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@EnableZkRegister4Scheduler
@LogEnable
@Import(value = {SiaTaskScheduler.class})
public @interface EnableSiaTaskScheduler {
}
