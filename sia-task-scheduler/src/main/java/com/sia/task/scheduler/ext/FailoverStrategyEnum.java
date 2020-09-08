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

package com.sia.task.scheduler.ext;

/**
 * @author: MAOZW
 * @Description: FailoverStrategyEnum
 * @date 2018/10/9 11:22
 */
public enum FailoverStrategyEnum {

    /**
     * MULTI_CALLS_TRANSFER:最大程度补偿，本机尝试多次，仍失败则进行转移，转移为轮询转移
     * TRANSFER：轮询转移
     */
    MULTI_CALLS_TRANSFER("MULTI_CALLS_TRANSFER",new FailoverMaximumCompensation()),
    TRANSFER("TRANSFER",new FailoverRound());

    private String routeType;
    private AbstractExecutorRouter executorRouter;

    public String getRouteType() {
        return routeType;
    }

    public AbstractExecutorRouter getExecutorRouter() {
        return executorRouter;
    }

    FailoverStrategyEnum(String routeType, AbstractExecutorRouter executorRouter) {
        this.routeType = routeType;
        this.executorRouter = executorRouter;
    }

    /**
     *
     * @param name
     * @param defaultItem
     * @return
     */
    public static FailoverStrategyEnum match(String name, FailoverStrategyEnum defaultItem) {
        if (name != null) {
            for (FailoverStrategyEnum failoverStrategyEnum : FailoverStrategyEnum.values()) {
                if (failoverStrategyEnum.routeType.equals(name)) {
                    return failoverStrategyEnum;
                }
            }
        }
        return defaultItem;
    }
}
