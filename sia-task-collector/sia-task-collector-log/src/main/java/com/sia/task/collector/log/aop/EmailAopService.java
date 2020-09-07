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

package com.sia.task.collector.log.aop;

import com.sia.task.collector.log.util.MessageUtil;
import com.sia.task.core.ModifyOnlineJobStatus;
import com.sia.task.core.task.DagTask;
import com.sia.task.core.util.Constant;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;

/**
 * 预警切面服务类
 * 通过拦截任务状态<code>{@link ModifyOnlineJobStatus#stopJobStatus}</code>变更来传递预警信息
 * 实现两类预警
 * 1. 邮件预警通知；
 * 2. 微信预警通知
 *
 * @author maozhengwei
 * @version V1.0.0
 * @TODO 1. 邮件预警提供了压制功能但是无开关功能；
 * 2. 微信预警没有压制功能和开关功能；
 * @date 2020/6/30 2:56 下午
 * @see
 **/
@Aspect
@Slf4j
public class EmailAopService {

    @Before("execution(* com.sia.task.core.ModifyOnlineJobStatus.stopJobStatus(..))")
    public void stopJobStatus(JoinPoint joinPoint) {
        DagTask task = null;
        try {
            Object[] args = joinPoint.getArgs();
            task = (DagTask) args[0];
            String content = (String) args[1];

            MessageUtil.sendMess(task, content);

        } catch (Exception ex) {
            log.error(Constant.LOG_EX_PREFIX + "stopJobStatus 记录日志-异常日志发送预警-出现错误 JobKey[{}], EmailContent[{}]", task, ex);

        }
    }
}
