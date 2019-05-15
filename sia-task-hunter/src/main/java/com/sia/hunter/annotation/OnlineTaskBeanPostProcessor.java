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

//
//package com.sia.hunter.annotation;
//
//import com.sia.hunter.collector.OnlineTaskCollector;
//import OnlineTaskAppProperty;
//import OnlineTaskHelper;
//import com.sia.hunter.pojo.OnlineTaskPojo;
//import org.springframework.aop.support.AopUtils;
//import org.springframework.beans.BeansException;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.beans.factory.config.BeanPostProcessor;
//import org.springframework.hunter.MethodIntrospector;
//import org.springframework.hunter.Ordered;
//import org.springframework.hunter.annotation.AnnotatedElementUtils;
//import org.springframework.stereotype.Component;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RequestMethod;
//
//import java.lang.reflect.Method;
//import java.util.LinkedList;
//import java.util.List;
//import java.util.Map;
//import java.util.Map.Entry;
//import java.util.Set;
//
//@Component
//public class OnlineTaskBeanPostProcessor implements BeanPostProcessor, Ordered {
//
//
//    @Autowired
//    private OnlineTaskAppProperty onlineTaskAppProperty;
//
//    private String buildLabel(String className, String methodName) {
//
//        return "className:[" + className + "], methodName:[" + methodName + "]";
//    }
//
//    /**
//     * 在Spring机制中可以指定后置处理器调用顺序，通过让BeanPostProcessor接口实现类实现Ordered接口getOrder方法，该方法返回一整数。
//     * <p>
//     * 默认值为 0，优先级最高，值越大优先级越低
//     */
//    public int getOrder() {
//
//        return Integer.MAX_VALUE - 10086;
//    }
//
//    /**
//     * bean初始化之前执行
//     *
//     * @param bean
//     * @param beanName
//     * @return
//     * @throws BeansException
//     */
//    @Override
//    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
//
//        return bean;
//    }
//
//    /**
//     * bean初始化之后执行
//     *
//     * @param bean
//     * @param beanName
//     * @return
//     * @throws BeansException
//     */
//    @Override
//    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
//
//        // bean 所在的类
//        Class<?> targetClass = AopUtils.getTargetClass(bean);
//        // 获得类中所有带 OnlineTask 注解 的方法
//        Map<Method, Set<OnlineTask>> OnlineTaskMethods = MethodIntrospector.selectMethods(targetClass,
//                new MethodIntrospector.MetadataLookup<Set<OnlineTask>>() {
//
//                    public Set<OnlineTask> inspect(Method method) {
//
//                        Set<OnlineTask> OnlineTaskMethods = AnnotatedElementUtils.getAllMergedAnnotations(method,
//                                OnlineTask.class);
//                        return (!OnlineTaskMethods.isEmpty() ? OnlineTaskMethods : null);
//                    }
//                });
//        // 带 OnlineTask注解 的方法集合非空
//        if (!OnlineTaskMethods.isEmpty()) {
//            String className = targetClass.getName();
//            // 获得类中所有带 RequestMapping 注解 的方法
//            Map<Method, Set<RequestMapping>> RequestMappingMethods = MethodIntrospector.selectMethods(targetClass,
//                    new MethodIntrospector.MetadataLookup<Set<RequestMapping>>() {
//
//                        public Set<RequestMapping> inspect(Method method) {
//
//                            Set<RequestMapping> RequestMappingMethods = AnnotatedElementUtils
//                                    .getAllMergedAnnotations(method, RequestMapping.class);
//                            return (!RequestMappingMethods.isEmpty() ? RequestMappingMethods : null);
//                        }
//                    });
//
//            // 判断方法是否使用 RequestMapping 注解
//            for (Entry<Method, Set<OnlineTask>> entry : OnlineTaskMethods.entrySet()) {
//                Method method = entry.getKey();
//
//                String description = "";
//                Set<OnlineTask> tasks = entry.getValue();
//                if (tasks != null) {
//                    for (OnlineTask task : tasks) {
//                        description = task.description();
//                    }
//                }
//
//                Set<RequestMapping> instance = RequestMappingMethods.get(method);
//                String methodName = method.getName();
//                String label = buildLabel(className, methodName);
//                // 方法没有使用 RequestMapping 注解
//                if (null == instance) {
//                    OnlineTaskCollector.setErrorMessage(label, "方法没有使用[RequestMapping]注解");
//                    continue;
//                }
//                for (RequestMapping request : instance) {
//
//                    // check start
//                    // 默认输入参数只有一个，且是String（JSON）
//                    Class<?>[] input = method.getParameterTypes();
//                    if (!OnlineTaskHelper.checkParameterTypes(input)) {
//                        OnlineTaskCollector.setErrorMessage(label, "默认输入参数只有一个，且是[String]（JSON）");
//                        continue;
//                    }
//
//                    // 默认返回值是String（JSON）
//                    Class<?> output = method.getReturnType();
//                    if (!OnlineTaskHelper.checkReturnType(output)) {
//                        OnlineTaskCollector.setErrorMessage(label, "默认返回值是[String]（JSON）");
//                        continue;
//                    }
//
//                    // 必须对外暴露POST方法
//                    RequestMethod[] httpMethod = request.method();
//                    if (!OnlineTaskHelper.checkRequestMethod(httpMethod)) {
//                        OnlineTaskCollector.setErrorMessage(label, "必须对外暴露[POST]方法");
//                        continue;
//                    }
//                    // check end
//
//                    // get valid paths, may have many path or none
//                    String[] httpPaths = request.path();
//                    // RequestMapping注解的HTTP PATH为空
//                    if (null == httpPaths || httpPaths.length <= 0) {
//                        OnlineTaskCollector.setErrorMessage(label, "[RequestMapping]注解的[HTTP PATH]为空");
//                        continue;
//                    }
//                    List<String> validPath = new LinkedList<String>();
//
//                    for (String path : httpPaths) {
//                        // HTTP的访问路径必须以"/"为前缀，且路径中不含"\"(用作替换)，否则认为不合法
//                        if (!OnlineTaskHelper.checkHttpPath(path)) {
//                            OnlineTaskCollector.setErrorMessage(label,
//                                    "HTTP的访问路径必须以\"/\"为前缀，且路径中不含\"\\\"(用作替换)，否则认为不合法");
//                            continue;
//                        }
//                        if (!validPath.contains(path)) {
//                            validPath.add(path);
//                        }
//                    }
//
//                    for (String path : validPath) {
//                        OnlineTaskPojo instanceInfo = new OnlineTaskPojo();
//                        instanceInfo.setGroupName(onlineTaskAppProperty.getGroupName());
//                        instanceInfo.setApplicationName(onlineTaskAppProperty.getApplicationName());
//                        instanceInfo.setIpAndPort(onlineTaskAppProperty.getIPAndPort());
//
//                        instanceInfo.setInput(OnlineTaskHelper.toList(input));
//                        instanceInfo.setOutput(output.getName());
//                        instanceInfo.setHttpMethod(OnlineTaskHelper.toList(httpMethod));
//
//                        instanceInfo.setHttpPath(path);
//                        instanceInfo.setMethodName(methodName);
//                        instanceInfo.setClassName(className);
//                        instanceInfo.setBeanName(beanName);
//
//                        instanceInfo.setDescription(description);
//
//                        OnlineTaskCollector.setOnlineTask(path, instanceInfo);
//
//                    }
//
//                }
//
//            }
//        }
//
//        return bean;
//
//    }
//
//}
