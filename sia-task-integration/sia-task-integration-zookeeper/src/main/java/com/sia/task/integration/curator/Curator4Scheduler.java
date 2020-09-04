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

import com.sia.task.core.task.SiaJobStatus;
import com.sia.task.core.util.StringHelper;
import com.sia.task.integration.curator.properties.ZookeeperConfiguration;
import com.sia.task.integration.curator.properties.ZookeeperConstant;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.recipes.locks.InterProcessMutex;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.data.ACL;
import org.apache.zookeeper.data.Id;
import org.apache.zookeeper.server.auth.DigestAuthenticationProvider;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author: pengfeili23
 * @Description: 封装 CuratorClient，专为调度器 Scheduler 提供操作API
 * @date: 2019年2月20日 下午3:29:20 代码检查
 */
@Slf4j
public class Curator4Scheduler {

    private long MAX_WAIT_SECONDS = 60;

    private CuratorClient curatorClient;

    private String taskRoot;

    /**
     * 获取封装的zookeeper的操作类:CuratorClient
     *
     * @return
     */
    public CuratorClient getCuratorClient() {

        return curatorClient;
    }

    /**
     * 构建路径root
     *
     * @return
     */
    private String buildTaskRoot() {
        StringBuilder taskPath = new StringBuilder().append(ZookeeperConstant.ZK_ROOT).append(taskRoot).append(ZookeeperConstant.ZK_SEPARATOR);
        return taskPath.toString();
    }

    public String getTaskRoot() {
        return taskRoot;
    }

    /**
     * curatorClient 使用时，应当添加权限控制
     *
     * @param curatorClient
     * @param needACL
     */
    public Curator4Scheduler(CuratorClient curatorClient, boolean needACL, String taskRoot) {

        this.curatorClient = curatorClient;
        this.taskRoot = taskRoot;
        inital(null, needACL);
    }

    /**
     * 使用时，应当添加权限控制
     *
     * @param zkAddress
     * @param needACL
     */
    public Curator4Scheduler(String zkAddress, boolean needACL, String taskRoot) {

        this.curatorClient = new CuratorClient(zkAddress, 10, 5000);
        this.taskRoot = taskRoot;
        inital(null, needACL);
    }

    public Curator4Scheduler(ZookeeperConfiguration configuration, boolean needACL) {
        this.curatorClient = new CuratorClient(configuration.getZooKeeperHosts(), configuration.getRETRY_TIMES(), configuration.getSLEEP_MS_BETWEEN_RETRIES());
        this.MAX_WAIT_SECONDS = configuration.getMAX_WAIT_SECONDS();
        this.taskRoot = configuration.getTaskRoot();
        inital(configuration, needACL);
    }

    /**
     * 初始化操作：赋权，初始化路径
     *
     * @param needACL
     */
    private void inital(ZookeeperConfiguration configuration, boolean needACL) {

        List<ACL> allAndCreate = new ArrayList<>(), onlyAll = new ArrayList<>(), allAndCreateAndRead = new ArrayList<>();
        ACL all = null, create = null, read = null;
        try {
            if (configuration != null) {
                all = new ACL(ZooDefs.Perms.ALL,
                        new Id(configuration.getDIGEST(), DigestAuthenticationProvider.generateDigest(configuration.getAllAuth())));
                create = new ACL(ZooDefs.Perms.CREATE,
                        new Id(configuration.getDIGEST(), DigestAuthenticationProvider.generateDigest(configuration.getCreateAuth())));
                read = new ACL(ZooDefs.Perms.READ,
                        new Id(configuration.getDIGEST(), DigestAuthenticationProvider.generateDigest(configuration.getCreateAuth())));
            } else {
                all = new ACL(ZooDefs.Perms.ALL,
                        new Id("digest", DigestAuthenticationProvider.generateDigest("SIA:SkyWorld")));
                create = new ACL(ZooDefs.Perms.CREATE,
                        new Id("digest", DigestAuthenticationProvider.generateDigest("guest:guest")));
                read = new ACL(ZooDefs.Perms.READ,
                        new Id("digest", DigestAuthenticationProvider.generateDigest("guest:guest")));
            }
        } catch (Exception e) {
            log.error("create ACL exception:", e);
        }
        allAndCreate.add(all);
        allAndCreate.add(create);
        onlyAll.add(all);
        allAndCreateAndRead.add(all);
        allAndCreateAndRead.add(create);
        allAndCreateAndRead.add(read);
        /**
         * 自己先认证，所有权限。
         * <p>
         * TODO: 注意：此处会触发所有路径的子节点创建事件！！！
         */
        this.curatorClient.addAllAuth(configuration.getDIGEST(), configuration.getAllAuth());
        /**
         * initial all part
         */
        initialOnlineRootPath(allAndCreate, needACL);
        initialTaskPath(allAndCreate, needACL);
        initialJobPath(onlyAll, needACL);
        initialSchedulerPath(onlyAll, needACL);
        initialLockPath(onlyAll, needACL);
        initialOfflinePath(onlyAll, needACL);
        initialJobTransferPath(onlyAll, needACL);
        initialJobRunOncePath(onlyAll, needACL);
        initialAuthPath(allAndCreateAndRead, needACL);

    }

    /**
     * 初始化任务调度根路径
     *
     * @param acls
     * @param needACL
     */
    private void initialOnlineRootPath(List<ACL> acls, boolean needACL) {

        try {

            // "/ZK_ONLINE_ROOT_PATH"
            String onlineRoot = new StringBuilder().append(ZookeeperConstant.ZK_ROOT).append(taskRoot).toString();
            if (!this.curatorClient.isExists(onlineRoot)) {
                this.curatorClient.createPersistentZKNode(onlineRoot);
            }
            if (needACL) {
                this.curatorClient.getCuratorFramework().setACL().withACL(acls).forPath(onlineRoot);
            }
        } catch (Exception e) {
            log.error("initialOnlineRootPath exception:", e);
        }

    }

