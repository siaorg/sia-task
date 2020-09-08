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

package com.sia.task.register.zookeeper.impl;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.sia.task.core.IMetadataUpload;
import com.sia.task.core.task.SiaTaskMeta;
import com.sia.task.core.util.Constant;
import com.sia.task.core.util.JsonHelper;
import com.sia.task.core.util.StringHelper;
import com.sia.task.integration.curator.CuratorClient;
import com.sia.task.integration.curator.properties.ZookeeperConfiguration;
import com.sia.task.integration.curator.properties.ZookeeperConstant;
import com.sia.task.register.zookeeper.collector.SiaTaskCollector;
import com.sia.task.register.zookeeper.core.ConnectionStateListener4Client;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.TreeCache;
import org.apache.curator.framework.recipes.cache.TreeCacheEvent;
import org.apache.curator.framework.recipes.cache.TreeCacheListener;
import org.apache.curator.framework.state.ConnectionStateListener;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author maozhengwei
 * @version V1.0.0
 * @data 2020/4/29 12:46 下午
 **/
@Slf4j
public class MetadataUploadService implements IMetadataUpload {

    @Resource
    private CuratorClient client;

    @Resource
    protected ZookeeperConfiguration configuration;


    @Override
    public void uploadTaskMetaData() {
        if (!checkRegister()) {
            log.error(Constant.LOG_PREFIX + "使用OnlineTask需要配置统一zookeeper地址");
            return;
        }
        //使用只写权限
        client.addCreateAuth(configuration.getDIGEST(), configuration.getCreateAuth());

        // ---------------预处理，开始---------------------------
        // 是否存在根路径
        if (!client.isExists(getZkOnlineRootPath())) {
            client.createPersistentZKNode(getZkOnlineRootPath());
        }
        // 是否存在Task路径
        if (!client.isExists(getZkOnlineTaskPath())) {
            client.createPersistentZKNode(getZkOnlineTaskPath());
        }
        // 是否存在调度器HTTP调用授权路径
        if (!client.isExists(getZkOnlineAuthPath())) {
            client.createPersistentZKNode(getZkOnlineAuthPath());
        }
        // 初始化授权白名单
        handleAuth(getZkOnlineAuthPath(), client);
        // 监听授权白名单的变化
        monitorAuth(getZkOnlineAuthPath(), client);
        // 是否存在group路径
        String groupPath = getZkOnlineTaskPath() + ZookeeperConstant.ZK_SEPARATOR + configuration.getGroupName();
        if (!client.isExists(groupPath)) {
            client.createPersistentZKNode(ZookeeperConstant.ZK_DEFAULT_VALUE);
        }
        // 是否存在application路径
        String applicationPath = groupPath + ZookeeperConstant.ZK_SEPARATOR + configuration.getApplicationName();
        if (!client.isExists(applicationPath)) {
            client.createPersistentZKNode(applicationPath);
        }
        // ---------------预处理，结束---------------------------

        final List<String> paths = new LinkedList<String>();
        int count = 0;

        try {
            Map<String, SiaTaskMeta> onlineTask = SiaTaskCollector.getOnlineTask();
            final int size = onlineTask.size();
            for (Map.Entry<String, SiaTaskMeta> onlineTaskEntry : onlineTask.entrySet()) {
                String httpPath = onlineTaskEntry.getKey();
                SiaTaskMeta instance = onlineTaskEntry.getValue();
                String metaData = buildMetaData(instance);
                // 装饰一下路径
                String encodedPath = null;
                if (!StringHelper.isEmpty(httpPath)) {
                    encodedPath = httpPath.replace(ZookeeperConstant.HTTP_SEPARATOR, ZookeeperConstant.HTTP_MASK);
                }
                String taskKey = configuration.getApplicationName() + ZookeeperConstant.ZK_KEY_SPLIT + encodedPath;
                String onlineTaskPath = applicationPath + ZookeeperConstant.ZK_SEPARATOR + taskKey;
                // 不存在则新建，taskKey 路径
                if (!client.isExists(onlineTaskPath)) {
                    client.createPersistentZKNode(onlineTaskPath, metaData);
                }
                // 设置<描述信息+输入参数个数>
                client.setData(onlineTaskPath, metaData);
                // 具体实例的路径
                String instancePath = onlineTaskPath + ZookeeperConstant.ZK_SEPARATOR
                        + configuration.getIPAndPort();
                if (!client.isExists(instancePath)) {
                    client.createEphemeralZKNode(instancePath, JsonHelper.toString(instance));
                } else {
                    log.info(Constant.LOG_PREFIX + instancePath + " already exists! We watchNodeDeleted");
                    watchNodeDeleted(instancePath, JsonHelper.toString(instance));
                }
                count++;
                paths.add(httpPath);
            }
            log.info(Constant.LOG_PREFIX + "#########上传OnlineTask结束，共成功上传[" + count + "/" + size + "]个OnlineTask#########");

        } catch (Exception ex) {
            log.error(Constant.LOG_EX_PREFIX, ex);
        }

        log.info(Constant.LOG_PREFIX + "↓↓↓↓↓↓↓↓↓↓上传OnlineTask明细↓↓↓↓↓↓↓↓↓↓");

        count = 0;
        for (String path : paths) {
            count++;
            log.info(Constant.LOG_PREFIX + "序号:[" + count + "]，HTTP PATH:[" + path + "]");
        }

        log.info(Constant.LOG_PREFIX + "↑↑↑↑↑↑↑↑↑↑上传OnlineTask明细，共[" + count + "]个↑↑↑↑↑↑↑↑↑↑");
        log.info(Constant.LOG_PREFIX + "↓↓↓↓↓↓↓↓↓↓以下是未上传的OnlineTask明细，分为两部分↓↓↓↓↓↓↓↓↓↓");
        log.info(Constant.LOG_PREFIX + "↓↓↓↓↓↓↓↓↓↓第一部分：合规，但会访问异常(HTTP PATH 重复)OnlineTask信息明细↓↓↓↓↓↓↓↓↓↓");

        count = 0;
        Set<String> errorTaskPath = SiaTaskCollector.getErrorTask().keySet();
        for (String path : errorTaskPath) {
            count++;
            log.info(Constant.LOG_PREFIX + "第一部分，序号:[" + count + "]，重复的HTTP PATH:[" + path + "]");
        }

        log.info(Constant.LOG_PREFIX + "↑↑↑↑↑↑↑↑↑↑第一部分：合规，但会访问异常(HTTP PATH 重复)OnlineTask信息明细，共[" + count
                + "]个↑↑↑↑↑↑↑↑↑↑");
        log.info(Constant.LOG_PREFIX + "↓↓↓↓↓↓↓↓↓↓第二部分：不合规的OnlineTask信息明细↓↓↓↓↓↓↓↓↓↓");

        count = 0;
        Map<String, String> errorMsg = SiaTaskCollector.getErrorMessage();
        for (Map.Entry<String, String> msg : errorMsg.entrySet()) {
            count++;
            log.info(Constant.LOG_PREFIX + "第二部分，序号:[" + count + "]，方法名:[" + msg.getKey() + "]，不合规的信息提示:["
                    + msg.getValue() + "]");
        }
        log.info(Constant.LOG_PREFIX + "↑↑↑↑↑↑↑↑↑↑第二部分：不合规的OnlineTask信息明细，，共[" + count + "]个↑↑↑↑↑↑↑↑↑↑");
        log.info(Constant.LOG_PREFIX + "↑↑↑↑↑↑↑↑↑↑以上是未上传的OnlineTask明细↑↑↑↑↑↑↑↑↑↑");

        stateChangeMonitor();
    }

