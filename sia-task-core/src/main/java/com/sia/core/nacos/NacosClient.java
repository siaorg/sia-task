package com.sia.core.nacos;

import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.NamingFactory;
import com.alibaba.nacos.api.naming.NamingMaintainFactory;
import com.alibaba.nacos.api.naming.NamingMaintainService;
import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.api.naming.listener.EventListener;
import com.alibaba.nacos.api.naming.pojo.Instance;
import com.sia.core.constant.Constant;
import com.sia.core.curator.Curator4Scheduler;
import com.sia.core.helper.StringHelper;
import org.apache.curator.framework.recipes.locks.InterProcessMutex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author jinghuali2
 * @version V1.0.0
 * @description NacosClient
 * @date 2019-7-1 14:26
 * @see
 **/
@Component
public class NacosClient  {

    private static final Logger LOGGER = LoggerFactory.getLogger(NacosClient.class);

    @Value("${nacosServers}")
    protected String nacosServerHosts;

    @Value("${nacosNamespaceName}")
    protected String nacosNamespace;

    @Value("${nacosNamespaceId}")
    protected String nacosNamespaceId;

    @Autowired
    protected Curator4Scheduler curator4Scheduler;

    //实例操作相关
    private NamingService namingService;
    //服务操作相关
    private NamingMaintainService namingMaintainService;

    @PostConstruct
    public void getNamingService() throws NacosException {
        Properties properties = new Properties();
        properties.setProperty("serverAddr", nacosServerHosts);
        properties.setProperty("namespace", nacosNamespaceId);

        namingService = NamingFactory.createNamingService(properties);
        namingMaintainService = NamingMaintainFactory.createMaintainService(properties);
    }

    /**
     * 根据taskKey删除task
     * @param taskKey
     * @return
     * @throws Exception
     */
    public boolean deleteTaskKey(String taskKey) throws Exception {
        List<Instance> instances = namingService.getAllInstances(Constant.NACOS_ONLINE_TASK, taskKey);
        instances.forEach(instance -> {
            try {
                namingService.deregisterInstance(Constant.NACOS_ONLINE_TASK, taskKey, instance);
            } catch (NacosException e) {
                LOGGER.error(">>>>>>nacos: deleteTaskKey error");
                return;
            }
        });

        namingMaintainService.deleteService(Constant.NACOS_ONLINE_TASK, taskKey);
        return true;
    }

    /**
     * 根据jobGroupName和jobKey删除job
     * @param jobGroupName
     * @param jobKey
     * @return
     */
    public boolean deleteJobKey(String jobGroupName, String jobKey) throws NacosException {
        List<Instance> instances = namingService.getAllInstances(Constant.NACOS_ONLINE_JOB, jobKey);
        instances.forEach(instance -> {
            try {
                namingService.deregisterInstance(Constant.NACOS_ONLINE_JOB, jobKey, instance);
            } catch (NacosException e) {
                LOGGER.error(">>>>>>nacos: deleteJobKey error");
                return;
            }
        });

        namingMaintainService.deleteService(Constant.NACOS_ONLINE_JOB, jobKey);
        return true;
    }

    /**
     * 根据jobGroupName和jobKey获取job状态
     * @param jobGroupName
     * @param jobKey
     * @return
     */
    public String getJobStatus(String jobGroupName, String jobKey) {
        List<Instance> instances = null;
        try {
            instances = namingService.getAllInstances(Constant.NACOS_ONLINE_JOB, jobKey);
        } catch (Exception e) {
            LOGGER.error(">>>>>>nacos: getJobStatus: " + e.getMessage());
        }
        if (instances.size() == 0 || instances == null){
            return null;
        }
        return instances.get(0).getMetadata().get(Constant.NACOS_JOB_STATUS);
    }

    /**
     * 获得调度器ip:port
     * @return
     */
    public List<String> getSchedulers(){
        List<Instance> schedulers = null;
        try {
            schedulers = namingService.getAllInstances(Constant.NACOS_ONLINE_SCHEDULER);
        } catch (Exception e) {
            LOGGER.error(">>>>>>nacos: getSchedulers: " + e.getMessage());
        }

        if (schedulers == null){
            return null;
        }

        return schedulers.stream().map(scheduler->scheduler.getIp() + ":" + scheduler.getPort()).collect(Collectors.toList());
    }

