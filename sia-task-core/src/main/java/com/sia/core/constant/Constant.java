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

package com.sia.core.constant;

/**
 * @description constants related to ZK
 * @see
 * @author pengfeili23
 * @date 2018-06-27 18:36:06
 * @version V1.0.0
 **/
public class Constant {
    /**
     * path identifier of ZK
     */
    public static final String ZK_SEPARATOR = "/";
    /**
     * root path of ZK
     */
    public static final String ZK_ROOT = "/";
    /**
     * separation character of KEY in ZK path
     */
    public static final String ZK_KEY_SPLIT = ":";
    /**
     * default value of ZK
     */
    public static final String ZK_DEFAULT_VALUE = "";

    /**
     * path of different modules
     */
    /**
     * root path of Task
     */
    public static final String ZK_ONLINE_TASK = "Task";
    /**
     * root path of Job
     */
    public static final String ZK_ONLINE_JOB = "Job";
    /**
     * root path of scheduler
     */
    public static final String ZK_ONLINE_SCHEDULER = "Scheduler";
    /**
     * root path of distributed lock
     */
    public static final String ZK_ONLINE_LOCK =  "Lock";
    /**
     * root path of the offline scheduler
     */
    public static final String ZK_OFFLINE_SCHEDULER =  "Offline";
    /**
     * ZK path containing the authorized ip in http call
     */
    public static final String ZK_ONLINE_AUTH =  "Schindler";
    /**
     * ZK path used in Job-bath-transfer
     */
    public static final String ZK_ONLINE_JOBTRANSFER = "JobTransfer";

    /**
     * default returned path
     */
    public static final String ZK_UNKNOWN_PATH = "UNKNOWN_PATH";


    /**
     * default value
     */
    /**
     * maximum waiting time when using distributed locks
     */
    public static final long MAX_WAIT_SECONDS = 60;
    /**
     * maximum attempts when connecting ZK
     */
    public static final int RETRY_TIMES = 10;
    /**
     * the time interval between each attempt when connecting ZK
     */
    public static final int SLEEP_MS_BETWEEN_RETRIES = 5000;

    /**
     * constants below used in hunting information
     */
    /**
     * separation character in http path
     */
    public static final String HTTP_SEPARATOR = "/";
    /**
     * HTTP路径的分隔符与ZK的路径分隔符一样，这里将HTTP的分隔符转义为反斜杠（\），再存ZK
     */
    public static final String HTTP_MASK = "\\";
    /**
     * separation character in app name
     * e.g. GROUP-APPLICATION-WHATEVER
     */
    public static final String APP_SEPARATOR = "-";
    /**
     * separation character in job_key
     */
    public static final String JOBKEY_SEPARATOR = "_";

    public static final String NACOS_REGISTRY = "nacos";
    public static final String ZOOKEEPER_REGISTRY = "zookeeper";

    // nacos
    public static final String NACOS_DEFAULT_GROUP = "DEFAULT_GROUP";
    public static final String NACOS_DEFAULT_CLUSTER = "DEFAULT";
    public static final String NACOS_ONLINE_TASK ="Task";
    public static final String NACOS_ONLINE_TASK_GROUP = "TASK_GROUP";
    public static final String NACOS_ONLINE_JOB = "Job";
    public static final String NACOS_ONLINE_JOB_GROUP = "JOB_GROUP";
    public static final String NACOS_JOB_STATUS = "status";
    public static final String NACOS_SCHEDULER_INFO = "schedulerInfo";
    public static final String NACOS_ONLINE_SCHEDULER = "Scheduler";
    public static final String NACOS_ONLINE_SCHEDULER_GROUP = "ONLINE_GROUP";
    public static final String NACOS_OFFLINE_SCHEDULER = "Offline";
    public static final String NACOS_OFFLINE_SCHEDULER_GROUP = "OFFLINE_GROUP";
    public static final String NACOS_ONLINE_SCHINDLER = "Schindler";
    public static final String NACOS_ONLINE_SCHINDLER_GROUP = "SCHINDLER_GROUP";
    public static final int NACOS_SCHINDLER_PORT = 0;
    public static final String NACOS_SEPARATOR = "---";


}
