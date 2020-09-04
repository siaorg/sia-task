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

import com.sia.task.admin.vo.TriggerTimesUtil;
import com.sia.task.core.util.Constant;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.text.ParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * @author maozhengwei
 * @version V1.0.0
 * @date 2020/7/22 2:36 下午
 * @see
 **/
@Slf4j
public class SchedulingModel {

    /**
     * 24h 一天中按每一个小时作为统计间隔进行统计计数
     */
    public static final int HoursPointOfDay = 24;

    /**
     * 60分 每小时的按每一分钟作为统计间隔进行统计计数
     */
    public static final int MinutePointOfHours = 60;

    /**
     * 4秒，每分钟按5秒作为一个统计间隔进行统计计数
     */
    public static final int SecondPointOfHours = 5;

    /**
     * 调度器实例信息
     * IP:PORT
     */
    @Getter
    @Setter
    private String instance;

    /**
     * HourMap
     * 第一层
     * k : v  - Hour : Count
     * 一个小时一个统计单位
     */
    private Map<Integer, Integer> hour = new TreeMap<>();

    /**
     * MinuterMap
     * 第一层
     * k : Hour
     * v: minute : count
     * 一分钟作为一个统计单位
     */
    private Map<Integer, Map<Integer, Integer>> minute = new HashMap<>();

    /**
     * 第一层
     * k : Hour
     * v : MinuterMap
     * 第二层
     * k : minute
     * v : MinuterMap
     * 第三层
     * k-v: second : count
     * 每5秒作为一个统计单位
     */
    private Map<Integer, Map<Integer, Map<Integer, Integer>>> seconds = new HashMap<>();

    public static SchedulingModel build(String instance) {
        SchedulingModel modelV2 = new SchedulingModel();
        modelV2.setInstance(instance);
        return modelV2;
    }

    /**
     * 获取 hourMap
     *
     * @return
     */
    public Map<Integer, Integer> getHour() {
        if (hour.isEmpty()) {
            for (int i = 0; i < HoursPointOfDay; i++) {
                hour.put(Integer.valueOf(i), 0);
            }
        }
        return hour;
    }

    /**
     * 获取指定小时的分钟统计Map
     *
     * @param hour
     * @return
     */
    public Map<Integer, Integer> getMinute4Hour(Integer hour) {
        if (!minute.containsKey(hour)) {
            TreeMap<Integer, Integer> tem = new TreeMap<>();
            for (int i = 0; i < MinutePointOfHours; i++) {
                tem.put(Integer.valueOf(i), 0);
            }
            minute.put(hour, tem);
        }
        return minute.get(hour);
    }

    /**
     * 获取指定小时指定分钟的秒统计Map
     *
     * @param hour
     * @param minute
     * @return
     */
    public Map<Integer, Integer> getSecondPoint4HourMinute(Integer hour, Integer minute) {
        if (!seconds.containsKey(hour)) {
            //hour
            for (int h = 0; h < HoursPointOfDay; h++) {
                TreeMap<Integer, Map<Integer, Integer>> minuteMap = new TreeMap<>();
                //minute
                for (int m = 0; m < MinutePointOfHours; m++) {
                    Map<Integer, Integer> secondMap = new TreeMap<>();
                    for (int i = 0; i < MinutePointOfHours; i++) {
                        if (i % 5 == 0) {
                            secondMap.put(Integer.valueOf(i), 0);
                        }
                    }
                    minuteMap.put(m, secondMap);
                }
                seconds.put(h, minuteMap);
            }
        }
        return seconds.get(hour).get(minute);
    }

    public SchedulingModel computerTimers(List<String> cronExpressions) {
        cronExpressions.forEach(cron -> computerTimers(cron));
        return this;
    }

    public void computerTimers(String cronExpression) {
        try {
            String dayExpress = TriggerTimesUtil.getDayExpress(cronExpression);
            if (TriggerTimesUtil.questionMark.equals(dayExpress)) {
                if (!TriggerTimesUtil.isContainCurrentDay(cronExpression)) {
                    return;
                }
            }
            computerTimers4Expression(cronExpression);
        } catch (ParseException ex) {
            log.info(Constant.LOG_EX_PREFIX + " computerTimers is fail [{}]", cronExpression, ex);
        }
    }


    private void computerTimers4Expression(String cronExpression) throws ParseException {
        // 1. 如果小时为* 则计算分钟，为每个小时的分钟增加
        // 2. 如果小时不为* 则计算分钟，为指定小时的分钟增加
        log.info(Constant.LOG_PREFIX + " computerTimers4Expression - [{}]", cronExpression);
        List<Integer> hours = TriggerTimesUtil.getTimeUnitByCronMeta(TriggerTimesUtil.getHourExpress(cronExpression), 24);
        List<Integer> minutes = TriggerTimesUtil.getTimeUnitByCronMeta(TriggerTimesUtil.getMinterExpress(cronExpression), 60);
        List<Integer> seconds = TriggerTimesUtil.getTimeUnitByCronMeta(TriggerTimesUtil.getSecondExpress(cronExpression), 60);
        int countByHour = minutes.size() * seconds.size();
        hours.forEach(h -> {
            //update hour
            Map<Integer, Integer> hour = getHour();
            hour.put(h, hour.get(h) + countByHour);
            //update minute
            Map<Integer, Integer> minute4Hour = getMinute4Hour(h);
            minutes.forEach(m -> {
                minute4Hour.put(m, minute4Hour.get(m) + seconds.size());
                //update second
                Map<Integer, Integer> secondPoint4HourMinute = getSecondPoint4HourMinute(h, m);
                seconds.forEach(s -> {
                    secondPoint4HourMinute.put(getSecondPointKey(s), secondPoint4HourMinute.get(getSecondPointKey(s)) + 1);
                });
            });
        });
    }


    /**
     * 判断在命中点集合是否存在被包含在统计间隔中命中点
     *
     * @param k      statistics interval
     * @param target hit point
     * @return
     */
    private boolean isContainer(int k, List<Integer> target) {
        int multiple = k + 4;
        for (int i = 0; i < target.size(); i++) {
            Integer integer = target.get(i);
            if (k <= integer && integer <= multiple) {
                return true;
            }
        }
        return false;
    }

    /**
     * 根据指定的时间秒数获取该值应处于的以秒做统计的单位区间
     *
     * @param v
     * @return
     */
    private static int getSecondPointKey(Integer v) {
        return v - v % SecondPointOfHours;
    }
}
