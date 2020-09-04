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

package com.sia.task.mapper;

import com.sia.task.pojo.Index4ManagerVo;
import com.sia.task.pojo.Index4UserVo;
import com.sia.task.pojo.TaskErrorLogVo;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 首页展示
 *
 * @author maozhengwei
 * @version V1.0.0
 * @date 2020/6/12 2:18 下午
 **/
public interface IndexMapper {

    /**
     * 首页大屏统计
     *
     * @return
     */
    Index4ManagerVo homePageStatistics4Manager();

    Index4UserVo homePageStatistics4User(String group);

    HashMap<String, Integer> taskTrackerHealthStatus4Manager();

    HashMap<String, Integer> taskTrackerHealthStatus4User(String group);

    List<TaskErrorLogVo> warningLogTop(String group);

    List<Map<String, String>> jobSchedulingStatistics(String group);

    List<Map<String, String>> taskExecutionStatistics(String group);
}
