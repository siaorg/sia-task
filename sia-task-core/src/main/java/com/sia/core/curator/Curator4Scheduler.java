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

import com.sia.core.constant.Constant;
import com.sia.core.helper.StringHelper;
import com.sia.core.status.JobStatus;
import org.apache.curator.framework.recipes.locks.InterProcessMutex;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.data.ACL;
import org.apache.zookeeper.data.Id;
import org.apache.zookeeper.server.auth.DigestAuthenticationProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 *
 *
 * @description encapsule CuratorClient and offer API to scheduler
 * @see
 * @author pengfeili23
 * @date 2019-02-20 15:29:20
 * @version V1.0.0
 **/
public class Curator4Scheduler {

    private static final Logger LOGGER = LoggerFactory.getLogger(Curator4Scheduler.class);

    private CuratorClient curatorClient;
    private String taskRoot;
    private String allAuth;
    private String createAuth;
    private String digest;


    /**
     *
     * acquire the zk operation class CuratorClient
     * {@link } can be checked for the result.
     * @param
     * @return
     * @throws
     */
    public CuratorClient getCuratorClient() {

        return curatorClient;
    }

    /**
     *
     * build zk root path
     * {@link } can be checked for the result.
     * @param
     * @return the root path of zk
     * @throws
     */
    private String buildTaskRoot(){
        StringBuilder taskPath = new StringBuilder().append(Constant.ZK_ROOT).append(taskRoot).append(Constant.ZK_SEPARATOR);
        return taskPath.toString();
    }

    public String getTaskRoot(){
        return taskRoot;
    }

    public String getAllAuth(){
        return allAuth;
    }

    public String getCreateAuth(){
        return createAuth;
    }

    public String getDigest(){
        return digest;
    }

    /**
     *
     * constructor of Curator4Scheduler
     * {@link } can be checked for the result.
     * @param
     * @return
     * @throws
     */
    public Curator4Scheduler(CuratorClient curatorClient,String taskRoot,String digest,String allAuth,String createAuth , boolean needACL) {

        this.curatorClient = curatorClient;
        this.taskRoot = taskRoot;
        this.digest = digest;
        this.allAuth = allAuth;
        this.createAuth = createAuth;
        inital(needACL);
    }

    /**
     *
     * constructor of Curator4Scheduler
     * {@link } can be checked for the result.
     * @param
     * @return
     * @throws
     */
    public Curator4Scheduler(String zkAddress,String taskRoot,String digest,String allAuth,String createAuth, boolean needACL) {

        this.curatorClient = new CuratorClient(zkAddress);
        this.taskRoot=taskRoot;
        this.digest = digest;
        this.allAuth = allAuth;
        this.createAuth = createAuth;
        inital(needACL);
    }

    /**
     *
     * init: authorize and initialize zk path
     * {@link } can be checked for the result.
     * @param needACL: need ACL or not
     * @return
     * @throws
     */
    private void inital(boolean needACL) {

        List<ACL> allAndCreate = new ArrayList<ACL>();
        List<ACL> onlyAll = new ArrayList<ACL>();
        List<ACL> allAndCreateAndRead = new ArrayList<ACL>();
        try {
            ACL all = new ACL(ZooDefs.Perms.ALL,
                    new Id(digest, DigestAuthenticationProvider.generateDigest(allAuth)));
            ACL create = new ACL(ZooDefs.Perms.CREATE,
                    new Id(digest, DigestAuthenticationProvider.generateDigest(createAuth)));
            ACL read = new ACL(ZooDefs.Perms.READ,
                    new Id(digest, DigestAuthenticationProvider.generateDigest(createAuth)));
            allAndCreate.add(all);
            allAndCreate.add(create);
            onlyAll.add(all);
            allAndCreateAndRead.add(all);
            allAndCreateAndRead.add(create);
            allAndCreateAndRead.add(read);
        } catch (Exception e) {
            LOGGER.error("create ACL exception:", e);
        }
        /**
         * 自己先认证，所有权限。
         * <p>
         * TODO: 注意：此处会触发所有路径的子节点创建事件！！！
         */
        this.curatorClient.addAllAuth(digest,allAuth);
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
        initialAuthPath(allAndCreateAndRead, needACL);

    }

