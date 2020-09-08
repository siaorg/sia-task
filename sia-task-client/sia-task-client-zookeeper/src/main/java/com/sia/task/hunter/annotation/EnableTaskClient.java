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

package com.sia.task.hunter.annotation;


import com.sia.task.hunter.aspect.OnlineTaskAspect4Scheduler;
import com.sia.task.hunter.register.TaskRegisterListener;
import com.sia.task.register.zookeeper.annotations.EnableZkRegister4Client;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;


/**
 * EnableTaskClient
 *
 * @author maozhengwei
 * @version V1.0.0
 * @date 2020/7/21 2:21 下午
 * @see
 **/
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@EnableZkRegister4Client
@Import(value = {
        TaskRegisterListener.class,
        OnlineTaskAspect4Scheduler.class
})
public @interface EnableTaskClient {
}
