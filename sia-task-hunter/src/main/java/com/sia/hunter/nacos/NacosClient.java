package com.sia.hunter.nacos;

import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.NamingFactory;
import com.alibaba.nacos.api.naming.NamingMaintainFactory;
import com.alibaba.nacos.api.naming.NamingMaintainService;
import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.api.naming.listener.EventListener;
import com.alibaba.nacos.api.naming.pojo.Instance;
import com.alibaba.nacos.api.naming.pojo.Service;
import com.sia.hunter.constant.OnlineTaskConstant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Map;
import java.util.Properties;

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
     * 查看是否存在某个服务
     * @param serviceName
     * @return
     * @throws Exception
     */
    public boolean existNacosService(String serviceName, String groupName) {
        boolean existService = true;
        Service service = null;
        try {
            service = namingMaintainService.queryService(serviceName, groupName);
        } catch (Exception e) {
            LOGGER.error(OnlineTaskConstant.LOGPREFIX + e);
        }

        if (service == null){
            existService = false;
        }
        return existService;
    }

    /**
     * 创建服务
     * @param serviceName
     * @return
     * @throws Exception
     */
    public void createNacosService(String serviceName, String groupName) {
        try {
            namingMaintainService.createService(serviceName, groupName);
        } catch (Exception e) {
            LOGGER.error(">>>>>>nacos: createNacosService:" + e.getMessage());
        }
    }

    /**
     * 创建实例
     */
    public void createNacosInstance(String serviceName, String groupName, String clusterName, String ipAndPort, Map<String, String> metadata, boolean isEphemeral) throws Exception {
        Instance instance = new Instance();
        instance.setIp(ipAndPort.split(":")[0]);
        instance.setPort(Integer.parseInt(ipAndPort.split(":")[1]));
        instance.setClusterName(clusterName);
        instance.setMetadata(metadata);
        instance.setHealthy(true);
        instance.setEphemeral(isEphemeral);

        namingService.registerInstance(serviceName, groupName, instance);
    }

    /**
     * 删除实例
     * @param serviceName
     * @return
     * @throws Exception
     */
    public void deleteNacosInstance(String serviceName, String groupName, String clusterName, String ipAndPort, boolean isEphemeral) throws Exception {
        Instance instance = new Instance();
        instance.setServiceName(serviceName);
        instance.setClusterName(groupName);
        instance.setIp(ipAndPort.split(":")[0]);
        instance.setPort(Integer.parseInt(ipAndPort.split(":")[1]));
        namingService.deregisterInstance(serviceName, clusterName, instance);
    }

    /**
     * 获取所有实例信息
     * @param serviceName
     * @param groupName
     * @param clusters
     * @return
     * @throws NacosException
     */
    public List<Instance> getAllInstances(String serviceName, String groupName, List<String> clusters) throws NacosException {
        return namingService.getAllInstances(serviceName, groupName, clusters);
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
