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

import com.sia.task.core.entity.BasicTask;
import com.sia.task.core.task.DagTask;
import com.sia.task.pojo.IndexGroupVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * <code>{@link BasicTask}</code> Mapper
 *
 * @author maozhengwei
 * @version V1.0.0
 * @date 2020/6/30 10:45 上午
 * @see
 **/
@Mapper
public interface BasicTaskMapper {

    /**
     * 删除task 通过taskKey
     *
     * @param taskKey
     * @return
     */
    int deleteByPrimaryKey(@Param("taskGroupName") String groupName, @Param("taskAppName") String appName, @Param("taskKey") String taskKey);

    /**
     * 插入task
     *
     * @param basicTask <code>{@link BasicTask}</code>
     * @return
     */
    int insertSelective(BasicTask basicTask);

    /**
     * 通过指定的过滤条件查询task
     *
     * @param groupName 项目组名称
     * @param appName   应用名称
     * @param taskKey   任务唯一键
     * @param taskDesc  任务描述
     * @return
     */
    List<BasicTask> selectTasksByCondition(@Param("taskGroupName") List<String> groupName,
                                           @Param("taskAppName") String appName,
                                           @Param("taskKey") String taskKey,
                                           @Param("taskDesc") String taskDesc);

    /**
     * 更新接口
     *
     * @param basicTask
     * @return
     */
    int updateByPrimaryKeySelective(BasicTask basicTask);

    /**
     * 通过指定的过滤条件查询task的数量
     *
     * @param groupName 项目组名称
     * @param appName   应用名称
     * @param taskKey   任务唯一键
     * @param taskDesc  任务描述
     * @return task的数量
     */
    int selectTaskCountByCondition(@Param("taskGroupName") List<String> groupName, @Param("taskAppName") String appName, @Param("taskKey") String taskKey, @Param("taskDesc") String taskDesc);

    /**
     * 插入或更新
     *
     * @param basicTask
     * @return
     */
    int insertOrUpdateByTaskKey(BasicTask basicTask);

    /**
     * 查询Task
     *
     * @param groupName 项目组名称
     * @param appName   应用名称
     * @param taskKey   任务唯一键
     * @return
     */
    List<DagTask> selectTaskInJob(@Param("taskGroupName") String groupName, @Param("taskAppName") String appName, @Param("taskKey") String taskKey);

    /**
     * 通过项目组名称查询Apps
     *
     * @param groupName 项目组名称
     * @return
     */
    List<String> selectAppsByGroup(String groupName);

    /**
     * 查询taskKey通过appName和groupName
     *
     * @param groupName 项目组名称
     * @param appName   应用名称
     * @return
     */
    List<String> selectTaskKeysByGroupAndApp(@Param("groupName") String groupName, @Param("appName") String appName);

    /**
     * 通过角色查询项目组
     *
     * @param roleNames 角色
     * @return
     */
    List<String> selectGroupsByAuth(List<String> roleNames);

    /**
     * 更新task
     *
     * @param basicTask
     * @return
     */
    int updateByTaskKey(BasicTask basicTask);

    /**
     * 任务管理界面项目名称和任务数量查询
     *
     * @param groups
     * @return
     */
    List<Map<String, Integer>> selectGroupAndCountI(@Param("groups") List<String> groups);

    /**
     * 根据TaskKey查询Task
     *
     * @param taskKey
     * @return
     */
    BasicTask selectTaskByTaskKey(@Param("taskKey") String taskKey);

    /**
     * 返回导航页项目组列表 根据权限过滤
     *
     * @param currentUserAllRoles 权限
     * @return 项目组列表
     */
    List<IndexGroupVo> getGroupList(List<String> currentUserAllRoles);
}