    /**
     * 初始化Task路径
     *
     * @param acls
     * @param needACL
     */
    private void initialTaskPath(List<ACL> acls, boolean needACL) {

        try {
            // "/SkyWorldOnlineTask/Task"
            String task = new StringBuilder().append(buildTaskRoot()).append(ZookeeperConstant.ZK_ONLINE_TASK).toString();
            if (!this.curatorClient.isExists(task)) {
                this.curatorClient.createPersistentZKNode(task);
            }
            if (needACL) {
                this.curatorClient.getCuratorFramework().setACL().withACL(acls).forPath(task);
            }
        } catch (Exception e) {
            log.error("initialTaskPath exception:", e);
        }

    }

    /**
     * 初始化Job路径
     *
     * @param acls
     * @param needACL
     */
    private void initialJobPath(List<ACL> acls, boolean needACL) {

        try {
            // "/ZK_ONLINE_ROOT_PATH/Job"
            String job = new StringBuilder().append(buildTaskRoot()).append(ZookeeperConstant.ZK_ONLINE_JOB).toString();
            if (!this.curatorClient.isExists(job)) {
                this.curatorClient.createPersistentZKNode(job);
            }
            if (needACL) {
                this.curatorClient.getCuratorFramework().setACL().withACL(acls).forPath(job);
            }
        } catch (Exception e) {
            log.error("initialJobPath exception:", e);
        }
    }

    /**
     * 初始化调度器路径
     *
     * @param acls
     * @param needACL
     */
    private void initialSchedulerPath(List<ACL> acls, boolean needACL) {

        try {
            // "/SkyWorldOnlineTask/Scheduler"
            String scheduler = new StringBuilder().append(buildTaskRoot()).append(ZookeeperConstant.ZK_ONLINE_SCHEDULER).toString();
            if (!this.curatorClient.isExists(scheduler)) {
                this.curatorClient.createPersistentZKNode(scheduler);
            }
            if (needACL) {
                this.curatorClient.getCuratorFramework().setACL().withACL(acls).forPath(scheduler);
            }
        } catch (Exception e) {
            log.error("initialExecutorPath exception:", e);
        }
    }

    /**
     * 初始化分布式锁路径
     *
     * @param acls
     * @param needACL
     */
    private void initialLockPath(List<ACL> acls, boolean needACL) {

        try {
            // "/ZK_ONLINE_ROOT_PATH/Lock"
            String lockPath = new StringBuilder().append(buildTaskRoot()).append(ZookeeperConstant.ZK_ONLINE_LOCK).toString();
            if (!this.curatorClient.isExists(lockPath)) {
                this.curatorClient.createPersistentZKNode(lockPath);
            }
            if (needACL) {
                this.curatorClient.getCuratorFramework().setACL().withACL(acls).forPath(lockPath);
            }

        } catch (Exception e) {
            log.error("initialExecutorPath exception:", e);
        }
    }

    /**
     * 初始化调度器下线（黑名单）路径
     *
     * @param acls
     * @param needACL
     */
    private void initialOfflinePath(List<ACL> acls, boolean needACL) {

        try {
            // "/SkyWorldOnlineTask/Offline"
            String offline = new StringBuilder().append(buildTaskRoot()).append(ZookeeperConstant.ZK_OFFLINE_SCHEDULER).toString();
            if (!this.curatorClient.isExists(offline)) {
                this.curatorClient.createPersistentZKNode(offline);
            }
            if (needACL) {
                this.curatorClient.getCuratorFramework().setACL().withACL(acls).forPath(offline);
            }
        } catch (Exception e) {
            log.error("initialExecutorPath exception:", e);
        }
    }

    /**
     * 调度器HTTP调用授权路径
     *
     * @param acls
     * @param needACL
     */
    private void initialAuthPath(List<ACL> acls, boolean needACL) {

        try {
            // "/ZK_ONLINE_ROOT_PATH/Schindler"
            String auth = new StringBuilder().append(buildTaskRoot()).append(ZookeeperConstant.ZK_ONLINE_AUTH).toString();
            if (!this.curatorClient.isExists(auth)) {
                this.curatorClient.createPersistentZKNode(auth);
            }
            if (needACL) {
                this.curatorClient.getCuratorFramework().setACL().withACL(acls).forPath(auth);
            }
        } catch (Exception e) {
            log.error("initialAuthPath exception:", e);
        }
    }

    /**
     * 获取调度器路径
     *
     * @param ipAndPort
     * @return
     */
    private String buildSchedulerPath(String ipAndPort) {

        boolean fail = StringHelper.isEmpty(ipAndPort) || !ipAndPort.contains(ZookeeperConstant.ZK_KEY_SPLIT);

        if (fail) {
            return ZookeeperConstant.ZK_UNKNOWN_PATH;
        }
        String path = new StringBuilder().append(buildTaskRoot()).append(ZookeeperConstant.ZK_ONLINE_SCHEDULER).append(ZookeeperConstant.ZK_SEPARATOR).append(ipAndPort).toString();
        return path;
    }

    /**
     * 检测路径是否无效
     *
     * @param path
     * @return
     */
    private boolean fakePath(String path) {

        return StringHelper.isEmpty(path) || ZookeeperConstant.ZK_UNKNOWN_PATH.equals(path);
    }

    /**
     * 调度器注册
     *
     * @param ipAndPort
     * @return
     */
    public boolean registerScheduler(String ipAndPort) {

        String path = buildSchedulerPath(ipAndPort);
        boolean fail = fakePath(path);

        if (fail) {
            return false;
        }
        return curatorClient.createEphemeralZKNode(path);

    }

    /**
     * 调度器注销
     *
     * @param ipAndPort
     * @return
     */
    public boolean unregisterScheduler(String ipAndPort) {

        String path = buildSchedulerPath(ipAndPort);
        boolean fail = fakePath(path);

        if (fail) {
            return false;
        }
        return curatorClient.deleteLeafZKNode(path);
    }

