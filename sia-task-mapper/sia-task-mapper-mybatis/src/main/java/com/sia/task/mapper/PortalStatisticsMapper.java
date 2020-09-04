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

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 日报服务信息统计
 *
 * @author jinghuali
 */
@Mapper
public interface PortalStatisticsMapper {
    /**
     * 从skyworld_portal_stat表中获取最近的时间
     *
     * @param
     * @return
     */
    Date getNearestTime();

    /**
     * 获取数据库当前时间
     */
    String getDbTime();

    /**
     * 获取basicJob和basicTask的总数数量
     */
    Map<String, Integer> getAllCount();

    /**
     * 获取新增加的JOB信息
     */
    List<Map<String, Object>> getNewJobInfo(@Param("startTime") String startTime, @Param("endTime") String endTime);

    /**
     * 获取新增加的task信息
     */
    List<Map<String, Object>> getNewTaskInfo(@Param("startTime") String startTime, @Param("endTime") String endTime);

    /**
     * 从jobLog中获取时间区间JobLog信息
     * 日报数据统计
     *
     * @param startTime
     * @param endTime
     * @return
     */
    List<Map<String, Object>> selectJobLogCallForReport(@Param("startTime") String startTime, @Param("endTime") String endTime);

    /**
     * 从taskLog中获取时间区间TaskLog信息
     * 日报数据统计
     *
     * @param startTime
     * @param endTime
     * @return
     */
    List<Map<String, Object>> selectTaskLogCallForReport(@Param("startTime") String startTime, @Param("endTime") String endTime);

    /**
     * 获取项目统计信息
     *
     * @param startTime
     * @param endTime
     * @return
     */
    List<Map<String, Object>> selectProjectInfo(@Param("startTime") String startTime, @Param("endTime") String endTime);

    /**
     * 获取taskNum信息
     *
     * @return
     */
    List<Map<String, Object>> getTaskInfo();

    /**
     * 获取异常job
     *
     * @param startTime
     * @param endTime
     * @return
     */
    List<Map<String, Object>> selectExpJobs(@Param("startTime") String startTime, @Param("endTime") String endTime);

    /**
     * 获取异常task信息
     *
     * @param startTime
     * @param endTime
     * @return
     */
    List<Map<String, Object>> getExpTaskInfo(@Param("startTime") String startTime, @Param("endTime") String endTime);

    /**
     * 清理skyworld_portal_stat表中的数据
     *
     * @param startTime
     * @return
     */
    int deletePortalDataByDate(@Param("create_time") String startTime);
}
