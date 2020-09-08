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

import java.util.List;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Polling strategy
 *
 * @author: MAOZW
 * @Description: FailoverRound
 * @date 2018/9/29 17:22
 */
public class FailoverRound extends AbstractExecutorRouter {

    private static ConcurrentHashMap<String, List<String>> CACHE_VALID_INSTANCES = new ConcurrentHashMap<>();
    private static long CACHE_VALID_ENABLE = 0;

    private static String refresh(DagTask task, List<String> addressList) {

        if (System.currentTimeMillis() > CACHE_VALID_ENABLE) {
            CACHE_VALID_INSTANCES.clear();
            CACHE_VALID_ENABLE = System.currentTimeMillis() + 1000 * 60 * 60 * 24;
        }

        if (!CACHE_VALID_INSTANCES.containsKey(task.getTaskKey())) {
            CACHE_VALID_INSTANCES.put(task.getTaskKey(), addressList);
        }

        List<String> list = CACHE_VALID_INSTANCES.get(task.getTaskKey());
        list.remove(task.getCurrentHandler());
        if (list.size() > 0) {
            return list.get(new Random().nextInt(list.size()));
        }
        CACHE_VALID_INSTANCES.remove(task.getTaskKey());
        return null;
    }

    @Override
    public String routeInstance(DagTask task, List<String> addressList) {
        String refresh = refresh(task, addressList);
        return refresh;
    }
}