    public String getSchedulerInfo(String ipAndPort){
        List<String> clusters = new ArrayList<>();
        clusters.add(Constant.NACOS_DEFAULT_CLUSTER);
        List<Instance> schedulers = null;
        try {
             schedulers = namingService.getAllInstances(Constant.NACOS_ONLINE_SCHEDULER, Constant.NACOS_ONLINE_SCHEDULER_GROUP, clusters);
        } catch (Exception e) {
            LOGGER.error(">>>>>>nacos: getSchedulerInfo: " + e.getMessage());
        }

        if (schedulers == null){
            return null;
        }

        String schedulerInfo = schedulers.stream().filter(scheduler->ipAndPort.equals(scheduler.getIp()+":"+scheduler.getPort())).findFirst()
                .map(scheduler->scheduler.getMetadata().get(Constant.NACOS_SCHEDULER_INFO)).get();
        return schedulerInfo;
    }

    /**
     * getBlackList
     * @return
     */
    public List<String> getBlackList(){
        List<String> clusters = new ArrayList<>();
        clusters.add(Constant.NACOS_DEFAULT_CLUSTER);
        List<Instance> blackList = null;
        try {
            blackList = namingService.getAllInstances(Constant.NACOS_OFFLINE_SCHEDULER, Constant.NACOS_OFFLINE_SCHEDULER_GROUP, clusters);
        } catch (Exception e) {
            LOGGER.error(">>>>>>nacos: getBlackList: " + e.getMessage());
        }

        if (blackList == null){
            return null;
        }

        return blackList.stream().map(instance -> instance.getIp()+":"+instance.getPort()).collect(Collectors.toList());
    }

    /**
     * openScheduler
     * @param ipAndPort
     * @return
     */
    public boolean openScheduler(String ipAndPort){
        String ip = ipAndPort.split(":")[0];
        int port = Integer.parseInt(ipAndPort.split(":")[1]);
        try {
            namingService.deregisterInstance(Constant.NACOS_OFFLINE_SCHEDULER, Constant.NACOS_OFFLINE_SCHEDULER_GROUP, ip, port, Constant.NACOS_DEFAULT_CLUSTER);
            return true;
        } catch (Exception e) {
            LOGGER.error(">>>>>>nacos: openScheduler: " + e.getMessage());
        }
        return false;
    }

    /**
     * closeScheduler
     * @param ipAndPort
     * @return
     */
    public boolean closeScheduler(String ipAndPort){
        Instance instance = new Instance();
        instance.setIp(ipAndPort.split(":")[0]);
        instance.setPort(Integer.parseInt(ipAndPort.split(":")[1]));
        instance.setClusterName(Constant.NACOS_DEFAULT_CLUSTER);
        instance.setEphemeral(false);
        try {
            namingService.registerInstance(Constant.NACOS_OFFLINE_SCHEDULER, Constant.NACOS_OFFLINE_SCHEDULER_GROUP, instance);
            return true;
        } catch (Exception e) {
            LOGGER.error(">>>>>>nacos: closeScheduler: " + e.getMessage());
        }
        return false;
    }

    /**
     * 获取白名单列表
     * @return
     */
    public List<String> getAuthList(){
        List<Instance> authList = null;
        List<String> clusters = new ArrayList<>();
        clusters.add(Constant.NACOS_DEFAULT_CLUSTER);
        try {
            authList = namingService.getAllInstances(Constant.NACOS_ONLINE_SCHINDLER, Constant.NACOS_ONLINE_SCHINDLER_GROUP, clusters);
        } catch (Exception e) {
            LOGGER.error(">>>>>>nacos: getAuthList: " + e.getMessage());
        }

        if (authList == null){
            return null;
        }

        return authList.stream().map(instance -> instance.getIp()).collect(Collectors.toList());
    }

    /**
     * ip加入白名单
     * @param ip
     * @return
     */
    public boolean addToAuth(String ip){
        Instance instance = new Instance();
        instance.setIp(ip);
        instance.setPort(Constant.NACOS_SCHINDLER_PORT);
        instance.setClusterName(Constant.NACOS_DEFAULT_CLUSTER);
        instance.setEphemeral(false);
        try {
            namingService.registerInstance(Constant.NACOS_ONLINE_SCHINDLER, Constant.NACOS_ONLINE_SCHINDLER_GROUP, instance);
            return true;
        } catch (Exception e) {
            LOGGER.error(">>>>>>nacos; addToAuth: " + e.getMessage());
        }
        return false;
    }

    /**
     * 从白名单中移除ip
     * @param ip
     * @return
     */
    public boolean removeFromAuth(String ip){
        try {
            Instance instance = new Instance();
            instance.setIp(ip);
            instance.setPort(Constant.NACOS_SCHINDLER_PORT);
            instance.setClusterName(Constant.NACOS_DEFAULT_CLUSTER);
            instance.setEphemeral(false);
            namingService.deregisterInstance(Constant.NACOS_ONLINE_SCHINDLER, Constant.NACOS_ONLINE_SCHINDLER_GROUP, instance);
            return true;
        } catch (Exception e) {
            LOGGER.error(">>>>>>nacos: removeFromAuth: " + e.getMessage());
        }
        return false;
    }

