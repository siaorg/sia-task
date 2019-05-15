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

package com.sia.scheduler.service;

import com.sia.core.mapper.PortalStatisticsMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * PortalStatisticsService
 * Home information statistics
 *
 * @see
 * @author jinghuali
 * @date 2018-04-18 11:21
 * @version V1.0.0
 **/
@Service
public class PortalStatisticsService {

    @Autowired
    private PortalStatisticsMapper portalStatisticsMapper;

    /**
     * 从skyworld_portal_stat表中获取最近的时间
     * @return
     */
    public String getNearestTime(){
        Date nearTime = portalStatisticsMapper.getNearestTime();
        return nearTime == null ? null : new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(nearTime);
    }

    /**
     * 从jobLog中获取时间区间JOB执行数量、成功数量、失败数量
     * @param startTime
     * @param endTime
     * @return
     */
    public List<Map<String, Object>> selectCallExpFinFromJobLog(String startTime,  String endTime){
        Map<String, Object> param = new HashMap<>(4);
        param.put("startTime", startTime);
        param.put("endTime", endTime);

        return portalStatisticsMapper.selectCallExpFinFromJobLog(param);
    }

    /**
     * 插入首页统计数据
     * @param param
     * @return
     */
    public int insertPortalStatistics(Map<String, Object> param){
        return portalStatisticsMapper.insertPortalStatistics(param);
    }

    /**
     * 获取数据库当前时间
     * @return
     */
    public String getDbTime(){
        return portalStatisticsMapper.getDbTime();
    }
}
