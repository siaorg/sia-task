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

package com.sia.scheduler.util.constant;

/**
 * Constant
 *
 * @description
 * @see
 * @author maozhengwei
 * @date 2018-04-17 15:11
 * @version V1.0.0
 **/
public class Constants {

    public static final String HTTP_PREFIX = "http://";

    /**
     * 前端状态码
     */
    public static final String SUCCESS = "success";
    public static final String FAIL = "fail";
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

    /**
     * 正则分隔符 箭头
     */
    public static final String REGEX_COMMA = ",";
    public static final String REGEX_COLON = ":";
    public static final String REGEX_ARROW = ">>>>>";

    /**
     * JobLog status
     */
    public static final String LOG_SUCCESS = "SUCCESS";
    public static final String LOG_START = "START";
    public static final String LOG_FAIL = "FAIL";
    public static final String LOG_FINISHED = "FINISHED";


    public static final String LOG_TASK_MSG_FAIL_STOP = "Scheduled failure，the failure strategy is STOP, to manual processing";
    public static final String LOG_TASK_MSG_FAIL_IGNORE = "Scheduled failure，the failure strategy is IGNORE, Ignore the failure to continue the task";
    public static final String LOG_TASK_MSG_FAIL_MULTI_CALLS_TRANSFER = "Scheduled failure，the failure strategy is MULTI_CALLS_TRANSFER, Try to continue the current actuator call task";
    public static final String LOG_TASK_MSG_FAIL_TRANSFER = "Scheduled failure，the failure strategy is TRANSFER, Try to continue the current actuator call task";
    public static final String LOG_TASK_MSG_FAIL_DETAIL = "调度失败  Task调度信息为 >>>>> ";
    public static final String LOG_TASK_MSG_END = "完成调度";


    /**
     * 日志前缀标记
     */
    public static final String LOG_PREFIX = ">>>>>>>>>>";
    public static final String LOG_EX_PREFIX = "××××××××";

    /**
     *
     */
    public static final String EMAIL_SUBJECT = "JOB 运行异常";
    public static final String LOG_TASK_CALLBACKERROR = "解析task返回参数出现异常";

    /**
     * 本机IP地址： a.b.c.d:qwer
     */
    public static String LOCALHOST;


}
