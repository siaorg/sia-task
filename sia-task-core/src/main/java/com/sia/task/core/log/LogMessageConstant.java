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

package com.sia.task.core.log;

public class LogMessageConstant {

    public static final String NO_DAG_TASK= "Job is not scheduled, please check the relationship of task scheduling";
    public static final String ERROR_CRON_EXPRESSION= "Cron Expression is invalid, please check the entered cron expression！";
    public static final String ERROR_ADD_JOB= "Add job to quartz is fail, please check the job！";

    public static final String emailResponseMessageSuccess = "SUCCESS";
    public static final String emailResponseMessageLIMITED = "EMAIL IS LIMITED";
    /**
     * JobLog status
     */
    public static final String REGEX_EX = ", Exception message : ";
    public static final String LOG_SUCCESS = "SUCCESS";
    public static final String LOG_START = "START";
    public static final String LOG_FAIL = "FAIL";
    public static final String LOG_FINISHED = "FINISHED";

    public static final String LOG_TASK_MSG_FAIL_BASE = "Execution error, the failover strategy is ";
    public static final String LOG_TASK_MSG_FAIL_STOP = LOG_TASK_MSG_FAIL_BASE + " [ STOP ], to manual processing";
    public static final String LOG_TASK_MSG_FAIL_IGNORE = LOG_TASK_MSG_FAIL_BASE + " [ IGNORE ], continue to schedule subsequent tasks";
    public static final String LOG_TASK_MSG_FAIL_TRANSFER = LOG_TASK_MSG_FAIL_BASE + " [ TRANSFER ], try to use other actuators for remote execution";
    public static final String LOG_TASK_MSG_FAIL_TRANSFER_STOP = LOG_TASK_MSG_FAIL_TRANSFER + " : Have traversed all the executors, there is no executor that can be successfully executed";
    public static final String LOG_STATUS_TASK_HANDLE_MULTI_CALLS = LOG_TASK_MSG_FAIL_BASE + " [ MULTI_CALLS ], retry...";
    public static final String LOG_STATUS_TASK_HANDLE_MULTI_CALLS_STOP = LOG_TASK_MSG_FAIL_BASE + " [ MULTI_CALLS ] : retry fail";
    public static final String LOG_STATUS_TASK_HANDLE_MULTI_CALLS_TRANSFER = LOG_TASK_MSG_FAIL_BASE + " [ MULTI_CALLS_TRANSFER ], retry...";
    public static final String LOG_STATUS_TASK_HANDLE_MULTI_CALLS_TRANSFER_STOP = LOG_TASK_MSG_FAIL_BASE + " [ MULTI_CALLS_TRANSFER ] : retry fail";

}
