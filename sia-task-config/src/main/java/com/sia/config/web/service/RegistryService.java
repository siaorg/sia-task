package com.sia.config.web.service;

import com.alibaba.nacos.api.exception.NacosException;
import com.sia.core.constant.Constant;
import com.sia.core.curator.Curator4Scheduler;
import com.sia.core.entity.BasicJob;
import com.sia.core.nacos.NacosClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 注册中心相关操作
 * 之后可以改成策略模式
 */
@Service
public class RegistryService {

    private static final Logger LOGGER = LoggerFactory.getLogger(RegistryService.class);

    @Value("${enbaleRegistry}")
    private String registry;

    @Autowired
    protected Curator4Scheduler curator4Scheduler;

    @Autowired
    protected NacosClient nacosClient;

    /**
     * 删除task
     * @param taskKeyInDB
     * @return
     */
    public boolean deleteTaskKey(String taskKeyInDB) {
        if (Constant.ZOOKEEPER_REGISTRY.equals(registry)){
            curator4Scheduler.deleteTaskKey(taskKeyInDB);
        }else if (Constant.NACOS_REGISTRY.equals(registry)){
            try {
                nacosClient.deleteTaskKey(taskKeyInDB);
            } catch (Exception e) {
                LOGGER.error(">>>>>>nacos: deleteTaskKey error: " + e.getMessage());
            }
        }
        return true;
    }

    /**
     * 根据jobGroupName和jobKey删除job
     * @param jobGroupName
     * @param jobKey
     * @return
     * @throws NacosException
     */
    public boolean deleteJobKey(String jobGroupName, String jobKey) {
        if (Constant.ZOOKEEPER_REGISTRY.equals(registry)){
            curator4Scheduler.deleteJobKey(jobGroupName, jobKey);
        }else if (Constant.NACOS_REGISTRY.equals(registry)){
            try {
                nacosClient.deleteJobKey(jobGroupName, jobKey);
            } catch (NacosException e) {
                LOGGER.error(">>>>>>nacos: deleteJobKey error: " + e.getMessage());
            }
        }
        return true;
    }

    /**
     * 根据jobGroupName和jobKey获取job状态
     * @param jobGroupName
     * @param jobKey
     * @return
     */
    public String getJobStatus(String jobGroupName, String jobKey) {
        String jobStatus = null;
        if (Constant.ZOOKEEPER_REGISTRY.equals(registry)){
            jobStatus = curator4Scheduler.getJobStatus(jobGroupName, jobKey);
        }else if (Constant.NACOS_REGISTRY.equals(registry)){
            jobStatus = nacosClient.getJobStatus(jobGroupName, jobKey);
        }
        return jobStatus;
    }

    /**
     * 获得调度器ip:port
     * @return
     */
    public List<String> getSchedulers(){
        List<String> schedulers = null;
        if (Constant.ZOOKEEPER_REGISTRY.equals(registry)){
            schedulers = curator4Scheduler.getSchedulers();
        }else if (Constant.NACOS_REGISTRY.equals(registry)){
            schedulers = nacosClient.getSchedulers();
        }
        return schedulers;
    }

    /**
     * 根据ipAndPort获得调度器信息
     * @param ipAndPort
     * @return
     */
    public String getSchedulerInfo(String ipAndPort){
        String schedulerInfo = null;
        if (Constant.ZOOKEEPER_REGISTRY.equals(registry)){
            schedulerInfo = curator4Scheduler.getSchedulerInfo(ipAndPort);
        }else if (Constant.NACOS_REGISTRY.equals(registry)){
            schedulerInfo = nacosClient.getSchedulerInfo(ipAndPort);
        }
        return schedulerInfo;
    }

    /**
     * getBlackList
     * @return
     */
    public List<String> getBlackList(){
        List<String> blackList = null;
        if (Constant.ZOOKEEPER_REGISTRY.equals(registry)){
            blackList = curator4Scheduler.getBlackList();
        }else if (Constant.NACOS_REGISTRY.equals(registry)){
            blackList = nacosClient.getBlackList();
        }
        return blackList;
    }

    /**
     * openScheduler
     * @param ipAndPort
     * @return
     */
    public boolean openScheduler(String ipAndPort){
        boolean openScheduler = true;
        if (Constant.ZOOKEEPER_REGISTRY.equals(registry)){
            openScheduler = curator4Scheduler.openScheduler(ipAndPort);
        }else if (Constant.NACOS_REGISTRY.equals(registry)){
            openScheduler = nacosClient.openScheduler(ipAndPort);
        }
        return openScheduler;
    }

