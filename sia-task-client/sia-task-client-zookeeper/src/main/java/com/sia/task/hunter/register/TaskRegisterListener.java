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

package com.sia.task.hunter.register;

import com.sia.task.core.IMetadataUpload;
import com.sia.task.core.task.SiaTaskMeta;
import com.sia.task.core.util.Constant;
import com.sia.task.core.util.StringHelper;
import com.sia.task.hunter.annotation.OnlineTask;
import com.sia.task.hunter.helper.OnlineTaskHelper;
import com.sia.task.integration.curator.properties.ZookeeperConfiguration;
import com.sia.task.integration.curator.properties.ZookeeperConstant;
import com.sia.task.register.zookeeper.collector.SiaTaskCollector;
import lombok.extern.slf4j.Slf4j;
import org.reflections.Reflections;
import org.reflections.scanners.MethodAnnotationsScanner;
import org.reflections.scanners.TypeAnnotationsScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.annotation.Resource;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;


/**
 * Listener registered for uploading task metadata
 *
 * @author pengfeili23
 * @date 2018年7月11日 下午4:11:19
 * <p>
 * 解耦客户端针对注册中心组建的耦合依赖 - @author: zhengweimao
 * <p>
 * 修复Reflections由于全量扫描导致大量Exception问题 - @author: zhengweimao
 * 建议使用客户端是指定<code>{@link ZookeeperConfiguration#setScanBasicPackage(String)}</code>
 */
@Slf4j
public class TaskRegisterListener implements ApplicationListener<ApplicationEvent> {

    @Resource
    private IMetadataUpload uploadTaskMetaData;

    @Resource
    ZookeeperConfiguration configuration;

    private String validContextPath = "";

    private String scanBasicPackage;

    private static final AtomicBoolean START = new AtomicBoolean(false);


    @Override
    public void onApplicationEvent(ApplicationEvent event) {
        if (event instanceof ContextRefreshedEvent) {
            if (START.compareAndSet(false, true)) {
                try {
                    onlineTaskDetector();
                    uploadTaskMetaData.uploadTaskMetaData();
                    log.info(Constant.LOG_PREFIX + "upload OnlineTask OK");
                } catch (Exception e) {
                    log.error(Constant.LOG_PREFIX + "upload OnlineTask FAIL, please mailto [you email address]");
                }
            } else {
                log.info(Constant.LOG_PREFIX + "upload OnlineTask already");
            }
        }

    }

    private String buildLabel(String className, String methodName) {

        return "className:[" + className + "], methodName:[" + methodName + "]";
    }

    private void onlineTaskDetector() {

        validContextPath = getValidContextPath();
        scanBasicPackage = getScanBasicPackage();
        ConfigurationBuilder builder = new ConfigurationBuilder().useParallelExecutor();
        if (StringHelper.isEmpty(scanBasicPackage)) {
            builder.setUrls(ClasspathHelper.forClassLoader());
        } else {
            builder.setUrls(ClasspathHelper.forPackage(scanBasicPackage));
        }
        builder.setScanners(new MethodAnnotationsScanner(), new TypeAnnotationsScanner());

        Reflections reflections = new Reflections(builder);
        Set<Method> onlineTaskMethods = reflections.getMethodsAnnotatedWith(OnlineTask.class);

        for (Method method : onlineTaskMethods) {
            annotationFilter(method);
        }
    }

