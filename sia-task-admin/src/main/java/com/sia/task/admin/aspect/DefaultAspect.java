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

package com.sia.task.admin.aspect;


import com.sia.task.admin.service.UserService;
import com.sia.task.core.util.Constant;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

/**
 * Log aspect
 *
 * @author maozhengwei
 * @version V1.0.0
 * @description
 * @data 2019-09-05 11:40
 * @see
 **/
@Aspect
@Component
@Slf4j
public class DefaultAspect {

    @Around("execution(* com.sia.task.admin.controller.*.*(..)) && !execution(* com.sia.task.admin.controller.*.select*(..))")
    public Object handlerControlerMethod(ProceedingJoinPoint joinPoint) throws Throwable {

        Object[] args = joinPoint.getArgs();

        log.info(Constant.LOG_PREFIX + " userName: [" + UserService.getCurrentUser() + "], operation: [{}], param: [{}] ", joinPoint.getSignature().getName(), args);

        return joinPoint.proceed();
    }
}
