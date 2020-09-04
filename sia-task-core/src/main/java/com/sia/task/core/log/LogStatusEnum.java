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

/**
 * 输出日志的类型
 *
 * @author maozhengwei
 * @data 2020/4/23 9:52 上午
 * @version V1.0.0
 **/
public enum LogStatusEnum {

    /**
     * Job status 存在三种
     * 1. LOG_STATUS_JOB_SCHEDULING：开始调度
     * 2. LOG_JOB_HANDLE_FAIL_STOP：调度失败停止；
     * 3. LOG_STATUS_TASK_HANDLE_FINISHED：调度成功，和task共享一个变量由endTask判断
     */
    LOG_STATUS_JOB_SCHEDULING,

    LOG_STATUS_JOB_FINISHED,

    LOG_JOB_HANDLE_FAIL_STOP,

    LOG_JOB_EXECUTION_VETOED,

    LOG_JOB_EXECUTION_MISFIRE,

    LOG_STATUS_TASK_HANDLE_BEGIN,

    LOG_STATUS_TASK_HANDLE_FINISHED,

    LOG_STATUS_TASK_HANDLE_IGNORE,

    LOG_STATUS_TASK_HANDLE_TRANSFER,

    LOG_STATUS_TASK_HANDLE_TRANSFER_STOP,

    LOG_STATUS_TASK_HANDLE_MULTI_CALLS,

    LOG_STATUS_TASK_HANDLE_MULTI_CALLS_STOP,

    LOG_STATUS_TASK_HANDLE_MULTI_CALLS_TRANSFER,

    LOG_STATUS_TASK_HANDLE_MULTI_CALLS_TRANSFER_STOP,

    LOG_STATUS_TASK_HANDLE_FAIL_STOP;

    public boolean isFail() {
        switch (this) {
            case LOG_STATUS_TASK_HANDLE_MULTI_CALLS_TRANSFER_STOP:
            case LOG_STATUS_TASK_HANDLE_MULTI_CALLS_STOP:
            case LOG_STATUS_TASK_HANDLE_TRANSFER_STOP:
            case LOG_STATUS_TASK_HANDLE_IGNORE:
            case LOG_JOB_HANDLE_FAIL_STOP:
            case LOG_STATUS_TASK_HANDLE_FAIL_STOP:
            case LOG_JOB_EXECUTION_MISFIRE:
            case LOG_JOB_EXECUTION_VETOED:
                return true;
            case LOG_STATUS_TASK_HANDLE_MULTI_CALLS_TRANSFER:
            case LOG_STATUS_TASK_HANDLE_MULTI_CALLS:
            case LOG_STATUS_TASK_HANDLE_TRANSFER:
            case LOG_STATUS_TASK_HANDLE_FINISHED:
            case LOG_STATUS_JOB_FINISHED:
            case LOG_STATUS_JOB_SCHEDULING:
            case LOG_STATUS_TASK_HANDLE_BEGIN:
            default:return false;
        }
    }
}
