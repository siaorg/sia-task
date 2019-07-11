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

package com.sia.hunter.register;

import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.listener.Event;
import com.alibaba.nacos.api.naming.listener.EventListener;
import com.alibaba.nacos.api.naming.listener.NamingEvent;
import com.alibaba.nacos.api.naming.pojo.Instance;
import com.sia.hunter.constant.OnlineTaskAppProperty;
import com.sia.hunter.helper.JSONHelper;
import com.sia.hunter.helper.OnlineTaskHelper;
import com.sia.hunter.nacos.NacosClient;
import com.sia.hunter.nacos.NacosClient4LessInstance;
import com.sia.hunter.pojo.OnlineTaskPojo;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.sia.hunter.annotation.OnlineTask;
import com.sia.hunter.collector.OnlineTaskCollector;
import com.sia.hunter.constant.OnlineTaskConstant;
import com.sia.hunter.zookeeper.CuratorClient;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.TreeCache;
import org.apache.curator.framework.recipes.cache.TreeCacheEvent;
import org.apache.curator.framework.recipes.cache.TreeCacheEvent.Type;
import org.apache.curator.framework.recipes.cache.TreeCacheListener;
import org.apache.curator.framework.state.ConnectionState;
import org.apache.curator.framework.state.ConnectionStateListener;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.Watcher.Event.EventType;
import org.apache.zookeeper.ZooKeeper;
import org.reflections.Reflections;
import org.reflections.scanners.MethodAnnotationsScanner;
import org.reflections.scanners.TypeAnnotationsScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.annotation.PreDestroy;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 *
 *
 * @description OnlineTaskRegister
 * @see
 * @author pengfeili23
 * @date 2018-07-11 16:11:19
 * @version V1.0.0
 **/
@Component
public class OnlineTaskRegister implements ApplicationListener<ApplicationEvent> {

    private static final Logger LOGGER = LoggerFactory.getLogger(OnlineTaskRegister.class);

    @Autowired
    public CuratorClient client;
    @Autowired
    private OnlineTaskAppProperty onlineTaskAppProperty;

    private String validContextPath = "";

    private static final AtomicBoolean START = new AtomicBoolean(false);

    @Value("${enbaleRegistry}")
    private String registry;

    @Value("${ZK_ONLINE_ROOT_PATH:SkyWorldOnlineTask}")
    private String zkOnlineRoot;

    /***
     * nacos相关
     */
    @Autowired
    public NacosClient nacosClient;

    private String getZkOnlineRootPath() {
        StringBuilder zkOnlineRootPath = new StringBuilder().append(OnlineTaskConstant.ZK_SEPARATOR).append(zkOnlineRoot);
        return zkOnlineRootPath.toString();
    }

    private String getZkOnlineTaskPath() {
        StringBuilder zkOnlineTaskPath = new StringBuilder().append(OnlineTaskConstant.ZK_SEPARATOR).append(zkOnlineRoot).append(OnlineTaskConstant.ZK_SEPARATOR).append(OnlineTaskConstant.ZK_ONLINE_TASK);
        return zkOnlineTaskPath.toString();
    }

    private String getZkOnlineAuthPath() {
        StringBuilder zkOnlineAuthPath = new StringBuilder().append(OnlineTaskConstant.ZK_SEPARATOR).append(zkOnlineRoot).append(OnlineTaskConstant.ZK_SEPARATOR).append(OnlineTaskConstant.ZK_ONLINE_AUTH);
        return zkOnlineAuthPath.toString();
    }

