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

package com.sia.hunter.aspect;

import com.sia.hunter.annotation.OnlineTask;
import com.sia.hunter.constant.OnlineTaskAppProperty;
import com.sia.hunter.exception.OnlineTaskAuthException;
import com.sia.hunter.exception.OnlineTaskException;
import com.sia.hunter.helper.DateFormatHelper;
import com.sia.hunter.helper.JSONHelper;
import com.sia.hunter.helper.StringHelper;
import com.sia.hunter.register.OnlineTaskRegister;
import com.sia.hunter.taskstatus.TaskStatus;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * compensation mechanism
 * @see
 * @author maozhengwei
 * @date 2019-04-25 11:19
 * @version V1.0.0
 **/
@Aspect
@Component
public class OnlineTaskAspect {

    private static Logger LOGGER = LoggerFactory.getLogger(OnlineTaskAspect.class);

    private static Map<String, TaskStatus> runningTaskMap = new ConcurrentHashMap<String, TaskStatus>();

    @Pointcut("@annotation(onlineTask)")
    public void serviceStatistics(OnlineTask onlineTask) {

    }

    // @Autowired
    // @Qualifier("producer")
    // private Producer producer;

    // @Value("${spring.application.name}")
    // public String applicationName;
    //
    // @Value("${spring.cloud.client.ipAddress}")
    // public String host;
    //
    // @Value("${server.port}")
    // public String port;

    // @Value("${online.task.message.switch:false}")
    // public boolean onlineTaskMessageSwitch;

    @Autowired
    private OnlineTaskAppProperty onlineTaskAppProperty;

    /**
     * Singletons are guaranteed to be single-threaded
     * @param joinPoint
     * @param onlineTask
     * @throws Throwable
     */
    @Before(value = "serviceStatistics(onlineTask)")
    public void deBefore(JoinPoint joinPoint, OnlineTask onlineTask) throws Throwable {

        if (!checkAuth(onlineTask)) {
            throw new OnlineTaskAuthException("This task is not authorized");
        }
        if (!onlineTaskAppProperty.getOnlinetaskSerial()) {
            return;
        }
        if (!isSerial(onlineTask)) {
            return;
        }
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = attributes.getRequest();
        String task = joinPoint.getSignature().getDeclaringTypeName() + "." + joinPoint.getSignature().getName();
        if (runningTaskMap.containsKey(task) && TaskStatus.RUNNING.equals(runningTaskMap.get(task))) {
            throw new OnlineTaskException("This task is running");
        }
        else {
            // if(onlineTaskMessageSwitch) {
            // producer.sendPubSub(AmqpConfig.ONLINE_TASK_EX_NAME, builtMqMessage(request).put("TaskStatus",
            // "Begin").toString());
            // LOGGER.info(builtMqMessage(request).put("TaskStatus", "Begin").put("MqMessage","true").toString());
            // }else{
            Map<String, String> info = builtMqMessage(request);
            info.put("TaskStatus", "Begin");
            info.put("MqMessage", "false");
            LOGGER.info(JSONHelper.toString(info));
            // }
            runningTaskMap.put(task, TaskStatus.RUNNING);
        }
    }

    /**
     * Task successfully executed
     * @param joinPoint
     * @param onlineTask
     * @param result
     */
    @AfterReturning(returning = "result", pointcut = "serviceStatistics(onlineTask)")
    public void afterReturning(JoinPoint joinPoint, OnlineTask onlineTask, Object result) {

        if (!onlineTaskAppProperty.getOnlinetaskSerial()) {
            return;
        }
        if (!isSerial(onlineTask)) {
            return;
        }
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = attributes.getRequest();
        String task = joinPoint.getSignature().getDeclaringTypeName() + "." + joinPoint.getSignature().getName();
        if (TaskStatus.RUNNING.equals(runningTaskMap.get(task))) {
            // if(onlineTaskMessageSwitch) {
            // producer.sendPubSub(AmqpConfig.ONLINE_TASK_EX_NAME, builtMqMessage(request).put("Result",
            // result).put("TaskStatus", "Success").toString());
            // LOGGER.info(builtMqMessage(request).put("TaskStatus", "Success").put("Result",
            // result).put("MqMessage","true").toString());
            // }else {
            Map<String, String> info = builtMqMessage(request);
            info.put("TaskStatus", "Success");
            info.put("MqMessage", "false");
            LOGGER.info(JSONHelper.toString(info));
            // }
            runningTaskMap.put(task, TaskStatus.STOP);
        }
    }

