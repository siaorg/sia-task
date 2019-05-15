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

import com.sia.core.entity.JobMTask;
import com.sia.scheduler.http.failover.strategy.FailoverStrategyEnum;

import java.util.List;

/**
 *
 * Routing policy tool class
 *
 * Provide the method {@Link #handle(JobMTask task, List<String> addressList)}, get the actual route value through JobMTask,
 * if it does not match, use the default route
 * @description
 * @see
 * @author maozhengwei
 * @date 2018-05-16 10:40
 * @version V1.0.0
 **/
public class RouteStrategyHandler {


    /**
     * Obtain an execution instance based on the routing policy
     *
     * @param task
     * @param addressList
     * @return instance
     */
    public static String handle(JobMTask task, List<String> addressList) {
        RouteStrategyEnum routeStrategyEnum = RouteStrategyEnum.match(task.getRouteStrategy(), RouteStrategyEnum.ROUTE_TYPE_RANDOM);
        return routeStrategyEnum.getExecutorRouter().routeRun(task, addressList);
    }

    /**
     * Failed routing policy Get execution instance
     *
     * @param task
     * @param addressList
     * @return
     */
    public static String failHandle(JobMTask task, List<String> addressList) {
            FailoverStrategyEnum routeStrategyEnum = FailoverStrategyEnum.match(task.getFailover(), FailoverStrategyEnum.TRANSFER);
            String instance = routeStrategyEnum.getExecutorRouter().routeRun(task, addressList);
            return instance;
    }

    /**
     * clear cache
     *
     * @param task
     */
    public static void clear(JobMTask task) {
        FailoverStrategyEnum routeStrategyEnum = FailoverStrategyEnum.match(task.getFailover(), FailoverStrategyEnum.TRANSFER);
        routeStrategyEnum.getExecutorRouter().clearTaskCache(task);
    }


}