    @Override
    public void onApplicationEvent(ApplicationEvent event) {
        // 应用刷新
        if (event instanceof ContextRefreshedEvent) {
            if (START.compareAndSet(false, true)) {
                // 关键代码 try-catch
                try {
                    onlineTaskDetector();
                    if ("zookeeper".equals(registry)){
                        onlineTaskUpload();
                        // handle ConnectionState.LOST
                        ConnectionStateListener listener = new ConnectionStateListener() {

                            @Override
                            public void stateChanged(CuratorFramework client, ConnectionState newState) {

                                LOGGER.info(OnlineTaskConstant.LOGPREFIX + "OnlineTaskRegister Zookeeper ConnectionState:"
                                        + newState.name());

                                if (newState == ConnectionState.LOST) {
                                    while (true) {
                                        try {
                                            if (client.getZookeeperClient().blockUntilConnectedOrTimedOut()) {
                                                LOGGER.info(OnlineTaskConstant.LOGPREFIX
                                                        + "OnlineTaskRegister Zookeeper Reconnected");
                                                onlineTaskUpload();
                                                LOGGER.info(OnlineTaskConstant.LOGPREFIX
                                                        + "OnlineTaskRegister onlineTaskUpload Redo");

                                                break;
                                            }
                                        }
                                        catch (InterruptedException e) {
                                            LOGGER.error(OnlineTaskConstant.LOGPREFIX
                                                            + "Zookeeper Reconnect FAIL, please mailto [***@********.cn]",
                                                    e);
                                        }
                                    }
                                }
                            }
                        };
                        ThreadFactory namedThreadFactory = new ThreadFactoryBuilder().setNameFormat("Zookeeper-Reconnected-%d").build();
                        ExecutorService pool =new ThreadPoolExecutor(4, 4, 0L, TimeUnit.MILLISECONDS,new LinkedBlockingQueue<Runnable>(1024), namedThreadFactory, new ThreadPoolExecutor.AbortPolicy());
                        client.client().getConnectionStateListenable().addListener(listener, pool);
                    }else if ("nacos".equals(registry)){
                        onlineTaskUpload4Nacos();
                    }

                    LOGGER.info(OnlineTaskConstant.LOGPREFIX + "upload OnlineTask OK");

                }
                catch (Exception e) {
                    LOGGER.error(OnlineTaskConstant.LOGPREFIX
                            + "upload OnlineTask FAIL, please mailto [***@********.cn]");
                }
            }
            else {
                LOGGER.info(OnlineTaskConstant.LOGPREFIX + "upload OnlineTask already");
            }
        }

    }