    /**
     * 更新调度器信息（负载均衡信息）
     *
     * @param ipAndPort
     * @param data
     * @return
     */
    public boolean updateScheduler(String ipAndPort, String data) {

        String path = buildSchedulerPath(ipAndPort);
        boolean fail = fakePath(path);

        if (fail) {
            return false;
        }
        return curatorClient.setData(path, data);

    }

    /**
     * 读取调度器信息
     *
     * @param ipAndPort
     * @return
     */
    public String getSchedulerInfo(String ipAndPort) {

        String path = buildSchedulerPath(ipAndPort);
        boolean fail = fakePath(path);

        if (fail) {
            return null;
        }
        return curatorClient.getData(path);

    }

    /**
     * 获取调度器列表
     *
     * @return
     */
    public List<String> getSchedulers() {

        String path = new StringBuilder().append(buildTaskRoot()).append(ZookeeperConstant.ZK_ONLINE_SCHEDULER).toString();
        return curatorClient.getChildren(path);
    }

    /**
     * 获取调度器下线路径（黑名单路径）
     *
     * @param ipAndPort
     * @return
     */
    private String buildOfflinePath(String ipAndPort) {

        boolean fail = StringHelper.isEmpty(ipAndPort) || !ipAndPort.contains(ZookeeperConstant.ZK_KEY_SPLIT);

        if (fail) {
            return ZookeeperConstant.ZK_UNKNOWN_PATH;
        }
        String path = new StringBuilder().append(buildTaskRoot()).append(ZookeeperConstant.ZK_OFFLINE_SCHEDULER).append(ZookeeperConstant.ZK_SEPARATOR).append(ipAndPort).toString();
        return path;
    }

    /**
     * 调度器下线（加入黑名单）
     *
     * @param ipAndPort
     * @return
     */
    public boolean closeScheduler(String ipAndPort) {

        String path = buildOfflinePath(ipAndPort);
        boolean fail = fakePath(path);

        if (fail) {
            return false;
        }
        return curatorClient.createPersistentZKNode(path);

    }

    /**
     * 调度器重新上线（从黑名单移除）
     *
     * @param ipAndPort
     * @return
     */
    public boolean openScheduler(String ipAndPort) {

        String path = buildOfflinePath(ipAndPort);
        boolean fail = fakePath(path);

        if (fail) {
            return false;
        }
        return curatorClient.deleteLeafZKNode(path);
    }

    /**
     * 获取下线的调度器列表（黑名单）
     *
     * @return
     */
    public List<String> getBlackList() {

        String path = new StringBuilder().append(buildTaskRoot()).append(ZookeeperConstant.ZK_OFFLINE_SCHEDULER).toString();
        return curatorClient.getChildren(path);
    }

    /**
     * 获取jobGroupName下的所有JobKey
     *
     * @param jobGroupName
     * @return
     */
    public List<String> getJobKeys(String jobGroupName) {

        boolean fail = StringHelper.isEmpty(jobGroupName);

        if (fail) {
            return null;
        }

        String path = new StringBuilder().append(buildTaskRoot()).append(ZookeeperConstant.ZK_ONLINE_JOB).append(ZookeeperConstant.ZK_SEPARATOR).append(jobGroupName).toString();
        return curatorClient.getChildren(path);

    }

    /**
     * 获取ZK上所有的JobKey
     *
     * @return
     */
    public List<String> getAllJobKeys() {

        String path = new StringBuilder().append(buildTaskRoot()).append(ZookeeperConstant.ZK_ONLINE_JOB).toString();
        List<String> jobGroupNames = curatorClient.getChildren(path);
        if (jobGroupNames == null) {
            return null;
        }

        List<String> allJobKeys = new LinkedList<String>();
        for (String jobGroupName : jobGroupNames) {
            List<String> jobKeys = getJobKeys(jobGroupName);
            if (jobKeys == null) {
                continue;
            }
            allJobKeys.addAll(jobKeys);
        }
        return allJobKeys;

    }

    /**
     * 获取JobKey路径
     *
     * @param jobGroupName
     * @param jobKey
     * @return
     */
    private String buildJobKeyPath(String jobGroupName, String jobKey) {

        boolean fail = StringHelper.isEmpty(jobGroupName) || StringHelper.isEmpty(jobKey);

        if (fail) {

            return ZookeeperConstant.ZK_UNKNOWN_PATH;
        }

        String path = new StringBuilder().append(buildTaskRoot()).append(ZookeeperConstant.ZK_ONLINE_JOB).append(ZookeeperConstant.ZK_SEPARATOR).append(jobGroupName).append(ZookeeperConstant.ZK_SEPARATOR).append(jobKey).toString();
        return path;
    }


    /**
     * 构建RunOnceJobKey路径
     *
     * @param jobKey
     * @return
     */
    private String buildRunOnceJobKeyPath(String jobKey) {

        boolean fail = StringHelper.isEmpty(jobKey);

        if (fail) {

            return ZookeeperConstant.ZK_UNKNOWN_PATH;
        }

        String path = new StringBuilder().append(buildTaskRoot()).append(ZookeeperConstant.ZK_ONLINE_JOB_RUNONCE).append(ZookeeperConstant.ZK_SEPARATOR).append(jobKey).toString();
        return path;
    }

    /**
     * 构建JobKey锁路径
     *
     * @param jobGroupName
     * @param jobKey
     * @return
     */
    private String buildJobKeyLock(String jobGroupName, String jobKey) {

        boolean fail = StringHelper.isEmpty(jobGroupName) || StringHelper.isEmpty(jobKey);

        if (fail) {

            return ZookeeperConstant.ZK_UNKNOWN_PATH;
        }

        String path = new StringBuilder().append(buildTaskRoot()).append(ZookeeperConstant.ZK_ONLINE_LOCK).append(ZookeeperConstant.ZK_SEPARATOR).append(jobGroupName).toString();
        return path;
    }

