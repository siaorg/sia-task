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

import com.sia.task.core.IMetadataLoader;
import com.sia.task.core.INotifyScheduler;
import com.sia.task.core.entity.BasicJob;
import com.sia.task.core.task.DagTask;
import com.sia.task.core.util.Constant;
import com.sia.task.core.util.NetworkHelper;
import com.sia.task.core.util.StringHelper;
import com.sia.task.integration.curator.Curator4Scheduler;
import com.sia.task.integration.curator.hanler.TreeCacheHandler;
import com.sia.task.integration.curator.properties.ZookeeperConfiguration;
import com.sia.task.integration.curator.properties.ZookeeperConstant;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.curator.framework.recipes.cache.TreeCacheEvent.Type;
import org.apache.curator.framework.state.ConnectionState;
import org.apache.curator.framework.state.ConnectionStateListener;
import org.springframework.boot.CommandLineRunner;

import javax.annotation.Resource;
import java.util.List;

/**
 * <p>
 * zookeeper-job listener to listen for JobKey related events
 * The listener class for zookeeper metadata 'job path' related events
 * </p>
 * Listen for job release and delete events, job status change events, and job scheduler change events,
 * as shown below, when the instance dies, and the instance reaches the warning value
 *
 * @author maozhengwei
 * @version V1.0.0
 * @data 2018-05-15 14:59
 * @see
 **/
@Slf4j
public class JobMonitor implements CommandLineRunner {

    @Resource
    private ZookeeperConfiguration configuration;

    @Resource
    private Curator4Scheduler curator4Scheduler;

    @Resource
    private IMetadataLoader loaderService;

    @Resource
    private INotifyScheduler notifyScheduler;


    private final int JOB_KEY_DEPTH = 4;

    private final int RUNONCE_JOB_KEY_DEPTH = 3;

    private final int JOB_KEY_PARENT_DEPTH = JOB_KEY_DEPTH - 1;

    private final int JOB_KEY_CHILD_DEPTH = JOB_KEY_DEPTH + 1;

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
     * 将初始化Zookeeper的操作封装到一起，需要一个对权限的初始化。
     */
    private void initJobZookeeper() throws Exception {
        curator4Scheduler.getCuratorClient().addAllAuth(configuration.getDIGEST(), configuration.getAllAuth());
        Constant.LOCALHOST = NetworkHelper.getServerIp() + ZookeeperConstant.ZK_KEY_SPLIT + configuration.getPort();
        //注册调度器的信息：需要上传调度器的实例信息
        curator4Scheduler.registerScheduler(Constant.LOCALHOST);
        //将自己加入调度器授权白名单
        curator4Scheduler.addToAuth(NetworkHelper.getServerIp());
        //初始化调度器相应的负载均衡信息
        LoadBalanceHelper.initLoadBalanceHelper(curator4Scheduler);
        LoadBalanceHelper.setFaultTolerant((int) configuration.getFaultTolerant());
        LoadBalanceHelper.setAlarmThreshold((int) configuration.getAlarmThreshold());
        log.info(Constant.LOG_PREFIX + " init load balance -[faultTolerant : {}, alarmThreshold : {} ] ", configuration.getFaultTolerant(), configuration.getAlarmThreshold());
        //更新调度器相应的负载均衡信息
        LoadBalanceHelper.updateScheduler(0, "--initJobZookeeper--");
        //检查自己是否在下线名单
        if (isInBlackList()) {
            LoadBalanceHelper.offline();
        } else {
            LoadBalanceHelper.online();
        }
        //监听下线名单的变化，改变自己的状态
        String OFFLINE_PATH = new StringBuilder().append(ZookeeperConstant.ZK_ROOT).append(curator4Scheduler.getTaskRoot()).append(ZookeeperConstant.ZK_SEPARATOR).append(ZookeeperConstant.ZK_OFFLINE_SCHEDULER).toString();
        monitorOffline(OFFLINE_PATH, curator4Scheduler);
        //抢占JOB：尝试获取/ZK_ONLINE_ROOT_PATH/Job 路径下的所有 /JobGroup 下的所有 /JobKey
        String JOB_PATH = new StringBuilder().append(ZookeeperConstant.ZK_ROOT).append(curator4Scheduler.getTaskRoot()).append(ZookeeperConstant.ZK_SEPARATOR).append(ZookeeperConstant.ZK_ONLINE_JOB).toString();
        handleJobKey(JOB_KEY_PARENT_DEPTH, JOB_PATH, curator4Scheduler);
        //监听ZK的JobKey创建与删除事件，监听JobKey子节点（即调度器节点）的删除事件
        monitorJobKey(JOB_PATH, curator4Scheduler);
        //监听ZK的JobRunOnce名单的变化(RunOnceJobKey的创建事件)
        String JOBRUNONCE_PATH = new StringBuilder().append(ZookeeperConstant.ZK_ROOT).append(curator4Scheduler.getTaskRoot()).append(ZookeeperConstant.ZK_SEPARATOR).append(ZookeeperConstant.ZK_ONLINE_JOB_RUNONCE).toString();
        monitorJobRunOnce(JOBRUNONCE_PATH, curator4Scheduler);
        log.info(Constant.LOG_PREFIX + "init JobMonitor OK,schedulerIPAndPort -> {}", Constant.LOCALHOST);
    }

