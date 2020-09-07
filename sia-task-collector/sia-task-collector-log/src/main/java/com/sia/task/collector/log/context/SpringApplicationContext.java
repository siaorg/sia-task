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

package com.sia.task.collector.log.context;

import com.sia.task.collector.log.email.EmailMessageService;
import com.sia.task.collector.log.email.WeCatMessageService;
import com.sia.task.collector.log.service.JobLogService;
import com.sia.task.collector.log.service.TaskLogService;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanInitializationException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.*;

/**
 * @author maozhengwei
 * @version V1.0.0
 * @description
 * @data 2019-10-28 14:22
 * @see
 **/
public class SpringApplicationContext implements ApplicationContextAware {

    /**
     * 预警压制时间默认值
     */
    @Value("${sia.task.alarm.email.elapse:1800000}")
    private long elapse;

    @Value("${sia.task.alarm.email.subject:微服务任务调度平台-预警}")
    private String emailSubject;

    @Value("${sia.task.alarm.email.default-email:}")
    protected String adminEmailers;

    @Value("${sia.task.alarm.email.service-id:}")
    protected String alarmServiceId;

    private static String LOG_EMAIL_SUBJECT;

    private static long LOG_EMAIL_ELAPSE;

    private static TaskLogService taskLogService;

    private static JobLogService logService;

    private static EmailMessageService emailService;

    private static WeCatMessageService weCatMessageService;

    /**
     * Set the ApplicationContext that this object runs in.
     * Normally this call will be used to initialize the object.
     * <p>Invoked after population of normal bean properties but before an init callback such
     * as {@link InitializingBean#afterPropertiesSet()}
     * or a custom init-method. Invoked after {@link ResourceLoaderAware#setResourceLoader},
     * {@link ApplicationEventPublisherAware#setApplicationEventPublisher} and
     * {@link MessageSourceAware}, if applicable.
     *
     * @param applicationContext the ApplicationContext object to be used by this object
     * @throws ApplicationContextException in case of context initialization errors
     * @throws BeansException              if thrown by application context methods
     * @see BeanInitializationException
     */
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        taskLogService = applicationContext.getBean(TaskLogService.class);
        logService = applicationContext.getBean(JobLogService.class);
        emailService = applicationContext.getBean(EmailMessageService.class);
        weCatMessageService = applicationContext.getBean(WeCatMessageService.class);
        LOG_EMAIL_SUBJECT = emailSubject;
        LOG_EMAIL_ELAPSE = elapse;
    }

    /**
     * 预警邮件的主题
     *
     * @return 默认值:微服务任务调度平台-任务执行异常预警
     */
    public static String getEmailSubject() {
        return LOG_EMAIL_SUBJECT;
    }

    /**
     * 预警压制时间
     *
     * @return 默认值 1800000 = 1000 * 60 * 30
     */
    public static long getEmailAlarmElapse() {
        return LOG_EMAIL_ELAPSE;
    }

    //service
    public static JobLogService getLogService() {
        return logService;
    }

    public static TaskLogService getTaskLogService() {
        return taskLogService;
    }

    public static EmailMessageService getEmailService() {
        return emailService;
    }

    public static WeCatMessageService getWeCatMessageService() {
        return weCatMessageService;
    }
}
