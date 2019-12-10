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
import com.sia.core.helper.NetworkHelper;
import com.sia.core.helper.StringHelper;
import com.sia.scheduler.service.BasicJobService;
import com.sia.scheduler.service.JobMTaskService;
import com.sia.scheduler.service.common.CommonService;
import com.sia.scheduler.service.common.EmailService;
import com.sia.scheduler.util.constant.Constants;
import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.curator.framework.recipes.cache.TreeCacheEvent;
import org.apache.curator.framework.recipes.cache.TreeCacheEvent.Type;
import org.apache.curator.framework.state.ConnectionState;
import org.apache.curator.framework.state.ConnectionStateListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 *
 * ZK Job 监听器 监听JobKey相关的事件
 *
 * @see
 * @author maozhengwei
 * @date 2019-05-15 20:14
 * @version V1.0.0
 **/
@Component
@Order(value = 2)
public class JobMonitor extends CommonService implements CommandLineRunner {

    private static final Logger LOGGER = LoggerFactory.getLogger(JobMonitor.class);

    @Value("${spring.cloud.client.ipAddress}:${server.port}")
    private String schedulerIPAndPort;

    @Value("${hunter.job.alarm.threshold:32}")
    private long alarmThreshold;

    @Value("${hunter.job.fault.tolerant:1}")
    private long faultTolerant;

    @Autowired
    private Curator4Scheduler curator4Scheduler;

    @Autowired
    private EmailService emailService;

    @Autowired
    private BasicJobService jobService;

    @Autowired
    private JobMTaskService jobMTaskService;

    private final int JOB_KEY_DEPTH = 4;

    private final int JOB_KEY_PARENT_DEPTH = JOB_KEY_DEPTH - 1;

    private final int JOB_KEY_CHILD_DEPTH = JOB_KEY_DEPTH + 1;

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
     * 将初始化Zookeeper的操作封装到一起，需要一个对权限的初始化。
     * */

    private void initJobZookeeper() throws Exception {
        /**
         *  对用户进行授权
         * */
        curator4Scheduler.getCuratorClient().addAllAuth(curator4Scheduler.getDigest(),curator4Scheduler.getAllAuth());
        /**
         * 调度器的HOST信息初始化（其他操作需要用到）
         */
        Constants.LOCALHOST = schedulerIPAndPort;
        /**
         * 注册调度器的信息：需要上传调度器的实例信息（schedulerIPAndPort）
         */
        curator4Scheduler.registerScheduler(Constants.LOCALHOST);
        /**
         * 将自己加入调度器授权白名单
         */
        curator4Scheduler.addToAuth(NetworkHelper.getServerIp());
        /**
         * 初始化调度器相应的负载均衡信息
         */
        LoadBalanceHelper.initLoadBalanceHelper(curator4Scheduler);
        LoadBalanceHelper.setFaultTolerant((int) faultTolerant);
        LoadBalanceHelper.setAlarmThreshold((int) alarmThreshold);

        /**
         * 更新调度器相应的负载均衡信息
         * <p>
         * <当前执行的JOB个数X，拒绝JOB阈值N，资源预警阈值M>
         */
        LoadBalanceHelper.updateScheduler(0);

        /**
         * 检查自己是否在下线名单
         */
        if (isInBlackList()) {

            LoadBalanceHelper.offline();
        } else {
            LoadBalanceHelper.online();
        }
        /**
         * 监听下线名单的变化，改变自己的状态
         */
        String OFFLINE_PATH = new StringBuilder().append(Constant.ZK_ROOT).append(curator4Scheduler.getTaskRoot()).append(Constant.ZK_SEPARATOR).append(Constant.ZK_OFFLINE_SCHEDULER).toString();
        monitorOffline(OFFLINE_PATH, curator4Scheduler);

        /**
         * 抢占JOB：尝试获取/ZK_ONLINE_ROOT_PATH/Job 路径下的所有 /JobGroup 下的所有 /JobKey
         */
        String JOB_PATH = new StringBuilder().append(Constant.ZK_ROOT).append(curator4Scheduler.getTaskRoot()).append(Constant.ZK_SEPARATOR).append(Constant.ZK_ONLINE_JOB).toString();
        handleJobKey(JOB_KEY_PARENT_DEPTH, JOB_PATH, curator4Scheduler);
        /**
         * 监听ZK的JobKey创建与删除事件，监听JobKey子节点（即调度器节点）的删除事件
         */
        monitorJobKey(JOB_PATH, curator4Scheduler);
        LOGGER.info(Constants.LOG_PREFIX + "init JobMonitor OK");
        LOGGER.info(Constants.LOG_PREFIX + "schedulerIPAndPort->" + schedulerIPAndPort);
    }