    @Override
    public void run(String... args) throws Exception {
        log.info(Constant.LOG_PREFIX + "try to init ZookeeperMonitor...");
        if (curator4Scheduler == null) {
            log.info(Constant.LOG_PREFIX + "zooKeeperHosts 为空，启动 ZookeeperMonitor 需要配置 zooKeeperHosts地址！！！");
            // 如果ZK没有链接成功，关闭应用，保证ZK的连接可用
            System.exit(-1);
            return;
        }
        initJobZookeeper();
        //添加监听
        ConnectionStateListener listener = (client, newState) -> {
            log.info(Constant.LOG_PREFIX + "OnlineTaskRegister Zookeeper ConnectionState:" + newState.name());
            if (newState == ConnectionState.LOST) {
                while (true) {
                    try {
                        if (client.getZookeeperClient().blockUntilConnectedOrTimedOut()) {
                            log.info(Constant.LOG_PREFIX + "OnlineTaskRegister Zookeeper Reconnected");
                            initJobZookeeper();
                            log.info(Constant.LOG_PREFIX + "OnlineTaskRegister onlineTaskUpload Redo");
                            break;
                        }
                    } catch (Exception e) {
                        log.error(Constant.LOG_PREFIX + "Zookeeper Reconnect FAIL, please mailto [you email address]", e);
                    }
                }
            }
            if (newState == ConnectionState.RECONNECTED) {
                log.info(Constant.LOG_PREFIX + " Zookeeper Reconnect newState: ConnectionState.RECONNECTED ... initJobZookeeper...");
                try {
                    initJobZookeeper();
                } catch (Exception e) {
                    log.error(Constant.LOG_PREFIX + " ConnectionState.RECONNECTED - Zookeeper Reconnect FAIL, please mailto [you email address]", e);
                }
            }
            curator4Scheduler.getCuratorClient().addAllAuth(configuration.getDIGEST(), configuration.getAllAuth());
        };
        curator4Scheduler.getCuratorClient().getCuratorFramework().getConnectionStateListenable().addListener(listener);
        log.info(Constant.LOG_EX_PREFIX + Constant.LOCALHOST + " success connect to Zookeeper");
    }

    /**
     * 获取/ZK_ONLINE_ROOT_PATH/Job 路径下的所有 /JobGroup 下的所有 /JobKey
     *
     * @param maxDepth
     * @param parentPath
     * @param curator4Scheduler
     * @throws Exception
     */
    private void handleJobKey(final int maxDepth, final String parentPath, final Curator4Scheduler curator4Scheduler) throws Exception {
        //根据路径深度，来做相应处理
        int depth = getDepth(parentPath);
        log.info(Constant.LOG_PREFIX + "Zookeeper path: [" + parentPath + "], depth: [" + depth + "]");

        // 读取子节点，继续往下，直到JobKey这一路径
        if (depth < maxDepth) {
            List<String> children = curator4Scheduler.getCuratorClient().getChildren(parentPath);
            if (children == null) {
                return;
            }
            for (String child : children) {
                String childPath = parentPath + ZookeeperConstant.ZK_SEPARATOR + child;
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
                String childPath = parentPath + ZookeeperConstant.ZK_SEPARATOR + child;
                // 尝试抢占JOB
                acquireJob(childPath, Constant.LOCALHOST, curator4Scheduler);
            }
        }
    }

