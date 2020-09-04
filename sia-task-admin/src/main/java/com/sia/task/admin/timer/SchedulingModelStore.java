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

package com.sia.task.admin.timer;

import com.google.common.collect.Maps;
import com.sia.task.admin.vo.SchedulingModelVo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * SchedulingModelStore
 *
 * @author maozhengwei
 * @version V1.0.0
 * @date 2020/7/26 5:16 下午
 * @see
 **/
public class SchedulingModelStore {

    private static Map<String, SchedulingModel> models = new HashMap<>();

    public static Map<String, SchedulingModel> getModels() {
        return models;
    }

    /**
     * 每个调度器的当日每个小时的调度值
     * X - Hour
     * Y - Instance
     * Z - Timers
     *
     * @return
     */
    public static SchedulingModelVo schedulingModelByHour() {
        List<Integer[]> perData = new ArrayList<>();
        Map<Integer, String> schedulerMap = new HashMap<>();
        final int[] offset = {0};
        models.forEach((k, v) -> {
            schedulerMap.put(offset[0], k);
            Map<Integer, Integer> hour = v.getHour();
            for (int i = 0; i < hour.size(); i++) {
                Integer[] hits = new Integer[3];
                hits[0] = i;
                hits[1] = offset[0];
                hits[2] = hour.get(i);
                perData.add(hits);
            }
            offset[0]++;
        });

        return new SchedulingModelVo(perData, schedulerMap);
    }

    /**
     * 获取指定调度器指定小时的分钟统计Map
     *
     * @param instance 调度器实例
     * @param hour     小时(0-23)
     * @return
     */
    public static Map<Integer, Integer> schedulingModelByMinute(String instance, int hour) {
        if (!models.containsKey(instance)) {
            return Maps.newHashMap();
        }
        SchedulingModel schedulingModel = models.get(instance);
        Map<Integer, Integer> minute4Hour = schedulingModel.getMinute4Hour(hour);
        return minute4Hour;
    }

    /**
     * 获取指定调度器指定某小时某分钟的统计Map
     *
     * @param instance 调度器实例
     * @param hour     小时(0-23)
     * @param minute   分钟(0-59)
     * @return
     */
    public static Map<Integer, Integer> schedulingModelBySecond(String instance, int hour, int minute) {
        if (!models.containsKey(instance)) {
            return Maps.newHashMap();
        }
        SchedulingModel schedulingModel = models.get(instance);
        Map<Integer, Integer> secondPoint4HourMinute = schedulingModel.getSecondPoint4HourMinute(hour, minute);
        return secondPoint4HourMinute;
    }
}
