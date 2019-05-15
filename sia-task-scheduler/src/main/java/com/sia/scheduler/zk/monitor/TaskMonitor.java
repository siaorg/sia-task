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

package com.sia.scheduler.zk.monitor;

import com.sia.core.constant.Constant;
import com.sia.hunter.constant.OnlineTaskConstant;
import com.sia.core.curator.Curator4Scheduler;
import com.sia.core.curator.handler.TreeCacheHandler;
import com.sia.core.entity.BasicTask;
import com.sia.core.helper.JSONHelper;
import com.sia.core.helper.StringHelper;
import com.sia.scheduler.service.BasicTaskService;
import com.sia.scheduler.util.constant.Constants;
import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.curator.framework.recipes.cache.TreeCacheEvent;
import org.apache.curator.framework.recipes.cache.TreeCacheEvent.Type;
import org.apache.curator.framework.state.ConnectionState;
import org.apache.curator.framework.state.ConnectionStateListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 *
 * askMonitor task事件监听处理逻辑
 *
 * @see
 * @author maozhengwei
 * @date 2019-04-18 11:15
 * @version V1.0.0
 **/
@Component
@Order(value = 3)
public class TaskMonitor implements CommandLineRunner {

    private static final Logger LOGGER = LoggerFactory.getLogger(TaskMonitor.class);

    @Autowired
    private Curator4Scheduler curator4Scheduler;

    @Autowired
    private BasicTaskService basicTaskService;

    private final int TASK_KEY_DEPTH = 5;

    private final int TASK_KEY_PARENT_DEPTH = TASK_KEY_DEPTH - 1;


    /**
     * 获取ZK路径的深度，后面根据事件发生路径的深度，处理相应的逻辑
     *
     * @param path
     * @return
     */
    private int getDepth(String path) {

        return StringHelper.countOccurrencesOf(path, Constant.ZK_SEPARATOR);
    }

    /**
     * 将ZK中的数据插入/更新到DB中
     *
     * @param taskPath
     * @param task
     * @param isUpdate
     */
    private void handleBasicTask(String taskPath, String task, boolean isUpdate) {

        if (StringHelper.isEmpty(taskPath) || StringHelper.isEmpty(task) || !taskPath.endsWith(task)) {
            return;
        }
        String metaData = curator4Scheduler.getCuratorClient().getData(taskPath);
        @SuppressWarnings("unchecked")
        Map<String, Object> metaMap = JSONHelper.toObject(metaData, Map.class);
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
        String[] params = task.split(Constant.ZK_KEY_SPLIT);
        if (params == null || params.length != splitLength) {
            return;
        }
        // taskAppName
        String taskAppName = params[0];

        if (!taskAppName.contains(Constant.APP_SEPARATOR)) {
            return;
        }
        int index = taskAppName.indexOf(Constant.APP_SEPARATOR);
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
        basicTask.setTaskSource(Constants.TASK_SOURCE_ZK);
        /**
         * TODO：添加Task的入参个数需要DB支持
         */
        basicTask.setParamCount(paramCount);
        /**
         * 调用DB的API，插入数据库，由DB保证数据的唯一性（不会重复插入）
         */
        try {
            int insertOrUpdateCount;
            insertOrUpdateCount = basicTaskService.insertOrUpdateByTaskKey(basicTask);
            LOGGER.info(Constants.LOG_PREFIX + "插入DB: insertOrUpdateByTaskKey，操作结果：" + (insertOrUpdateCount > 0));
        }
        catch (Exception e) {
            LOGGER.error(Constants.LOG_PREFIX + "basicTask is {}" + JSONHelper.toString(basicTask));
            LOGGER.error(Constants.LOG_PREFIX, e);
        }
    }

    /**
     * ZookeeperMonitor初始化操作
     * */
    public void initTaskZookeeper() throws Exception {
        /**
         * 对用户进行授权
         * */
        String TASK_PATH = new StringBuilder().append(Constant.ZK_ROOT).append(curator4Scheduler.getTaskRoot()).append(Constant.ZK_SEPARATOR).append(Constant.ZK_ONLINE_TASK).toString();
        curator4Scheduler.getCuratorClient().addAllAuth(curator4Scheduler.getDigest(),curator4Scheduler.getAllAuth());
        // 尝试将ZK上的TaskKey存入DB
        handleTaskKey(TASK_KEY_PARENT_DEPTH, TASK_PATH, curator4Scheduler);
        // 监听ZK的TaskKey创建事件，存储DB
        handleTreeCache(TASK_KEY_DEPTH, TASK_PATH, curator4Scheduler);
        LOGGER.info(Constants.LOG_PREFIX + "init TaskMonitor OK");
    }

