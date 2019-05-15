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
 * JOB log storage type enumeration
 *
 * @see
 * @author maozhengwei
 * @date 2018-06-06 15:05
 * @version V1.0.0
 **/
public enum JobLogEnum {

    /**
     * 日志类型
     * LOG_ENDTASK_FINISHED：EndTask runs over
     * LOG_JOB_HANDLE_BEGIN：Job run begins
     * LOG_JOB_HANDLE_FAIL_STOP：Job failed to run, the policy is STOP
     * LOG_JOB_FAIL_MULTI_CALLS：Job failed to run, the policy is MULTI_CALLS
     * LOG_JOB_FAIL_MULTI_CALLS_TRANSFER：Job failed to run, the strategy is MULTI_CALLS_TRANSFER
     * LOG_JOB_FAIL_TRANSFER：Job failed to run, the strategy is TRANSFER
     * LOG_JOB_FAIL_IGNORE：Job failed to run, the strategy is IGNORE
     */
    LOG_ENDTASK_FINISHED("LOG_ENDTASK_FINISHED"),
    LOG_JOB_HANDLE_BEGIN("LOG_JOB_HANDLE_BEGIN"),
    LOG_JOB_HANDLE_FAIL_STOP("LOG_JOB_HANDLE_FAIL_STOP"),
    LOG_JOB_FAIL_MULTI_CALLS("LOG_JOB_FAIL_MULTI_CALLS"),
    LOG_JOB_FAIL_MULTI_CALLS_TRANSFER("LOG_JOB_FAIL_MULTI_CALLS_TRANSFER"),
    LOG_JOB_FAIL_TRANSFER("LOG_JOB_FAIL_TRANSFER"),
    LOG_JOB_FAIL_IGNORE("LOG_JOB_FAIL_IGNORE");

    private String value;

    JobLogEnum(String value) {

        this.value = value;
    }

    @Override
    public String toString() {
        return value;
    }

}
