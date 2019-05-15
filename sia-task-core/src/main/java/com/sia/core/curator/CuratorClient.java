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

package com.sia.core.curator;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.sia.core.constant.Constant;
import com.sia.core.curator.handler.NodeCacheHandler;
import com.sia.core.curator.handler.PathCacheHandler;
import com.sia.core.curator.handler.TreeCacheHandler;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.NodeCache;
import org.apache.curator.framework.recipes.cache.NodeCacheListener;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;
import org.apache.curator.framework.recipes.cache.TreeCache;
import org.apache.curator.framework.recipes.cache.TreeCacheEvent;
import org.apache.curator.framework.recipes.cache.TreeCacheListener;
import org.apache.curator.framework.state.ConnectionState;
import org.apache.curator.framework.state.ConnectionStateListener;
import org.apache.curator.retry.RetryNTimes;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sia.core.helper.StringHelper;
import com.google.common.util.concurrent.ThreadFactoryBuilder;

/**
 *
 *
 * @description encapsuled Curator client API
 * @see
 * @author pengfeili23
 * @date 2018-06-27 18:49:40
 * @version V1.0.0
 **/
public class CuratorClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(CuratorClient.class);

    /**
     * CuratorFramework instance
     */
    private CuratorFramework client;

    /**
     * store PathChildrenCache, NodeCache and TreeCache
     */
    private Map<String, PathChildrenCache> pathCacheMap = new ConcurrentHashMap<String, PathChildrenCache>();
    private Map<String, NodeCache> nodeCacheMap = new ConcurrentHashMap<String, NodeCache>();
    private Map<String, TreeCache> treeCacheMap = new ConcurrentHashMap<String, TreeCache>();

    /**
     * store ExecutorService
     */
    private Map<String, ExecutorService> pathCacheExecutor = new ConcurrentHashMap<String, ExecutorService>();
    private Map<String, ExecutorService> nodeCacheExecutor = new ConcurrentHashMap<String, ExecutorService>();
    private Map<String, ExecutorService> treeCacheExecutor = new ConcurrentHashMap<String, ExecutorService>();

    /**
     *
     * init zk connection
     * {@link } can be checked for the result.
     * @param
     * @return
     * @throws
     */
    public CuratorClient(String zkAddress) {

        // 建立ZK连接
        client = CuratorFrameworkFactory.newClient(zkAddress,
                new RetryNTimes(Constant.RETRY_TIMES, Constant.SLEEP_MS_BETWEEN_RETRIES));

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
            ExecutorService pool =new ThreadPoolExecutor(4, 4, 0L, TimeUnit.MILLISECONDS,new LinkedBlockingQueue<Runnable>(1024), namedThreadFactory, new ThreadPoolExecutor.AbortPolicy());
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
     *
     * get CuratorFramework for some use (e.g. lock)
     * {@link } can be checked for the result.
     * @param
     * @return
     * @throws
     */
    public CuratorFramework getCuratorFramework() {

        return client;
    }

    /**
     *
     * add create authorization, can only create children in give path
     * {@link } can be checked for the result.
     * @param
     * @return
     * @throws
     */
    public void addCreateAuth(String digest,String createAuth) {

        try {
            client.getZookeeperClient().getZooKeeper().addAuthInfo(digest, createAuth.getBytes());
//            client.getZookeeperClient().getZooKeeper().addAuthInfo(Constant.DIGEST, Constant.CREATEAUTH.getBytes());
            LOGGER.info("addCreateAuth success");
        } catch (Exception e) {
            LOGGER.info("addCreateAuth fail: ", e);
        }
    }

    /**
     * all permissions
     */
    public void addAllAuth(String digest,String allAuth) {

        try {
            client.getZookeeperClient().getZooKeeper().addAuthInfo(digest, allAuth.getBytes());
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

        return createPersistentZKNode(path, Constant.ZK_DEFAULT_VALUE);
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

        return createEphemeralZKNode(path, Constant.ZK_DEFAULT_VALUE);
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

        return createFixedPersistentZKNode(path, Constant.ZK_DEFAULT_VALUE);
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

        return createFixedEphemeralZKNode(path, Constant.ZK_DEFAULT_VALUE);
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
            response = new String(datas, "utf-8");
            LOGGER.info("读取数据成功, path:" + path + ", content:" + response);
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
            LOGGER.info("getChildren，读取数据成功, path:" + path);
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
        ExecutorService pool =new ThreadPoolExecutor(4, 4, 0L, TimeUnit.MILLISECONDS,new LinkedBlockingQueue<Runnable>(1024), namedThreadFactory, new ThreadPoolExecutor.AbortPolicy());
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
        ExecutorService pool =new ThreadPoolExecutor(4, 4, 0L, TimeUnit.MILLISECONDS,new LinkedBlockingQueue<Runnable>(1024), namedThreadFactory, new ThreadPoolExecutor.AbortPolicy());
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
        ExecutorService pool =new ThreadPoolExecutor(4, 4, 0L, TimeUnit.MILLISECONDS,new LinkedBlockingQueue<Runnable>(1024), namedThreadFactory, new ThreadPoolExecutor.AbortPolicy());
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
     *
     * remove the given PathChildrenCache
     * {@link } can be checked for the result.
     * @param
     * @return
     * @throws
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
     *
     * remove the given NodeCache
     * {@link } can be checked for the result.
     * @param
     * @return
     * @throws
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
     *
     * remove the given TreeCache
     * {@link } can be checked for the result.
     * @param
     * @return
     * @throws
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
     *
     * remove the given PathChildrenCache
     * {@link } can be checked for the result.
     * @param
     * @return
     * @throws
     */
    public void closeAllPathCache() throws Exception {

        Set<String> paths = pathCacheMap.keySet();
        for (String path : paths) {
            closePathCache(path);
        }
    }

    /**
     *
     * remove all the NodeCache
     * {@link } can be checked for the result.
     * @param
     * @return
     * @throws
     */
    public void closeAllNodeCache() throws Exception {

        Set<String> paths = nodeCacheMap.keySet();
        for (String path : paths) {
            closeNodeCache(path);
        }
    }

    /**
     *
     * remove all the TreeCache
     * {@link } can be checked for the result.
     * @param
     * @return
     * @throws
     */
    public void closeAllTreeCache() throws Exception {

        Set<String> paths = treeCacheMap.keySet();
        for (String path : paths) {
            closeTreeCache(path);
        }
    }

    /**
     *
     * close zk connection and free resources
     * {@link } can be checked for the result.
     * @param
     * @return
     * @throws
     */
    public void close() throws Exception {

        closeAllPathCache();
        closeAllNodeCache();
        closeAllTreeCache();
        closeCient();

    }

    /**
     *
     * closeCient
     * {@link } can be checked for the result.
     * @param
     * @return
     * @throws
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
}
