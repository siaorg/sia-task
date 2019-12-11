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

import com.sia.core.entity.JobLog;
import com.sia.core.web.vo.JobAndTaskLogVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 *
 *
 * @description JobLogMapper
 * @see
 * @author MAOZW
 * @date 2018-04-18 11:10
 * @version V1.0.0
 **/
@Mapper
public interface JobLogMapper {

    /**
     *
     * return the count of jobLog
     * {@link } can be checked for the result.
     * @param
     * @return
     * @throws
     */
    int selectCountJobLogs(Map<String, Object> param);

    /**
     *
     * acquire list of Log
     * {@link } can be checked for the result.
     * @param
     * @return return the result set of encapsuled taskLog
     * @throws
     */
    List<JobAndTaskLogVO> selectJobLogAndTaskLogList(Map<String, Object> param);

    /**
     *
     * insert jobLog
     * {@link } can be checked for the result.
     * @param
     * @return
     * @throws
     */
    int insertSelective(JobLog record);

    /**
     *
     * update jobLog
     * {@link } can be checked for the result.
     * @param
     * @return
     * @throws
     */
    int updateByPrimaryKeySelective(JobLog record);

    /**
     *
     * delete jobLog
     * {@link } can be checked for the result.
     * @param
     * @return
     * @throws
     */
    int deleteJobLogByDate(Map<String, String> taskMap);

    /**
     *
     * acquire jobLog count of each jobGroup
     * {@link } can be checked for the result.
     * @param
     * @return
     * @throws
     */
    List<Map<String, Integer>>  selectCountGroupsJobLogs(Map<String, Object> param);

    /**
     * 通过traceId从JobLog中得到JobLogId
     * @param traceId
     * @return
     */
    Integer selectJobLogIdByTraceId(@Param("traceId") String traceId);

}
