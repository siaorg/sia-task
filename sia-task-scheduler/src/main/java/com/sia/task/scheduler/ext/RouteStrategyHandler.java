package com.sia.task.scheduler.ext;/*-
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

//package com.sia.task.online.ext;
//
//
//import com.sia.base.task.JobMTask;
//
//import java.util.List;
//
///**
// *
// *
// * @author: MAOZW
// * @Description: 路由策略工具类
// * @date 2018/5/1610:40
// */
//public class RouteStrategyHandler {
//
//
//    /**
//     * 根据路由策略获取执行实例
//     *
//     * @param task        任务
//     * @param addressList 执行器List
//     * @return instance
//     */
//    public static String handle(JobMTask task, List<String> addressList) {
//        RouteStrategy routeStrategyEnum = RouteStrategy.match(task.getRouteStrategy());
//        return routeStrategyEnum.getExecutorRouter().routeInstance(task, addressList);
//    }
//
//    /**
//     * 失败路由策略 获取执行实例
//     *
//     * @param task
//     * @param addressList
//     * @return
//     */
//    public static String failHandle(JobMTask task, List<String> addressList) {
//        FailoverStrategyEnum routeStrategyEnum = FailoverStrategyEnum.match(task.getFailover());
//        String instance = routeStrategyEnum.getExecutorRouter().routeInstance(task, addressList);
//        return instance;
//    }
//
//
//
//
//}
