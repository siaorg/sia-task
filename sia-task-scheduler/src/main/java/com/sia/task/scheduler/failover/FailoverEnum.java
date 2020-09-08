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

package com.sia.task.scheduler.failover;


/**
 * 异步调用失败策略
 *
 * @author maozhengwei
 * @version V1.0.0
 * @data 2018/6/6 11:07 上午
 **/
public enum FailoverEnum {

    /**
     * 调度失败处理策略
     * STOP:停止运行；
     * IGNORE：忽略失败；
     * MULTI_CALLS（已弃用）：本机尝试多次；
     * MULTI_CALLS_TRANSFER：本机尝试多次，仍失败则进行转移，转移为轮询转移；
     * TRANSFER：转移尝试其他实例
     */
    STOP("STOP"),
    IGNORE("IGNORE"),
    @Deprecated
    MULTI_CALLS("MULTI_CALLS"),
    MULTI_CALLS_TRANSFER("MULTI_CALLS_TRANSFER"),
    TRANSFER("TRANSFER");

    private String value;

    FailoverEnum(String value) {

        this.value = value;
    }

    public String getValue() {
        return value;
    }

    /**
     * getByValue
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
