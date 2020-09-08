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

package com.sia.task.register.zookeeper.core;

import com.sia.task.core.entity.BasicTask;
import com.sia.task.core.util.Constant;
import com.sia.task.core.util.JsonHelper;
import com.sia.task.core.util.StringHelper;
import com.sia.task.integration.curator.Curator4Scheduler;
import com.sia.task.integration.curator.hanler.TreeCacheHandler;
import com.sia.task.integration.curator.properties.ZookeeperConfiguration;
import com.sia.task.integration.curator.properties.ZookeeperConstant;
import com.sia.task.register.zookeeper.impl.MetadataLoaderService;
import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.curator.framework.recipes.cache.TreeCacheEvent.Type;
import org.apache.curator.framework.state.ConnectionState;
import org.apache.curator.framework.state.ConnectionStateListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

/**
 * TaskMonitor task事件监听处理逻辑
 *
 * @author: zhengweimao
 * @date 2018/4/1811:10
 */
public class TaskMonitor implements CommandLineRunner {

    private static final Logger LOGGER = LoggerFactory.getLogger(TaskMonitor.class);

    @Resource
    private ZookeeperConfiguration configuration;

    @Autowired
    private Curator4Scheduler curator4Scheduler;

    @Resource
    MetadataLoaderService metadataLoaderService;

    private final int TASK_KEY_DEPTH = 5;

    private final int TASK_KEY_PARENT_DEPTH = TASK_KEY_DEPTH - 1;


    /**
     * 获取ZK路径的深度，后面根据事件发生路径的深度，处理相应的逻辑
     *
     * @param path
     * @return
     */
    private int getDepth(String path) {
        return StringHelper.countOccurrencesOf(path, ZookeeperConstant.ZK_SEPARATOR);
    }

    /**
     * 将ZK中的数据插入/更新到DB中
     *
     * @param taskPath
     * @param task
     */
    private void handleBasicTask(String taskPath, String task) {

        if (StringHelper.isEmpty(taskPath) || StringHelper.isEmpty(task) || !taskPath.endsWith(task)) {
            return;
        }
        String metaData = curator4Scheduler.getCuratorClient().getData(taskPath);
        @SuppressWarnings("unchecked")
        Map<String, Object> metaMap = JsonHelper.toObject(metaData, Map.class);
        String taskDesc = metaData;
        Integer paramCount = 1;

        if (metaMap != null) {
            // taskDesc
            taskDesc = (String) metaMap.get("INFO");
            // paramCount
            paramCount = (Integer) metaMap.get("COUNT");
        }
        // taskKey
        String taskKey = curator4Scheduler.decodeHttpPath(task);
        //ZK中的task由联合字段 taskAppName：httpPath
        int splitLength = 2;
        String[] params = task.split(ZookeeperConstant.ZK_KEY_SPLIT);
        if (params == null || params.length != splitLength) {
            return;
        }
        // taskAppName
        String taskAppName = params[0];

        if (!taskAppName.contains(ZookeeperConstant.APP_SEPARATOR)) {
            return;
        }
        int index = taskAppName.indexOf(ZookeeperConstant.APP_SEPARATOR);
        // taskGroupName
        String taskGroupName = taskAppName.substring(0, index);

        String httpPath = params[1];
        // taskAppHttpPath
        String taskAppHttpPath = curator4Scheduler.decodeHttpPath(httpPath);

        BasicTask basicTask = new BasicTask();
        basicTask.setTaskKey(taskKey);
        basicTask.setTaskGroupName(taskGroupName);
        basicTask.setTaskAppName(taskAppName);
        basicTask.setTaskAppHttpPath(taskAppHttpPath);
        basicTask.setTaskDesc(taskDesc);
        basicTask.setTaskSource(Constant.TASK_SOURCE_ZK);
        basicTask.setParamCount(paramCount);
        try {
            int insertOrUpdateCount = metadataLoaderService.saveTaskMetadata(basicTask);
            LOGGER.info(Constant.LOG_PREFIX + "插入DB: insertOrUpdateByTaskKey，操作结果：" + (insertOrUpdateCount > 0));
        } catch (Exception ex) {
            LOGGER.error(Constant.LOG_EX_PREFIX + "basicTask is {}" + JsonHelper.toString(basicTask), ex);
        }
    }

