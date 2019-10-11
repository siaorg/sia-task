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

package com.sia.scheduler.context;

import com.sia.core.curator.Curator4Scheduler;
import com.sia.scheduler.http.impl.HttpCallbackFail;
import com.sia.scheduler.service.BasicJobService;
import com.sia.scheduler.service.JobLogService;
import com.sia.scheduler.service.JobMTaskService;
import com.sia.scheduler.service.TaskLogService;
import com.sia.scheduler.service.common.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

/**
 *
 * Provide a way to use Spring-managed bean objects in non-Spring-managed classes.
 * The target class inherits the class when it is used.
 *
 * @see com.sia.scheduler.context.SpringContext
 * @author maozhengwei
 * @date 2018-09-29 10:57
 * @version V1.0.0
 **/
public class BaseSpringContext {
    @Autowired
    protected TaskLogService taskLogService;
    @Autowired
    protected HttpCallbackFail httpCallbackLog;
    @Autowired
    protected Curator4Scheduler curator4Scheduler;
    @Autowired
    protected JobMTaskService jobMTaskService;
    @Autowired
    protected JobLogService jobLogService;
    @Autowired
    protected BasicJobService basicJobService;
    @Autowired
    protected EmailService emailService;
}
