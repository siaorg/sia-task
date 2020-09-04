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

package com.sia.task.integration.curator;

import com.google.common.collect.Lists;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.sia.task.core.util.StringHelper;
import com.sia.task.integration.curator.hanler.NodeCacheHandler;
import com.sia.task.integration.curator.hanler.PathCacheHandler;
import com.sia.task.integration.curator.hanler.TreeCacheHandler;
import com.sia.task.integration.curator.properties.ZookeeperConstant;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.*;
import org.apache.curator.framework.state.ConnectionState;
import org.apache.curator.framework.state.ConnectionStateListener;
import org.apache.curator.retry.RetryNTimes;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.data.ACL;
import org.apache.zookeeper.data.Id;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.*;

/**
 * 封装的Curator客户端基本操作
 *
 * @author pengfeili23
 * @date 2018年6月27日 下午6:49:40
 * <p>
 * 1. 增加ZK管理端API操作
 * 2. 限制日志输出
 */
public class CuratorClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(CuratorClient.class);

    /**
     * CuratorFramework instance
     */
    private CuratorFramework client;

    /**
     * store PathChildrenCache, NodeCache and TreeCache
     */
    private final Map<String, PathChildrenCache> pathCacheMap = new ConcurrentHashMap<String, PathChildrenCache>();
    private final Map<String, NodeCache> nodeCacheMap = new ConcurrentHashMap<String, NodeCache>();
    private final Map<String, TreeCache> treeCacheMap = new ConcurrentHashMap<String, TreeCache>();

    /**
     * store ExecutorService
     */
    private final Map<String, ExecutorService> pathCacheExecutor = new ConcurrentHashMap<String, ExecutorService>();
    private final Map<String, ExecutorService> nodeCacheExecutor = new ConcurrentHashMap<String, ExecutorService>();
    private final Map<String, ExecutorService> treeCacheExecutor = new ConcurrentHashMap<String, ExecutorService>();

    /**
     * 初始化ZK连接
     *
     * @param zkAddress
     */
    public CuratorClient(String zkAddress, int retryTimes, int sleepMsBetweenRetries) {
        // 建立ZK连接
        client = CuratorFrameworkFactory.newClient(zkAddress, new RetryNTimes(retryTimes, sleepMsBetweenRetries));

        try {
            client.start();
            // 使用连接状态监听器（主要用来检测断线重连事件），因为 CuratorFramework 会自动断线重连，这里只记录事件
            ConnectionStateListener listener = new ConnectionStateListener() {
                @Override
                public void stateChanged(CuratorFramework client, ConnectionState newState) {
                    LOGGER.info("Zookeeper ConnectionState:" + newState.name());
                }
            };
            ThreadFactory namedThreadFactory = new ThreadFactoryBuilder().setNameFormat("Zookeeper-ConnectionState-%d").build();
            ExecutorService pool = new ThreadPoolExecutor(4, 4, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>(1024), namedThreadFactory, new ThreadPoolExecutor.AbortPolicy());
            client.getConnectionStateListenable().addListener(listener, pool);
            LOGGER.info("success connect to Zookeeper: " + zkAddress);
            // 应用关闭时，主动释放资源
            shutdownHook();
        } catch (Exception ex) {
            LOGGER.error("", ex);
            if (client != null) {
                closeCient();
            }
        }
    }

    /**
     * get CuratorFramework for some use (e.g. lock)
     *
     * @return
     */
    public CuratorFramework getCuratorFramework() {

        return client;
    }

    /**
     * add create authorization, can only create children in give path
     */
    public void addCreateAuth(String scheme, String auth) {

        try {
            client.getZookeeperClient().getZooKeeper().addAuthInfo(scheme, auth.getBytes());
            LOGGER.info("addCreateAuth success");
        } catch (Exception e) {
            LOGGER.info("addCreateAuth fail: ", e);
        }
    }

    /**
     * all permissions
     */
    public void addAllAuth(String scheme, String auth) {

        try {
            client.getZookeeperClient().getZooKeeper().addAuthInfo(scheme, auth.getBytes());
            LOGGER.info("addAllAuth success");
        } catch (Exception e) {
            LOGGER.info("addAllAuth fail: ", e);
        }
    }

    /**
     * createPersistentZKNode, creatingParentsIfNeeded for given path, CreateMode.PERSISTENT
     *
     * @param path
     * @param data
     * @return
     */
    public boolean createPersistentZKNode(String path, String data) {

        if (StringHelper.isEmpty(path) || isExists(path) || data == null) {
            return false;
        }
        try {

            String zkPath = client.create().creatingParentsIfNeeded().withMode(CreateMode.PERSISTENT).forPath(path,
                    data.getBytes());
            LOGGER.info("createPersistentZKNode，创建节点成功，节点地址:" + zkPath);
            return true;
        } catch (Exception e) {
            LOGGER.error("createPersistentZKNode，创建节点失败:" + e.getMessage() + "，path:" + path, e);
        }
        return false;
    }

    /**
     * createPersistentZKNode, set default value
     *
     * @param path
     * @return
     */
    public boolean createPersistentZKNode(String path) {

        return createPersistentZKNode(path, ZookeeperConstant.ZK_DEFAULT_VALUE);
    }

    /**
     * createEphemeralZKNode, creatingParentsIfNeeded for given path, leaf node is CreateMode.EPHEMERAL
     *
     * @param path
     * @param data
     * @return
     */
    public boolean createEphemeralZKNode(String path, String data) {

        if (StringHelper.isEmpty(path) || isExists(path) || data == null) {
            return false;
        }
        try {

            String zkPath = client.create().creatingParentsIfNeeded().withMode(CreateMode.EPHEMERAL).forPath(path,
                    data.getBytes());
            LOGGER.info("createEphemeralZKNode，创建节点成功，节点地址:" + zkPath);
            return true;
        } catch (Exception e) {
            LOGGER.error("createEphemeralZKNode，创建节点失败:" + e.getMessage() + "，path:" + path, e);
        }
        return false;
    }

    /**
     * createEphemeralZKNode, set default value
     *
     * @param path
     * @return
     */
    public boolean createEphemeralZKNode(String path) {

        return createEphemeralZKNode(path, ZookeeperConstant.ZK_DEFAULT_VALUE);
    }

    /**
     * createFixedPersistentZKNode, creating for given path( will not create parent path), CreateMode.PERSISTENT
     *
     * @param path
     * @param data
     * @return
     */
    public boolean createFixedPersistentZKNode(String path, String data) {

        if (StringHelper.isEmpty(path) || isExists(path) || data == null) {
            return false;
        }
        try {

            String zkPath = client.create().withMode(CreateMode.PERSISTENT).forPath(path, data.getBytes());
            LOGGER.info("createFixedPersistentZKNode，创建节点成功，节点地址:" + zkPath);
            return true;
        } catch (Exception e) {
            LOGGER.error("createFixedPersistentZKNode，创建节点失败:" + e.getMessage() + "，path:" + path, e);
        }
        return false;
    }

    /**
     * createFixedPersistentZKNode, set default value
     *
     * @param path
     * @return
     */
    public boolean createFixedPersistentZKNode(String path) {

        return createFixedPersistentZKNode(path, ZookeeperConstant.ZK_DEFAULT_VALUE);
    }

    /**
     * createFixedEphemeralZKNode, creating for given path( will not create parent path), leaf node is
     * CreateMode.EPHEMERAL
     *
     * @param path
     * @param data
     * @return
     */
    public boolean createFixedEphemeralZKNode(String path, String data) {

        if (StringHelper.isEmpty(path) || isExists(path) || data == null) {
            return false;
        }
        try {
            String zkPath = client.create().withMode(CreateMode.EPHEMERAL).forPath(path, data.getBytes());
            LOGGER.info("createFixedEphemeralZKNode，创建节点成功，节点地址:" + zkPath);
            return true;
        } catch (Exception e) {
            LOGGER.error("createFixedEphemeralZKNode，创建节点失败:" + e.getMessage() + "，path:" + path, e);
        }
        return false;
    }

    /**
     * createFixedEphemeralZKNode, set default value
     *
     * @param path
     * @return
     */
    public boolean createFixedEphemeralZKNode(String path) {

        return createFixedEphemeralZKNode(path, ZookeeperConstant.ZK_DEFAULT_VALUE);
    }

    /**
     * setData
     *
     * @param path
     * @param data
     * @return
     */
    public boolean setData(String path, String data) {

        if (!isExists(path) || data == null) {
            return false;
        }
        try {
            Stat stat = client.setData().forPath(path, data.getBytes());
            LOGGER.info("setData，更新数据成功, path:" + path + ", stat: " + stat);
            return true;
        } catch (Exception e) {
            LOGGER.error("setData，更新节点数据失败:" + e.getMessage() + "，path:" + path, e);
        }
        return false;
    }

    /**
     * may return null if path not exists
     *
     * @param path
     * @return
     */
    public String getData(String path) {

        String response = null;
        if (!isExists(path)) {
            return response;
        }
        try {
            byte[] datas = client.getData().forPath(path);
            response = datas == null ? "" : new String(datas, "utf-8");
            if (LOGGER.isDebugEnabled()) {
                LOGGER.info("读取数据成功, path:" + path + ", content:" + response);
            }
        } catch (Exception e) {
            LOGGER.error("getData，读取数据失败! path: " + path + ", errMsg:" + e.getMessage(), e);
        }
        return response;
    }

    /**
     * may return null if path not exists
     *
     * @param path
     * @return
     */
    public List<String> getChildren(String path) {

        List<String> list = null;
        if (!isExists(path)) {
            return list;
        }
        try {
            list = client.getChildren().forPath(path);
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("getChildren，读取数据成功, path:" + path);
            }
        } catch (Exception e) {
            LOGGER.error("getChildren，读取数据失败! path: " + path + ", errMsg:" + e.getMessage(), e);
        }
        return list;
    }

    /**
     * for given path
     *
     * @param path
     * @return
     */
    public boolean isExists(String path) {

        if (StringHelper.isEmpty(path)) {
            return false;
        }
        try {
            Stat stat = client.checkExists().forPath(path);
            return null != stat;
        } catch (Exception e) {
            LOGGER.error("isExists 读取数据失败! path: " + path + ", errMsg:" + e.getMessage(), e);
        }
        return false;
    }

    /**
     * for given path (node) isPersistent or (EPHEMERAL)
     *
     * @param path
     * @return
     */
    public boolean isPersistent(String path) {

        if (StringHelper.isEmpty(path)) {
            return false;
        }
        try {
            Stat stat = client.checkExists().forPath(path);
            if (stat == null) {
                return false;
            }
            // If it is not an ephemeral node, it will be zero.
            return stat.getEphemeralOwner() == 0L;
        } catch (Exception e) {
            LOGGER.error("isPersistent 读取数据失败! path: " + path + ", errMsg:" + e.getMessage(), e);
        }
        return false;
    }

    /**
     * only delete leaf node for given path
     *
     * @param path
     * @return
     */
    public boolean deleteLeafZKNode(String path) {

        if (!isExists(path)) {
            return false;
        }
        try {
            client.delete().forPath(path);
            LOGGER.info("deleteLeafZKNode，删除节点成功，节点地址:" + path);
            return true;
        } catch (Exception e) {
            LOGGER.error("deleteLeafZKNode，删除节点失败:" + e.getMessage() + "，path:" + path, e);
        }
        return false;
    }

    /**
     * deletingChildrenIfNeeded for given path
     *
     * @param path
     * @return
     */
    public boolean deletePathZKNode(String path) {

        if (!isExists(path)) {
            return false;
        }
        try {
            client.delete().deletingChildrenIfNeeded().forPath(path);
            LOGGER.info("deletePathZKNode，删除节点成功，节点地址:" + path);
            return true;
        } catch (Exception e) {
            LOGGER.error("deletePathZKNode，删除节点失败:" + e.getMessage() + "，path:" + path, e);
        }
        return false;
    }

    /**
     * 创建 PathChildrenCache，对指定路径节点的一级子目录监听，不对该节点的操作监听，对其子目录的增删改操作监听。如果指定路径删除后又创建，Watcher失效。
     *
     * @param path
     * @param handler
     * @return
     * @throws Exception
     */
    public PathChildrenCache createPathCache(String path, PathCacheHandler handler) throws Exception {

        // 创建PathChildrenCache监听器
        PathChildrenCache childrenCache = new PathChildrenCache(this.client, path, true);
        PathChildrenCacheListener childrenCacheListener = new PathChildrenCacheListener() {

            @Override
            public void childEvent(CuratorFramework client, PathChildrenCacheEvent event) throws Exception {

                handler.process(event);

            }
        };
        ThreadFactory namedThreadFactory = new ThreadFactoryBuilder().setNameFormat("Zookeeper-PathChildrenCacheListener-%d").build();
        ExecutorService pool = new ThreadPoolExecutor(4, 4, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>(1024), namedThreadFactory, new ThreadPoolExecutor.AbortPolicy());
        childrenCache.getListenable().addListener(childrenCacheListener, pool);
        // 对PathChildrenCache做统一管理，同一路径只能创建同一类型的监听（创建多个也只有一个生效）
        PathChildrenCache current = pathCacheMap.putIfAbsent(path, childrenCache);
        // 加入资源MAP
        if (current == null) {
            pathCacheExecutor.putIfAbsent(path, pool);
            childrenCache.start();
            LOGGER.info("Register zookeeper path: [" + path + "]'s PathChildrenCache successfully!");
            return childrenCache;
        }
        // 资源早已存在，关闭无效的资源
        LOGGER.info("zookeeper path: [" + path + "]'s PathChildrenCache already exists!");
        childrenCache.close();
        pool.shutdown();
        return current;
    }

    /**
     * 创建 NodeCache，对一个节点进行监听，监听事件包括指定路径的增删改操作。如果指定路径删除后又创建，Watcher继续生效。
     *
     * @param path
     * @param handler
     * @return
     * @throws Exception
     */
    public NodeCache createNodeCache(String path, NodeCacheHandler handler) throws Exception {

        // 创建nodeCache监听器
        NodeCache nodeCache = new NodeCache(this.client, path, false);
        NodeCacheListener nodeListener = new NodeCacheListener() {

            @Override
            public void nodeChanged() throws Exception {

                handler.process(nodeCache);

            }
        };

        ThreadFactory namedThreadFactory = new ThreadFactoryBuilder().setNameFormat("Zookeeper-NodeCacheListener-%d").build();
        ExecutorService pool = new ThreadPoolExecutor(4, 4, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>(1024), namedThreadFactory, new ThreadPoolExecutor.AbortPolicy());
        nodeCache.getListenable().addListener(nodeListener, pool);
        // 对NodeCache做统一管理，同一路径只能创建同一类型的监听（创建多个也只有一个生效）
        NodeCache current = nodeCacheMap.putIfAbsent(path, nodeCache);
        // 加入资源MAP
        if (current == null) {
            nodeCacheExecutor.putIfAbsent(path, pool);
            nodeCache.start();
            LOGGER.info("Register zookeeper path: [" + path + "]'s NodeCache successfully!");
            return nodeCache;
        }
        // 资源早已存在，关闭无效的资源
        LOGGER.info("zookeeper path: [" + path + "]'s NodeCache already exists!");
        nodeCache.close();
        pool.shutdown();
        return current;
    }

    /**
     * 综合NodeCache和PathChildrenCahce的特性，是对整个目录进行监听，可以设置监听深度。如果指定路径删除后又创建，Watcher继续生效。
     *
     * @param path
     * @param handler
     * @return
     * @throws Exception
     */
    public TreeCache createTreeCache(String path, TreeCacheHandler handler) throws Exception {

        // 创建treeCache监听器
        TreeCache treeCache = new TreeCache(this.client, path);
        TreeCacheListener treeCacheListener = new TreeCacheListener() {

            @Override
            public void childEvent(CuratorFramework client, TreeCacheEvent event) throws Exception {

                handler.process(event);
            }
        };
        ThreadFactory namedThreadFactory = new ThreadFactoryBuilder().setNameFormat("Zookeeper-TreeCacheListener-%d").build();
        ExecutorService pool = new ThreadPoolExecutor(4, 4, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>(1024), namedThreadFactory, new ThreadPoolExecutor.AbortPolicy());
        treeCache.getListenable().addListener(treeCacheListener, pool);
        // 对TreeCache做统一管理，同一路径只能创建同一类型的监听（创建多个也只有一个生效）
        TreeCache current = treeCacheMap.putIfAbsent(path, treeCache);
        // 加入资源MAP
        if (current == null) {
            treeCacheExecutor.putIfAbsent(path, pool);
            treeCache.start();
            LOGGER.info("Register zookeeper path: [" + path + "]'s TreeCache successfully!");
            return treeCache;
        }
        // 资源早已存在，关闭无效的资源
        LOGGER.info("zookeeper path: [" + path + "]'s TreeCache already exists!");
        treeCache.close();
        pool.shutdown();
        return current;
    }

    /**
     * 移除指定路径的PathChildrenCache（如果有）
     *
     * @param path
     * @throws Exception
     */
    public void closePathCache(String path) throws Exception {

        if (StringHelper.isEmpty(path)) {
            return;
        }
        PathChildrenCache pathCache = pathCacheMap.get(path);
        if (pathCache != null) {
            pathCacheMap.remove(path);
            pathCache.close();
            LOGGER.info("close PathChildrenCache:" + path);
        }
        ExecutorService executor = pathCacheExecutor.get(path);
        if (executor != null) {
            pathCacheExecutor.remove(path);
            executor.shutdown();
            LOGGER.info("close ExecutorService for PathChildrenCache:" + path);
        }
    }

    /**
     * 移除指定路径的NodeCache（如果有）
     *
     * @param path
     * @throws Exception
     */
    public void closeNodeCache(String path) throws Exception {

        if (StringHelper.isEmpty(path)) {
            return;
        }

        NodeCache nodeCache = nodeCacheMap.get(path);
        if (nodeCache != null) {
            nodeCacheMap.remove(path);
            nodeCache.close();
            LOGGER.info("close NodeCache:" + path);
        }
        ExecutorService executor = nodeCacheExecutor.get(path);
        if (executor != null) {
            nodeCacheExecutor.remove(path);
            executor.shutdown();
            LOGGER.info("close ExecutorService for NodeCache:" + path);
        }
    }

    /**
     * 移除指定路径的TreeCache（如果有）
     *
     * @param path
     * @throws Exception
     */
    public void closeTreeCache(String path) throws Exception {

        if (StringHelper.isEmpty(path)) {
            return;
        }
        TreeCache treeCache = treeCacheMap.get(path);
        if (treeCache != null) {
            treeCacheMap.remove(path);
            treeCache.close();
            LOGGER.info("close TreeCache:" + path);
        }
        ExecutorService executor = treeCacheExecutor.get(path);
        if (executor != null) {
            treeCacheExecutor.remove(path);
            executor.shutdown();
            LOGGER.info("close ExecutorService for TreeCache:" + path);
        }
    }

    /**
     * 移除所有路径的PathChildrenCache
     *
     * @throws Exception
     */
    public void closeAllPathCache() throws Exception {

        Set<String> paths = pathCacheMap.keySet();
        for (String path : paths) {
            closePathCache(path);
        }
    }

    /**
     * 移除所有路径的NodeCache
     *
     * @throws Exception
     */
    public void closeAllNodeCache() throws Exception {

        Set<String> paths = nodeCacheMap.keySet();
        for (String path : paths) {
            closeNodeCache(path);
        }
    }

    /**
     * 移除所有路径的TreeCache
     *
     * @throws Exception
     */
    public void closeAllTreeCache() throws Exception {

        Set<String> paths = treeCacheMap.keySet();
        for (String path : paths) {
            closeTreeCache(path);
        }
    }

    /**
     * 主动关闭ZK连接，释放资源
     *
     * @throws Exception
     */
    public void close() throws Exception {

        closeAllPathCache();
        closeAllNodeCache();
        closeAllTreeCache();
        closeCient();

    }

    /**
     * closeCient
     */
    public void closeCient() {
        try {
            client.close();
        } catch (Exception ex) {
            LOGGER.error("", ex);
        }
    }

    /**
     * 用于应用正常关闭时，主动断开与ZK的连接，保证临时节点快速失效！
     */
    private void shutdownHook() {

        LOGGER.info("addShutdownHook for CuratorClient");
        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {

            @Override
            public void run() {

                try {
                    LOGGER.info("shutdownHook begin");
                    close();
                    LOGGER.info("shutdownHook end");
                } catch (Exception e) {
                    LOGGER.error("", e);
                }
            }
        }));
    }

    //新增ZkAPI操作


    /**
     * 获得节点状态值
     *
     * @param path
     * @return
     * @throws Exception
     */
    public Stat getStat(String path) throws Exception {
        Stat stat = client.checkExists().forPath(path);
        return stat;
    }

    /**
     * 获得节点ACL信息
     *
     * @param path
     * @return
     * @throws Exception
     */
    public Map<String, Object> getACL(String path) throws Exception {
        ACL acl = client.getACL().forPath(path).get(0);
        Id id = acl.getId();
        HashMap<String, Object> map = new HashMap<>();
        map.put("perms", acl.getPerms());
        map.put("id", id.getId());
        map.put("scheme", id.getScheme());
        return map;
    }

    /**
     * 获得节点的version号，如果节点不存在，返回 -1
     *
     * @param path
     * @return
     * @throws Exception
     */
    public int getVersion(String path) throws Exception {
        Stat stat = this.getStat(path);
        if (stat != null) {
            return stat.getVersion();
        } else {
            return -1;
        }
    }

    /**
     * 创建节点
     *
     * @param path    节点path
     * @param payload 初始数据内容
     * @return
     */
    public void createNode(String path, byte[] payload) throws Exception {
        client.create().creatingParentsIfNeeded().forPath(path, payload);
        LOGGER.info("节点创建成功, Path: " + path);
    }

    /**
     * createNodeWithACL
     * Create a node under ACL mode
     *
     * @param path
     * @param payload
     * @throws Exception
     */
    public void createNodeWithACL(String path, byte[] payload) throws Exception {
        ACL acl = new ACL(ZooDefs.Perms.ALL, ZooDefs.Ids.AUTH_IDS);
        List<ACL> aclList = Lists.newArrayList(acl);
        try {
            client.create().withACL(aclList).forPath(path, payload);
        } catch (Exception e) {
            LOGGER.error("Create security file failed.");
            e.printStackTrace();
        }
    }

    /**
     * 删除指定节点
     *
     * @param path 节点path
     */
    public void deleteNode(String path) throws Exception {
        client.delete().forPath(path);
        LOGGER.info("节点删除成功, Path: " + path);
    }

    /**
     * 更新指定节点数据内容
     *
     * @param path    节点path
     * @param payload 数据内容
     * @return
     */
    public boolean setData(String path, byte[] payload) throws Exception {
        Stat stat = client.setData().forPath(path, payload);
        if (stat != null) {
            //logger.info("设置数据成功，path：" + path );
            return true;
        } else {
            LOGGER.error("设置数据失败，path：" + path);
            return false;
        }
    }

    /**
     * CAS更新指定节点数据内容
     *
     * @param path    节点path
     * @param payload 数据内容
     * @param version 版本号
     * @return
     * @throws Exception
     */
    public int setDataWithVersion(String path, byte[] payload, int version) throws Exception {
        try {
            Stat stat = null;
            if (version != -1) {
                stat = client.setData().withVersion(version).forPath(path, payload);
            } else {
                stat = client.setData().forPath(path, payload);
            }
            if (stat != null) {
                //logger.info("CAS设置数据成功，path：" + path );
                return stat.getVersion();
            } else {
                LOGGER.error("CAS设置数据失败，path : {}", path);
                return -1;
            }
        } catch (KeeperException.BadVersionException ex) {
            LOGGER.error("CAS设置数据失败，path : {},error msg : {}", path, ex.getMessage());
            return -1;
        }

    }

}