    /**
     * 创建JobStatus路径
     *
     * @param jobGroupName
     * @param jobKey
     * @return
     */
    private String buildJobStatusPath(String jobGroupName, String jobKey) {

        boolean fail = StringHelper.isEmpty(jobGroupName) || StringHelper.isEmpty(jobKey);

        if (fail) {

            return ZookeeperConstant.ZK_UNKNOWN_PATH;

        }
        String path = new StringBuilder().append(buildTaskRoot()).append(ZookeeperConstant.ZK_ONLINE_JOB).append(ZookeeperConstant.ZK_SEPARATOR).append(jobGroupName).append(ZookeeperConstant.ZK_SEPARATOR).append(jobKey).toString();
        return path;
    }

    /**
     * 创建JobStatus锁路径
     *
     * @param jobGroupName
     * @param jobKey
     * @return
     */
    private String buildJobStatusLock(String jobGroupName, String jobKey) {

        boolean fail = StringHelper.isEmpty(jobGroupName) || StringHelper.isEmpty(jobKey);
        if (fail) {
            return ZookeeperConstant.ZK_UNKNOWN_PATH;

        }
        String path = new StringBuilder().append(buildTaskRoot()).append(ZookeeperConstant.ZK_ONLINE_LOCK).append(ZookeeperConstant.ZK_SEPARATOR).append(jobGroupName).append(ZookeeperConstant.ZK_SEPARATOR).append(jobKey).toString();
        return path;
    }

    /**
     * 删除JobKey
     *
     * @param jobGroupName
     * @param jobKey
     * @return
     */
    public boolean deleteJobKey(String jobGroupName, String jobKey) {

        String path = buildJobKeyPath(jobGroupName, jobKey);
        boolean fail = fakePath(path);

        if (fail) {
            return false;

        }
        String lockPath = buildJobKeyLock(jobGroupName, jobKey);
        InterProcessMutex lock = null;
        try {
            lock = new InterProcessMutex(curatorClient.getCuratorFramework(), lockPath);
            if (lock.acquire(MAX_WAIT_SECONDS, TimeUnit.SECONDS)) {

                return curatorClient.deletePathZKNode(path);

            }
        } catch (Exception e) {
            log.error("acquire lock exception:", e);
        } finally {
            try {
                if (lock != null) {
                    lock.release();
                }
            } catch (Exception e) {
                log.error("release lock exception:", e);
            }
        }
        return false;
    }

    /**
     * 创建JobKey
     *
     * @param jobGroupName
     * @param jobKey
     * @return
     */
    public boolean createJobKey(String jobGroupName, String jobKey) {

        String path = buildJobKeyPath(jobGroupName, jobKey);
        boolean fail = fakePath(path);

        if (fail) {
            return false;

        }
        String lockPath = buildJobKeyLock(jobGroupName, jobKey);
        InterProcessMutex lock = null;
        try {
            lock = new InterProcessMutex(curatorClient.getCuratorFramework(), lockPath);
            if (lock.acquire(MAX_WAIT_SECONDS, TimeUnit.SECONDS)) {

                // 初始状态为STOP，需要激活
                return curatorClient.createPersistentZKNode(path, SiaJobStatus.STOP.toString());

            }
        } catch (Exception e) {
            log.error("acquire lock exception:", e);
        } finally {
            try {
                if (lock != null) {
                    lock.release();
                }
            } catch (Exception e) {
                log.error("release lock exception:", e);
            }
        }
        return false;
    }

    /**
     * 检查JobStatus是否符合
     *
     * @param status
     * @return
     */
    private boolean checkStatus(String status) {

        if (StringHelper.isEmpty(status)) {
            return false;
        }
        return SiaJobStatus.READY.toString().equals(status) || SiaJobStatus.PAUSE.toString().equals(status)
                || SiaJobStatus.RUNNING.toString().equals(status) || SiaJobStatus.STOP.toString().equals(status);
    }

    /**
     * 改变JobStatus的状态，调度器使用
     *
     * @param jobGroupName
     * @param jobKey
     * @param schedulerIPAndPort
     * @param oldStatus
     * @param newStatus
     * @return
     */
    public boolean casJobStatus4Scheduler(String jobGroupName, String jobKey, String schedulerIPAndPort,
                                          String oldStatus, String newStatus) {

        if (schedulerIPAndPort == null) {
            return false;
        }
        return casJobStatus(jobGroupName, jobKey, schedulerIPAndPort, oldStatus, newStatus);
    }

    /**
     * 改变JobStatus的状态，编排器使用
     *
     * @param jobGroupName
     * @param jobKey
     * @param oldStatus
     * @param newStatus
     * @return
     */
    public boolean casJobStatus4User(String jobGroupName, String jobKey, String oldStatus, String newStatus) {

        return casJobStatus(jobGroupName, jobKey, null, oldStatus, newStatus);
    }

    /**
     * 改变JobStatus的状态，CompareAndSwap操作
     *
     * @param jobGroupName
     * @param jobKey
     * @param schedulerIPAndPort
     * @param oldStatus
     * @param newStatus
     * @return
     */
    private boolean casJobStatus(String jobGroupName, String jobKey, String schedulerIPAndPort, String oldStatus,
                                 String newStatus) {

        String path = buildJobStatusPath(jobGroupName, jobKey);
        boolean fail = fakePath(path) || !checkStatus(oldStatus) || !checkStatus(newStatus);

        if (fail) {
            return false;

        }

        String lockPath = buildJobStatusLock(jobGroupName, jobKey);
        InterProcessMutex lock = null;
        try {
            lock = new InterProcessMutex(curatorClient.getCuratorFramework(), lockPath);
            if (lock.acquire(MAX_WAIT_SECONDS, TimeUnit.SECONDS)) {

                // do some work inside of the critical section here
                String currStatus = curatorClient.getData(path);
                // status match
                if (currStatus != null && currStatus.equals(oldStatus)) {
                    // do not check schedulerIPAndPort
                    if (schedulerIPAndPort == null) {
                        return curatorClient.setData(path, newStatus);
                    }
                    // validate schedulerIPAndPort
                    List<String> schedulers = curatorClient.getChildren(path);
                    if (schedulers == null) {
                        return false;
                    }
                    for (String scheduler : schedulers) {
                        if (scheduler.equals(schedulerIPAndPort)) {
                            return curatorClient.setData(path, newStatus);
                        }
                    }

                }

            }
        } catch (Exception e) {
            log.error("acquire lock exception:", e);
        } finally {
            try {
                if (lock != null) {
                    lock.release();
                }
            } catch (Exception e) {
                log.error("release lock exception:", e);
            }
        }
        return false;

    }

