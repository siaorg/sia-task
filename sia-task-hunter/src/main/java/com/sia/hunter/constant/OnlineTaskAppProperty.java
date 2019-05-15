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

package com.sia.hunter.constant;

import com.sia.hunter.helper.NetworkHelper;
import com.sia.hunter.helper.StringHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * OnlineTask profile processing class
 * @see
 * @author pengfeili23
 * @date 2018-07-11 16:11
 * @version V1.0.0
 **/
@Component
public class OnlineTaskAppProperty {

    private static final Logger LOGGER = LoggerFactory.getLogger(OnlineTaskAppProperty.class);

    @Value("${server.context-path:}")
    String contextPath;

    @Value("${spring.application.name}")
    String applicationName;

    @Value("${spring.cloud.client.ipAddress:}")
    String ipAddress;

    @Value("${server.port}")
    String serverPort;

    @Value("${spring.hunter.serial:true}")
    boolean onlinetaskSerial;

    /**
     * New property: the network card that the user specifies to take effect,
     * meaning the IP that the user specifies to take effect
     */
    @Value("${network.ethNum:}")
    String ethNum;

    static {
        /**
         * First print all available < network card name, the corresponding IP>
         */
        Map<String, String> all = NetworkHelper.getAllIpsByInetAddress();

        for (Map.Entry<String, String> item : all.entrySet()) {
            LOGGER.info("ethNum:[" + item.getKey() + "],IP:[" + item.getValue() + "]");
        }
    }

    public boolean getOnlinetaskSerial() {

        return onlinetaskSerial;
    }

    /**
     * Get the current application's port,
     * spring boot directly from the server.port attribute,
     * and non-spring boot from the web applicatioin
     *
     * @return
     */
    public String getServerPort() {

        if (StringHelper.isEmpty(serverPort)) {
            LOGGER.error(OnlineTaskConstant.LOGPREFIX + "Please configure the application port，e.g. [server.port: 8080]");
            return OnlineTaskConstant.UNKNOWN_PORT;
        }
        return serverPort;
    }

    /**
     * Gets the current application of IP, spring boot directly from the spring.
     * The cloud. The client. The ipAddress attribute,
     * the spring boot reading available network card address to get from it
     * @return
     */
    public String getIPAddress() {

        /**
         * The user specifies the effective network card with highest priority
         */
        if (!StringHelper.isEmpty(ethNum)) {
            ipAddress = NetworkHelper.getIpByEthNum(ethNum);
        }
        /**
         * If the specified network card is not effective,
         * or ${spring. Cloud. Client. IpAddress:} injection failure, entering the default logic before
         */
        if (StringHelper.isEmpty(ipAddress)) {
            //Returns the first from the list of available IP addresses, or 127.0.0.1 if none
            ipAddress=NetworkHelper.getServerIp();

        }
        return ipAddress;
    }

    /**
     * Non-spring boot applications need to specify application.name
     *
     * @return
     */
    public String getApplicationName() {

        if (StringHelper.isEmpty(applicationName)) {
            LOGGER.error(
                    OnlineTaskConstant.LOGPREFIX + "Please configure the application name，e.g. [spring.application.name: GROUP-APPLICATION-SUFFIX");
            return OnlineTaskConstant.UNKNOWN_APPLICATION;
        }
        return applicationName;
    }

    public String getIPAndPort() {

        return getIPAddress() + OnlineTaskConstant.ZK_KEY_SPLIT + getServerPort();
    }

    /**
     * application.name contains group.name
     *
     * @return
     */
    public String getGroupName() {

        String appName = getApplicationName();
        if (!appName.contains(OnlineTaskConstant.APP_SEPARATOR)) {
            return OnlineTaskConstant.UNKNOWN_GROUP;
        }
        int index = appName.indexOf(OnlineTaskConstant.APP_SEPARATOR);
        return appName.substring(0, index);
    }

    public String getContextPath() {

        if (StringHelper.isEmpty(contextPath)) {

            LOGGER.info(OnlineTaskConstant.LOGPREFIX + "No application context is configured, using default values:[" + OnlineTaskConstant.DEFAULT_CONTEXT + "]");
            LOGGER.info(OnlineTaskConstant.LOGPREFIX + "Configure the application context as needed，e.g. [server.context-path: /CONTEXT/PATH");
            return OnlineTaskConstant.DEFAULT_CONTEXT;
        }
        return contextPath;
    }
}
