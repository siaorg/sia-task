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

import com.sia.task.core.task.DagTask;
import com.sia.task.pojo.JobMTaskVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;


/**
 *
 * @author maozhengwei
 * @data 2018/4/18 7:02 下午
 * @version V1.0.0
 **/
@Mapper
public interface DagTaskMapper {

    /**
     * 删除某个Job配置的task信息
     * @param jobGroup
     * @param jobKey
     * @return
     */
    int deleteByJobKeyAndJobGroup(@Param("jobGroup")String jobGroup, @Param("jobKey")String jobKey);

    /**
     * 配置某个job的task信息
     * @param jobMTask
     * @return
     */
    int insertSelective(DagTask jobMTask);

    /**
     * 查询某个job配置的task信息
     * @param jobGroup
     * @param jobKey
     * @return
     */
    List<DagTask> selectByJobGroupAndKey(@Param("jobGroup")String jobGroup, @Param("jobKey")String jobKey);

    /**
     * 查询某个job配置的task信息 增加深度
     * TODO loadDagTask4Job 与 selectDagTask4ContainsIP 冗余
     * @param jobGroup
     * @param jobKey
     * @return
     * @see DagTaskMapper#loadDagTask4Job(String, String)
     */
    @Deprecated
    List<JobMTaskVO> selectDagTask4ContainsIP(@Param("jobGroup")String jobGroup, @Param("jobKey")String jobKey);

    /**
     * 查询对接项目总数和任务总数
     * @return
     */
    Map<String, String> selectActuatorsAndJobCount();

    /**
     * 查询Job的task配置信息
     * 1. 包含预警邮箱信息
     * 2. 包含持久化执行器信息
     * @param jobGroup
     * @param jobKey
     * @return
     */
    List<DagTask> loadDagTask4Job(@Param("jobGroup")String jobGroup, @Param("jobKey")String jobKey);

    /**
     * 获取各个JobGroup下的job与task及联系人列表
     * @param groups
     * @return
     */
    List<Map<String, Object>> selectJobGroupDetails(@Param("groups")List<String> groups);
}