    /**
     * 获取JobKey的执行状态
     *
     * @param jobGroupName
     * @param jobKey
     * @return
     */
    public String getJobStatus(String jobGroupName, String jobKey) {

        String path = buildJobStatusPath(jobGroupName, jobKey);
        boolean fail = fakePath(path);

        if (fail) {
            return null;
        }

        return curatorClient.getData(path);
    }

    /**
     * 获取执行JobKey的调度器名称
     *
     * @param jobGroupName
     * @param jobKey
     * @return
     */
    public List<String> getJobScheduler(String jobGroupName, String jobKey) {

        String path = buildJobStatusPath(jobGroupName, jobKey);
        boolean fail = fakePath(path);

        if (fail) {
            return null;

        }
        return curatorClient.getChildren(path);
    }

    /**
     * 调度器抢占JobKey
     *
     * @param jobGroupName
     * @param jobKey
     * @param schedulerIPAndPort
     * @return
     */
    public boolean acquireJob(String jobGroupName, String jobKey, String schedulerIPAndPort) {

        String path = buildJobStatusPath(jobGroupName, jobKey);
        boolean fail = fakePath(path);

        if (fail) {
            return false;

        }

        String lockPath = buildJobStatusLock(jobGroupName, jobKey);
        InterProcessMutex lock = null;
        try {
            lock = new InterProcessMutex(curatorClient.getCuratorFramework(), lockPath);
            if (lock.acquire(MAX_WAIT_SECONDS, TimeUnit.SECONDS)) {

                // do some work inside of the critical section here
                List<String> schedulers = curatorClient.getChildren(path);
                // exception
                if (schedulers == null) {
                    return false;
                }
                // I am first
                if (schedulers.size() == 0) {
                    String schedulerPath = new StringBuilder().append(path).append(ZookeeperConstant.ZK_SEPARATOR).append(schedulerIPAndPort).toString();
                    return curatorClient.createFixedEphemeralZKNode(schedulerPath);
                }
                // others are already acquire

            }
        } catch (Exception e) {
            log.error("acquire lock exception:", e);
        } finally {
            try {
                if (lock != null) {
                    lock.release();
                }
            } catch (Exception e) {
                log.error("release lock exception:", e);
            }
        }
        return false;
    }

    /**
     * 调度器释放抢占的JobKey
     *
     * @param jobGroupName
     * @param jobKey
     * @param schedulerIPAndPort
     * @return
     */
    public boolean releaseJob(String jobGroupName, String jobKey, String schedulerIPAndPort) {

        String path = buildJobStatusPath(jobGroupName, jobKey);
        boolean fail = fakePath(path);

        if (fail) {
            return false;

        }

        String lockPath = buildJobStatusLock(jobGroupName, jobKey);
        InterProcessMutex lock = null;
        try {
            lock = new InterProcessMutex(curatorClient.getCuratorFramework(), lockPath);
            if (lock.acquire(MAX_WAIT_SECONDS, TimeUnit.SECONDS)) {

                // do some work inside of the critical section here
                List<String> schedulers = curatorClient.getChildren(path);
                // exception
                if (schedulers == null) {
                    return false;
                }

                for (String scheduler : schedulers) {
                    if (scheduler.equals(schedulerIPAndPort)) {
                        String schedulerPath = new StringBuilder().append(path).append(ZookeeperConstant.ZK_SEPARATOR).append(schedulerIPAndPort).toString();
                        return curatorClient.deleteLeafZKNode(schedulerPath);
                    }
                }

            }
        } catch (Exception e) {
            log.error("acquire lock exception:", e);
        } finally {
            try {
                if (lock != null) {
                    lock.release();
                }
            } catch (Exception e) {
                log.error("release lock exception:", e);
            }
        }
        return false;
    }

    /**
     * 编码httpPath（用于存储ZK）
     *
     * @param httpPath
     * @return
     */
    public String encodeHttpPath(String httpPath) {

        if (StringHelper.isEmpty(httpPath)) {
            return httpPath;
        }
        return httpPath.replace(ZookeeperConstant.HTTP_SEPARATOR, ZookeeperConstant.HTTP_MASK);
    }

    /**
     * 解码httpPath
     *
     * @param httpPath
     * @return
     */
    public String decodeHttpPath(String httpPath) {

        if (StringHelper.isEmpty(httpPath)) {
            return httpPath;
        }
        return httpPath.replace(ZookeeperConstant.HTTP_MASK, ZookeeperConstant.HTTP_SEPARATOR);
    }

    /**
     * 获取TaskKey路径
     *
     * @param taskKeyInDB
     * @return
     */
    private String buildTaskKeyPath(String taskKeyInDB) {

        boolean fail = StringHelper.isEmpty(taskKeyInDB) || !taskKeyInDB.contains(ZookeeperConstant.ZK_KEY_SPLIT)
                || !taskKeyInDB.contains(ZookeeperConstant.APP_SEPARATOR) || !taskKeyInDB.contains(ZookeeperConstant.HTTP_SEPARATOR)
                || taskKeyInDB.contains(ZookeeperConstant.HTTP_MASK);

        if (fail) {
            return ZookeeperConstant.ZK_UNKNOWN_PATH;

        }
        String[] groupAndApp = taskKeyInDB.split(ZookeeperConstant.ZK_KEY_SPLIT);
        if (groupAndApp.length < 2) {
            return ZookeeperConstant.ZK_UNKNOWN_PATH;
        }
        String taskApplicationName = groupAndApp[0];
        String taskGroupName = taskApplicationName.split(ZookeeperConstant.APP_SEPARATOR)[0];
        String taskHttpPath = groupAndApp[1];
        return buildTaskKeyPath(taskGroupName, taskApplicationName, taskHttpPath);
    }