    private void annotationFilter(Method method) {

        Class<?> declaringClass = method.getDeclaringClass();
        String className = declaringClass.getName();
        String methodName = method.getName();
        String label = buildLabel(className, methodName);

        String middlePath = getTypeRequestMapping(declaringClass);
        String description = "";
        String beanName = "";
        boolean hasRequestMapping = false;
        Annotation[] methodAnnotations = method.getAnnotations();

        for (Annotation annotation : methodAnnotations) {
            if (annotation.annotationType().equals(OnlineTask.class)) {
                // 抓取自定义注解信息
                OnlineTask online = (OnlineTask) annotation;
                description = online.description();
            }
        }
        for (Annotation annotation : methodAnnotations) {
            if (annotation.annotationType().equals(RequestMapping.class)) {
                hasRequestMapping = true;
                // 抓取 RequestMapping 注解内容
                RequestMapping request = (RequestMapping) annotation;

                // check start
                // 默认输入参数只有一个，且是String（JSON）
                Class<?>[] input = method.getParameterTypes();
                if (OnlineTaskHelper.checkParameterTypes(input) < 0) {
                    SiaTaskCollector.setErrorMessage(label, "无参或默认输入参数只有一个，且是[String]（JSON）");
                    continue;
                }

                // 默认返回值是String（JSON）
                Class<?> output = method.getReturnType();
                if (!OnlineTaskHelper.checkReturnType(output)) {
                    SiaTaskCollector.setErrorMessage(label, "默认返回值是[String]（JSON）");
                    continue;
                }

                // 必须对外暴露POST方法
                RequestMethod[] httpMethod = request.method();
                if (!OnlineTaskHelper.checkRequestMethod(httpMethod)) {
                    SiaTaskCollector.setErrorMessage(label, "必须对外暴露[POST]方法");
                    continue;
                }
                // check end

                // get valid paths, may have many path or none
                String[] httpPaths = getHttpPath(request);
                // RequestMapping注解的HTTP PATH为空
                if (null == httpPaths || httpPaths.length <= 0) {
                    SiaTaskCollector.setErrorMessage(label,
                            "[RequestMapping]注解的[value]属性为空。因为Spring较低版本，[RequestMapping]注解没有[path]属性，这里请使用[value]属性来配置访问路径");
                    continue;
                }
                List<String> validPath = new LinkedList<String>();

                for (String path : httpPaths) {
                    // HTTP的访问路径必须以"/"为前缀，且路径中不含"\"(用作替换)，否则认为不合法
                    if (!OnlineTaskHelper.checkHttpPath(path)) {
                        SiaTaskCollector.setErrorMessage(label, "HTTP的访问路径必须以\"/\"为前缀，且路径中不含\"\\\"(用作替换)，否则认为不合法");
                        continue;
                    }
                    if (!validPath.contains(path)) {
                        validPath.add(path);
                    }
                }

                for (String path : validPath) {
                    String finalPath = validContextPath + middlePath + path;
                    SiaTaskMeta instanceInfo = new SiaTaskMeta();
                    instanceInfo.setGroupName(configuration.getGroupName());
                    instanceInfo.setApplicationName(configuration.getApplicationName());
                    instanceInfo.setIpAndPort(configuration.getIPAndPort());

                    instanceInfo.setInput(OnlineTaskHelper.toList(input));
                    instanceInfo.setOutput(output.getName());
                    instanceInfo.setHttpMethod(OnlineTaskHelper.toList(httpMethod));

                    instanceInfo.setHttpPath(finalPath);
                    instanceInfo.setMethodName(methodName);
                    instanceInfo.setClassName(className);
                    instanceInfo.setBeanName(beanName);
                    instanceInfo.setDescription(description);

                    SiaTaskCollector.setOnlineTask(finalPath, instanceInfo);
                }
            }
        }
        if (!hasRequestMapping) {
            SiaTaskCollector.setErrorMessage(label, "方法没有使用[RequestMapping]注解");
        }

    }

    private String getValidContextPath() {

        String contextPath = configuration.getContextPath();
        if (!OnlineTaskHelper.checkHttpPath(contextPath)) {
            String label = "contextPath:[" + contextPath + "]";
            SiaTaskCollector.setErrorMessage(label, "HTTP的访问路径必须以\"/\"为前缀，且路径中不含\"\\\"(用作替换)，否则认为不合法");
            contextPath = ZookeeperConstant.DEFAULT_CONTEXT;
        }
        if (contextPath.equals(ZookeeperConstant.DEFAULT_CONTEXT)) {
            contextPath = "";
        }
        return contextPath;
    }

    private String getScanBasicPackage() {
        return configuration.getScanBasicPackage();
    }

    private String getTypeRequestMapping(Class<?> declaringClass) {

        String label = "className:[" + declaringClass.getName() + "], annotationType:[RequestMapping]";
        Annotation[] classAnnotaion = declaringClass.getAnnotations();
        for (Annotation annotation : classAnnotaion) {

            if (annotation.annotationType().equals(RequestMapping.class)) {
                // 抓取 RequestMapping 注解内容
                RequestMapping requestMapping = (RequestMapping) annotation;
                String[] httpPaths = getHttpPath(requestMapping);
                if (null == httpPaths || httpPaths.length <= 0) {
                    SiaTaskCollector.setErrorMessage(label,
                            "[RequestMapping]注解的[value]属性为空。因为Spring较低版本，[RequestMapping]注解没有[path]属性，这里请使用[value]属性来配置访问路径");
                    continue;
                }
                for (String path : httpPaths) {
                    // HTTP的访问路径必须以"/"为前缀，且路径中不含"\"(用作替换)，否则认为不合法
                    if (!OnlineTaskHelper.checkHttpPath(path)) {
                        SiaTaskCollector.setErrorMessage(label, "HTTP的访问路径必须以\"/\"为前缀，且路径中不含\"\\\"(用作替换)，否则认为不合法");
                        continue;
                    }
                    if (path.equals(ZookeeperConstant.DEFAULT_CONTEXT)) {
                        path = "";
                    }
                    return path;
                }
            }
        }
        return "";
    }

    /**
     * 为了兼容低版本
     *
     * @param request
     * @return
     */
    private String[] getHttpPath(RequestMapping request) {

        String[] httpPaths = request.value();
        try {
            if (null == httpPaths || httpPaths.length <= 0) {
                httpPaths = request.path();
            }
        } catch (Throwable e) {
            log.error(Constant.LOG_PREFIX + "使用Spring较低版本，RequestMapping注解没有path属性", e);
        }

        return httpPaths;
    }
}