    @Override
    public List<String> loadAuthIpList() {
        return AUTH_LIST.get();
    }

    private boolean checkRegister() {

        return client != null && client.getCuratorFramework() != null;
    }

    private String getZkOnlineRootPath() {
        StringBuilder zkOnlineRootPath = new StringBuilder().append(ZookeeperConstant.ZK_SEPARATOR).append(configuration.getTaskRoot());
        return zkOnlineRootPath.toString();
    }

    private String getZkOnlineTaskPath() {
        StringBuilder zkOnlineTaskPath = new StringBuilder().append(ZookeeperConstant.ZK_SEPARATOR).append(configuration.getTaskRoot()).append(ZookeeperConstant.ZK_SEPARATOR).append(ZookeeperConstant.ZK_ONLINE_TASK);
        return zkOnlineTaskPath.toString();
    }

    private String getZkOnlineAuthPath() {
        StringBuilder zkOnlineAuthPath = new StringBuilder().append(ZookeeperConstant.ZK_SEPARATOR).append(configuration.getTaskRoot()).append(ZookeeperConstant.ZK_SEPARATOR).append(ZookeeperConstant.ZK_ONLINE_AUTH);
        return zkOnlineAuthPath.toString();
    }

    private String buildMetaData(SiaTaskMeta instance) {

        Map<String, Object> meta = new HashMap<String, Object>(4);

        if (instance != null) {
            String description = instance.getDescription();
            Integer count = instance.getInput().size();

            meta.put("INFO", description);
            meta.put("COUNT", count);
        } else {
            meta.put("INFO", "NEVER HAPPEN");
            meta.put("COUNT", 1);
        }
        return JsonHelper.toString(meta);

    }

