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

package com.sia.task.scheduler.util;

import com.google.common.collect.Maps;
import com.sia.task.core.util.AbstractExpireMap;
import com.sia.task.core.util.Constant;
import com.sia.task.core.util.JsonHelper;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * scheduler misfire 数据记录
 *
 * @author maozhengwei
 * @version V1.0.0
 * @date 2020/6/10 4:21 下午
 **/
@Slf4j
public class SchedulerMisFireDataMap extends AbstractExpireMap<String, JobMisFireData> {

    private static final long expTime = 24 * 60 * 60 * 1000;

    static SchedulerMisFireDataMap misFireDataMap = new SchedulerMisFireDataMap(expTime, TimeUnit.MILLISECONDS);

    public SchedulerMisFireDataMap(long expTime, TimeUnit unit) {
        super(expTime, unit);
    }

    @Override
    protected void timerExpireCallback(String key, JobMisFireData val) {
        HashMap<String, String> misFireDataHashMap = Maps.newHashMap();
        misFireDataHashMap.put(key, JsonHelper.toString(val));
        log.warn(Constant.LOG_PREFIX + "timerExpireCallback " + new LogSummary("JobMisfire timerExpireCallback", null, misFireDataHashMap));
    }

    private static Map<String, JobMisFireData> getMap() {
        return misFireDataMap.getDataMap();
    }

    /**
     * 记录Job被misfire的行为
     * 注意:
     * job misfire 只保存一段时间内的记录，具体这段时间的大小请参看<code>{@link SchedulerMisFireDataMap#expTime}</code>
     *
     * @param jobKey      失火的JobKey
     * @param misFireTime 失火时间
     */
    public static void putMisfireCount4Job(String jobKey, Long misFireTime) {
        if (misFireDataMap.containsKey(jobKey)) {
            JobMisFireData jobMisFireData = getMap().get(jobKey);
            jobMisFireData.getMisFireTimes().add(misFireTime);
        } else {
            JobMisFireData misFireData = new JobMisFireData(jobKey, new ArrayList<>(Arrays.asList(misFireTime)));
            misFireDataMap.put(jobKey, misFireData);
        }
    }

    /**
     * 获取当前调度器misfire计数
     * 注意：
     * 1. 只返回没有被过期移除的job的misfire计数。
     * 2. job misfire ttl 参看<code>{@link SchedulerMisFireDataMap#expTime}</code>
     *
     * @return misfire计数
     */
    public static int getMisfireCount4AllJob() {
        AtomicInteger count = new AtomicInteger();
        getMap().forEach((k, v) -> count.addAndGet(v.misFireTimes.size()));
        return count.get();
    }

    public static Integer getMisfireCount(String jobKey) {
        Integer integer = misFireDataMap.get(jobKey).misFireTimes.size();
        return integer;
    }
}

@Data
@AllArgsConstructor
class JobMisFireData {
    String jobKey;
    List<Long> misFireTimes;
}
