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

import com.sia.task.core.task.DagTask;
import com.sia.task.core.util.Constant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * 固定IP
 *
 * @author: MAOZW
 * @Description: 路由策略--固定IP
 * @date 2018/4/2318:25
 */
public class ExecutorRouteSpecify extends AbstractExecutorRouter {

    private static final Logger logger = LoggerFactory.getLogger(ExecutorRouteSpecify.class);

    /**
     * enums run job
     *
     * @param taskKey
     * @param addressList
     * @return ReturnT.content: final address
     */
    @Override
    public String routeInstance(DagTask taskKey, List<String> addressList) {
        if (addressList.isEmpty()) {
            logger.warn(Constant.LOG_PREFIX + " enums address fail, addressList is empty, jobKey={} taskKey={}", taskKey.getJobKey(), taskKey.getTaskKey());
            return null;
        } else {
            return route(taskKey, addressList);
        }
    }

    private String route(DagTask task, List<String> addressList) {
        return task.getFixIp();
    }

}
