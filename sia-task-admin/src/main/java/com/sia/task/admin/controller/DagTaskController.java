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

package com.sia.task.admin.controller;

import com.sia.task.admin.service.DagTaskService;
import com.sia.task.admin.vo.SiaResponseBody;
import com.sia.task.core.task.DagTask;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;


/**
 * 任务编排管理
 *
 * @author maozhengwei
 * @version V1.0.0
 * @data 2018-04-19 18:38
 * @see
 **/
@RestController
@RequestMapping("/taskinjobapi")
public class DagTaskController {

    @Resource
    DagTaskService dagTaskService;

    /**
     * 查询JOB对应编排的Task
     * 不包含执行器信息
     *
     * @param jobGroup
     * @param jobKey
     * @return
     */
    @RequestMapping(value = "/selectTaskByJobKey", method = RequestMethod.GET)
    @ResponseBody
    public String selectTasksWithoutActuatorsInJob(@RequestParam String jobGroup, @RequestParam String jobKey) {
        return SiaResponseBody.success(dagTaskService.selectTasksWithoutActuatorsInJob(jobGroup, jobKey));
    }

    /**
     * JOB对应编排的Task依赖图
     *
     * @param jobGroup
     * @param jobKey
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/selectTaskDependencyByJobKey", method = RequestMethod.GET)
    @ResponseBody
    public String selectDepGraphByJobKey(@RequestParam String jobGroup, @RequestParam String jobKey) {
        return SiaResponseBody.success(dagTaskService.selectDependencyGraph4Job(jobGroup, jobKey));
    }

    /**
     * 设置任务编排关系
     *
     * @param dagTaskExt
     * @return
     */
    @PostMapping("/inserttaskinjob")
    @ResponseBody
    public String updateJobMTask(@RequestBody List<DagTask> dagTaskExt) {
        return SiaResponseBody.success(dagTaskService.updateDagTask(dagTaskExt));
    }
}
