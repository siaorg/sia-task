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

import java.lang.annotation.*;

/**
 * Annotation of <code>OnlineTask</code>, mark the <code>Task</code> so that the metadata of the Task can be captured
 * and uploaded to the registration center when the application starts.
 * <p>
 * Add a description of the field
 *
 * @author maozhengwei
 * @version V1.0.0
 * @description
 * @data 2020/5/11 11:31 上午
 * @see
 **/
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
public @interface OnlineTask {

    /**
     * Task description information, describing the specific purpose of the task
     *
     * @return
     */
    String description() default "";

    /**
     * Whether to enable task singleton single thread
     * <p>
     * 1. If you use it, you must enable the AOP <code>spring.onlinetask.serial=true</code>
     * 2. <code>@OnlineTask(enableSerial=true)</code>
     *
     * @return Return true by default
     */
    boolean enableSerial() default true;

    /**
     * Indicates whether the task has permission authentication enabled
     * <p>
     * If the enableAuth is enabled， an unauthorized scheduler call will prompt:'This task is not authorized'
     *
     * @return Return true by default
     */
    boolean enableAuth() default true;
}