    /**
     * 调度器获取JOB的逻辑
     * <p>
     * 针对抢占失败或者注册失败进行补偿,去除注册时是否存在的检测逻辑。
     *
     * @param jobKeyPath
     * @param schedulerIPAndPort
     * @param curator4Scheduler
     * @throws Exception
     */
    private synchronized void acquireJob(String jobKeyPath, String schedulerIPAndPort, Curator4Scheduler curator4Scheduler) throws Exception {
        //在抢占Job之前，调度器应该实时更新负载均衡信息

        String jobGroup = getJobGroup(jobKeyPath);
        String jobKey = getJobKey(jobKeyPath);
        LoadBalanceHelper.updateScheduler(0, " Pre acquireJob: " + jobKey);

        if (!shouldIAcquire()) {
            log.info(Constant.LOG_PREFIX + "调度器已下线 / JOB个数达到上限，不再抢占 [{}]", jobKeyPath);
            return;
        }

        log.info(Constant.LOG_PREFIX + " start seize Job，JobKey:{}", jobKey);
        boolean acquireJobStatus = curator4Scheduler.acquireJob(jobGroup, jobKey, schedulerIPAndPort);

        if (acquireJobStatus) {
            log.info(Constant.LOG_PREFIX + " Successfully seized JOB, began to register Quartz; JobKey:{}", jobKey);

            BasicJob job = loaderService.loadJob(jobGroup, jobKey);
            List<DagTask> dagTasks = loaderService.loadDagTask4Job(jobGroup, jobKey);

            if (notifyScheduler.checkExists(jobGroup, jobKey)) {
                log.info(Constant.LOG_PREFIX + "Job preemption succeeded, registered job already exists，No need to deal with it, the possible reason is that Job has not been released , instance [{}];  key [{}]", schedulerIPAndPort, jobKey);
                return;
            }

            if (notifyScheduler.addJob(job, dagTasks, notifyScheduler.getOnlineTaskClass())) {
                log.info(Constant.LOG_PREFIX + " JOB registration Quartz completed, JobKey:{}", jobKey);

                if (LoadBalanceHelper.isAlarm()) {
                    String message = curator4Scheduler.getSchedulerInfo(Constant.LOCALHOST);
                    // TODO emailService.sendEmail (null, message, Constant.LOCALHOST + " -> Jobs reach threshold");
                    log.info(Constant.LOG_PREFIX + " 单个调度器实例拥有JOB个数达到预警阈值，发送预警邮件: {}", message);
                }
            } else {
                log.info(Constant.LOG_PREFIX + " JOB registration Quartz failed, JobKey:{}", jobKey);
            }
            return;
        }
        // 否则，本次抢占JOB失败
        log.info(Constant.LOG_PREFIX + " Failed to preempt JOB, JobKey: {}", jobKey);
    }

    /**
     * 监听ZK的JobKey创建与删除事件，监听JobKey子节点（即调度器节点）的删除事件
     *
     * @param parentPath
     * @param curator4Scheduler
     * @throws Exception
     */
    private void monitorJobKey(String parentPath, Curator4Scheduler curator4Scheduler) throws Exception {
        TreeCacheHandler treeCacheHandler = event -> {
            ChildData data = event.getData();
            if (data == null) {
                return;
            }
            Type type = event.getType();
            String path = data.getPath();
            int depth = getDepth(path);
            // JobKey节点的创建事件
            if (type == Type.NODE_ADDED && depth == JOB_KEY_DEPTH) {

                log.info(Constant.LOG_PREFIX + "触发事件：[" + type + "]，事件路径：[" + path + "]，事件路径深度：[" + depth + "]");
                acquireJob(path, Constant.LOCALHOST, curator4Scheduler);
            }
            // JobKey节点的删除事件
            if (type == Type.NODE_REMOVED && depth == JOB_KEY_DEPTH) {
                // 将JobKey相关的调度从Quartz中删除（关闭相关资源）
                log.info(Constant.LOG_PREFIX + "触发事件：[" + type + "]，事件路径：[" + path + "]，事件路径深度：[" + depth + "]");
                String jobGroup = getJobGroup(path);
                String jobKey = getJobKey(path);
                if (notifyScheduler.checkExists(jobGroup, jobKey) && notifyScheduler.removeJob(jobGroup, jobKey)) {
                    LoadBalanceHelper.updateScheduler(-1, "Type.NODE_REMOVED - removeJob: " + jobKey);
                }
            }
            // 调度器节点的删除事件
            if (type == Type.NODE_REMOVED && depth == JOB_KEY_CHILD_DEPTH) {
                log.info(Constant.LOG_PREFIX + "触发事件：[" + type + "]，事件路径：[" + path + "]，事件路径深度：[" + depth + "]");
                String jobKeyPath = getJobKeyPath(path);
                log.info(Constant.LOG_PREFIX + "触发事件：[" + type + "]，jobKeyPath路径：[" + jobKeyPath + "]");
                acquireJob(jobKeyPath, Constant.LOCALHOST, curator4Scheduler);
            }
        };
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
        String JOB_PATH = new StringBuilder().append(ZookeeperConstant.ZK_ROOT).append(curator4Scheduler.getTaskRoot()).append(ZookeeperConstant.ZK_SEPARATOR).append(ZookeeperConstant.ZK_ONLINE_JOB).toString();
        TreeCacheHandler treeCacheHandler = event -> {
            Type type = event.getType();
            // 调度器下线名单变化（+1或-1）
            if (type == Type.NODE_ADDED || type == Type.NODE_REMOVED) {
                if (isInBlackList()) {
                    LoadBalanceHelper.offline();
                } else {
                    /*
                     * 操作次序不能变动！！！
                     * 1. 不在下线名单，首先尝试打开调度器获取Job的开关
                     * 2. 如果上线操作成功，说明先前在下线名单，现在放出来了，此时应该去获取Job。否则，自己本来就已上线，此时不需额外操作
                     */
                    if (LoadBalanceHelper.online()) {
                        /*
                         * 上线操作成功，然后重新开始争抢JOB
                         * 抢占JOB：尝试获取/ZK_ONLINE_ROOT_PATH/Job 路径下的所有 /JobGroup 下的所有 /JobKey
                         */
                        handleJobKey(JOB_KEY_PARENT_DEPTH, JOB_PATH, curator4Scheduler);
                    }
                }
            }
        };
        // 创建监听器
        curator4Scheduler.getCuratorClient().createTreeCache(parentPath, treeCacheHandler);
    }