    /**
     *
     * init: initialize zk root path
     * {@link } can be checked for the result.
     * @param acls: authorized list; needACL: need ACL or not
     * @return
     * @throws
     */
    private void initialOnlineRootPath(List<ACL> acls, boolean needACL) {

        try {

            // "/ZK_ONLINE_ROOT_PATH"
            String onlineRoot = new StringBuilder().append(Constant.ZK_ROOT).append(taskRoot).toString();
            if (!this.curatorClient.isExists(onlineRoot)) {
                this.curatorClient.createPersistentZKNode(onlineRoot);
            }
            if (needACL) {
                this.curatorClient.getCuratorFramework().setACL().withACL(acls).forPath(onlineRoot);
            }
        } catch (Exception e) {
            LOGGER.error("initialOnlineRootPath exception:", e);
        }

    }

    /**
     *
     * init: initialize zk task path
     * {@link } can be checked for the result.
     * @param
     * @return
     * @throws
     */
    private void initialTaskPath(List<ACL> acls, boolean needACL) {

        try {
            // "/SkyWorldOnlineTask/Task"
            String task = new StringBuilder().append(buildTaskRoot()).append(Constant.ZK_ONLINE_TASK).toString();
            if (!this.curatorClient.isExists(task)) {
                this.curatorClient.createPersistentZKNode(task);
            }
            if (needACL) {
                this.curatorClient.getCuratorFramework().setACL().withACL(acls).forPath(task);
            }
        } catch (Exception e) {
            LOGGER.error("initialTaskPath exception:", e);
        }

    }

    /**
     *
     * init: initialize zk Job path
     * {@link } can be checked for the result.
     * @param
     * @return
     * @throws
     */
    private void initialJobPath(List<ACL> acls, boolean needACL) {

        try {
            // "/ZK_ONLINE_ROOT_PATH/Job"
            String job = new StringBuilder().append(buildTaskRoot()).append(Constant.ZK_ONLINE_JOB).toString();
            if (!this.curatorClient.isExists(job)) {
                this.curatorClient.createPersistentZKNode(job);
            }
            if (needACL) {
                this.curatorClient.getCuratorFramework().setACL().withACL(acls).forPath(job);
            }
        } catch (Exception e) {
            LOGGER.error("initialJobPath exception:", e);
        }
    }

    /**
     *
     * init: initialize zk scheduler path
     * {@link } can be checked for the result.
     * @param
     * @return
     * @throws
     */
    private void initialSchedulerPath(List<ACL> acls, boolean needACL) {

        try {
            // "/SkyWorldOnlineTask/Scheduler"
            String scheduler =  new StringBuilder().append(buildTaskRoot()).append(Constant.ZK_ONLINE_SCHEDULER).toString();
            if (!this.curatorClient.isExists(scheduler)) {
                this.curatorClient.createPersistentZKNode(scheduler);
            }
            if (needACL) {
                this.curatorClient.getCuratorFramework().setACL().withACL(acls).forPath(scheduler);
            }
        } catch (Exception e) {
            LOGGER.error("initialExecutorPath exception:", e);
        }
    }

    /**
     *
     * init: initialize zk distributed-lock path
     * {@link } can be checked for the result.
     * @param
     * @return
     * @throws
     */
    private void initialLockPath(List<ACL> acls, boolean needACL) {

        try {
            // "/ZK_ONLINE_ROOT_PATH/Lock"
            String lockPath = new StringBuilder().append(buildTaskRoot()).append(Constant.ZK_ONLINE_LOCK).toString();
            if (!this.curatorClient.isExists(lockPath)) {
                this.curatorClient.createPersistentZKNode(lockPath);
            }
            if (needACL) {
                this.curatorClient.getCuratorFramework().setACL().withACL(acls).forPath(lockPath);
            }

        } catch (Exception e) {
            LOGGER.error("initialExecutorPath exception:", e);
        }
    }

    /**
     *
     * init: initialize zk offline-scheduler path
     * {@link } can be checked for the result.
     * @param
     * @return
     * @throws
     */
    private void initialOfflinePath(List<ACL> acls, boolean needACL) {

        try {
            // "/SkyWorldOnlineTask/Offline"
            String offline =  new StringBuilder().append(buildTaskRoot()).append(Constant.ZK_OFFLINE_SCHEDULER).toString();
            if (!this.curatorClient.isExists(offline)) {
                this.curatorClient.createPersistentZKNode(offline);
            }
            if (needACL) {
                this.curatorClient.getCuratorFramework().setACL().withACL(acls).forPath(offline);
            }
        } catch (Exception e) {
            LOGGER.error("initialExecutorPath exception:", e);
        }
    }