    /**
     * ZookeeperMonitor初始化操作
     * 1. addAllAuth
     * 2. 实例启动是是否全量存储task元数据到数据库中，默认不开启
     * 3. 创建taskKey路径事件监听
     * 4. 增加定时器，规避实例启动后的首次事件的处理逻辑
     *
     * @throws Exception
     */
    public void initTaskZookeeper() throws Exception {
        //对用户进行授权
        String TASK_PATH = new StringBuilder().append(ZookeeperConstant.ZK_ROOT).append(curator4Scheduler.getTaskRoot()).append(ZookeeperConstant.ZK_SEPARATOR).append(ZookeeperConstant.ZK_ONLINE_TASK).toString();
        curator4Scheduler.getCuratorClient().addAllAuth(configuration.getDIGEST(), configuration.getAllAuth());

        if (configuration.getMetadataSyncStore()) {
            LOGGER.info(Constant.LOG_PREFIX + " All task metadata storage database starts ...");
            handleTaskKey(TASK_KEY_PARENT_DEPTH, TASK_PATH, curator4Scheduler);
            LOGGER.info(Constant.LOG_PREFIX + " All tasks metadata storage database has been completed");
        }

        handleTreeCache(TASK_KEY_DEPTH, TASK_PATH, curator4Scheduler);
        LOGGER.info(Constant.LOG_PREFIX + " init TaskMonitor OK");

        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                configuration.setMetadataSyncStore(true);
                LOGGER.info(Constant.LOG_PREFIX + " Modify metadataSyncStore -> {}", configuration.getMetadataSyncStore());
            }
        }, configuration.getMetadataSyncDelay());
    }

    @Override
    public void run(String... args) throws Exception {

        LOGGER.info(Constant.LOG_PREFIX + "try to init ZookeeperMonitor");
        if (curator4Scheduler == null) {
            LOGGER.info(Constant.LOG_PREFIX + "zooKeeperHosts 为空，启动 ZookeeperMonitor 需要配置 zooKeeperHosts地址！！！");
            return;
        }
        //在此处添加监听器
        initTaskZookeeper();
        ConnectionStateListener listener = (client, newState) -> {

            LOGGER.info(Constant.LOG_PREFIX + "OnlineTaskRegister Zookeeper ConnectionState:" + newState.name());

            if (newState == ConnectionState.LOST) {
                while (true) {
                    try {
                        if (client.getZookeeperClient().blockUntilConnectedOrTimedOut()) {
                            LOGGER.info(Constant.LOG_PREFIX + "OnlineTaskRegister Zookeeper Reconnected");
                            initTaskZookeeper();
                            LOGGER.info(Constant.LOG_PREFIX + "OnlineTaskRegister onlineTaskUpload Redo");
                            break;
                        }
                    } catch (Exception e) {
                        LOGGER.error(Constant.LOG_PREFIX + "Zookeeper Reconnect FAIL, please mailto [you email address]", e);
                    }
                }
            }
        };
        curator4Scheduler.getCuratorClient().getCuratorFramework().getConnectionStateListenable().addListener(listener);
        String TASK_PATH = new StringBuilder().append(ZookeeperConstant.ZK_ROOT).append(curator4Scheduler.getTaskRoot()).append(ZookeeperConstant.ZK_SEPARATOR).append(ZookeeperConstant.ZK_ONLINE_TASK).toString();
        LOGGER.info(TASK_PATH + "initTaskZookeeper finished!");
    }

    /**
     * 尝试将ZK上的TaskKey存入DB
     *
     * @param maxDepth
     * @param parentPath
     * @param curator4Scheduler
     * @throws Exception
     */
    private void handleTaskKey(final int maxDepth, final String parentPath, final Curator4Scheduler curator4Scheduler) throws Exception {

        //根据路径深度，来做相应处理
        int depth = getDepth(parentPath);
        LOGGER.info(Constant.LOG_PREFIX + "Zookeeper path: [" + parentPath + "], depth: [" + depth + "]");

        //读取子节点，继续往下，直到TaskKey这一路径
        if (depth < maxDepth) {
            List<String> children = curator4Scheduler.getCuratorClient().getChildren(parentPath);
            if (children == null) {
                return;
            }
            for (String child : children) {
                String childPath = parentPath + ZookeeperConstant.ZK_SEPARATOR + child;
                handleTaskKey(maxDepth, childPath, curator4Scheduler);
            }
        }
        //路径已至TaskKey这一层，需要组织数据，将Task数据存DB。插入数据库的操作保证幂等，同一个记录不会被多次插入！
        else if (depth == maxDepth) {
            List<String> children = curator4Scheduler.getCuratorClient().getChildren(parentPath);
            if (children == null) {
                return;
            }
            for (String child : children) {
                String childPath = parentPath + ZookeeperConstant.ZK_SEPARATOR + child;
                handleBasicTask(childPath, child);
            }
        }
    }

    /**
     * 监听ZK的TaskKey创建事件，存储DB
     *
     * @param maxDepth
     * @param parentPath
     * @param curator4Scheduler
     * @throws Exception
     */
    private void handleTreeCache(int maxDepth, String parentPath, Curator4Scheduler curator4Scheduler) throws Exception {
        //创建树形监听，根据路径深度做相应处理
        TreeCacheHandler treeCacheHandler = event -> {
            ChildData data = event.getData();
            if (data != null) {
                Type type = event.getType();
                String path = data.getPath();
                int depth = getDepth(path);
                // TaskKey这一层的节点创建事件，尝试插入DB
                if (type == Type.NODE_ADDED && depth == maxDepth) {
                    LOGGER.info(Constant.LOG_PREFIX + "触发事件：[" + type + "]，事件路径：[" + path + "]，事件路径深度：[" + depth + "], 存储处理：[" + configuration.getMetadataSyncStore() + "]");
                    // 避免实例启动的时候全量刷盘
                    if (configuration.getMetadataSyncStore()) {
                        String[] paths = path.split(ZookeeperConstant.ZK_SEPARATOR);
                        LOGGER.info(Constant.LOG_PREFIX + "begin save task metadata to the database, task metadata is : " + path);
                        handleBasicTask(path, paths[paths.length - 1]);
                    }
                }
                // TaskKey这一层的节点更新事件，尝试更新DB
                if (type == Type.NODE_UPDATED && depth == maxDepth) {
                    LOGGER.info(Constant.LOG_PREFIX + "触发事件：[" + type + "]，事件路径：[" + path + "]，事件路径深度：[" + depth + "]");
                    // 以下开始获取存储DB所需的信息，并尝试更新DB
                    String[] paths = path.split(ZookeeperConstant.ZK_SEPARATOR);
                    handleBasicTask(path, paths[paths.length - 1]);
                }
            }
        };
        // 创建监听器
        curator4Scheduler.getCuratorClient().createTreeCache(parentPath, treeCacheHandler);
    }
}
