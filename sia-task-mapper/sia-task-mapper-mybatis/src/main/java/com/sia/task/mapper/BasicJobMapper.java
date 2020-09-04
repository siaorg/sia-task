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

import com.sia.task.core.entity.BasicJob;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * BasicJobMapper
 *
 * @author maozhengwei
 * @version V1.0.0
 * @data 2018/4/18 4:53 下午
 **/
@Mapper
public interface BasicJobMapper {

    /**
     * 删除Job
     *
     * @param jobGroup
     * @param jobKey
     * @return
     */
    int deleteByJobKeyAndJobGroup(@Param("jobGroup") String jobGroup, @Param("jobKey") String jobKey);

    /**
     * 插入
     *
     * @param record
     * @return
     */
    int insertSelective(BasicJob record);

    /**
     * 查询符合指定过滤条件的Job
     *
     * @param groups  项目组名称
     * @param jobKey  唯一键
     * @param jobDesc job描述信息
     * @return
     */
    List<BasicJob> selectJobsByCondition(@Param("groups") List<String> groups, @Param("jobKey") String jobKey, @Param("jobDesc") String jobDesc);

    /**
     * 更新接口
     *
     * @param record
     * @return
     */
    int updateByPrimaryKey(BasicJob record);

    /**
     * 获取权限列表
     *
     * @param roleNames
     * @return
     */
    List<BasicJob> selectGroupByAuth(List<String> roleNames);

    /**
     * 查询符合指定过滤条件的Job数量
     *
     * @param jobGroup  项目组名称
     * @param jobKey  唯一键
     * @param jobDesc job描述信息
     * @return
     */
    int selectJobCountByCondition(@Param("jobGroup") String jobGroup, @Param("jobKey") String jobKey, @Param("jobDesc") String jobDesc);

    /**
     * 根据Key Group 获取Job
     *
     * @param jobGroup
     * @param jobKey
     * @return
     */
    BasicJob selectJob(@Param("jobGroup") String jobGroup, @Param("jobKey") String jobKey);

    /**
     * 查下Job是否存在后置任务
     *
     * @param jobKey
     * @return
     */
    BasicJob selectJob4Childs(@Param("jobKey") String jobKey);

    /**
     * 获取JOB列表
     *
     * @param jobKeys
     * @return
     */
    List<BasicJob> selectByJobKeyList(@Param("jobKeys") List<String> jobKeys);

    /**
     * 获取JOB管理中项目名称和JOB数量
     *
     * @param group
     * @return
     */
    List<Map<String, Integer>> selectJopCount4Group(@Param("groups") List<String> group);

    /**
     * 增加根据关联查询job的逻辑
     *
     * @param jobPlan
     * @return
     */
    List<BasicJob> selectJobsByPlan(String jobPlan);
}