    /**
     *
     * init: initialize zk ip-auth path in http call
     * {@link } can be checked for the result.
     * @param
     * @return
     * @throws
     */
    private void initialAuthPath(List<ACL> acls, boolean needACL) {

        try {
            // "/ZK_ONLINE_ROOT_PATH/Schindler"
            String auth = new StringBuilder().append(buildTaskRoot()).append(Constant.ZK_ONLINE_AUTH).toString();
            if (!this.curatorClient.isExists(auth)) {
                this.curatorClient.createPersistentZKNode(auth);
            }
            if (needACL) {
                this.curatorClient.getCuratorFramework().setACL().withACL(acls).forPath(auth);
            }
        } catch (Exception e) {
            LOGGER.error("initialAuthPath exception:", e);
        }
    }

    /**
     *
     * acquire scheduler path
     * {@link } can be checked for the result.
     * @param ipAndPort: ip and port of scheduler
     * @return zk path of scheduler
     * @throws
     */
    private String buildSchedulerPath(String ipAndPort) {

        boolean fail = StringHelper.isEmpty(ipAndPort) || !ipAndPort.contains(Constant.ZK_KEY_SPLIT);

        if (fail) {
            return Constant.ZK_UNKNOWN_PATH;
        }
        String path = new StringBuilder().append(buildTaskRoot()).append(Constant.ZK_ONLINE_SCHEDULER).append(Constant.ZK_SEPARATOR).append(ipAndPort).toString();
        return path;
    }

    /**
     *
     * check whether the zk path is valid
     * {@link } can be checked for the result.
     * @param path: zk path
     * @return true: zk path is valid; false: zk path is not valid
     * @throws
     */
    private boolean fakePath(String path) {

        return StringHelper.isEmpty(path) || Constant.ZK_UNKNOWN_PATH.equals(path);
    }

    /**
     *
     * scheduler registered to zk
     * {@link } can be checked for the result.
     * @param ipAndPort: ip and port of scheduler
     * @return true/false
     * @throws
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
     *
     * scheduler unregistered from zk
     * {@link } can be checked for the result.
     * @param ipAndPort: ip and port of scheduler
     * @return true/false
     * @throws
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
     *
     * update the load-balance info of scheduler
     * {@link } can be checked for the result.
     * @param ipAndPort: ip and port of scheduler; data: (MY_JOB_NUM, ALARM_JOB_NUM, MAX_JOB_NUM)
     * @return true/false
     * @throws
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
     *
     * acquire scheduler info
     * {@link } can be checked for the result.
     * @param ipAndPort: ip and port of scheduler
     * @return MY_JOB_NUM, ALARM_JOB_NUM, MAX_JOB_NUM
     * @throws
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
     *
     * acquire scheduler list
     * {@link } can be checked for the result.
     * @param
     * @return list of schedulers
     * @throws
     */
    public List<String> getSchedulers() {

        String path = new StringBuilder().append(buildTaskRoot()).append(Constant.ZK_ONLINE_SCHEDULER).toString();
        return curatorClient.getChildren(path);
    }

    /**
     *
     * build offline-scheduler zk path(black-list zk path)
     * {@link } can be checked for the result.
     * @param ipAndPort: ip and port of scheduler
     * @return offline-scheduler zk path
     * @throws
     */
    private String buildOfflinePath(String ipAndPort) {

        boolean fail = StringHelper.isEmpty(ipAndPort) || !ipAndPort.contains(Constant.ZK_KEY_SPLIT);

        if (fail) {
            return Constant.ZK_UNKNOWN_PATH;
        }
        String path = new StringBuilder().append(buildTaskRoot()).append(Constant.ZK_OFFLINE_SCHEDULER).append(Constant.ZK_SEPARATOR).append(ipAndPort).toString();
        return path;
    }