    /**
     *
     * OnlineTask upload
     * {@link } can be checked for the result.
     * @param
     * @return
     * @throws
     */
    private void onlineTaskUpload() {

        if (!checkRegister()) {
            LOGGER.error(OnlineTaskConstant.LOGPREFIX + "使用OnlineTask需要配置统一zookeeper地址");
            return;
        }

        /**
         * 使用只写权限
         */
        client.addCreateAuth();

        // ---------------预处理，开始---------------------------
        /**
         * 是否存在根路径
         */
        if (!client.isExists(getZkOnlineRootPath())) {
            client.createPersistentZKNode(getZkOnlineRootPath());
        }

        /**
         * 是否存在Task路径
         */
        if (!client.isExists(getZkOnlineTaskPath())) {
            client.createPersistentZKNode(getZkOnlineTaskPath());
        }

        /**
         * 是否存在调度器HTTP调用授权路径
         */
        if (!client.isExists(getZkOnlineAuthPath())) {
            client.createPersistentZKNode(getZkOnlineAuthPath());
        }
        /**
         * 初始化授权白名单
         */
        try {
            handleAuth(getZkOnlineAuthPath(), client);
        }
        catch (Exception e) {
            LOGGER.error(OnlineTaskConstant.LOGPREFIX, e);
        }
        /**
         * 监听授权白名单的变化
         */
        try {
            monitorAuth(getZkOnlineAuthPath(), client);
        }
        catch (Exception e) {
            LOGGER.error(OnlineTaskConstant.LOGPREFIX, e);
        }
        /**
         * 是否存在group路径
         */
        String groupPath = getZkOnlineTaskPath() + OnlineTaskConstant.ZK_SEPARATOR
                + onlineTaskAppProperty.getGroupName();
        if (!client.isExists(groupPath)) {
            client.createPersistentZKNode(groupPath);
        }

        /**
         * 是否存在application路径
         */
        String applicationPath = groupPath + OnlineTaskConstant.ZK_SEPARATOR
                + onlineTaskAppProperty.getApplicationName();
        if (!client.isExists(applicationPath)) {
            client.createPersistentZKNode(applicationPath);
        }
        // ---------------预处理，结束---------------------------

        final List<String> paths = new LinkedList<String>();
        int count = 0;

        try {
            Map<String, OnlineTaskPojo> onlineTask = OnlineTaskCollector.getOnlineTask();
            final int size = onlineTask.size();
            for (Map.Entry<String, OnlineTaskPojo> onlineTaskEntry : onlineTask.entrySet()) {
                String httpPath = onlineTaskEntry.getKey();
                OnlineTaskPojo instance = onlineTaskEntry.getValue();
                String metaData = buildMetaData(instance);

                /**
                 * 装饰一下路径
                 */
                String encodedPath = OnlineTaskHelper.encodeHttpPath(httpPath);
                String taskKey = onlineTaskAppProperty.getApplicationName() + OnlineTaskConstant.ZK_KEY_SPLIT
                        + encodedPath;
                String onlineTaskPath = applicationPath + OnlineTaskConstant.ZK_SEPARATOR + taskKey;

                /**
                 * 不存在则新建，taskKey 路径
                 */
                if (!client.isExists(onlineTaskPath)) {
                    client.createPersistentZKNode(onlineTaskPath, metaData);
                }

                /**
                 * 设置<描述信息+输入参数个数>
                 */
                client.setData(onlineTaskPath, metaData);

                /**
                 * 具体实例的路径
                 */
                String instancePath = onlineTaskPath + OnlineTaskConstant.ZK_SEPARATOR
                        + onlineTaskAppProperty.getIPAndPort();
                if (!client.isExists(instancePath)) {
                    client.createEphemeralZKNode(instancePath, JSONHelper.toString(instance));
                }
                else {

                    LOGGER.info(OnlineTaskConstant.LOGPREFIX + instancePath + " already exists! We watchNodeDeleted");
                    watchNodeDeleted(instancePath, JSONHelper.toString(instance));
                }
                count++;
                paths.add(httpPath);
            }
            LOGGER.info(OnlineTaskConstant.LOGPREFIX + "#########上传OnlineTask结束，共成功上传[" + count + "/" + size
                    + "]个OnlineTask#########");

        }
        catch (Exception ex) {
            LOGGER.error(OnlineTaskConstant.LOGPREFIX, ex);
        }

        LOGGER.info(OnlineTaskConstant.LOGPREFIX + "↓↓↓↓↓↓↓↓↓↓上传OnlineTask明细↓↓↓↓↓↓↓↓↓↓");

        count = 0;
        for (String path : paths) {
            count++;
            LOGGER.info(OnlineTaskConstant.LOGPREFIX + "序号:[" + count + "]，HTTP PATH:[" + path + "]");
        }

        LOGGER.info(OnlineTaskConstant.LOGPREFIX + "↑↑↑↑↑↑↑↑↑↑上传OnlineTask明细，共[" + count + "]个↑↑↑↑↑↑↑↑↑↑");
        LOGGER.info(OnlineTaskConstant.LOGPREFIX + "↓↓↓↓↓↓↓↓↓↓以下是未上传的OnlineTask明细，分为两部分↓↓↓↓↓↓↓↓↓↓");
        LOGGER.info(OnlineTaskConstant.LOGPREFIX + "↓↓↓↓↓↓↓↓↓↓第一部分：合规，但会访问异常(HTTP PATH 重复)OnlineTask信息明细↓↓↓↓↓↓↓↓↓↓");

        count = 0;
        Set<String> errorTaskPath = OnlineTaskCollector.getErrorTask().keySet();
        for (String path : errorTaskPath) {
            count++;
            LOGGER.info(OnlineTaskConstant.LOGPREFIX + "第一部分，序号:[" + count + "]，重复的HTTP PATH:[" + path + "]");
        }

        LOGGER.info(OnlineTaskConstant.LOGPREFIX + "↑↑↑↑↑↑↑↑↑↑第一部分：合规，但会访问异常(HTTP PATH 重复)OnlineTask信息明细，共[" + count
                + "]个↑↑↑↑↑↑↑↑↑↑");
        LOGGER.info(OnlineTaskConstant.LOGPREFIX + "↓↓↓↓↓↓↓↓↓↓第二部分：不合规的OnlineTask信息明细↓↓↓↓↓↓↓↓↓↓");

        count = 0;
        Map<String, String> errorMsg = OnlineTaskCollector.getErrorMessage();
        for (Entry<String, String> msg : errorMsg.entrySet()) {
            count++;
            LOGGER.info(OnlineTaskConstant.LOGPREFIX + "第二部分，序号:[" + count + "]，方法名:[" + msg.getKey() + "]，不合规的信息提示:["
                    + msg.getValue() + "]");
        }
        LOGGER.info(OnlineTaskConstant.LOGPREFIX + "↑↑↑↑↑↑↑↑↑↑第二部分：不合规的OnlineTask信息明细，，共[" + count + "]个↑↑↑↑↑↑↑↑↑↑");
        LOGGER.info(OnlineTaskConstant.LOGPREFIX + "↑↑↑↑↑↑↑↑↑↑以上是未上传的OnlineTask明细↑↑↑↑↑↑↑↑↑↑");
    }