    /**
     * 获取TaskKey路径
     *
     * @param taskGroupName
     * @param taskApplicationName
     * @param taskHttpPath
     * @return
     */
    private String buildTaskKeyPath(String taskGroupName, String taskApplicationName, String taskHttpPath) {

        boolean fail = StringHelper.isEmpty(taskGroupName) || StringHelper.isEmpty(taskApplicationName)
                || !taskApplicationName.contains(ZookeeperConstant.APP_SEPARATOR)
                || !taskApplicationName.startsWith(taskGroupName) || StringHelper.isEmpty(taskHttpPath)
                || !taskHttpPath.contains(ZookeeperConstant.HTTP_SEPARATOR) || taskHttpPath.contains(ZookeeperConstant.HTTP_MASK);

        if (fail) {

            return ZookeeperConstant.ZK_UNKNOWN_PATH;
        }
        String path = new StringBuilder().append(buildTaskRoot()).append(ZookeeperConstant.ZK_ONLINE_TASK).append(ZookeeperConstant.ZK_SEPARATOR).append(taskGroupName).append(ZookeeperConstant.ZK_SEPARATOR).
                append(taskApplicationName).append(ZookeeperConstant.ZK_SEPARATOR).append(taskApplicationName).append(ZookeeperConstant.ZK_KEY_SPLIT).append(encodeHttpPath(taskHttpPath)).toString();
        return path;
    }

    /**
     * 获取执行器列表
     *
     * @param taskKeyInDB
     * @return
     */
    public List<String> getExecutors(String taskKeyInDB) {

        String path = buildTaskKeyPath(taskKeyInDB);
        boolean fail = fakePath(path);

        if (fail) {
            return null;

        }
        return curatorClient.getChildren(path);
    }

    /**
     * 获取执行器列表
     *
     * @param taskGroupName
     * @param taskApplicationName
     * @param taskHttpPath
     * @return
     */
    public List<String> getExecutors(String taskGroupName, String taskApplicationName, String taskHttpPath) {

        String path = buildTaskKeyPath(taskGroupName, taskApplicationName, taskHttpPath);
        boolean fail = fakePath(path);

        if (fail) {
            return null;

        }
        return curatorClient.getChildren(path);
    }

    /**
     * 获取调度器授权路径（白名单路径）
     *
     * @param ip
     * @return
     */
    private String buildAuthPath(String ip) {

        boolean fail = StringHelper.isEmpty(ip);

        if (fail) {
            return ZookeeperConstant.ZK_UNKNOWN_PATH;
        }
        String path = new StringBuilder().append(buildTaskRoot()).append(ZookeeperConstant.ZK_ONLINE_AUTH).append(ZookeeperConstant.ZK_SEPARATOR).append(ip).toString();
        return path;
    }

    /**
     * 调度器授权（加入白名单）
     *
     * @param ip
     * @return
     */
    public boolean addToAuth(String ip) {

        String path = buildAuthPath(ip);
        boolean fail = fakePath(path);

        if (fail) {
            return false;
        }
        return curatorClient.createPersistentZKNode(path);
    }

    /**
     * 取消调度器授权（从白名单移除）
     *
     * @param ip
     * @return
     */
    public boolean removeFromAuth(String ip) {

        String path = buildAuthPath(ip);
        boolean fail = fakePath(path);

        if (fail) {
            return false;
        }
        return curatorClient.deleteLeafZKNode(path);
    }

    /**
     * 获取调度器授权列表（白名单）
     *
     * @return
     */
    public List<String> getAuthList() {

        String path = new StringBuilder().append(buildTaskRoot()).append(ZookeeperConstant.ZK_ONLINE_AUTH).toString();
        return curatorClient.getChildren(path);
    }

    /**
     * 删除ZK上注册的TaskKey
     *
     * @param taskKeyInDB
     * @return
     */
    public boolean deleteTaskKey(String taskKeyInDB) {

        String path = buildTaskKeyPath(taskKeyInDB);
        boolean fail = fakePath(path);

        // 路径不存在，当然不可能删除成功
        if (fail) {
            return false;

        }
        List<String> children = curatorClient.getChildren(path);
        if (null != children && children.isEmpty()) {
            return curatorClient.deleteLeafZKNode(path);
        }
        // 执行器不为空，不能删除
        return false;
    }

    /**
     * 删除ZK上注册的TaskKey
     *
     * @param taskGroupName
     * @param taskApplicationName
     * @param taskHttpPath
     * @return
     */
    public boolean deleteTaskKey(String taskGroupName, String taskApplicationName, String taskHttpPath) {

        String path = buildTaskKeyPath(taskGroupName, taskApplicationName, taskHttpPath);
        boolean fail = fakePath(path);

        // 路径不存在，当然不可能删除成功
        if (fail) {
            return false;

        }
        List<String> children = curatorClient.getChildren(path);
        if (null != children && children.isEmpty()) {
            return curatorClient.deleteLeafZKNode(path);
        }
        // 执行器不为空，不能删除
        return false;
    }


    /**
     * 初始化JobTransfer路径
     *
     * @param acls
     * @param needACL
     */
    private void initialJobTransferPath(List<ACL> acls, boolean needACL) {

        try {
            // "/SkyWorldOnlineTask/JobTransfer"
            String JobTransfer = new StringBuilder().append(buildTaskRoot()).append(ZookeeperConstant.ZK_ONLINE_JOBTRANSFER).toString();
            if (!this.curatorClient.isExists(JobTransfer)) {
                this.curatorClient.createPersistentZKNode(JobTransfer);
            }
            if (needACL) {
                this.curatorClient.getCuratorFramework().setACL().withACL(acls).forPath(JobTransfer);
            }
        } catch (Exception e) {
            log.error("initialJobTransferPath exception:", e);
        }
    }

