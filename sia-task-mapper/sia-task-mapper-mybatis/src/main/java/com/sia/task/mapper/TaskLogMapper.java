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

import com.sia.task.core.entity.TaskLog;
import com.sia.task.pojo.TaskLogsVO;
import org.apache.ibatis.annotations.MapKey;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * TaskLogMapper
 *
 * @author maozhengwei
 * @version V1.0.0
 * @description
 * @data 2020/4/22 3:20 下午
 * @see
 **/
@Mapper
public interface TaskLogMapper {

    /**
     * 插入TaskLog接口
     *
     * @param record
     * @return
     */
    int insertSelective(TaskLog record) throws Exception;

    /**
     * 删除接口
     *
     * @param createTime
     * @return
     */
    int deleteTaskLogByDate(@Param("createTime") String createTime) throws Exception;


    /**
     * 根据TraceId查询Task日志,该接口暂时不用,后期应用在前端日志的懒加载查询，加快日志查询的速度
     * 根据traceIdList查询task日志
     *
     * @param traceIdList
     * @return
     * @throws Exception
     */
    @MapKey("traceId")
    Map<String, TaskLogsVO> selectByBatchTraceId(List<String> traceIdList) throws Exception;

}
