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

package com.sia.task.core.util;

import java.io.IOException;
import java.net.*;
import java.util.Enumeration;
import java.util.Map;
import java.util.TreeMap;

/**
 * 获取机器IP地址
 *
 * @author: pengfeili23
 * @Description: 获取机器IP地址
 * @date: 2018年7月11日 下午4:11:19
 */
public class NetworkHelper {

    private NetworkHelper() {

    }

    /**
     * 通过方法
     *
     * <pre>
     * InetAddress.getLocalHost().getHostAddress()
     * </pre>
     * <p>
     * 来获取IP。可能返回回环地址。
     *
     * @return 如果 UnknownHostException 则返回 null
     */
    public static String getIpByHostAddress() {

        try {
            return InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            // ignore
        }
        return null;

    }

    /**
     * 根据网络接口获取IP地址。
     *
     * @param ethNum 网络接口名，Linux下一般是eth0
     * @return 可能返回 null
     */
    public static String getIpByEthNum(String ethNum) {

        Map<String, String> ips = getAllIpsByInetAddress();
        return ips.get(ethNum);

    }

    /**
     * 返回所有有效的IP地址，格式为
     *
     * <pre>
     * <网卡号，有效的IP地址>
     * </pre>
     *
     * @return 可能返回空 Map
     */
    public static Map<String, String> getAllIpsByInetAddress() {

        Map<String, String> response = new TreeMap<String, String>();

        InetAddress address;
        Enumeration<NetworkInterface> allNetInterfaces;
        try {
            allNetInterfaces = NetworkInterface.getNetworkInterfaces();

            while (allNetInterfaces.hasMoreElements()) {
                NetworkInterface netInterface = allNetInterfaces.nextElement();
                // network interface is up and running
                if (netInterface.isUp()) {
                    Enumeration<InetAddress> ips = netInterface.getInetAddresses();
                    while (ips.hasMoreElements()) {
                        address = ips.nextElement();
                        // IPV4 and non-loopback address
                        if (address != null && address instanceof Inet4Address && !address.isLoopbackAddress()
                                && address.getHostAddress().indexOf(":") == -1) {
                            String ethNum = netInterface.getName();
                            String ip = address.getHostAddress();
                            response.put(ethNum, ip);
                        }
                    }
                }
            }
        } catch (SocketException e) {
            // ignore
        }

        return response;
    }

    /**
     * 获取服务器地址，返回一个可用的IP地址
     *
     * @return 若无有效的IP地址，则返回 127.0.0.1
     */
    public static String getServerIp() {

        // return first valid address
        Map<String, String> ips = getAllIpsByInetAddress();
        for (Map.Entry<String, String> item : ips.entrySet()) {
            return item.getValue();
        }
        // backup
        String resp = getIpByHostAddress();
        if (resp == null) {
            // default
            resp = "127.0.0.1";
        }

        return resp;
    }

    /**
     * COPY FROM {@code org.springframework.cloud.commons.util.InetUtils}
     *
     * @return may return null
     */
    public static InetAddress findFirstNonLoopbackAddress() {

        InetAddress result = null;
        try {
            int lowest = Integer.MAX_VALUE;
            for (Enumeration<NetworkInterface> nics = NetworkInterface.getNetworkInterfaces(); nics
                    .hasMoreElements(); ) {
                NetworkInterface ifc = nics.nextElement();
                if (ifc.isUp()) {

                    if (ifc.getIndex() < lowest || result == null) {
                        lowest = ifc.getIndex();
                    } else if (result != null) {
                        continue;
                    }

                    for (Enumeration<InetAddress> addrs = ifc.getInetAddresses(); addrs.hasMoreElements(); ) {
                        InetAddress address = addrs.nextElement();
                        if (address instanceof Inet4Address && !address.isLoopbackAddress()) {

                            result = address;
                        }
                    }

                }
            }
        } catch (IOException ex) {
            // ignore
        }

        if (result != null) {
            return result;
        }

        try {
            return InetAddress.getLocalHost();
        } catch (UnknownHostException e) {
            // ignore
        }

        return null;
    }
}