    private boolean checkRegister() {

        return client != null && client.getCuratorFramework() != null;
    }

    @PreDestroy
    public void destory() {
        if (checkRegister()) {
            try {
                this.client.close();
            }
            catch (Exception e) {
                LOGGER.error(OnlineTaskConstant.LOGPREFIX, e);
            }
            LOGGER.info(OnlineTaskConstant.LOGPREFIX + "close zookeeper connection");
        }
    }

    /**
     *
     * 当应用快速重启（重启时间间隔<临时节点的过期时间），不会重新创建临时节点（节点已存在），等临时节点过期后，预期应该存在的节点消失啦。这里通过添加监听器，在上次临时节点过期后，再新建本次临时节点，保证临时节点的存在符合预期。
     * {@link } can be checked for the result.
     * @param
     * @return
     * @throws
     */
    private void watchNodeDeleted(final String path, final String data) {

        try {
            ZooKeeper zk = client.getCuratorFramework().getZookeeperClient().getZooKeeper();
            zk.exists(path, new Watcher() {

                @Override
                public void process(WatchedEvent event) {

                    if (event.getType().equals(EventType.NodeDeleted)) {
                        client.createEphemeralZKNode(path, data);
                    }
                    else {
                        watchNodeDeleted(path, data);
                    }
                }
            });
        }
        catch (Exception e) {
            LOGGER.error(OnlineTaskConstant.LOGPREFIX, e);
        }
    }

    private String buildLabel(String className, String methodName) {

        return "className:[" + className + "], methodName:[" + methodName + "]";
    }

    private void onlineTaskDetector() {

        validContextPath = getValidContextPath();

        ConfigurationBuilder builder = new ConfigurationBuilder().useParallelExecutor()
                .setUrls(ClasspathHelper.forClassLoader())
                .setScanners(new MethodAnnotationsScanner(), new TypeAnnotationsScanner());
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
                    OnlineTaskCollector.setErrorMessage(label, "无参或默认输入参数只有一个，且是[String]（JSON）");
                    continue;
                }

                // 默认返回值是String（JSON）
                Class<?> output = method.getReturnType();
                if (!OnlineTaskHelper.checkReturnType(output)) {
                    OnlineTaskCollector.setErrorMessage(label, "默认返回值是[String]（JSON）");
                    continue;
                }

