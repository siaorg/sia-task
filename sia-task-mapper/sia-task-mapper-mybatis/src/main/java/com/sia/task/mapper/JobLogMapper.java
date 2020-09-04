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

import com.sia.task.core.entity.JobLog;
import com.sia.task.pojo.JobLogVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * @author: MAOZW
 * @Description: JobLogMapper
 * @date 2018/4/1811:10
 */
@Mapper
public interface JobLogMapper {

    /**
     * 查询job 日志数量
     * @param jobGroup
     * @param jobKey
     * @return
     * @throws Exception
     */
    int selectCount4JobLogs(@Param("jobGroup")String jobGroup, @Param("jobKey")String jobKey) throws Exception;

    /**
     * 查询日志列表
     * @param jobGroup
     * @param jobKey
     * @param traceId
     * @return 返回封装job task日志的结果集
     * @throws Exception
     */
    List<JobLogVO> selectLogs4SiaDagTask(@Param("jobGroup")String jobGroup, @Param("jobKey")String jobKey, @Param("traceId")String traceId) throws Exception;

    /**
     * 插入JobLog
     * @param record
     * @return
     */
    int insertSelective(JobLog record) throws Exception;

    /**
     * 更新JobLog日志
     * @param record
     * @return
     */
    int updataJobLogByTraceId(JobLog record) throws Exception;

    /**
     * 删除Job日志
     * @param createTime
     * @return
     * @throws Exception
     */
    int deleteJobLogByDate(@Param("create_time")String createTime) throws Exception;

    /**
     * 获取每个jobGroup的日志运行数目
     * @param group
     * @return
     */
    List<Map<String, Integer>> selectLogCount4Groups(@Param("groups")List<String> group) throws Exception;

    /**
     * 查询日志列表
     * @param
     * @return 返回封装job task日志的结果集(分页)
     */
    List<JobLogVO> selectLogs4Page(@Param("jobGroup")String jobGroup, @Param("jobKey")String jobKey, @Param("start")int start, @Param("size")int size);
}
