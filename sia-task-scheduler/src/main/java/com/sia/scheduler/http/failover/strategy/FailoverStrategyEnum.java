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

package com.sia.scheduler.http.failover.strategy;

import com.sia.scheduler.http.route.AbstractExecutorRouter;

/**
 *
 * FailoverStrategyEnum
 *
 * Provide a static method to get specific strategies during the running phase of the program.
 *
 * If no policy name is specified or there is no matching policy then the system uses the default policy.
 * The default policy is also required to be dynamically passed in. The default policy used in this example is Failover Round.
 *
 * @see FailoverMaximumCompensation
 * @see FailoverRound
 * @author maozhengwei
 * @date 2019-10-09 11:22
 * @version V1.0.0
 **/
public enum FailoverStrategyEnum {

    /**
     * MULTI_CALLS_TRANSFER : FailoverMaximumCompensation
     * @see FailoverMaximumCompensation
     * TRANSFER : FailoverRound
     * @see FailoverRound
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
     * Provide static methods for getting specific strategies
     *
     * @param name Policy name by which to match the policy
     * @param defaultItem Default policy, this example default policy uses Failover Round
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