                // 必须对外暴露POST方法
                RequestMethod[] httpMethod = request.method();
                if (!OnlineTaskHelper.checkRequestMethod(httpMethod)) {
                    OnlineTaskCollector.setErrorMessage(label, "必须对外暴露[POST]方法");
                    continue;
                }
                // check end

                // get valid paths, may have many path or none
                String[] httpPaths = getHttpPath(request);
                // RequestMapping注解的HTTP PATH为空
                if (null == httpPaths || httpPaths.length <= 0) {
                    OnlineTaskCollector.setErrorMessage(label,
                            "[RequestMapping]注解的[value]属性为空。因为Spring较低版本，[RequestMapping]注解没有[path]属性，这里请使用[value]属性来配置访问路径");
                    continue;
                }
                List<String> validPath = new LinkedList<String>();

                for (String path : httpPaths) {
                    // HTTP的访问路径必须以"/"为前缀，且路径中不含"\"(用作替换)，否则认为不合法
                    if (!OnlineTaskHelper.checkHttpPath(path)) {
                        OnlineTaskCollector.setErrorMessage(label, "HTTP的访问路径必须以\"/\"为前缀，且路径中不含\"\\\"(用作替换)，否则认为不合法");
                        continue;
                    }
                    if (!validPath.contains(path)) {
                        validPath.add(path);
                    }
                }