    /**
     * 监听ZK的Job单次运行列表的新增动作
     *
     * @param parentPath
     * @param curator4Scheduler
     * @throws Exception
     */

    public void monitorJobRunOnce(String parentPath, Curator4Scheduler curator4Scheduler) throws Exception {
        TreeCacheHandler treeCacheHandler = event -> {
            ChildData data = event.getData();
            if (data == null) {
                return;
            }
            Type type = event.getType();
            String path = data.getPath();
            int depth = getDepth(path);
            String jobKey = getJobKey(path);
            String jobGroup = jobKey.split(ZookeeperConstant.JOBKEY_SEPARATOR)[0];
            // RunOnceJobKey节点的创建事件
            if (type == Type.NODE_ADDED && depth == RUNONCE_JOB_KEY_DEPTH) {
                log.info(Constant.LOG_PREFIX + " 触发事件：[" + type + "]，事件路径：[" + path + "]，事件路径深度：[" + depth + "]");
                // Job单次运行
                if (curator4Scheduler.getJobScheduler(jobGroup, jobKey).contains(Constant.LOCALHOST)) {
                    curator4Scheduler.deleteRunOnceJobKey(jobKey);
                    notifyScheduler.triggerJob(jobGroup, jobKey);
                }
            }
        };
        // 创建监听器
        curator4Scheduler.getCuratorClient().createTreeCache(parentPath, treeCacheHandler);
    }

    private String getJobGroup(String jobKeyPath) {
        String[] split = jobKeyPath.split(ZookeeperConstant.ZK_SEPARATOR);
        return split[split.length - 2];
    }

    private String getJobKeyPath(String path) {
        return path.substring(0, path.lastIndexOf(ZookeeperConstant.ZK_SEPARATOR));
    }

    private String getJobKey(String jobKeyPath) {
        String[] split = jobKeyPath.split(ZookeeperConstant.ZK_SEPARATOR);
        return split[split.length - 1];
    }

    /**
     * 判断自己是否应该抢占JOB
     *
     * @return
     */
    private boolean shouldIAcquire() {
        // 调度器下线/JOB个数达到上限，不再抢占
        return !(LoadBalanceHelper.isOffline() || LoadBalanceHelper.isRefuse());
    }

    /**
     * 判断自己是否在下线名单
     *
     * @return
     */
    private boolean isInBlackList() {
        List<String> offLines = curator4Scheduler.getBlackList();
        if (offLines == null || offLines.isEmpty()) {
            return false;
        }
        for (String scheduler : offLines) {
            if (scheduler.equals(Constant.LOCALHOST)) {
                return true;
            }
        }
        return false;
    }
}