    /**
     * closeScheduler
     * @param ipAndPort
     * @return
     */
    public boolean closeScheduler(String ipAndPort){
        boolean closeScheduler = true;
        if (Constant.ZOOKEEPER_REGISTRY.equals(registry)){
            closeScheduler = curator4Scheduler.closeScheduler(ipAndPort);
        }else if (Constant.NACOS_REGISTRY.equals(registry)){
            closeScheduler = nacosClient.closeScheduler(ipAndPort);
        }
        return closeScheduler;
    }

    /**
     * 获取白名单列表
     * @return
     */
    public List<String> getAuthList(){
        List<String> authList = null;
        if (Constant.ZOOKEEPER_REGISTRY.equals(registry)){
            authList = curator4Scheduler.getAuthList();
        }else if (Constant.NACOS_REGISTRY.equals(registry)){
            authList = nacosClient.getAuthList();
        }
        return authList;
    }

    /**
     * ip加入白名单
     * @param ip
     * @return
     */
    public boolean addToAuth(String ip){
        boolean flag = true;
        if (Constant.ZOOKEEPER_REGISTRY.equals(registry)){
            flag = curator4Scheduler.addToAuth(ip);
        }else if (Constant.NACOS_REGISTRY.equals(registry)){
            flag = nacosClient.addToAuth(ip);
        }
        return flag;
    }

    /**
     * 从白名单中移除ip
     * @param ip
     * @return
     */
    public boolean removeFromAuth(String ip){
        boolean flag = true;
        if (Constant.ZOOKEEPER_REGISTRY.equals(registry)){
            flag = curator4Scheduler.removeFromAuth(ip);
        }else if (Constant.NACOS_REGISTRY.equals(registry)){
            flag = nacosClient.removeFromAuth(ip);
        }
        return flag;
    }

    /**
     * 创建jobKey
     * @param jobGroupName
     * @param jobKey
     * @return
     */
    public boolean createJobKey(String jobGroupName, String jobKey){
        boolean flag = true;
        if (Constant.ZOOKEEPER_REGISTRY.equals(registry)){
            flag = curator4Scheduler.createJobKey(jobGroupName, jobKey);
        }else if (Constant.NACOS_REGISTRY.equals(registry)){
            flag = nacosClient.createJobKey(jobGroupName, jobKey);
        }
        return flag;
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
        boolean flag = true;
        if (Constant.ZOOKEEPER_REGISTRY.equals(registry)){
            flag = curator4Scheduler.casJobStatus4User(jobGroupName, jobKey, oldStatus, newStatus);
        }else if (Constant.NACOS_REGISTRY.equals(registry)){
            flag = nacosClient.casJobStatus4User(jobGroupName, jobKey, oldStatus, newStatus);
        }
        return flag;
    }

    /**
     * 获得执行器列表
     * @param taskGroupName
     * @param taskApplicationName
     * @param taskHttpPath
     * @return
     */
    public List<String> getExecutors(String taskGroupName, String taskApplicationName, String taskHttpPath){
        List<String> executors = null;
        if (Constant.ZOOKEEPER_REGISTRY.equals(registry)){
            executors = curator4Scheduler.getExecutors(taskGroupName, taskApplicationName, taskHttpPath);
        }else if (Constant.NACOS_REGISTRY.equals(registry)){
            executors = nacosClient.getExecutors(taskGroupName, taskApplicationName, taskHttpPath);
        }
        return executors;
    }

    /**
     * 获得执行器列表
     * @param taskKey
     * @return
     */
    public List<String> getExecutors(String taskKey){
        List<String> executors = null;
        if (Constant.ZOOKEEPER_REGISTRY.equals(registry)){
            executors = curator4Scheduler.getExecutors(taskKey);
        }else if (Constant.NACOS_REGISTRY.equals(registry)){
            executors = nacosClient.getExecutors(taskKey);
        }
        return executors;
    }

    /**
     *
     * @param jobGroupName
     * @param jobKey
     * @return
     */
    public List<String> getJobScheduler(String jobGroupName, String jobKey){
        List<String> jobScheduler = null;
        if (Constant.ZOOKEEPER_REGISTRY.equals(registry)){
            jobScheduler = curator4Scheduler.getJobScheduler(jobGroupName, jobKey);
        }else if (Constant.NACOS_REGISTRY.equals(registry)){
            jobScheduler = nacosClient.getJobScheduler(jobGroupName, jobKey);
        }
        return jobScheduler;
    }

    /**
     *
     * @param scheduler
     * @return
     */
    public List<String> getJobKeyListByScheduler(String scheduler){
        List<String> jobKeyList = null;
        if (Constant.ZOOKEEPER_REGISTRY.equals(registry)){
            jobKeyList = curator4Scheduler.getJobKeyListByScheduler(scheduler);
        }else if (Constant.NACOS_REGISTRY.equals(registry)){
            jobKeyList = nacosClient.getJobKeyListByScheduler(scheduler);
        }
        return jobKeyList;
    }
}
