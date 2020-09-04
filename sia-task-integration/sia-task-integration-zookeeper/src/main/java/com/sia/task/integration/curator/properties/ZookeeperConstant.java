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

/**
 * zookeeper 基础数据常量
 *
 * @author maozhengwei
 * @version V1.0.0
 * @date 2020/5/7 11:43 上午
 **/
public class ZookeeperConstant {

    /**
     * ZK路径标识
     */
    public static final String ZK_SEPARATOR = "/";
    /**
     * ZK的根路径
     */
    public static final String ZK_ROOT = "/";
    /**
     * ZK路径中，作为KEY的分隔符
     */
    public static final String ZK_KEY_SPLIT = ":";
    /**
     * ZK默认VALUE值
     */
    public static final String ZK_DEFAULT_VALUE = "";

    //不同模块路径
    /**
     * Task根路径
     */
    public static final String ZK_ONLINE_TASK = "Task";
    /**
     * Job根路径
     */
    public static final String ZK_ONLINE_JOB = "Job";
    /**
     * 调度器根路径
     */
    public static final String ZK_ONLINE_SCHEDULER = "Scheduler";
    /**
     * 分布式锁根路径
     */
    public static final String ZK_ONLINE_LOCK = "Lock";
    /**
     * 调度器下线根路径
     */
    public static final String ZK_OFFLINE_SCHEDULER = "Offline";
    /**
     * 调度器HTTP调用授权路径
     */
    public static final String ZK_ONLINE_AUTH = "Schindler";
    /**
     * Job一键批量转移JOBTRANSFER路径
     */
    public static final String ZK_ONLINE_JOBTRANSFER = "JobTransfer";
    /**
     * Job一键批量转移JOBRUNONCE路径
     */
    public static final String ZK_ONLINE_JOB_RUNONCE = "JobRunOnce";
    /**
     * 默认返回路径值
     */
    public static final String ZK_UNKNOWN_PATH = "UNKNOWN_PATH";
    /**
     * HTTP路径的分隔符
     */
    public static final String HTTP_SEPARATOR = "/";
    /**
     * HTTP路径的分隔符与ZK的路径分隔符一样，这里将HTTP的分隔符转义为反斜杠（\），再存ZK
     */
    public static final String HTTP_MASK = "\\";
    /**
     * 应用名中的分隔符，示例：GROUP-APPLICATION-WHATEVER
     */
    public static final String APP_SEPARATOR = "-";
    /**
     * JobKey中组名分隔符
     */
    public static final String JOBKEY_SEPARATOR = "_";
    /**
     * 编码格式
     */
    public static final String UTF8 = "utf-8";

    /**
     * 用于抓取信息的处理
     */
    public static final String UNKNOWN_GROUP = "unknown";
    public static final String UNKNOWN_APPLICATION = "unknown-application";
    public static final String UNKNOWN_PORT = "unknown_port";
    public static final String DEFAULT_CONTEXT = "/";
}