                for (String path : validPath) {
                    String finalPath = validContextPath + middlePath + path;
                    OnlineTaskPojo instanceInfo = new OnlineTaskPojo();
                    instanceInfo.setGroupName(onlineTaskAppProperty.getGroupName());
                    instanceInfo.setApplicationName(onlineTaskAppProperty.getApplicationName());
                    instanceInfo.setIpAndPort(onlineTaskAppProperty.getIPAndPort());

                    instanceInfo.setInput(OnlineTaskHelper.toList(input));
                    instanceInfo.setOutput(output.getName());
                    instanceInfo.setHttpMethod(OnlineTaskHelper.toList(httpMethod));

                    instanceInfo.setHttpPath(finalPath);
                    instanceInfo.setMethodName(methodName);
                    instanceInfo.setClassName(className);
                    instanceInfo.setBeanName(beanName);

                    instanceInfo.setDescription(description);

                    OnlineTaskCollector.setOnlineTask(finalPath, instanceInfo);

                }

            }

        }
        if (!hasRequestMapping) {
            OnlineTaskCollector.setErrorMessage(label, "方法没有使用[RequestMapping]注解");
        }

    }

    private String getValidContextPath() {

        String contextPath = onlineTaskAppProperty.getContextPath();
        if (!OnlineTaskHelper.checkHttpPath(contextPath)) {
            String label = "contextPath:[" + contextPath + "]";
            OnlineTaskCollector.setErrorMessage(label, "HTTP的访问路径必须以\"/\"为前缀，且路径中不含\"\\\"(用作替换)，否则认为不合法");
            contextPath = OnlineTaskConstant.DEFAULT_CONTEXT;
        }
        if (contextPath.equals(OnlineTaskConstant.DEFAULT_CONTEXT)) {
            contextPath = "";
        }
        return contextPath;
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
                    OnlineTaskCollector.setErrorMessage(label,
                            "[RequestMapping]注解的[value]属性为空。因为Spring较低版本，[RequestMapping]注解没有[path]属性，这里请使用[value]属性来配置访问路径");
                    continue;
                }
                for (String path : httpPaths) {
                    // HTTP的访问路径必须以"/"为前缀，且路径中不含"\"(用作替换)，否则认为不合法
                    if (!OnlineTaskHelper.checkHttpPath(path)) {
                        OnlineTaskCollector.setErrorMessage(label, "HTTP的访问路径必须以\"/\"为前缀，且路径中不含\"\\\"(用作替换)，否则认为不合法");
                        continue;
                    }
                    if (path.equals(OnlineTaskConstant.DEFAULT_CONTEXT)) {
                        path = "";
                    }
                    return path;
                }
            }
        }
        return "";
    }

    /**
     *
     * 为了兼容低版本
     * {@link } can be checked for the result.
     * @param
     * @return
     * @throws
     */
    private String[] getHttpPath(RequestMapping request) {

        String[] httpPaths = request.value();
        try {
            if (null == httpPaths || httpPaths.length <= 0) {
                httpPaths = request.path();
            }
        }
        catch (Throwable e) {
            LOGGER.error(OnlineTaskConstant.LOGPREFIX + "使用Spring较低版本，RequestMapping注解没有path属性", e);
        }

        return httpPaths;
    }

    private String buildMetaData(OnlineTaskPojo instance) {

        Map<String, Object> meta = new HashMap<String, Object>(4);

        if (instance != null) {
            String description = instance.getDescription();
            Integer count = instance.getInput().size();

            meta.put("INFO", description);
            meta.put("COUNT", count);
        }
        else {
            meta.put("INFO", "NEVER HAPPEN");
            meta.put("COUNT", 1);
        }
        return JSONHelper.toString(meta);

    }

    /**
     *
     * 监听调度器HTTP调用授权路径（白名单）的变化
     * {@link } can be checked for the result.
     * @param
     * @return
     * @throws
     */
    private void monitorAuth(final String parentPath, final CuratorClient instance) throws Exception {

        // 创建treeCache监听器
        TreeCache treeCache = new TreeCache(instance.getCuratorFramework(), parentPath);
        TreeCacheListener treeCacheListener = new TreeCacheListener() {

            @Override
            public void childEvent(CuratorFramework client, TreeCacheEvent event) throws Exception {

                Type type = event.getType();
                // 白名单的变化（+1或-1）
                if (type == Type.NODE_ADDED || type == Type.NODE_REMOVED) {
                    handleAuth(parentPath, instance);
                }
            }

        };

        ThreadFactory namedThreadFactory = new ThreadFactoryBuilder().setNameFormat("Zookeeper-treeCacheListener-%d").build();
        ExecutorService pool =new ThreadPoolExecutor(4, 4, 0L, TimeUnit.MILLISECONDS,new LinkedBlockingQueue<Runnable>(1024), namedThreadFactory, new ThreadPoolExecutor.AbortPolicy());
        treeCache.getListenable().addListener(treeCacheListener, pool);
        treeCache.start();
        LOGGER.info("Register zookeeper path: [" + parentPath + "]'s TreeCache successfully!");

    }

    /**
     * 会有不一致的问题吗？
     * <p>
     * 原子更新。如果因为并发问题而更新失败，则会再次尝试更新，此时会读取修改后的内容，保证当前是最新的。
     * <p>
     * 如果总是更新失败，则意味着白名单操作频繁，最多尝试10次之后放弃。
     * <p>
     * 此时本地缓存的结果可能不是最新，但一定是尝试更新（多次并发修改）期间存在的有效值。
     * 
     * @param parentPath
     * @param instance
     * @throws Exception
     */
    private void handleAuth(final String parentPath, final CuratorClient instance) throws Exception {

        for (int i = 0; i < MAX_TRY; i++) {
            List<String> expect = AUTH_LIST.get();
            List<String> whiteList = instance.getChildren(parentPath);
            if (whiteList == null) {
                return;
            }
            if (AUTH_LIST.compareAndSet(expect, whiteList)) {
                return;
            }

        }
        LOGGER.warn("handleAuth fail [" + MAX_TRY + "]times, abort!");
    }

    private static final int MAX_TRY = 10;
    private static final AtomicReference<List<String>> AUTH_LIST = new AtomicReference<List<String>>(null);

    public static List<String> getAuthList() {

        return AUTH_LIST.get();
    }

