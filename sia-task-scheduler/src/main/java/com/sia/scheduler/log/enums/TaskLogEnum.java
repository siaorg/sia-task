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

package com.sia.scheduler.log.enums;


/**
 *
 * Task log storage type enumeration
 *
 * @see
 * @author maozhengwei
 * @date 2018-06-06 15:10
 * @version V1.0.0
 **/
public enum TaskLogEnum {

    /**
     * Define the task schedule log identifier
     */
    LOG_ENDTASK_FINISHED("LOG_ENDTASK_FINISHED"),
    LOG_TASK_FINISHED("LOG_TASK_FINISHED"),
    LOG_TASK_CALLBACKERROR("LOG_TASK_CALLBACKERROR"),
    LOG_TASK_HANDLE_BEGIN("LOG_TASK_HANDLE_BEGIN"),
    LOG_TASK_FAIL_STOP("LOG_TASK_FAIL_STOP"),
    LOG_TASK_FAIL_IGNORE("LOG_TASK_FAIL_IGNORE"),
    LOG_TASK_FAIL_MULTI_CALLS("LOG_TASK_FAIL_MULTI_CALLS"),
    LOG_TASK_FAIL_MULTI_CALLS_TRANSFER("LOG_TASK_FAIL_MULTI_CALLS_TRANSFER"),
    LOG_TASK_FAIL_DETAIL("LOG_TASK_FAIL_DETAIL"),
    LOG_TASK_FAIL_TRANSFER("LOG_TASK_FAIL_TRANSFER");

    private String value;

    TaskLogEnum(String value) {

        this.value = value;
    }

    @Override
    public String toString() {
        return value;
    }


    public static TaskLogEnum getByValue(String value) {
        for (TaskLogEnum taskLogEnum : values()) {
            if (taskLogEnum.toString().equals(value)) {
                return taskLogEnum;
            }
        }
        return null;
    }
}