    @Override
    public void run(String... args) throws Exception {

        LOGGER.info(Constants.LOG_PREFIX + "try to init ZookeeperMonitor");
        if (curator4Scheduler == null) {
            LOGGER.info(Constants.LOG_PREFIX + "zooKeeperHosts 为空，启动 ZookeeperMonitor 需要配置 zooKeeperHosts地址！！！");
            return;
        }
        //在此处添加监听器
        initTaskZookeeper();
        ConnectionStateListener listener = (client, newState) -> {

            LOGGER.info(OnlineTaskConstant.LOGPREFIX + "OnlineTaskRegister Zookeeper ConnectionState:"
                    + newState.name());

            if (newState == ConnectionState.LOST) {
                while (true) {
                    try {
                        if (client.getZookeeperClient().blockUntilConnectedOrTimedOut()) {
                            LOGGER.info(OnlineTaskConstant.LOGPREFIX
                                    + "OnlineTaskRegister Zookeeper Reconnected");
                            initTaskZookeeper();
                            LOGGER.info(OnlineTaskConstant.LOGPREFIX
                                    + "OnlineTaskRegister onlineTaskUpload Redo");

                            break;
                        }
                    }
                    catch (InterruptedException e) {
                        LOGGER.error(OnlineTaskConstant.LOGPREFIX
                                + "Zookeeper Reconnect FAIL, please mailto [***@********.cn]", e);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        };
        curator4Scheduler.getCuratorClient().getCuratorFramework().getConnectionStateListenable().addListener(listener);
        String TASK_PATH = new StringBuilder().append(Constant.ZK_ROOT).append(curator4Scheduler.getTaskRoot()).append(Constant.ZK_SEPARATOR).append(Constant.ZK_ONLINE_TASK).toString();
        LOGGER.info(TASK_PATH + "成功写入数据库");
    }

    /**
     * 尝试将ZK上的TaskKey存入DB
     *
     * @param maxDepth
     * @param parentPath
     * @param curator4Scheduler
     * @throws Exception
     */
    private void handleTaskKey(final int maxDepth, final String parentPath, final Curator4Scheduler curator4Scheduler)
            throws Exception {

        /**
         * 根据路径深度，来做相应处理
         */
        int depth = getDepth(parentPath);
        LOGGER.info(Constants.LOG_PREFIX + "Zookeeper path: [" + parentPath + "], depth: [" + depth + "]");

        /**
         * 读取子节点，继续往下，直到TaskKey这一路径
         */
        if (depth < maxDepth) {

            List<String> children = curator4Scheduler.getCuratorClient().getChildren(parentPath);
            if (children == null) {
                return;
            }

            for (String child : children) {
                String childPath = parentPath + Constant.ZK_SEPARATOR + child;
                handleTaskKey(maxDepth, childPath, curator4Scheduler);

            }

        }
        /**
         * 路径已至TaskKey这一层，需要组织数据，将Task数据存DB。插入数据库的操作保证幂等，同一个记录不会被多次插入！
         */
        else if (depth == maxDepth) {
            List<String> children = curator4Scheduler.getCuratorClient().getChildren(parentPath);
            if (children == null) {
                return;
            }

            for (String child : children) {
                String childPath = parentPath + Constant.ZK_SEPARATOR + child;

                // 如果还未存储过 ，则尝试存储DB（这一步其实是锦上添花，可以把判断条件去掉，也不影响正确性）
                // if (!isExist(childPath)) {
                // 以下开始获取存储DB所需的信息，并尝试存储DB
                handleBasicTask(childPath, child, false);
                // }
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
    private void handleTreeCache(int maxDepth, String parentPath, Curator4Scheduler curator4Scheduler)
            throws Exception {

        /**
         * 创建树形监听，根据路径深度做相应处理
         */
        TreeCacheHandler treeCacheHandler = new TreeCacheHandler() {

            @Override
            public void process(TreeCacheEvent event) throws Exception {

                ChildData data = event.getData();
                if (data != null) {
                    Type type = event.getType();
                    String path = data.getPath();
                    int depth = getDepth(path);
                    // TaskKey这一层的节点创建事件，尝试插入DB
                    if (data != null && type == Type.NODE_ADDED && depth == maxDepth) {

                        LOGGER.info(Constants.LOG_PREFIX + "触发事件：[" + type + "]，事件路径：[" + path + "]，事件路径深度：["
                                + depth + "]");
                        // 如果还未存储过 ，则尝试存储DB
                        // if (!isExist(path)) {
                        // 以下开始获取存储DB所需的信息，并尝试存储DB
                        String[] paths = path.split(Constant.ZK_SEPARATOR);
                        handleBasicTask(path, paths[paths.length - 1], false);
                        // }
                    }
                    // TaskKey这一层的节点更新事件，尝试更新DB
                    if (data != null && type == Type.NODE_UPDATED && depth == maxDepth) {

                        LOGGER.info(Constants.LOG_PREFIX + "触发事件：[" + type + "]，事件路径：[" + path + "]，事件路径深度：["
                                + depth + "]");

                        // 以下开始获取存储DB所需的信息，并尝试更新DB
                        String[] paths = path.split(Constant.ZK_SEPARATOR);
                        handleBasicTask(path, paths[paths.length - 1], true);
                    }

                    // TODO: 获取该节点的父亲节点查询所有子节点做存入 DB，补偿策略，待考虑
                }
            }

        };
        // 创建监听器
        curator4Scheduler.getCuratorClient().createTreeCache(parentPath, treeCacheHandler);
    }
}
