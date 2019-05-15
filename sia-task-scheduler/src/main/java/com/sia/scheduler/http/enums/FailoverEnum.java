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

package com.sia.scheduler.http.enums;

/**
 *
 * Declare an enumeration class to define the processing logic for the failure of the task http call;
 * By selecting different strategies to specify the task to perform the corresponding supplementary solution when the failure occurs.
 * Different strategies will affect the final execution status.
 * Users need to carefully select the strategy that meets their needs according to the actual situation.
 * @see
 * @author maozhengwei
 * @date 2018-06-15 15:05
 * @version V1.0.0
 **/
public enum FailoverEnum {


    /**
     * Task call failure processing policy type.
     *
     * STOP:Stop running,Selecting this policy will immediately stop the scheduling of the current job.
     * Subsequent tasks will not continue. In this example, the front-end function will stop abnormally corresponding to the prompt,
     * and the alarm mechanism will be triggered at the same time;
     *
     * IGNORE：Ignore failure，Selecting this policy will not stop the job scheduling, and subsequent tasks will continue to execute.
     * Can be understood as successful, but unlike success, it triggers an early warning mechanism；
     *
     * MULTI_CALLS（Deprecated）：；
     *
     * MULTI_CALLS_TRANSFER：{@see com.sia.scheduler.http.failover.strategy.FailoverMaximumCompensation}
     *
     * TRANSFER：
     *
     * SHARDING（Deprecated）：；{@see com.sia.scheduler.http.enums.FailoverEnum}
     */
    STOP("stop"),
    IGNORE("ignore"),
    MULTI_CALLS("multi_calls"),
    MULTI_CALLS_TRANSFER("multi_calls_transfer"),
    TRANSFER("transfer"),
    SHARDING("sharding");

    private String value;

    FailoverEnum(String value) {

        this.value = value;
    }

    /**
     *
     * Get the policy enumeration object by the policy type value
     *
     * @param value
     * @return
     */
    public static FailoverEnum getByValue(String value) {

        for (FailoverEnum failoverEnum : values()) {
            if (failoverEnum.toString().equals(value)) {
                return failoverEnum;
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return value;
    }
}
