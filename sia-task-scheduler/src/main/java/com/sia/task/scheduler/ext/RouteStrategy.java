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
 * @Description: 路由模式枚举
 * @date 2018/4/2318:24
 */
public enum RouteStrategy {

    //随机
    ROUTE_TYPE_RANDOM("ROUTE_TYPE_RANDOM", new ExecutorRouteRandom()),

    //固定IP
    ROUTE_TYPE_SPECIFY("ROUTE_TYPE_SPECIFY", new ExecutorRouteSpecify());

    RouteStrategy(String routeType, AbstractExecutorRouter executorRouter) {
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
     * 路由策略  增加默认策略
     *
     * @param routeType
     * @return
     */
    public static RouteStrategy match(String routeType) {
        return getRouteStrategy(routeType, RouteStrategy.ROUTE_TYPE_RANDOM);
    }

    private static RouteStrategy getRouteStrategy(String routeType, RouteStrategy defaultItem){
        if (routeType != null) {
            for (RouteStrategy routeStrategyEnum : RouteStrategy.values()) {
                if (routeStrategyEnum.routeType.equals(routeType)) {
                    return routeStrategyEnum;
                }
            }
        }
        return defaultItem;
    }
}
