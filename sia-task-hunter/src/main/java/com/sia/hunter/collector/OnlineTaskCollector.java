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

package com.sia.hunter.collector;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.sia.hunter.helper.StringHelper;
import com.sia.hunter.pojo.OnlineTaskPojo;

/**
 * OnlineTask information processing class
 * @see
 * @author pengfeili23
 * @date 2018-07-11 16:11
 * @version V1.0.0
 **/
public class OnlineTaskCollector {

    /**
     * Store compliant OnlineTask information
     */
    private static final Map<String, OnlineTaskPojo> ONLINE_TASK_MAP = new ConcurrentHashMap<String, OnlineTaskPojo>();

    /**
     * Store the compliance, but access the OnlineTask information for the exception (HTTP PATH repeat)
     */
    private static final Map<String, List<OnlineTaskPojo>> ERROR_TASK_MAP = new ConcurrentHashMap<String, List<OnlineTaskPojo>>();

    /**
     * Store non-compliant OnlineTask information
     */
    private static final Map<String, String> ERROR_MESSGAE_MAP = new ConcurrentHashMap<String, String>();

    /**
     * Get the canonical OnlineTask information
     *
     * @return
     */
    public static Map<String, OnlineTaskPojo> getOnlineTask() {

        Map<String, OnlineTaskPojo> copy = new ConcurrentHashMap<String, OnlineTaskPojo>(ONLINE_TASK_MAP);
        return copy;
    }

    /**
     * Add OnlineTask information
     *
     * @param httpPath
     * @param taskPojo
     */
    public static void setOnlineTask(String httpPath, OnlineTaskPojo taskPojo) {

        // wrong parameters
        if (StringHelper.isEmpty(httpPath) || null == taskPojo) {
            return;
        }
        // bad httpPath, add taskPOJO to ERROR_TASK_MAP
        if (ERROR_TASK_MAP.containsKey(httpPath)) {
            List<OnlineTaskPojo> itask = ERROR_TASK_MAP.get(httpPath);
            itask.add(taskPojo);
            return;
        }
        // httpPath already exists, remove it from ONLINE_TASK_MAP
        if (ONLINE_TASK_MAP.containsKey(httpPath)) {
            OnlineTaskPojo oldTask = ONLINE_TASK_MAP.get(httpPath);
            ONLINE_TASK_MAP.remove(httpPath);

            List<OnlineTaskPojo> wtask = new LinkedList<OnlineTaskPojo>();
            wtask.add(oldTask);
            wtask.add(taskPojo);
            ERROR_TASK_MAP.put(httpPath, wtask);
            return;
        }
        // valid
        ONLINE_TASK_MAP.put(httpPath, taskPojo);
    }

    /**
     * Gets the information of the OnlineTask that repeats the HTTP access path (which is normal but access is abnormal)
     *
     * @return
     */
    public static Map<String, List<OnlineTaskPojo>> getErrorTask() {

        Map<String, List<OnlineTaskPojo>> copy = new ConcurrentHashMap<String, List<OnlineTaskPojo>>(ERROR_TASK_MAP);
        return copy;
    }

    /**
     * Get the < method name, error message > in the fetch OnlineTask process
     *
     * @return
     */
    public static Map<String, String> getErrorMessage() {

        Map<String, String> copy = new ConcurrentHashMap<String, String>(ERROR_MESSGAE_MAP);
        return copy;
    }

    public static void setErrorMessage(String label, String errorMessage) {

        // wrong parameters
        if (StringHelper.isEmpty(label) || StringHelper.isEmpty(errorMessage)) {
            return;
        }
        ERROR_MESSGAE_MAP.put(label, errorMessage);
    }

}
