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

package com.sia.task.integration.curator.properties;

import com.sia.task.core.util.Constant;
import com.sia.task.core.util.NetworkHelper;
import com.sia.task.core.util.StringHelper;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;

import java.util.Map;

/**
 * Zookeeper 常量配置
 *
 * @author maozhengwei
 * @version V1.0.0
 * @date 2020/4/30 11:48 上午
 **/
@Data
@Slf4j
public class ZookeeperConfiguration {

    /**
     * zookeeper.host-list
     */
    @Value("${sia.task.zookeeper.host-list:}")
    private String zooKeeperHosts;

    /**
     * 根路径
     */
    @Value("${sia.task.zookeeper.root-path:SIA-ROOT}")
    private String taskRoot;

    /**
     * 本机地址
     */
    @Value("${server.port}")
    private String port;

    /**
     * 警报阈值
     */
    @Value("${sia.task.alarm-threshold:100}")
    private long alarmThreshold;

    /**
     * 容错值
     */
    @Value("${sia.task.fault-tolerant:1}")
    private long faultTolerant;

    /**
     * 同步开关
     */
    @Value("${sia.task.metadata.sync.store:false}")
    private Boolean metadataSyncStore;

    /**
     * 同步开关延时
     */
    @Value("${sia.task.metadata.sync.delay:60000}")
    private Long metadataSyncDelay;

    /**
     * 使用分布式锁时最长等待时间
     */
    @Value("${sia.task.zookeeper.max-wait-seconds:60}")
    private long MAX_WAIT_SECONDS = 60;

    /**
     * 与ZK建立连接失败时，最多尝试次数
     */
    @Value("${sia.task.zookeeper.connect-lost-retry-times:10}")
    private int RETRY_TIMES = 10;

    /**
     * 与ZK建立连接失败时，每次尝试之间的时间间隔
     */
    @Value("${sia.task.zookeeper.sleep-ms-between-retries:5000}")
    private int SLEEP_MS_BETWEEN_RETRIES = 5000;

    //---------------------------用户权限数据---------------------------
    /**
     * 所有权限（创建、读取、写入，删除，赋权）
     */
    @Value("${sia.task.zookeeper.root-auth:SIA:ROOT}")
    private String allAuth;

    /**
     * 只有创建权限
     */
    @Value("${sia.task.zookeeper.create-auth:guest:guest}")
    private String createAuth;

    /**
     * ZK权限模式
     */
    @Value("${sia.task.zookeeper.auth-schema:digest}")
    private String DIGEST;

    @Value("${server.context-path:}")
    String contextPath;

    @Value("${sia.task.hunter.scanBasicPackage:}")
    String scanBasicPackage;

    @Value("${spring.application.name}")
    String applicationName;

    @Value("${spring.cloud.client.ipAddress:}")
    String ipAddress;

    @Value("${spring.hunter.serial:true}")
    boolean onlinetaskSerial;

    /**
     * 新增属性：由用户指定生效的网卡，意味着指定生效的IP
     */
    @Value("${network.ethNum:}")
    String ethNum;

    static {
        /**
         * 先打印所有可用的<网卡名，对应的IP>
         */
        Map<String, String> all = NetworkHelper.getAllIpsByInetAddress();

        for (Map.Entry<String, String> item : all.entrySet()) {
            log.info("ethNum:[" + item.getKey() + "],IP:[" + item.getValue() + "]");
        }
    }

    /**
     * 获取当前应用的port，spring boot直接从server.port属性中获取，非spring boot从web applicatioin中获取
     *
     * @return
     */
    public String getServerPort() {

        if (StringHelper.isEmpty(port)) {
            log.error(Constant.LOG_EX_PREFIX + "请配置应用端口，e.g. [server.port: 8080]");
            return ZookeeperConstant.UNKNOWN_PORT;
        }
        return port;
    }

    /**
     * 获取当前应用的ip，spring boot直接从spring.cloud.client.ipAddress属性中获取，
     * 非spring boot读取可用的网卡地址从中获取
     *
     * @return
     */
    public String getIPAddress() {

        /**
         * 用户指定生效的网卡，优先级最高
         */
        if (!StringHelper.isEmpty(ethNum)) {
            ipAddress = NetworkHelper.getIpByEthNum(ethNum);
        }
        /**
         * 若指定的网卡不生效，或者${spring.cloud.client.ipAddress:}注入失败，则进入之前的默认逻辑
         */
        if (StringHelper.isEmpty(ipAddress)) {
            //从可用的IP列表中返回第一个，如果没有，则返回 127.0.0.1
            ipAddress = NetworkHelper.getServerIp();

        }
        return ipAddress;
    }

    /**
     * 非spring boot应用需要指定 application.name
     *
     * @return
     */
    public String getApplicationName() {

        if (StringHelper.isEmpty(applicationName)) {
            log.error(Constant.LOG_EX_PREFIX + "请配置应用名称，e.g. [spring.application.name: GROUP-APPLICATION-SUFFIX");
            return ZookeeperConstant.UNKNOWN_APPLICATION;
        }
        return applicationName;
    }

    public String getIPAndPort() {
        return getIPAddress() + ZookeeperConstant.ZK_KEY_SPLIT + getServerPort();
    }

    /**
     * application.name 中包含group.name
     *
     * @return
     */
    public String getGroupName() {

        String appName = getApplicationName();
        if (!appName.contains(ZookeeperConstant.APP_SEPARATOR)) {
            return ZookeeperConstant.UNKNOWN_GROUP;
        }
        int index = appName.indexOf(ZookeeperConstant.APP_SEPARATOR);
        return appName.substring(0, index);
    }

    public String getContextPath() {

        if (StringHelper.isEmpty(contextPath)) {

            log.info(Constant.LOG_PREFIX + "没有配置应用上下文，使用默认值:[" + ZookeeperConstant.DEFAULT_CONTEXT + "]");
            log.info(Constant.LOG_PREFIX + "如需配置应用上下文，e.g. [server.context-path: /CONTEXT/PATH");
            return ZookeeperConstant.DEFAULT_CONTEXT;
        }
        return contextPath;
    }
}

