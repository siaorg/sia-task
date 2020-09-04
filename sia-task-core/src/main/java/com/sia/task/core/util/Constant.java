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

/**
 * 常量
 * @author maozhengwei
 * @data 2020/4/23 9:24 下午
 * @version V1.0.0
 **/
public class Constant {

    public static final String HTTP_PREFIX = "http://";

    /**
     * 参数来源
     */
    public static final String FROM_UI = "FROM_UI";
    public static final String FROM_TASK = "FROM_TASK";

    /**
     * ENDTask
     */
    public static final String ENDTASK = "endTask";

    /**
     * TASK 来源
     */
    public static final String TASK_SOURCE_ZK = "TASK_SOURCE_ZK";
    public static final String TASK_SOURCE_UI = "TASK_SOURCE_UI";


    public static final String REGEX_COMMA = ",";
    public static final String REGEX_COLON = ":";

    /**
     * 日志前缀标记
     */
    public static final String LOG_PREFIX = " ::: ";
    public static final String LOG_EX_PREFIX = " --- ";

    /**
     * 本机IP地址： a.b.c.d:qwer
     */
    public static String LOCALHOST;

    /**
     * 应用组名，应用名，jobKey的字符串组成规则只能包含数字、字母，下划线，中划线
     */
    public static final String REGEX = "^[-A-Za-z0-9-_]+$";

    /**
     * IP校验
     */
    public static final String IP_REGEX = "^(1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]\\d|[1-9])\\."
            + "(1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]\\d|\\d)\\."
            + "(1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]\\d|\\d)\\."
            + "(1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]\\d|\\d)$";
}
