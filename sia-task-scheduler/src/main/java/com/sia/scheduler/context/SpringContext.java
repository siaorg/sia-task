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
import com.sia.core.entity.BasicJob;
import com.sia.scheduler.http.impl.HttpCallbackFail;
import com.sia.scheduler.service.BasicJobService;
import com.sia.scheduler.service.JobLogService;
import com.sia.scheduler.service.JobMTaskService;
import com.sia.scheduler.service.TaskLogService;
import com.sia.scheduler.service.common.EmailService;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;

/**
 *
 * Provide a way to use Spring-managed bean objects in non-Spring-managed classes.
 * Inherit the Application Context Aware class，After the Spring container is initialized,
 * the initialization of the Spring Context global variable is performed.
 * Provide a static method to get a concrete instance of the current class `static variable`。
 *
 * @see org.springframework.context.ApplicationContextAware
 * @author maozhengwei
 * @date 2018-08-28 10:52
 * @version V1.0.0
 **/
@Component
public class SpringContext implements ApplicationContextAware {

    private static ConcurrentHashMap<String, BasicJob> runningJob = new ConcurrentHashMap<>();

    private static TaskLogService taskLogService;
    private static HttpCallbackFail httpCallbackFail;
    private static Curator4Scheduler curator4Scheduler;
    private static JobMTaskService jobMTaskService;
    private static JobLogService jobLogService;
    private static BasicJobService basicJobService;
    private static EmailService emailService;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        taskLogService = applicationContext.getBean(TaskLogService.class);
        httpCallbackFail = applicationContext.getBean(HttpCallbackFail.class);
        curator4Scheduler = applicationContext.getBean(Curator4Scheduler.class);
        jobMTaskService = applicationContext.getBean(JobMTaskService.class);
        jobLogService = applicationContext.getBean(JobLogService.class);
        basicJobService = applicationContext.getBean(BasicJobService.class);
        emailService = applicationContext.getBean(EmailService.class);
    }

    public static TaskLogService getTaskLogService() {
        return taskLogService;
    }

    public static HttpCallbackFail getAsyncBackLog() {
        return httpCallbackFail;
    }

    public static Curator4Scheduler getCurator4Scheduler() {
        return curator4Scheduler;
    }

    public static JobMTaskService getJobMTaskService() {
        return jobMTaskService;
    }


    public static JobLogService getJobLogService() {
        return jobLogService;
    }

    public static BasicJobService getBasicJobService() {
        return basicJobService;
    }

    public static EmailService getEmailService() {
        return emailService;
    }

    public static ConcurrentHashMap<String, BasicJob> getRunningJob() {
        return runningJob;
    }

    public static void setRunningJob(ConcurrentHashMap<String, BasicJob> runningJob) {
        SpringContext.runningJob = runningJob;
    }
}