    /**
     *
     * make scheduler offline
     * {@link } can be checked for the result.
     * @param ipAndPort: ip and port of scheduler
     * @return true/false
     * @throws
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
     *
     * make scheduler upline
     * {@link } can be checked for the result.
     * @param ipAndPort: ip and port of scheduler
     * @return true/false
     * @throws
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
     *
     * acquire list of offline schedulers
     * {@link } can be checked for the result.
     * @param
     * @return list of offline schedulers
     * @throws
     */
    public List<String> getBlackList() {

        String path = new StringBuilder().append(buildTaskRoot()).append(Constant.ZK_OFFLINE_SCHEDULER).toString();
        return curatorClient.getChildren(path);
    }

    /**
     *
     * acquire all jobKeys in jobGroupName
     * {@link } can be checked for the result.
     * @param jobGroupName
     * @return list of jobKeys
     * @throws
     */
    public List<String> getJobKeys(String jobGroupName) {

        boolean fail = StringHelper.isEmpty(jobGroupName);

        if (fail) {
            return null;
        }

        String path = new StringBuilder().append(buildTaskRoot()).append(Constant.ZK_ONLINE_JOB).append(Constant.ZK_SEPARATOR).append(jobGroupName).toString();
        return curatorClient.getChildren(path);

    }

