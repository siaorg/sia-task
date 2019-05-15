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
import com.sia.scheduler.util.constant.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Random;

/**
 *
 * An implementation method of task executor instance routing policy: stochastic strategy.
 *
 * The strategy provides a way to randomly generate a number within the total number of all actuator instances
 * and then obtain the executor corresponding to that number as the result output.
 *
 * @description
 * @see
 * @author maozhengwei
 * @date 2018-04-28 18:25
 * @version V1.0.0
 **/
public class ExecutorRouteRandom extends AbstractExecutorRouter {

    private static final Logger logger = LoggerFactory.getLogger(ExecutorRouteRandom.class);

    private static Random random = new Random();

    /**
     * enums run job
     *
     * @param taskKey
     * @param addressList
     * @return ReturnT.content: final address
     */
    @Override
    public String routeRun(JobMTask taskKey, List<String> addressList) {
        if (addressList.isEmpty()){
            logger.warn(Constants.LOG_PREFIX + " failed to get executor instance, addressList is empty, jobKey={} taskKey={}",taskKey.getJobKey(),taskKey.getTaskKey());
            return null;
        }else{
            return route(taskKey,  addressList);
        }
    }


    private String route(JobMTask task, List<String> addressList){
        return addressList.get(random.nextInt(addressList.size()));
    }

}
