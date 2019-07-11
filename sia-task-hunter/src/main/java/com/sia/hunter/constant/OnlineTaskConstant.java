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

/**
 * Macro management class
 * @see
 * @author pengfeili23
 * @date 2018-07-11 16:11
 * @version V1.0.0
 **/
public class OnlineTaskConstant {

    /**
     * For ZK operations
     */
    public static final String ZK_ONLINE_TASK = "Task";
    public static final String ZK_ONLINE_AUTH = "Schindler";

    public static final String ZK_SEPARATOR = "/";
    public static final String ZK_KEY_SPLIT = ":";
    public static final String ZK_DEFAULT_VALUE = "";

    public static final String CREATEAUTH = "guest:guest";
    public static final String DIGEST = "digest";

    public static final int RETRY_TIMES = 10;
    public static final int SLEEP_MS_BETWEEN_RETRIES = 5000;

    /**
     * Used for fetching information processing
     */
    public static final String HTTP_SEPARATOR = "/";
    public static final String HTTP_MASK = "\\";
    public static final String APP_SEPARATOR = "-";
    public static final String UNKNOWN_GROUP = "unknown";
    public static final String UNKNOWN_APPLICATION = "unknown-application";
    public static final String UNKNOWN_PORT = "unknown_port";
    public static final String DEFAULT_CONTEXT = "/";

    /**
     * Used for log location
     */
    public static final String LOGPREFIX = "OnlineTask->";

    /**
     * used for nacos
     */
    public static final String NACOS_ONLINE_AUTH = "Schindler";
    public static final String NACOS_ONLINE_AUTH_GROUP = "SCHINDLER_GROUP";
    public static final String HTTP_PREFIX = "http://";
    public static final String NACOS_ONLINE_TASK = "Task";
    public static final String NACOS_ONLINE_TASK_GROUP = "TASK_GROUP";
    public static final String NACOS_KEY_SPLIT = "---";
    public static final String NACOS_SEPARATOR = "/";
}