    /**
     * Task execution exception
     * @param joinPoint
     * @param ex
     * @param onlineTask
     */
    @AfterThrowing(pointcut = "serviceStatistics(onlineTask)", throwing = "ex")
    public void throwss(JoinPoint joinPoint, Throwable ex, OnlineTask onlineTask)
            throws OnlineTaskException, OnlineTaskAuthException {

        if (ex instanceof OnlineTaskException) {
            LOGGER.info("OnlineTaskExcpetion do not handle");
            throw new OnlineTaskException("This task is running");

        }
        else if (ex instanceof OnlineTaskAuthException) {
            LOGGER.info("OnlineTaskAuthExcpetion do not handle");
            throw new OnlineTaskAuthException("This task is not authorized");
        }
        else {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder
                    .getRequestAttributes();
            HttpServletRequest request = attributes.getRequest();
            String task = joinPoint.getSignature().getDeclaringTypeName() + "." + joinPoint.getSignature().getName();
            if (TaskStatus.RUNNING.equals(runningTaskMap.get(task))) {
                // if(onlineTaskMessageSwitch) {
                // producer.sendPubSub(AmqpConfig.ONLINE_TASK_EX_NAME, builtMqMessage(request).put("TaskStatus",
                // "Exception").toString());
                // LOGGER.info(builtMqMessage(request).put("TaskStatus", "Fail").put("MqMessage","true").toString());
                // }else{
                Map<String, String> info = builtMqMessage(request);
                info.put("TaskStatus", "Fail");
                info.put("MqMessage", "false");
                LOGGER.info(JSONHelper.toString(info));
                // }
                runningTaskMap.put(task, TaskStatus.STOP);
            }
        }
    }

    /**
     * @param request
     * @return
     */
    public Map<String, String> builtMqMessage(HttpServletRequest request) {

        Map<String, String> jsonObject = new HashMap<String, String>(8);
        jsonObject.put("GroupName", onlineTaskAppProperty.getApplicationName().substring(0,
                onlineTaskAppProperty.getApplicationName().indexOf("-")));
        jsonObject.put("ApplicationName", onlineTaskAppProperty.getApplicationName());
        jsonObject.put("TaskName", onlineTaskAppProperty.getApplicationName() + ":" + request.getServletPath());
        jsonObject.put("Host", onlineTaskAppProperty.getIPAddress() + ":" + onlineTaskAppProperty.getServerPort());
        jsonObject.put("RequestURL", request.getRequestURL().toString());
        jsonObject.put("Date", DateFormatHelper.format(new Date()));
        return jsonObject;
    }

    private boolean isSerial(OnlineTask instance) {

        boolean flag = true;
        try {
            flag = instance.enableSerial();
        }
        catch (Throwable e) {
            LOGGER.error("OnlineTask.enableSerial() throws :", e);
        }
        return flag;
    }

    private boolean isAuth(OnlineTask instance) {

        boolean flag = true;
        try {
            flag = instance.enableAuth();
        }
        catch (Throwable e) {
            LOGGER.error("OnlineTask.enableAuth() throws :", e);
        }
        return flag;
    }

    private boolean shouldIAccept(String ip) {

        if (StringHelper.isEmpty(ip)) {
            return false;
        }

        List<String> whiteList = OnlineTaskRegister.getAuthList();
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

    private boolean checkAuth(OnlineTask instance) {

        if (isAuth(instance)) {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder
                    .getRequestAttributes();
            HttpServletRequest request = attributes.getRequest();
            String ip = request.getRemoteAddr();
            LOGGER.debug("who call me ? ->[" + ip + "]");
            return shouldIAccept(ip);
        }
        return true;
    }
}