    /**
     *  创建jobKey
     * @param jobGroupName
     * @param jobKey
     * @return
     */
    public boolean createJobKey(String jobGroupName, String jobKey){
        try {
            namingMaintainService.createService(Constant.NACOS_ONLINE_JOB, jobKey);
            return true;
        } catch (Exception e) {
            LOGGER.error(">>>>>>nacos: createJobKey: " + e.getMessage());
        }
        return false;
    }

    /**
     * 更改jobKey状态
     * @param jobGroupName
     * @param jobKey
     * @param oldStatus
     * @param newStatus
     * @return
     */
    public boolean casJobStatus4User(String jobGroupName, String jobKey, String oldStatus, String newStatus){
        String lockPath = curator4Scheduler.buildJobStatusLock(jobGroupName, jobKey);
        InterProcessMutex lock = null;
        try {
            lock = new InterProcessMutex(curator4Scheduler.getCuratorClient().getCuratorFramework(), lockPath);
            if (lock.acquire(Constant.MAX_WAIT_SECONDS, TimeUnit.SECONDS)) {

                List<Instance> instances = namingService.getAllInstances(Constant.NACOS_ONLINE_JOB, jobKey);
                if (instances.size() != 1){
                    return false;
                }

                String currStatus = instances.get(0).getMetadata().get(Constant.NACOS_JOB_STATUS);
                if (currStatus != null && currStatus.equals(oldStatus)){
                    Instance instance = instances.get(0);
                    instance.getMetadata().put(Constant.NACOS_JOB_STATUS, newStatus);
                    namingService.registerInstance(Constant.NACOS_ONLINE_JOB, jobKey, instance);
                    return true;
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
     * 获得执行器列表
     * @param taskGroupName
     * @param taskApplicationName
     * @param taskHttpPath
     * @return
     */
    public List<String> getExecutors(String taskGroupName, String taskApplicationName, String taskHttpPath){
        String taskKey = new StringBuilder().append(taskApplicationName).append(Constant.NACOS_SEPARATOR).append(taskHttpPath.substring(1, taskHttpPath.length())).toString();
        return getExecutors(taskKey);
    }

    /**
     * 获得执行器列表
     * @param taskKey
     * @return
     */
    public List<String> getExecutors(String taskKey){
        try {
            List<Instance> instances = namingService.getAllInstances(Constant.NACOS_ONLINE_TASK, taskKey);
            List<String> executors = instances.stream().map(instance -> instance.getIp() + ":" + instance.getPort()).collect(Collectors.toList());
            return executors;
        } catch (Exception e) {
            LOGGER.error(">>>>>>nacos: getExecutors: " + e.getMessage());
        }
        return null;
    }

    /**
     *
     * @param jobGroupName
     * @param jobKey
     * @return
     */
    public List<String> getJobScheduler(String jobGroupName, String jobKey){
        List<String> clusters = new ArrayList<>();
        clusters.add(jobKey);
        List<Instance> jobScheduler = null;
        try {
             jobScheduler = namingService.getAllInstances(Constant.NACOS_ONLINE_JOB, Constant.NACOS_ONLINE_JOB_GROUP, clusters);
        } catch (Exception e) {
            LOGGER.error(">>>>>>nacos: getJobScheduler: " + e.getMessage());
        }

        if (jobScheduler == null){
            return null;
        }

        return jobScheduler.stream().map(instance -> instance.getIp()+":"+instance.getPort()).collect(Collectors.toList());
    }

    /**
     *
     * @param scheduler
     * @return
     */
    public List<String> getJobKeyListByScheduler(String scheduler){
        List<Instance> instances = null;
        try {
            instances = namingService.getAllInstances(Constant.NACOS_ONLINE_JOB, Constant.NACOS_ONLINE_JOB_GROUP);
            List<String> jobKeys = instances.stream().filter(instance -> scheduler.equals(instance.getIp()+":"+instance.getPort()))
                    .map(instance -> instance.getClusterName().replace("_", Constant.NACOS_SEPARATOR)).collect(Collectors.toList());
            return jobKeys;
        } catch (Exception e) {
            LOGGER.error(">>>>>>nacos: getJobKeyListByScheduler: " + e.getMessage());
        }
        return null;
    }


    /**
     * 订阅服务
     * @param serviceName
     * @param groupName
     * @param clusters
     * @param eventListener
     * @throws NacosException
     */
    public void subscribe(String serviceName, String groupName, List<String> clusters, EventListener eventListener) throws NacosException {
        namingService.subscribe(serviceName, groupName, clusters, eventListener);
    }

}