////////////////////////////////--------------------NACOS-------------------///////////////////////////////

    private void handleAuth4Nacos() throws NacosException {
        for (int i = 0; i < MAX_TRY; i++) {
            List<String> expect = AUTH_LIST.get();
            List<Instance> instances = nacosClient.getAllInstances(OnlineTaskConstant.NACOS_ONLINE_AUTH, OnlineTaskConstant.NACOS_ONLINE_AUTH_GROUP, null);
            List<String> whiteList = new ArrayList<>();
            for (Instance instance : instances){
                whiteList.add(instance.getIp());
            }

            if (whiteList == null) {
                return;
            }
            if (AUTH_LIST.compareAndSet(expect, whiteList)) {
                return;
            }

        }
        LOGGER.warn("handleAuth fail [" + MAX_TRY + "]times, abort!");
    }

    private Map<String, String> buildTaskMetaData(OnlineTaskPojo instance){
        Map<String, String> metadata = new HashMap<>();
        metadata.put("groupName", instance.getGroupName());
        metadata.put("applicationName", instance.getApplicationName());
        metadata.put("httpPath", instance.getHttpPath());
        metadata.put("ipAndPort", instance.getIpAndPort());
        metadata.put("output", instance.getOutput());
        metadata.put("input", JSONHelper.toString(instance.getInput()));
        metadata.put("methodName", instance.getMethodName());
        metadata.put("className", instance.getClassName());
        metadata.put("beanName", instance.getBeanName());
        metadata.put("httpMethod", JSONHelper.toString(instance.getHttpMethod()));
        metadata.put("description", instance.getDescription());
        return metadata;
    }

    /**
     * 监听调度器HTTP调用授权路径（白名单）的变化
     * @throws Exception
     */
    private void monitorAuth4Nacos() throws NacosException {
        nacosClient.subscribe(OnlineTaskConstant.NACOS_ONLINE_AUTH, OnlineTaskConstant.NACOS_ONLINE_AUTH_GROUP, null, new EventListener() {
            @Override
            public void onEvent(Event event) {
                try {
                    handleAuth4Nacos();
                } catch (NacosException e) {
                    LOGGER.error(OnlineTaskConstant.LOGPREFIX + "handleAuth4Nacos: {}", e.getErrMsg());
                }
            }
        });
    }

    /**
     * OnlineTask upload for nacos
     * {@link } can be checked for the result.
     * @param
     * @return
     * @throws
     */
    private void onlineTaskUpload4Nacos() throws Exception {

        // 是否存在调度器HTTP调用授权路径
        if (!nacosClient.existNacosService(OnlineTaskConstant.NACOS_ONLINE_AUTH, OnlineTaskConstant.NACOS_ONLINE_AUTH_GROUP)) {
            nacosClient.createNacosService(OnlineTaskConstant.NACOS_ONLINE_AUTH, OnlineTaskConstant.NACOS_ONLINE_AUTH_GROUP);
        }
        // 初始化授权白名单
        try {
            handleAuth4Nacos();
        }
        catch (Exception e) {
            LOGGER.error(OnlineTaskConstant.LOGPREFIX, e);
        }
        // 监听授权白名单的变化
        monitorAuth4Nacos();

        // 是否存在Task路径
        if (!nacosClient.existNacosService(OnlineTaskConstant.NACOS_ONLINE_TASK, OnlineTaskConstant.NACOS_ONLINE_TASK_GROUP)){
            nacosClient.createNacosService(OnlineTaskConstant.NACOS_ONLINE_TASK, OnlineTaskConstant.NACOS_ONLINE_TASK_GROUP);
        }

        // ---------------预处理结束,task开始上传---------------------------

        final List<String> paths = new LinkedList<>();
        int count = 0;

        try{
            Map<String, OnlineTaskPojo> onlineTask = OnlineTaskCollector.getOnlineTask();
            final int size = onlineTask.size();
            for (Map.Entry<String, OnlineTaskPojo> onlineTaskEntry : onlineTask.entrySet()){
                String httpPath = onlineTaskEntry.getKey();
                OnlineTaskPojo instance = onlineTaskEntry.getValue();
                String taskKeyMetaData = buildMetaData(instance);

                // 装饰一下路径
                String taskKey = onlineTaskAppProperty.getApplicationName() + OnlineTaskConstant.NACOS_KEY_SPLIT
                        + httpPath.substring(1, httpPath.length());

                //删除已有相同实例，防止因为启动频繁导致的问题
                nacosClient.deleteNacosInstance(OnlineTaskConstant.NACOS_ONLINE_TASK, OnlineTaskConstant.NACOS_ONLINE_TASK_GROUP, taskKey, onlineTaskAppProperty.getIPAndPort(), true);
                nacosClient.createNacosInstance(OnlineTaskConstant.NACOS_ONLINE_TASK, OnlineTaskConstant.NACOS_ONLINE_TASK_GROUP, taskKey, onlineTaskAppProperty.getIPAndPort(), buildTaskMetaData(instance), true);

                count++;
                paths.add(httpPath);
            }
        }catch (Exception ex){
            LOGGER.error(OnlineTaskConstant.LOGPREFIX, ex);
        }

        LOGGER.info(OnlineTaskConstant.LOGPREFIX + "↓↓↓↓↓↓↓↓↓↓上传OnlineTask明细↓↓↓↓↓↓↓↓↓↓");

        count = 0;
        for (String path : paths) {
            count++;
            LOGGER.info(OnlineTaskConstant.LOGPREFIX + "序号:[" + count + "]，HTTP PATH:[" + path + "]");
        }

        LOGGER.info(OnlineTaskConstant.LOGPREFIX + "↑↑↑↑↑↑↑↑↑↑上传OnlineTask明细，共[" + count + "]个↑↑↑↑↑↑↑↑↑↑");
        LOGGER.info(OnlineTaskConstant.LOGPREFIX + "↓↓↓↓↓↓↓↓↓↓以下是未上传的OnlineTask明细，分为两部分↓↓↓↓↓↓↓↓↓↓");
        LOGGER.info(OnlineTaskConstant.LOGPREFIX + "↓↓↓↓↓↓↓↓↓↓第一部分：合规，但会访问异常(HTTP PATH 重复)OnlineTask信息明细↓↓↓↓↓↓↓↓↓↓");

        count = 0;
        Set<String> errorTaskPath = OnlineTaskCollector.getErrorTask().keySet();
        for (String path : errorTaskPath) {
            count++;
            LOGGER.info(OnlineTaskConstant.LOGPREFIX + "第一部分，序号:[" + count + "]，重复的HTTP PATH:[" + path + "]");
        }

        LOGGER.info(OnlineTaskConstant.LOGPREFIX + "↑↑↑↑↑↑↑↑↑↑第一部分：合规，但会访问异常(HTTP PATH 重复)OnlineTask信息明细，共[" + count
                + "]个↑↑↑↑↑↑↑↑↑↑");
        LOGGER.info(OnlineTaskConstant.LOGPREFIX + "↓↓↓↓↓↓↓↓↓↓第二部分：不合规的OnlineTask信息明细↓↓↓↓↓↓↓↓↓↓");

        count = 0;
        Map<String, String> errorMsg = OnlineTaskCollector.getErrorMessage();
        for (Entry<String, String> msg : errorMsg.entrySet()) {
            count++;
            LOGGER.info(OnlineTaskConstant.LOGPREFIX + "第二部分，序号:[" + count + "]，方法名:[" + msg.getKey() + "]，不合规的信息提示:["
                    + msg.getValue() + "]");
        }
        LOGGER.info(OnlineTaskConstant.LOGPREFIX + "↑↑↑↑↑↑↑↑↑↑第二部分：不合规的OnlineTask信息明细，，共[" + count + "]个↑↑↑↑↑↑↑↑↑↑");
        LOGGER.info(OnlineTaskConstant.LOGPREFIX + "↑↑↑↑↑↑↑↑↑↑以上是未上传的OnlineTask明细↑↑↑↑↑↑↑↑↑↑");
    }

}