    /**
     * 一键批量转移Job注册
     *
     * @param ipAndPort
     * @return
     */
    public boolean registerJobTransfer(String ipAndPort) {

        String path = buildJobTransferPath(ipAndPort);
        boolean fail = fakePath(path);

        if (fail) {
            return false;
        }
        return curatorClient.createEphemeralZKNode(path);

    }


    /**
     * 获取Job一键转移JobTransfer路径
     *
     * @param ipAndPort
     * @return
     */
    private String buildJobTransferPath(String ipAndPort) {

        boolean fail = StringHelper.isEmpty(ipAndPort) || !ipAndPort.contains(ZookeeperConstant.ZK_KEY_SPLIT);

        if (fail) {
            return ZookeeperConstant.ZK_UNKNOWN_PATH;
        }
        String path = new StringBuilder().append(buildTaskRoot()).append(ZookeeperConstant.ZK_ONLINE_JOBTRANSFER).append(ZookeeperConstant.ZK_SEPARATOR).append(ipAndPort).toString();
        return path;
    }

    /**
     * 更新Job一键转移信息
     *
     * @param ipAndPort
     * @param data
     * @return
     */
    public boolean updateJobTransfer(String ipAndPort, String data) {

        String path = buildJobTransferPath(ipAndPort);
        boolean fail = fakePath(path);

        if (fail) {
            return false;
        }
        return curatorClient.setData(path, data);

    }

    /**
     * 读取Job一键转移信息
     *
     * @param ipAndPort
     * @return
     */
    public String getJobTransferInfo(String ipAndPort) {

        String path = buildJobTransferPath(ipAndPort);
        boolean fail = fakePath(path);

        if (fail) {
            return null;
        }
        return curatorClient.getData(path);

    }

    /**
     * 初始化JobRunOnce路径
     *
     * @param acls
     * @param needACL
     */
    private void initialJobRunOncePath(List<ACL> acls, boolean needACL) {

        try {
            // "/SkyWorldOnlineTask/JobRunOnce"
            String JobRunOnce = new StringBuilder().append(buildTaskRoot()).append(ZookeeperConstant.ZK_ONLINE_JOB_RUNONCE).toString();
            if (!this.curatorClient.isExists(JobRunOnce)) {
                this.curatorClient.createPersistentZKNode(JobRunOnce);
            }
            if (needACL) {
                this.curatorClient.getCuratorFramework().setACL().withACL(acls).forPath(JobRunOnce);
            }
        } catch (Exception e) {
            log.error("initialJobRunOncePath exception:", e);
        }
    }

    /**
     * 创建RunOnceJobKey
     *
     * @param jobKey
     * @return
     */
    public boolean createRunOnceJobKey(String jobKey) {

        String path = buildRunOnceJobKeyPath(jobKey);
        boolean fail = fakePath(path);

        if (fail) {
            return false;

        }
        return curatorClient.createPersistentZKNode(path, SiaJobStatus.READY.toString());
    }

    /**
     * 删除RunOnceJobKey
     *
     * @param jobKey
     * @return
     */
    public boolean deleteRunOnceJobKey(String jobKey) {

        String path = buildRunOnceJobKeyPath(jobKey);
        boolean fail = fakePath(path);

        if (fail) {
            return false;

        }
        return curatorClient.deletePathZKNode(path);
    }

    //-------------------------新增ZKAPI----------------------------------

    /**
     * 根据path获取对应zk树。
     *
     * @return zk tree json
     */
    @Deprecated
    public ZkNode loadZkTreeOfPath(String path) throws Exception {
        ZkNode root = initialTreeOfPath(path, false);
        ZkNode lastCommonParent = getLastCommonParent(root);
        lastCommonParent.setChildren(loadChildrenRecursively(lastCommonParent));
        log.info("loadZkTreeOfPath request process ok. path:{}", path);
        return root;
    }

    /**
     * 根据path获取对应节点的zk子树。
     *
     * @return zk tree json
     */
    public List<ZkNode> loadSubTreeOfPath(String path) throws Exception {
        ZkNode root = initialTreeOfPath(path, false);
        ZkNode lastCommonParent = getLastCommonParent(root);
        List<ZkNode> children = loadChildrenRecursively(lastCommonParent);
        log.info("loadSubTreeOfPath request process ok. path:{}", path);
        return children;
    }

    /**
     * 根据path获取对应节点的zk子树。
     * 每次获取三层目录
     *
     * @return zk tree json
     */
    public ZkNode loadLevelOfPath(String path) throws Exception {
        if (!curatorClient.isExists(path)) {
            return null;
        }
        ZkNode nodeOfPath = new ZkNode();
        nodeOfPath.setPath(path);
        nodeOfPath.setName(path.substring(path.lastIndexOf("/")));
        nodeOfPath.setChildren(loadNodeRecursively(nodeOfPath, 0));
        nodeOfPath.setStat(curatorClient.getStat(path));
        nodeOfPath.setAcl(curatorClient.getACL(path));
        log.info("loadLevelOfPath request process ok. path:{}", path);
        return nodeOfPath;
    }

    /**
     * 根据path,添加新节点到对应zk树。
     *
     * @return
     */
    public void addZkNodeOfPath(String path, String nodeName, String nodeData) throws Exception {
        String newNodePath = path + "/" + nodeName;
        curatorClient.createNode(newNodePath, nodeData.getBytes(ZookeeperConstant.UTF8));
        log.info("addZkNodeOfPath request process ok. path:{} , nodeName:{}", path, nodeName);
    }

    /**
     * 根据path,删除对应zk节点。
     *
     * @return
     */
    public String deleteZkNodeOfPath(String path) throws Exception {
        String s = deleteNodeRecursively(path);
        if (s != null) {
            return s;
        }
        log.info("deleteZkNodeOfPath request process ok. path:{} ", path);
        return null;
    }

