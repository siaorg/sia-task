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

package com.sia.core.helper;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Enumeration;
import java.util.Map;
import java.util.TreeMap;

/**
 *
 *
 * @description acquire ip address
 * @see
 * @author pengfeili23
 * @date 2018-07-11 16:11:19
 * @version V1.0.0
 **/
public class NetworkHelper {

    private NetworkHelper() {

    }

    /**
     *
     * get ip address and return lo address if possible
     * {@link } can be checked for the result.
     * @param
     * @return
     * @throws
     */
    public static String getIpByHostAddress() {

        try {
            return InetAddress.getLocalHost().getHostAddress();
        }
        catch (UnknownHostException e) {
            // ignore
        }
        return null;

    }

    /**
     *
     * get ip address through network interface
     * {@link } can be checked for the result.
     * @param
     * @return
     * @throws
     */
    public static String getIpByEthNum(String ethNum) {

        Map<String, String> ips = getAllIpsByInetAddress();
        return ips.get(ethNum);

    }

    /**
     *
     * return all the valid ip address
     * {@link } can be checked for the result.
     * @param
     * @return
     * @throws
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
        }
        catch (SocketException e) {
            // ignore
        }

        return response;
    }

    /**
     *
     * return an available server ip; return 127.0.0.1 if no valid ip
     * {@link } can be checked for the result.
     * @param
     * @return
     * @throws
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

    public static InetAddress findFirstNonLoopbackAddress() {

        InetAddress result = null;
        try {
            int lowest = Integer.MAX_VALUE;
            for (Enumeration<NetworkInterface> nics = NetworkInterface.getNetworkInterfaces(); nics
                    .hasMoreElements();) {
                NetworkInterface ifc = nics.nextElement();
                if (ifc.isUp()) {

                    if (ifc.getIndex() < lowest || result == null) {
                        lowest = ifc.getIndex();
                    }
                    else if (result != null) {
                        continue;
                    }

                    for (Enumeration<InetAddress> addrs = ifc.getInetAddresses(); addrs.hasMoreElements();) {
                        InetAddress address = addrs.nextElement();
                        if (address instanceof Inet4Address && !address.isLoopbackAddress()) {

                            result = address;
                        }
                    }

                }
            }
        }
        catch (IOException ex) {
            // ignore
        }

        if (result != null) {
            return result;
        }

        try {
            return InetAddress.getLocalHost();
        }
        catch (UnknownHostException e) {
            // ignore
        }

        return null;
    }
}
