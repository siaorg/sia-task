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

package com.sia.task.register.zookeeper.collector;

import com.sia.task.core.task.SiaTaskMeta;
import com.sia.task.core.util.StringHelper;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author: pengfeili23
 * @Description: OnlineTaskCollector
 * @date: 2018年7月11日 下午4:11:19
 */
public class SiaTaskCollector {

    /**
     * 存储合规的 OnlineTask 信息
     */
    private static final Map<String, SiaTaskMeta> ONLINE_TASK_MAP = new ConcurrentHashMap<String, SiaTaskMeta>();

    /**
     * 存储合规，但会访问异常（HTTP PATH 重复）的 OnlineTask 信息
     */
    private static final Map<String, List<SiaTaskMeta>> ERROR_TASK_MAP = new ConcurrentHashMap<String, List<SiaTaskMeta>>();

    /**
     * 存储不合规的 OnlineTask 信息
     */
    private static final Map<String, String> ERROR_MESSGAE_MAP = new ConcurrentHashMap<String, String>();

    /**
     * 获取合乎规范的OnlineTask的信息
     *
     * @return
     */
    public static Map<String, SiaTaskMeta> getOnlineTask() {

        Map<String, SiaTaskMeta> copy = new ConcurrentHashMap<String, SiaTaskMeta>(ONLINE_TASK_MAP);
        return copy;
    }

    /**
     * 增加OnlineTask信息
     *
     * @param httpPath
     * @param taskPojo
     */
    public static void setOnlineTask(String httpPath, SiaTaskMeta taskPojo) {

        // wrong parameters
        if (StringHelper.isEmpty(httpPath) || null == taskPojo) {
            return;
        }
        // bad httpPath, add taskPOJO to ERROR_TASK_MAP
        if (ERROR_TASK_MAP.containsKey(httpPath)) {
            List<SiaTaskMeta> itask = ERROR_TASK_MAP.get(httpPath);
            itask.add(taskPojo);
            return;
        }
        // httpPath already exists, remove it from ONLINE_TASK_MAP
        if (ONLINE_TASK_MAP.containsKey(httpPath)) {
            SiaTaskMeta oldTask = ONLINE_TASK_MAP.get(httpPath);
            ONLINE_TASK_MAP.remove(httpPath);

            List<SiaTaskMeta> wtask = new LinkedList<SiaTaskMeta>();
            wtask.add(oldTask);
            wtask.add(taskPojo);
            ERROR_TASK_MAP.put(httpPath, wtask);
            return;
        }
        // valid
        ONLINE_TASK_MAP.put(httpPath, taskPojo);
    }

    /**
     * 获取重复HTTP访问路径的（合乎规范但访问会异常的）OnlineTask的信息
     *
     * @return
     */
    public static Map<String, List<SiaTaskMeta>> getErrorTask() {

        Map<String, List<SiaTaskMeta>> copy = new ConcurrentHashMap<String, List<SiaTaskMeta>>(ERROR_TASK_MAP);
        return copy;
    }

    /**
     * 获取抓取OnlineTask过程中的<方法名,出错信息>
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
