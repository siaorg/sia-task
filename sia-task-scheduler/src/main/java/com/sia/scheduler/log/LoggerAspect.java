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

package com.sia.scheduler.log;

import com.sia.core.entity.JobMTask;
import com.sia.scheduler.log.enums.JobLogEnum;
import com.sia.scheduler.log.enums.TaskLogEnum;
import com.sia.scheduler.service.JobLogService;
import com.sia.scheduler.service.TaskLogService;
import com.sia.scheduler.service.common.EmailService;
import com.sia.scheduler.util.constant.Constants;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * scheduler log aop
 *
 * @description
 * @see
 * @author maozhengwei
 * @date 2018-04-28 10:32
 * @version V1.0.0
 **/
@Aspect
@Component
public class LoggerAspect {

    private static final Logger LOGGER = LoggerFactory.getLogger(LoggerAspect.class);

    @Autowired
    private JobLogService jobLogService;
    @Autowired
    private EmailService emailService;
    @Autowired
    private TaskLogService taskLogService;


    @Pointcut("@annotation(com.sia.scheduler.log.annotations.LogAnnotation)")
    public void logAspect() {
    }

    /**
     * 前置
     * TODO  优化switch
     * @param joinPoint
     */
    @Before("logAspect()")
    public void logBefore(JoinPoint joinPoint) {
        LOGGER.info(Constants.LOG_PREFIX + " AOP Before");
        try {
            JobMTask onlineTask = null;
            Throwable throwable = null;
            Object[] args = joinPoint.getArgs();
            for (Object obj : args) {
                if (obj instanceof JobMTask) {
                    onlineTask = (JobMTask) obj;
                }
                if (obj instanceof Throwable) {
                    throwable = (Throwable) obj;
                }
            }
            switch (joinPoint.getSignature().getName()) {
                case "onIgnore":
                    onIgnore(onlineTask, throwable);
                    break;
                case "onTransfer":
                    onTransfer(onlineTask, throwable);
                    break;
                case "onMultiCallsAndTransfer":
                    onMultiCallsAndTransfer(onlineTask, throwable);
                    break;
                default:
                    onStop(onlineTask, throwable);
            }
        } catch (Exception ex) {
            LOGGER.error(Constants.LOG_PREFIX + " LoggerAspect logBefore Exception ：", ex);
        }
    }

    private void onTransfer(JobMTask onlineTask, Throwable throwable) {
        LOGGER.info(Constants.LOG_PREFIX + " AOP Before >>> onTransfer [{}], [{}]",onlineTask, throwable);
        recordDetailFail(onlineTask, throwable,false);
        jobLogService.updateJobLog(onlineTask, JobLogEnum.LOG_JOB_FAIL_TRANSFER.toString());
    }

    private void onMultiCallsAndTransfer(JobMTask onlineTask, Throwable throwable) {
        LOGGER.info(Constants.LOG_PREFIX + " AOP Before >>> onMultiCallsAndTransfer [{}], [{}]",onlineTask, throwable);
        recordDetailFail(onlineTask, throwable,false);
        jobLogService.updateJobLog(onlineTask, JobLogEnum.LOG_JOB_FAIL_MULTI_CALLS_TRANSFER.toString());
    }

    protected void onIgnore(JobMTask onlineTask, Throwable throwable) {
        LOGGER.info(Constants.LOG_PREFIX + " AOP Before >>> onIgnore [{}], [{}]",onlineTask, throwable);
        recordDetailFail(onlineTask,throwable,true);
        jobLogService.updateJobLog(onlineTask, JobLogEnum.LOG_JOB_HANDLE_FAIL_STOP.toString());
    }

    protected void onStop(JobMTask onlineTask, Throwable throwable){
        LOGGER.info(Constants.LOG_PREFIX + " AOP Before >>> onStop [{}], [{}]",onlineTask, throwable);
        recordDetailFail(onlineTask,throwable,false);
        jobLogService.updateJobLog(onlineTask, JobLogEnum.LOG_JOB_HANDLE_FAIL_STOP.toString());
    }

    /**
     * 记录日志以及是否进行邮件预警
     * @param onlineTask
     * @param throwable
     * @param isEmail
     */
    private void recordDetailFail(JobMTask onlineTask, Throwable throwable,boolean isEmail) {
        try {
            String message = onlineTask.toString() + Constants.REGEX_ARROW;
            if (throwable != null) {
                message = message + " Exception : " + throwable.getMessage() + Constants.REGEX_COLON + throwable.getLocalizedMessage();
            }
            taskLogService.recordTaskLog(onlineTask, TaskLogEnum.LOG_TASK_FAIL_DETAIL.toString(), message);
            LOGGER.info(Constants.LOG_PREFIX + " AOP Before >>> recordDetailFail >>> recordTaskLog finished [{}], [{}]",onlineTask, message);
            if (isEmail){
                emailService.sendLimitedEmail(onlineTask.getJobAlarmEmail(), message, Constants.EMAIL_SUBJECT, onlineTask.getFailover(), 1000 * 60 * 30);
            }
        } catch (Exception e) {
            LOGGER.info(Constants.LOG_PREFIX + "LoggerAspect recordDetailFail Exception ：", e);
        }
    }
}