    /**
     * 根据path，生成（公共祖先的）父子关系链
     *
     * @param pathParam
     * @param createIfNotExists
     * @return
     */
    private ZkNode initialTreeOfPath(String pathParam, boolean createIfNotExists) throws Exception {
        //我们的地址是 /SkyWorldOnlineTask/xxx的形式。需要对第一个 / 进行特殊处理,生成其代表的根节点
        ZkNode root = new ZkNode("/");
        root.setPath("/");
        root.setExisted(true);
        // 根据给定的路径前缀，生成父子关系链，直至给定路径对应的最后一级的节点（最后一个公共父节点）
        String[] pathArr = pathParam.split("/");
        ZkNode parent = root;
        if (pathArr.length > 0) {
            for (String nodeName : pathArr) {
                if (!StringHelper.isEmpty(nodeName)) {
                    ZkNode node = new ZkNode(nodeName);
                    String curPath = parent.getPath() + "/" + nodeName;
                    if (parent.getPath().equals("/")) { //我们的地址是 /SkyWorldOnlineTask/xxx的形式。需要对第一个 / 进行特殊处理.
                        curPath = parent.getPath() + nodeName;
                    }
                    node.setPath(curPath);
                    boolean nodeExisted = curatorClient.isExists(curPath);
                    if (!nodeExisted && createIfNotExists) {
                        curatorClient.createNode(curPath, null);
                        nodeExisted = true;

                    }
                    node.setExisted(curatorClient.isExists(curPath));
                    if (nodeExisted) {
                        String data = curatorClient.getData(curPath);
                        if (!StringHelper.isEmpty(data)) {
                            node.setContent(data);
                        }
                    }
                    List<ZkNode> children = new ArrayList<>();
                    children.add(node);
                    parent.setChildren(children);
                    parent = node;
                }
            }
        }
        return root;
    }

    /**
     * @param parent
     * @return
     */
    private ZkNode getLastCommonParent(ZkNode parent) {
        ZkNode lastCommonParent = parent;
        lastCommonParent.setToggled(true);
        List<ZkNode> children = lastCommonParent.getChildren();
        if (children == null || children.size() == 0) {
            return lastCommonParent;
        } else {
            // 这个方法只在“公共祖先链（每层只有一个节点）”的情况下使用，所以直接get(0)即可。
            lastCommonParent = children.get(0);
            return getLastCommonParent(lastCommonParent);
        }
    }

    /**
     * 获取父节点下所有子树
     *
     * @param parent
     * @return
     */
    private List<ZkNode> loadChildrenRecursively(ZkNode parent) {
        List<ZkNode> childrenNodes = new ArrayList();
        List<String> children = null;
        try {
            children = curatorClient.getChildren(parent.getPath());
            if (children != null && children.size() > 0) {
                for (String nodeName : children) {
                    ZkNode node = new ZkNode(nodeName);
                    String path = parent.getPath() + "/" + nodeName;
                    if (parent.getPath().equals("/")) { //我们的地址是 /SkyWorldOnlineTask/xxx的形式。需要对第一个 / 进行特殊处理.
                        path = parent.getPath() + nodeName;
                    }
                    node.setPath(path);
                    String data = curatorClient.getData(path);
                    if (!StringHelper.isEmpty(data)) {
                        node.setContent(data);
                    }
                    node.setExisted(true);
                    childrenNodes.add(node);
                }
                parent.setChildren(childrenNodes);
                for (ZkNode node : childrenNodes) {
                    loadChildrenRecursively(node);
                }
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return childrenNodes;
    }


    private List<ZkNode> loadNodeRecursively(ZkNode nodeOfPath, int level) throws Exception {
        List<ZkNode> childrenNode = new ArrayList();
        try {
            List<String> children = null;
            String path = nodeOfPath.getPath();
            if (!curatorClient.isExists(path)) {
                log.info("节点:{}不存在.", path);
                return null;
            }
            children = curatorClient.getChildren(path);
            if (children != null && children.size() > 0) {
                Collections.sort(children, (s1, s2) -> s1.compareToIgnoreCase(s2));
                for (String child : children) {
                    ZkNode node = new ZkNode(child);
                    String childPath = null;
                    if (path.equals("/")) {
                        childPath = path + child;
                    } else {
                        childPath = path + "/" + child;
                    }
                    node.setPath(childPath);
                    node.setStat(curatorClient.getStat(path));
                    node.setAcl(curatorClient.getACL(path));
                    childrenNode.add(node);
                    if (level > 0) {
                        loadNodeRecursively(node, level - 1);
                    }
                }
                nodeOfPath.setChildren(childrenNode);
            }
            return childrenNode;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return childrenNode;
        }
    }


    /**
     * 递归删除给定路径的zk结点
     */
    private String deleteNodeRecursively(String path) throws Exception {
        List<String> children = null;
        //新增判空操作，删除一个不存在的节点时会报空指针。。。
        children = curatorClient.getChildren(path);
        String result = null;
        if (children != null && children.size() > 0) {
            for (String child : children) {
                String childPath = null;
                if (path.equals("/")) {
                    childPath = path + child;
                } else {
                    childPath = path + "/" + child;
                }
                result = deleteNodeRecursively(childPath);
            }
        }
        if (curatorClient.getChildren(path) != null && curatorClient.getChildren(path).size() > 0) {
            return result;
        }
        curatorClient.deleteNode(path);
        return null;
    }

    public ZkNode loadNodeJson(String path) throws Exception {
        if (!curatorClient.isExists(path)) {
            return null;
        }
        String data = curatorClient.getData(path);
        ZkNode zkNode = new ZkNode();
        zkNode.setStat(curatorClient.getStat(path));
        zkNode.setAcl(curatorClient.getACL(path));
        zkNode.setPath(path);
        if (!StringHelper.isEmpty(data)) {
            zkNode.setContent(data);
        }
        return zkNode;
    }

    public void updateNodeJson(String path, String content) throws Exception {

        curatorClient.setData(path, content.getBytes(ZookeeperConstant.UTF8));
    }

}