    @Override
    public void run(String... args) throws Exception {

        LOGGER.info(Constants.LOG_PREFIX + "try to init ZookeeperMonitor");

        if (curator4Scheduler == null) {
            LOGGER.info(Constants.LOG_PREFIX + "zooKeeperHosts 为空，启动 ZookeeperMonitor 需要配置 zooKeeperHosts地址！！！");
            /**
             * TODO 如果ZK没有链接成功，关闭应用，保证ZK的连接可用
             */
            System.exit(-1);
            return;
        }
        initJobZookeeper();
        //添加监听
        ConnectionStateListener listener = (client, newState) -> {

            LOGGER.info(OnlineTaskConstant.LOGPREFIX + "OnlineTaskRegister Zookeeper ConnectionState:"
                    + newState.name());

            if (newState == ConnectionState.LOST) {
                while (true) {
                    try {
                        if (client.getZookeeperClient().blockUntilConnectedOrTimedOut()) {
                            LOGGER.info(OnlineTaskConstant.LOGPREFIX
                                    + "OnlineTaskRegister Zookeeper Reconnected");
                            initJobZookeeper();
                            LOGGER.info(OnlineTaskConstant.LOGPREFIX
                                    + "OnlineTaskRegister onlineTaskUpload Redo");

                            break;
                        }
                    }
                    catch (InterruptedException e) {
                        LOGGER.error(OnlineTaskConstant.LOGPREFIX
                                        + "Zookeeper Reconnect FAIL, please mailto [***@********.cn]",
                                e);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        };
        curator4Scheduler.getCuratorClient().getCuratorFramework().getConnectionStateListenable().addListener(listener);
        LOGGER.info(Constants.LOCALHOST + "success connect to Zookeeper" );
    }

    /**
     * 尝试获取/ZK_ONLINE_ROOT_PATH/Job 路径下的所有 /JobGroup 下的所有 /JobKey
     *
     * @param maxDepth
     * @param parentPath
     * @param curator4Scheduler
     * @throws Exception
     */
    private void handleJobKey(final int maxDepth, final String parentPath, final Curator4Scheduler curator4Scheduler)
            throws Exception {

        /**
         * 根据路径深度，来做相应处理
         */
        int depth = getDepth(parentPath);
        LOGGER.info(Constants.LOG_PREFIX + "Zookeeper path: [" + parentPath + "], depth: [" + depth + "]");

        // 读取子节点，继续往下，直到JobKey这一路径
        if (depth < maxDepth) {

            List<String> children = curator4Scheduler.getCuratorClient().getChildren(parentPath);
            if (children == null) {
                return;
            }
            for (String child : children) {
                String childPath = parentPath + Constant.ZK_SEPARATOR + child;
                handleJobKey(maxDepth, childPath, curator4Scheduler);
            }
        }
        // 路径已至JobKey这一层，需要抢占JobKey
        else if (depth == maxDepth) {
            List<String> children = curator4Scheduler.getCuratorClient().getChildren(parentPath);
            if (children == null) {
                return;
            }
            for (String child : children) {
                String childPath = parentPath + Constant.ZK_SEPARATOR + child;

                // 尝试抢占JOB
                acquireJob(childPath, Constants.LOCALHOST, curator4Scheduler);
            }
        }
    }

    /**
     * 调度器获取JOB的逻辑
     *
     * @param jobKeyPath
     * @param schedulerIPAndPort
     * @param curator4Scheduler
     * @throws Exception
     */
    private synchronized void acquireJob(String jobKeyPath, String schedulerIPAndPort,
                                         Curator4Scheduler curator4Scheduler) throws Exception {

        /**
         * 在抢占Job之前，调度器应该实时更新负载均衡信息
         */
        LoadBalanceHelper.updateScheduler(0);

        if (!shouldIAcquire()) {
            return;
        }

        String jobGroup = getJobGroup(jobKeyPath);
        String jobKey = getJobKey(jobKeyPath);
        // 第一步，尝试抢占JOB
        LOGGER.info(Constants.LOG_PREFIX + "尝试抢占JOB，JobGroupName is {} JobKey is {}", jobGroup, jobKey);
        boolean acquireJobStatus = curator4Scheduler.acquireJob(jobGroup, jobKey, schedulerIPAndPort);
        // 第二步，如果抢占成功则进行调度操作
        if (acquireJobStatus) {
            LOGGER.info(Constants.LOG_PREFIX + " 抢占Job成功, 注册Job; instance [{}]; group [{}], key [{}]", schedulerIPAndPort, jobGroup, jobKey);
            boolean checkExists = checkExists(jobGroup, jobKey);
            if (!checkExists) {
                if (scheduleJob(jobGroup, jobKey)) {
                    LOGGER.info(Constants.LOG_PREFIX + "注册Job成功，group [{}], key [{}]", jobGroup, jobKey);
                    // 获取JOB数增1
                    LoadBalanceHelper.updateScheduler(1);
                    /**
                     * JOB个数达到预警阈值，发送预警邮件
                     */

                    if (LoadBalanceHelper.isAlarm()) {
                        String message = curator4Scheduler.getSchedulerInfo(Constants.LOCALHOST);
                        emailService.sendEmail(null, message, Constants.LOCALHOST + "->Jobs reach threshold");
                    }
                }else {
                    LOGGER.info(Constants.LOG_PREFIX + "释放Job : 注册Job失败，group [{}], key [{}]", jobGroup, jobKey);
                    curator4Scheduler.releaseJob(jobGroup, jobKey, schedulerIPAndPort);
                }
                return;
            }
            LOGGER.info(Constants.LOG_PREFIX + "抢占Job成功 , 注册Job已存在，不做处理，可能原因Job没有释放 , instance [{}]; group [{}], key [{}]", schedulerIPAndPort, jobGroup, jobKey);

        }else {
            // 否则，本次抢占JOB失败
            LOGGER.info(Constants.LOG_PREFIX + " 抢占Job失败, instance [{}]; group [{}], key [{}]", schedulerIPAndPort, jobGroup, jobKey);
        }
    }

    /**
     * 监听ZK的JobKey创建与删除事件，监听JobKey子节点（即调度器节点）的删除事件
     *
     * @param parentPath
     * @param curator4Scheduler
     * @throws Exception
     */
    private void monitorJobKey(String parentPath, Curator4Scheduler curator4Scheduler) throws Exception {

        TreeCacheHandler treeCacheHandler = new TreeCacheHandler() {

            @Override
            public void process(TreeCacheEvent event) throws Exception {

                ChildData data = event.getData();
                if (data == null) {
                    return;
                }
                Type type = event.getType();
                String path = data.getPath();
                int depth = getDepth(path);
                // JobKey节点的创建事件
                if (type == Type.NODE_ADDED && depth == JOB_KEY_DEPTH) {

                    LOGGER.info(Constants.LOG_PREFIX + "触发事件：[" + type + "]，事件路径：[" + path + "]，事件路径深度：[" + depth
                            + "]");
                    // 第一步，尝试抢占JOB；第二步，如果抢占成功则进行调度操作
                    acquireJob(path, Constants.LOCALHOST, curator4Scheduler);

                }
                // JobKey节点的删除事件
                if (type == Type.NODE_REMOVED && depth == JOB_KEY_DEPTH) {
                    // 将JobKey相关的调度从Quartz中删除（关闭相关资源）
                    LOGGER.info(Constants.LOG_PREFIX + "触发事件：[" + type + "]，事件路径：[" + path + "]，事件路径深度：[" + depth
                            + "]");
                    String jobGroup = getJobGroup(path);
                    String jobKey = getJobKey(path);

                    boolean checkExists = checkJobExists(jobGroup, jobKey);
                    if (checkExists) {
                        removeJob(jobGroup, jobKey);
                        // 获取JOB数减1
                        LoadBalanceHelper.updateScheduler(-1);
                    }

                    jobMTaskService.cleanTasksCache(jobGroup, jobKey);
                }
                // 调度器节点的删除事件
                if (type == Type.NODE_REMOVED && depth == JOB_KEY_CHILD_DEPTH) {
                    LOGGER.info(Constants.LOG_PREFIX + "触发事件：[" + type + "]，事件路径：[" + path + "]，事件路径深度：[" + depth
                            + "]");

                    String jobKeyPath = getJobKeyPath(path);
                    LOGGER.info(Constants.LOG_PREFIX + "触发事件：[" + type + "]，jobKeyPath路径：[" + jobKeyPath + "]");
                    // 第一步，尝试抢占JOB；第二步，如果抢占成功则进行调度操作
                    acquireJob(jobKeyPath, Constants.LOCALHOST, curator4Scheduler);

                }
            }

        };
        // 创建监听器
        curator4Scheduler.getCuratorClient().createTreeCache(parentPath, treeCacheHandler);
    }

    /**
     * 监听下线名单的变化，改变自己的状态
     *
     * @param parentPath
     * @param curator4Scheduler
     * @throws Exception
     */
    private void monitorOffline(String parentPath, Curator4Scheduler curator4Scheduler) throws Exception {

        String JOB_PATH =  new StringBuilder().append(Constant.ZK_ROOT).append(curator4Scheduler.getTaskRoot()).append(Constant.ZK_SEPARATOR).append(Constant.ZK_ONLINE_JOB).toString();

        TreeCacheHandler treeCacheHandler = new TreeCacheHandler() {

            @Override
            public void process(TreeCacheEvent event) throws Exception {

                Type type = event.getType();

                // 调度器下线名单变化（+1或-1）
                if (type == Type.NODE_ADDED || type == Type.NODE_REMOVED) {
                    // 在下线名单
                    if (isInBlackList()) {
                        // 开启调度器下线开关，准备下线：（1）不再争抢JOB（2）对于已获得的JOB，调度JOB结束后主动释放
                        LoadBalanceHelper.offline();
                        // 之后，Job执行完后触发释放逻辑，其他调度器会争抢释放的Job，实时转移，无缝衔接
                        // 所有Job释放完毕后，调度器停机，会从调度器列表中注销自己
                    } else {
                        /**
                         * 操作次序不能变动！！！
                         */
                        // 不在下线名单，首先尝试打开调度器获取Job的开关。
                        // 如果上线操作成功，说明先前在下线名单，现在放出来了，此时应该去获取Job。否则，自己本来就已上线，此时不需额外操作。
                        if (LoadBalanceHelper.online()) {
                            // 上线操作成功，然后重新开始争抢JOB
                            /**
                             * 抢占JOB：尝试获取/ZK_ONLINE_ROOT_PATH/Job 路径下的所有 /JobGroup 下的所有 /JobKey
                             */
                            handleJobKey(JOB_KEY_PARENT_DEPTH, JOB_PATH, curator4Scheduler);
                        }
                    }
                }

            }

        };
        // 创建监听器
        curator4Scheduler.getCuratorClient().createTreeCache(parentPath, treeCacheHandler);
    }

    private String getJobGroup(String jobKeyPath) {

        String[] split = jobKeyPath.split(Constant.ZK_SEPARATOR);
        return split[split.length - 2];
    }

    private String getJobKeyPath(String path) {

        return path.substring(0, path.lastIndexOf(Constant.ZK_SEPARATOR));

    }

    private String getJobKey(String jobKeyPath) {

        String[] split = jobKeyPath.split(Constant.ZK_SEPARATOR);
        return split[split.length - 1];
    }

    /**
     * 判断自己是否应该抢占JOB
     *
     * @return
     */
    private boolean shouldIAcquire() {

        /**
         * 调度器准备下线，不再抢占
         */
        if (LoadBalanceHelper.isOffline()) {
            return false;
        }

        /**
         * JOB个数达到上限，不再抢占
         *
         */

        if (LoadBalanceHelper.isRefuse()) {
            return false;
        }
        return true;
    }

    /**
     * 判断自己是否在下线名单
     *
     * @return
     */
    private boolean isInBlackList() {

        List<String> offLines = curator4Scheduler.getBlackList();
        if (offLines == null) {
            return false;
        }
        for (String scheduler : offLines) {
            if (scheduler.equals(Constants.LOCALHOST)) {
                return true;
            }
        }
        return false;
    }

}
