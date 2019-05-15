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

package com.sia.config.web.util;

import com.sia.core.dag.DAGHelper;
import com.sia.core.entity.JobMTask;

import java.util.*;

/**
 * Task loop checking under Job
 * @see
 * @author maozhengwei
 * @date 2019-04-28 15:40
 * @version V1.0.0
 **/

public class OnlineJobDagCheck {

    /**
     * Task loop checking under Job
     * @param jobMTaskList
     * @return
     */
    public static List<String> doDagCheck(List<JobMTask> jobMTaskList) {
        Map<String, List<String>> relyMap = new HashMap<>();
        for (JobMTask jobMTask : jobMTaskList) {
            List<String> preTask = Arrays.asList(jobMTask.getPreTaskKey().split(","));
            relyMap.put(jobMTask.getTaskKey(), (preTask == null || preTask.contains("")) ? Collections.emptyList() : preTask);
        }
        return DAGHelper.findACycle(relyMap);
    }

}