    private static final int MAX_TRY = 10;
    private final AtomicReference<List<String>> AUTH_LIST = new AtomicReference<List<String>>(null);

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
    private void handleAuth(final String parentPath, final CuratorClient instance) {
        try {
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
        } catch (Exception e) {
            log.error(Constant.LOG_EX_PREFIX + "handleAuth fail [" + MAX_TRY + "]times, abort!", e);
        }
        log.warn(Constant.LOG_PREFIX + "handleAuth fail [" + MAX_TRY + "]times, abort!");
    }

    /**
     * 监听调度器HTTP调用授权路径（白名单）的变化
     *
     * @param parentPath
     * @param instance
     * @throws Exception
     */
    private void monitorAuth(final String parentPath, final CuratorClient instance) {
        // 创建treeCache监听器
        try {
            TreeCache treeCache = new TreeCache(instance.getCuratorFramework(), parentPath);
            TreeCacheListener treeCacheListener = new TreeCacheListener() {
                @Override
                public void childEvent(CuratorFramework client, TreeCacheEvent event) {
                    TreeCacheEvent.Type type = event.getType();
                    // 白名单的变化（+1或-1）
                    if (type == TreeCacheEvent.Type.NODE_ADDED || type == TreeCacheEvent.Type.NODE_REMOVED) {
                        handleAuth(parentPath, instance);
                    }
                }
            };
            ThreadFactory namedThreadFactory = new ThreadFactoryBuilder().setNameFormat("Zookeeper-treeCacheListener-%d").build();
            ExecutorService pool = new ThreadPoolExecutor(4, 4, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>(1024), namedThreadFactory, new ThreadPoolExecutor.AbortPolicy());
            treeCache.getListenable().addListener(treeCacheListener, pool);
            treeCache.start();
            log.info("Register zookeeper path: [" + parentPath + "]'s TreeCache successfully!");
        } catch (Exception e) {
            log.error(Constant.LOG_EX_PREFIX + " monitorAuth fail! ", e);
        }
    }

    /**
     * 当应用快速重启（重启时间间隔<临时节点的过期时间），不会重新创建临时节点（节点已存在），等临时节点过期后，预期应该存在的节点消失啦。这里通过添加监听器，在上次临时节点过期后，再新建本次临时节点，保证临时节点的存在符合预期。
     *
     * @param path
     * @param data
     */
    private void watchNodeDeleted(final String path, final String data) {

        try {
            ZooKeeper zk = client.getCuratorFramework().getZookeeperClient().getZooKeeper();
            zk.exists(path, new Watcher() {

                @Override
                public void process(WatchedEvent event) {

                    if (event.getType().equals(Event.EventType.NodeDeleted)) {
                        client.createEphemeralZKNode(path, data);
                    } else {
                        watchNodeDeleted(path, data);
                    }
                }
            });
        } catch (Exception e) {
            log.error(Constant.LOG_EX_PREFIX + " watchNodeDeleted fail!", e);
        }
    }

    void stateChangeMonitor() {
        ConnectionStateListener stateListener = new ConnectionStateListener4Client(this);
        ThreadFactory namedThreadFactory = new ThreadFactoryBuilder().setNameFormat("Zookeeper-Reconnected-%d").build();
        ExecutorService pool = new ThreadPoolExecutor(4, 4, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>(1024), namedThreadFactory, new ThreadPoolExecutor.AbortPolicy());
        client.getCuratorFramework().getConnectionStateListenable().addListener(stateListener, pool);
    }
}
