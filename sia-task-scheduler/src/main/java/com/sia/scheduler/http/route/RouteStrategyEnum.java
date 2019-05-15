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

package com.sia.scheduler.http.route;

/**
 *
 * Route pattern enumeration
 *
 * @see ExecutorRouteRandom
 * @see ExecutorRouteSpecify
 * @author maozhengwei
 * @date 2018-04-23 19:11
 * @version V1.0.0
 **/
public enum RouteStrategyEnum {

    //随机
    ROUTE_TYPE_RANDOM("ROUTE_TYPE_RANDOM", new ExecutorRouteRandom()),

    //固定IP
    ROUTE_TYPE_SPECIFY("ROUTE_TYPE_SPECIFY", new ExecutorRouteSpecify()),

    //分片
    ROUTE_TYPE_SHARDING("ROUTE_TYPE_SHARDING", new ExecutorRouteSharding());

    RouteStrategyEnum(String routeType, AbstractExecutorRouter executorRouter) {
        this.routeType = routeType;
        this.executorRouter = executorRouter;
    }

    private String routeType;
    
    private AbstractExecutorRouter executorRouter;

    public String getRouteType() {
        return routeType;
    }

    public AbstractExecutorRouter getExecutorRouter() {
        return executorRouter;
    }

    /**
     * Routing strateg
     *
     * Increase default policy.
     *
     * @param name
     * @param defaultItem
     * @return
     */
    public static RouteStrategyEnum match(String name, RouteStrategyEnum defaultItem) {
        if (name != null) {
            for (RouteStrategyEnum routeStrategyEnum : RouteStrategyEnum.values()) {
                if (routeStrategyEnum.routeType.equals(name)) {
                    return routeStrategyEnum;
                }
            }
        }
        return defaultItem;
    }
}
