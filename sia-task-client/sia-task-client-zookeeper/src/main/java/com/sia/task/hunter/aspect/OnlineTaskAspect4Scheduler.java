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

package com.sia.task.hunter.aspect;

import com.sia.task.core.IMetadataUpload;
import com.sia.task.core.http.SiaHttpResponse;
import com.sia.task.core.task.SiaJobStatus;
import com.sia.task.core.util.DateFormatHelper;
import com.sia.task.core.util.JsonHelper;
import com.sia.task.core.util.StringHelper;
import com.sia.task.hunter.annotation.OnlineTask;
import com.sia.task.integration.curator.properties.ZookeeperConfiguration;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


/**
 * <p>
 *
 * @author huangqian
 * @author maozhengwei
 * version V1.0.0
 * date 2020/5/11 11:11 上午
 * </p>
 * @date 2018/6/29 11:07
 * @description 补偿机制
 * </p>
 *
 * <p>
 * Fixed an issue that caused the scheduler to fail to obtain exception information due to an exception being ignored
 **/
@Aspect
public class OnlineTaskAspect4Scheduler {

    private static Logger log = LoggerFactory.getLogger(OnlineTaskAspect4Scheduler.class);

    private static Map<String, SiaJobStatus> runningTaskMap = new ConcurrentHashMap<>();

    private static final String OnlineJobStatusKey = "OnlineJobStatus";
    private static final String MqMessageKey = "MqMessage";
    private static final String GroupNameKey = "GroupName";
    private static final String ApplicationNameKey = "ApplicationName";
    private static final String TaskNameKey = "TaskName";
    private static final String HostKey = "Host";
    private static final String RequestURLKey = "RequestURL";
    private static final String DateKey = "Date";


    @Resource
    IMetadataUpload upload;
    @Resource
    ZookeeperConfiguration configuration;


    @Pointcut("@annotation(onlineTask)")
    public void serviceStatistics(OnlineTask onlineTask) {

    }


    @Around(value = "serviceStatistics(onlineTask)")
    public Object around(ProceedingJoinPoint point, OnlineTask onlineTask) throws Throwable {
        //白名单检查
        Object o;
        if (!checkAuth(onlineTask)) {
            return SiaHttpResponse.failure("This task is not authorized!");
        }
        //单例单线程检查
        if (configuration.isOnlinetaskSerial() && isSerial(onlineTask)) {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            HttpServletRequest request = attributes.getRequest();
            String task = point.getSignature().getDeclaringTypeName() + "." + point.getSignature().getName();
            if (runningTaskMap.containsKey(task) && SiaJobStatus.RUNNING.equals(runningTaskMap.get(task))) {
                return SiaHttpResponse.failure("This task is running!");
            } else {
                log.info(JsonHelper.toString(builtMqMessage(request, "Begin", "false")));
                runningTaskMap.put(task, SiaJobStatus.RUNNING);
                o = point.proceed();
                if (SiaJobStatus.RUNNING.equals(runningTaskMap.get(task))) {
                    log.info(JsonHelper.toString(builtMqMessage(request, "Success", "false")));
                    runningTaskMap.put(task, SiaJobStatus.STOP);
                }
                return o;
            }
        }
        try {
            o = point.proceed();
        } catch (Exception e) {
            return SiaHttpResponse.failure(e.getMessage(), "An - unknown exception occurred in the executor running task!");
        }
        return o;
    }

    /**
     * 任务执行异常
     *
     * @param joinPoint
     * @param ex
     * @param onlineTask
     */
    @AfterThrowing(pointcut = "serviceStatistics(onlineTask)", throwing = "ex")
    public void throwss(JoinPoint joinPoint, Throwable ex, OnlineTask onlineTask) {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = attributes.getRequest();
        String task = joinPoint.getSignature().getDeclaringTypeName() + "." + joinPoint.getSignature().getName();
        if (SiaJobStatus.RUNNING.equals(runningTaskMap.get(task))) {
            log.info(JsonHelper.toString(builtMqMessage(request, "Fail", "false")));
            runningTaskMap.put(task, SiaJobStatus.STOP);
        }
    }

    /**
     * log message
     *
     * @param request
     * @param onlineJobStatusKey
     * @param mqMessage
     * @return
     */
    public Map<String, String> builtMqMessage(HttpServletRequest request, String onlineJobStatusKey, String mqMessage) {

        Map<String, String> logMess = new HashMap<String, String>(8);
        logMess.put(GroupNameKey, configuration.getApplicationName().substring(0,
                configuration.getApplicationName().indexOf("-")));
        logMess.put(ApplicationNameKey, configuration.getApplicationName());
        logMess.put(TaskNameKey, configuration.getApplicationName() + ":" + request.getServletPath());
        logMess.put(HostKey, configuration.getIPAddress() + ":" + configuration.getServerPort());
        logMess.put(RequestURLKey, request.getRequestURL().toString());
        logMess.put(DateKey, DateFormatHelper.format(new Date()));
        logMess.put(OnlineJobStatusKey, onlineJobStatusKey);
        logMess.put(MqMessageKey, mqMessage);
        return logMess;
    }

    /**
     * 是否开启单例线程管控
     *
     * @param instance
     * @return
     */
    private boolean isSerial(OnlineTask instance) {

        boolean flag = true;
        try {
            flag = instance.enableSerial();
        } catch (Throwable e) {
            log.error("OnlineTask.enableSerial() throws :", e);
        }
        return flag;
    }

    /**
     * 检查调度器是否是合法的调度器
     *
     * @param instance
     * @return
     */
    private boolean checkAuth(OnlineTask instance) {
        if (isAuth(instance)) {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder
                    .getRequestAttributes();
            HttpServletRequest request = attributes.getRequest();
            String ip = request.getRemoteAddr();
            log.info("who call me ? ->[" + ip + "]");
            return shouldIAccept(ip);
        }
        return true;
    }

    private boolean isAuth(OnlineTask instance) {
        boolean flag = true;
        try {
            flag = instance.enableAuth();
        } catch (Throwable e) {
            log.error("OnlineTask.enableAuth() throws :", e);
        }
        return flag;
    }

    private boolean shouldIAccept(String ip) {
        if (StringHelper.isEmpty(ip)) {
            return false;
        }
        List<String> whiteList = upload.loadAuthIpList();
        if (whiteList == null) {
            return false;
        }
        for (String item : whiteList) {
            if (ip.equals(item)) {
                return true;
            }
        }
        return false;
    }
}
