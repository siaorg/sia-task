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

package com.sia.task.admin.service;

import com.sia.task.admin.timer.SchedulingModelStore;
import com.sia.task.admin.vo.SchedulingModelVo;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * 调度统计模型 service
 *
 * @author maozhengwei
 * @version V1.0.0
 * @date 2020/7/25 6:31 下午
 * @see
 **/
@Service
public class SchedulingModelService {


    /**
     * 每个调度器的当日每个小时的调度值
     *
     * @return
     */
    public SchedulingModelVo schedulingModelByHour() {
        return SchedulingModelStore.schedulingModelByHour();
    }

    /**
     * 获取指定调度器指定小时的分钟统计Map
     *
     * @param instance 调度器实例
     * @param hour     小时(0-23)
     * @return
     */
    public Map<Integer, Integer> schedulingModelByMinute(String instance, int hour) {
        return SchedulingModelStore.schedulingModelByMinute(instance, hour);
    }

    /**
     * 获取指定调度器指定某小时某分钟的统计Map
     *
     * @param instance 调度器实例
     * @param hour     小时(0-23)
     * @param minute   分钟(0-59)
     * @return
     */
    public Map<Integer, Integer> schedulingModelBySecond(String instance, int hour, int minute) {
        return SchedulingModelStore.schedulingModelBySecond(instance, hour, minute);
    }
}