    /**
     *
     * acquire all jobKeys in zk
     * {@link } can be checked for the result.
     * @param
     * @return list of jobKeys
     * @throws
     */
    public List<String> getAllJobKeys() {

        String path = new StringBuilder().append(buildTaskRoot()).append(Constant.ZK_ONLINE_JOB).toString();
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
     *
     * build zk path of jobKey
     * {@link } can be checked for the result.
     * @param jobGroupName, jobKey
     * @return zk path of jobKey
     * @throws
     */
    private String buildJobKeyPath(String jobGroupName, String jobKey) {

        boolean fail = StringHelper.isEmpty(jobGroupName) || StringHelper.isEmpty(jobKey);

        if (fail) {

            return Constant.ZK_UNKNOWN_PATH;
        }

        String path = new StringBuilder().append(buildTaskRoot()).append(Constant.ZK_ONLINE_JOB).append(Constant.ZK_SEPARATOR).append(jobGroupName).append(Constant.ZK_SEPARATOR).append(jobKey).toString();
        return path;
    }

    /**
     *
     * build zk distributed-lock path of jobKey
     * {@link } can be checked for the result.
     * @param jobGroupName, jobKey
     * @return zk distributed-lock path of jobKey
     * @throws
     */
    private String buildJobKeyLock(String jobGroupName, String jobKey) {

        boolean fail = StringHelper.isEmpty(jobGroupName) || StringHelper.isEmpty(jobKey);

        if (fail) {

            return Constant.ZK_UNKNOWN_PATH;
        }

        String path = new StringBuilder().append(buildTaskRoot()).append(Constant.ZK_ONLINE_LOCK).append(Constant.ZK_SEPARATOR ).append(jobGroupName).toString();
        return path;
    }

    /**
     *
     * delete jobKey in zk
     * {@link } can be checked for the result.
     * @param jobGroupName, jobKey
     * @return true/false
     * @throws
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
            if (lock.acquire(Constant.MAX_WAIT_SECONDS, TimeUnit.SECONDS)) {

                return curatorClient.deletePathZKNode(path);

            }
        } catch (Exception e) {
            LOGGER.error("acquire lock exception:", e);
        } finally {
            try {
                if (lock != null) {
                    lock.release();
                }
            } catch (Exception e) {
                LOGGER.error("release lock exception:", e);
            }
        }
        return false;
    }

    /**
     *
     * create jobKey in zk
     * {@link } can be checked for the result.
     * @param jobGroupName, jobKey
     * @return true/false
     * @throws
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
            if (lock.acquire(Constant.MAX_WAIT_SECONDS, TimeUnit.SECONDS)) {

                // 初始状态为STOP，需要激活
                return curatorClient.createPersistentZKNode(path, JobStatus.STOP.toString());

            }
        } catch (Exception e) {
            LOGGER.error("acquire lock exception:", e);
        } finally {
            try {
                if (lock != null) {
                    lock.release();
                }
            } catch (Exception e) {
                LOGGER.error("release lock exception:", e);
            }
        }
        return false;
    }

    /**
     *
     * build zk jobStatus path
     * {@link } can be checked for the result.
     * @param jobGroupName, jobKey
     * @return zk jobStatus path
     * @throws
     */
    private String buildJobStatusPath(String jobGroupName, String jobKey) {

        boolean fail = StringHelper.isEmpty(jobGroupName) || StringHelper.isEmpty(jobKey);

        if (fail) {

            return Constant.ZK_UNKNOWN_PATH;

        }
        String path = new StringBuilder().append(buildTaskRoot()).append(Constant.ZK_ONLINE_JOB).append(Constant.ZK_SEPARATOR ).append(jobGroupName).append(Constant.ZK_SEPARATOR).append(jobKey).toString();
        return path;
    }

    /**
     *
     * build zk distributed-lock path of jobStatus
     * {@link } can be checked for the result.
     * @param jobGroupName, jobKey
     * @return zk distributed-lock path of jobStatus
     * @throws
     */
    public String buildJobStatusLock(String jobGroupName, String jobKey) {

        boolean fail = StringHelper.isEmpty(jobGroupName) || StringHelper.isEmpty(jobKey);
        if (fail) {
            return Constant.ZK_UNKNOWN_PATH;

        }
        String path = new StringBuilder().append(buildTaskRoot()).append(Constant.ZK_ONLINE_LOCK).append(Constant.ZK_SEPARATOR).append(jobGroupName).append(Constant.ZK_SEPARATOR).append(jobKey).toString();
        return path;
    }

    /**
     *
     * check whether the job status is valid
     * {@link } can be checked for the result.
     * @param status
     * @return true/false
     * @throws
     */
    private boolean checkStatus(String status) {

        if (StringHelper.isEmpty(status)) {
            return false;
        }
        return JobStatus.READY.toString().equals(status) || JobStatus.PAUSE.toString().equals(status)
                || JobStatus.RUNNING.toString().equals(status) || JobStatus.STOP.toString().equals(status);
    }

    /**
     *
     * change job status; used by scheduler
     * {@link } can be checked for the result.
     * @param
     * @return true/false
     * @throws
     */
    public boolean casJobStatus4Scheduler(String jobGroupName, String jobKey, String schedulerIPAndPort,
                                          String oldStatus, String newStatus) {

        if (schedulerIPAndPort == null) {
            return false;
        }
        return casJobStatus(jobGroupName, jobKey, schedulerIPAndPort, oldStatus, newStatus);
    }

    /**
     *
     * change job status; used by config
     * {@link } can be checked for the result.
     * @param
     * @return true/false
     * @throws
     */
    public boolean casJobStatus4User(String jobGroupName, String jobKey, String oldStatus, String newStatus) {

        return casJobStatus(jobGroupName, jobKey, null, oldStatus, newStatus);
    }

    /**
     *
     * change job status; CompareAndSwap
     * {@link } can be checked for the result.
     * @param
     * @return true/false
     * @throws
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
            if (lock.acquire(Constant.MAX_WAIT_SECONDS, TimeUnit.SECONDS)) {

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
            LOGGER.error("acquire lock exception:", e);
        } finally {
            try {
                if (lock != null) {
                    lock.release();
                }
            } catch (Exception e) {
                LOGGER.error("release lock exception:", e);
            }
        }
        return false;

    }

    /**
     *
     * acquire job status
     * {@link } can be checked for the result.
     * @param
     * @return job status
     * @throws
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
     *
     * acquire scheduler executing jobKey
     * {@link } can be checked for the result.
     * @param
     * @return scheduler
     * @throws
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
     *
     * scheduler acquiring jobKey
     * {@link } can be checked for the result.
     * @param
     * @return true/false
     * @throws
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
            if (lock.acquire(Constant.MAX_WAIT_SECONDS, TimeUnit.SECONDS)) {

                // do some work inside of the critical section here
                List<String> schedulers = curatorClient.getChildren(path);
                // exception
                if (schedulers == null) {
                    return false;
                }
                // I am first
                if (schedulers.size() == 0) {
                    String schedulerPath = new StringBuilder().append(path).append(Constant.ZK_SEPARATOR).append(schedulerIPAndPort).toString();
                    return curatorClient.createFixedEphemeralZKNode(schedulerPath);
                }
                // others are already acquire

            }
        } catch (Exception e) {
            LOGGER.error("acquire lock exception:", e);
        } finally {
            try {
                if (lock != null) {
                    lock.release();
                }
            } catch (Exception e) {
                LOGGER.error("release lock exception:", e);
            }
        }
        return false;
    }

    /**
     *
     * scheduler releasing the acquired jobKey
     * {@link } can be checked for the result.
     * @param
     * @return true/false
     * @throws
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
            if (lock.acquire(Constant.MAX_WAIT_SECONDS, TimeUnit.SECONDS)) {

                // do some work inside of the critical section here
                List<String> schedulers = curatorClient.getChildren(path);
                // exception
                if (schedulers == null) {
                    return false;
                }

                for (String scheduler : schedulers) {
                    if (scheduler.equals(schedulerIPAndPort)) {
                        String schedulerPath = new StringBuilder().append(path).append(Constant.ZK_SEPARATOR).append(schedulerIPAndPort).toString();
                        return curatorClient.deleteLeafZKNode(schedulerPath);
                    }
                }

            }
        } catch (Exception e) {
            LOGGER.error("acquire lock exception:", e);
        } finally {
            try {
                if (lock != null) {
                    lock.release();
                }
            } catch (Exception e) {
                LOGGER.error("release lock exception:", e);
            }
        }
        return false;
    }

    /**
     *
     * encode httpPath
     * {@link } can be checked for the result.
     * @param
     * @return encoded httpPath
     * @throws
     */
    public String encodeHttpPath(String httpPath) {

        if (StringHelper.isEmpty(httpPath)) {
            return httpPath;
        }
        return httpPath.replace(Constant.HTTP_SEPARATOR, Constant.HTTP_MASK);
    }

    /**
     *
     * decode httpPath
     * {@link } can be checked for the result.
     * @param
     * @return decoded httpPath
     * @throws
     */
    public String decodeHttpPath(String httpPath) {

        if (StringHelper.isEmpty(httpPath)) {
            return httpPath;
        }
        return httpPath.replace(Constant.HTTP_MASK, Constant.HTTP_SEPARATOR);
    }

    /**
     *
     * acquire zk path of TaskKey
     * {@link } can be checked for the result.
     * @param
     * @return zk path of TaskKey
     * @throws
     */
    private String buildTaskKeyPath(String taskKeyInDB) {

        boolean fail = StringHelper.isEmpty(taskKeyInDB) || !taskKeyInDB.contains(Constant.ZK_KEY_SPLIT)
                || !taskKeyInDB.contains(Constant.APP_SEPARATOR) || !taskKeyInDB.contains(Constant.HTTP_SEPARATOR)
                || taskKeyInDB.contains(Constant.HTTP_MASK);

        if (fail) {
            return Constant.ZK_UNKNOWN_PATH;

        }
        String[] groupAndApp = taskKeyInDB.split(Constant.ZK_KEY_SPLIT);
        if (groupAndApp.length < 2) {
            return Constant.ZK_UNKNOWN_PATH;
        }
        String taskApplicationName = groupAndApp[0];
        String taskGroupName = taskApplicationName.split(Constant.APP_SEPARATOR)[0];
        String taskHttpPath = groupAndApp[1];
        return buildTaskKeyPath(taskGroupName, taskApplicationName, taskHttpPath);
    }

    /**
     *
     * build zk path of TaskKey
     * {@link } can be checked for the result.
     * @param
     * @return zk path of TaskKey
     * @throws
     */
    private String buildTaskKeyPath(String taskGroupName, String taskApplicationName, String taskHttpPath) {

        boolean fail = StringHelper.isEmpty(taskGroupName) || StringHelper.isEmpty(taskApplicationName)
                || !taskApplicationName.contains(Constant.APP_SEPARATOR)
                || !taskApplicationName.startsWith(taskGroupName) || StringHelper.isEmpty(taskHttpPath)
                || !taskHttpPath.contains(Constant.HTTP_SEPARATOR) || taskHttpPath.contains(Constant.HTTP_MASK);

        if (fail) {

            return Constant.ZK_UNKNOWN_PATH;
        }
        String path = new StringBuilder().append(buildTaskRoot()).append(Constant.ZK_ONLINE_TASK).append(Constant.ZK_SEPARATOR).append(taskGroupName).append(Constant.ZK_SEPARATOR).
                append(taskApplicationName).append(Constant.ZK_SEPARATOR).append(taskApplicationName).append(Constant.ZK_KEY_SPLIT).append(encodeHttpPath(taskHttpPath)).toString();
        return path;
    }

    /**
     *
     * acquire list of executors
     * {@link } can be checked for the result.
     * @param
     * @return list of executors
     * @throws
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
     *
     * acquire list of executors
     * {@link } can be checked for the result.
     * @param
     * @return list of executors
     * @throws
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
     *
     * build ip-auth zk path(white-list path)
     * {@link } can be checked for the result.
     * @param
     * @return ip-auth zk path
     * @throws
     */
    private String buildAuthPath(String ip) {

        boolean fail = StringHelper.isEmpty(ip);

        if (fail) {
            return Constant.ZK_UNKNOWN_PATH;
        }
        String path = new StringBuilder().append(buildTaskRoot()).append(Constant.ZK_ONLINE_AUTH).append(Constant.ZK_SEPARATOR).append(ip).toString();
        return path;
    }

    /**
     *
     * add ip to white-list
     * {@link } can be checked for the result.
     * @param
     * @return true/false
     * @throws
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
     *
     * remove ip from white-list
     * {@link } can be checked for the result.
     * @param
     * @return true/false
     * @throws
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
     *
     * acquire ip-list from white-list
     * {@link } can be checked for the result.
     * @param
     * @return ip-list
     * @throws
     */
    public List<String> getAuthList() {

        String path = new StringBuilder().append(buildTaskRoot()).append(Constant.ZK_ONLINE_AUTH).toString();
        return curatorClient.getChildren(path);
    }

    /**
     *
     * delete TaskKey from zk
     * {@link } can be checked for the result.
     * @param
     * @return
     * @throws
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
     *
     * delete TaskKey from zk
     * {@link } can be checked for the result.
     * @param
     * @return
     * @throws
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
     *
     * init zk JobTransfer path
     * {@link } can be checked for the result.
     * @param
     * @return
     * @throws
     */
    private void initialJobTransferPath(List<ACL> acls, boolean needACL) {

        try {
            // "/SkyWorldOnlineTask/JobTransfer"
            String JobTransfer =  new StringBuilder().append(buildTaskRoot()).append(Constant.ZK_ONLINE_JOBTRANSFER).toString();
            if (!this.curatorClient.isExists(JobTransfer)) {
                this.curatorClient.createPersistentZKNode(JobTransfer);
            }
            if (needACL) {
                this.curatorClient.getCuratorFramework().setACL().withACL(acls).forPath(JobTransfer);
            }
        } catch (Exception e) {
            LOGGER.error("initialExecutorPath exception:", e);
        }
    }

    /**
     *
     * one-key batch transfer jobKey
     * {@link } can be checked for the result.
     * @param
     * @return
     * @throws
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
     *
     * build zk JobTransfer path
     * {@link } can be checked for the result.
     * @param
     * @return
     * @throws
     */
    private String buildJobTransferPath(String ipAndPort) {

        boolean fail = StringHelper.isEmpty(ipAndPort) || !ipAndPort.contains(Constant.ZK_KEY_SPLIT);

        if (fail) {
            return Constant.ZK_UNKNOWN_PATH;
        }
        String path = new StringBuilder().append(buildTaskRoot()).append(Constant.ZK_ONLINE_JOBTRANSFER).append(Constant.ZK_SEPARATOR).append(ipAndPort).toString();
        return path;
    }

    /**
     *
     * update job info of one-key transfer
     * {@link } can be checked for the result.
     * @param
     * @return
     * @throws
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
     *
     * acquire job info of one-key transfer
     * {@link } can be checked for the result.
     * @param
     * @return
     * @throws
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
     * Gets a list of jobkeys that the Scheduler executes
     *
     * @param scheduler
     * @return
     */
    public List <String> getJobKeyListByScheduler(String scheduler) {
        List<String> jobKeyList = new ArrayList<>();
        String path = new StringBuilder().append(Constant.ZK_ROOT).append(taskRoot).append(Constant.ZK_SEPARATOR).append(Constant.ZK_ONLINE_JOB).toString();
        List<String> jobGroupNames = curatorClient.getChildren(path);
        for(String jobGroupName:jobGroupNames) {
            List<String> jobKeys = getJobKeys(jobGroupName);
            for(String jobKey:jobKeys) {
                List<String> jobSchedulerList = getJobScheduler(jobGroupName,jobKey);
                if(jobSchedulerList.contains(scheduler)) {
                    jobKeyList.add(jobKey);
                }
            }
        }
        return jobKeyList;
    }

}
