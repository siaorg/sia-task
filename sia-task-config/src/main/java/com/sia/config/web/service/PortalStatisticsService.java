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

package com.sia.config.web.service;

import com.sia.core.helper.DateFormatHelper;
import com.sia.core.helper.StringHelper;
import com.sia.core.mapper.PortalStatisticsMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Home page information statistics,
 * Contains statistics on Job and Task scheduling
 * @see
 * @author jinghuali
 * @date 2019-04-28 15:40
 * @version V1.0.0
 **/
@Service
public class PortalStatisticsService {

    @Autowired
    private PortalStatisticsMapper portalStatisticsMapper;

    /**
     * Front-end call interface
     * get the Job and Task call count details
     * @return
     */
    public List<Map<String, Object>> getJobCallStatistics(String scheduler, String startTime, String endTime){
        List<Map<String, Object>> res = new ArrayList<>();

        Map<String, String> param = new HashMap<>(4);
        param.put("scheduler", StringHelper.isEmpty(scheduler)? null : scheduler);
        param.put("startTime", StringHelper.isEmpty(startTime) ? DateFormatHelper.getFormatByDay(new Date(),-1) : startTime);
        param.put("endTime", StringHelper.isEmpty(endTime)? DateFormatHelper.format(new Date()): endTime);
        List<Map<String, Object>> jobCallStatistics = portalStatisticsMapper.getJobCallStatistics(param);

        List<Object> times = jobCallStatistics.stream().map(m->m.get("times")).distinct().collect(Collectors.toList());

        List<Object> schedulers = jobCallStatistics.stream().map(m->m.get("scheduler")).distinct().collect(Collectors.toList());
        schedulers.forEach(s->{
            List<Map<String, Object>> sch1 = jobCallStatistics.stream().filter(m->s.equals(m.get("scheduler"))).collect(Collectors.toList());

            List<Object> timesTmp = sch1.stream().map(m->m.get("times")).distinct().collect(Collectors.toList());
            List<Object> diff = times.stream().filter(t->!timesTmp.contains(t)).collect(Collectors.toList());
            diff.forEach(t->{
                Map<String, Object> map = new HashMap<>(8);
                map.put("scheduler", s);
                map.put("task_call_count", 0);
                map.put("job_call_count", 0);
                map.put("times", t);
                sch1.add(map);
            });
            sch1.sort(Comparator.comparing(m->(String)m.get("times")));

            Map<String, Object> map = new HashMap<>(4);
            map.put("scheduler", s);
            map.put("info", sch1);
            res.add(map);
        });

        return res;
    }

    /**
     * get Job statistics
     * Real-time and historical job  data statistics
     * @return
     */
    public List<Map<String,String>> getJobStatistics(String scheduler, String startTime, String endTime) {
        Map<String, String> param = new HashMap<>(4);
        param.put("scheduler", scheduler);
        param.put("startTime", StringHelper.isEmpty(startTime) ? DateFormatHelper.getFormatByDay(new Date(),-1) : startTime);
        param.put("endTime", StringHelper.isEmpty(endTime)? DateFormatHelper.format(new Date()): endTime);
        return portalStatisticsMapper.getJobStatistics(param);
    }

    /**
     * get Task statistics
     * Real-time and historical Task  data statistics
     * @return
     */
    public List<Map<String, String>> getTaskStatistics(String scheduler, String startTime, String endTime) {
        Map<String, String> param = new HashMap<>(4);
        param.put("scheduler", scheduler);
        param.put("startTime", StringHelper.isEmpty(startTime) ? DateFormatHelper.getFormatByDay(new Date(),-1) : startTime);
        param.put("endTime", StringHelper.isEmpty(endTime)? DateFormatHelper.format(new Date()): endTime);
        return portalStatisticsMapper.getTaskStatistics(param);
    }

    /**
     * get scheduling statistics
     * @Return scheduling statistics
     */
    public Map<String, Integer> getJobCallCount(){
        Integer cnt = portalStatisticsMapper.selectAllJobLogs();

        if (cnt == null){
            cnt = 0;
        }

        Map<String, Integer> res = new HashMap<>(2);
        res.put("jobCallCount", cnt);
        return res;
    }
}
