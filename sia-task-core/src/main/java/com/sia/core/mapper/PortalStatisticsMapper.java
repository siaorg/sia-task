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

package com.sia.core.mapper;

import org.apache.ibatis.annotations.Mapper;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 *
 *
 * @description data statistics of portal
 * @see
 * @author jinghuali
 * @date 2018-04-18 11:10
 * @version V1.0.0
 **/
@Mapper
public interface PortalStatisticsMapper {

    /**
     *
     * return the nearest time from skyworld_portal_stat
     * {@link } can be checked for the result.
     * @param
     * @return
     * @throws
     */
    Date getNearestTime();

    /**
     *
     * return finished-job count, success-job count and fail-job count in time span
     * {@link } can be checked for the result.
     * @param
     * @return
     * @throws
     */
    List<Map<String, Object>> selectCallExpFinFromJobLog(Map<String, Object> param);

    /**
     *
     * insert data statistics of portal
     * {@link } can be checked for the result.
     * @param
     * @return
     * @throws
     */
    int insertPortalStatistics(Map<String, Object> param);

    /**
     *
     * acquire the count of job call
     * {@link } can be checked for the result.
     * @param
     * @return
     * @throws
     */
    List<Map<String, Object>> getJobCallStatistics(Map<String, String> param);

    /**
     *
     * return job executing data
     * {@link } can be checked for the result.
     * @param
     * @return
     * @throws
     */
    List<Map<String, String>> getJobStatistics(Map<String, String> param);

    /**
     *
     * return task executing data
     * {@link } can be checked for the result.
     * @param
     * @return
     * @throws
     */
    List<Map<String,String>> getTaskStatistics(Map<String, String> param);

    /**
     *
     * return the overall count of job call
     * {@link } can be checked for the result.
     * @param
     * @return
     * @throws
     */
    Integer selectAllJobLogs();

    /**
     *
     * return the current DB time
     * {@link } can be checked for the result.
     * @param
     * @return
     * @throws
     */
    String getDbTime();
}
