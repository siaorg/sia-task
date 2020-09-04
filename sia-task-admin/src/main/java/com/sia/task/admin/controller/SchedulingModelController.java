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

import com.sia.task.admin.service.SchedulingModelService;
import com.sia.task.admin.vo.SiaResponseBody;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@Slf4j
@RestController
@RequestMapping("/schedulingmodel")
public class SchedulingModelController {


    @Resource
    SchedulingModelService schedulingModelService;

    /**
     * 每个调度器的当日每个小时的调度值
     *
     * @return
     */
    @RequestMapping(value = "/schedulingModelByHour", method = RequestMethod.GET)
    @ResponseBody
    public String schedulingModelByHour() {
        return SiaResponseBody.success(schedulingModelService.schedulingModelByHour());
    }

    /**
     * 获取指定调度器指定小时的分钟统计Map
     *
     * @param instance 调度器实例
     * @param hour     小时(0-23)
     * @return
     */
    @RequestMapping(value = "/schedulingModelByMinute", method = RequestMethod.GET)
    @ResponseBody
    public String schedulingModelByMinute(@RequestParam String instance, @RequestParam int hour) {
        return SiaResponseBody.success(schedulingModelService.schedulingModelByMinute(instance, hour));
    }

    /**
     * 获取指定调度器指定某小时某分钟的统计Map
     *
     * @param instance 调度器实例
     * @param hour     小时(0-23)
     * @param minute   分钟(0-59)
     * @return
     */
    @RequestMapping(value = "/schedulingModelBySecond", method = RequestMethod.GET)
    @ResponseBody
    public String schedulingModelBySecond(@RequestParam String instance, @RequestParam int hour, @RequestParam int minute) {
        return SiaResponseBody.success(schedulingModelService.schedulingModelBySecond(instance, hour, minute));
    }
}
