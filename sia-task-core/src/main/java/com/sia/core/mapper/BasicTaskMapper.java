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

import com.sia.core.entity.BasicTask;
import com.sia.core.entity.JobMTask;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 *
 *
 * @description BasicTaskMapper
 * @see
 * @author MAOZW
 * @date 2018-04-18 11:10
 * @version V1.0.0
 **/
@Mapper
public interface BasicTaskMapper {

    /**
     *
     * delete task by taskKey
     * {@link } can be checked for the result.
     * @param
     * @return
     * @throws
     */
    int deleteByPrimaryKey(Map<String, String> taskKey);

    /**
     *
     * insert task
     * {@link } can be checked for the result.
     * @param
     * @return
     * @throws
     */
    int insertSelective(BasicTask basicTask);

    /**
     *
     * return data through auth
     * {@link } can be checked for the result.
     * @param
     * @return
     * @throws
     */
    List<BasicTask> selectAuth(List<String> roleNames);

    /**
     *
     * return list of tasks through appName and groupName
     * {@link } can be checked for the result.
     * @param
     * @return
     * @throws
     */
    List<BasicTask> selectTasksByAppNameOrGroupNameL(Map<String, Object> param);

    /**
     *
     * update task by primary key
     * {@link } can be checked for the result.
     * @param
     * @return
     * @throws
     */
    int updateByPrimaryKeySelective(BasicTask basicTask);

    /**
     *
     * return the count of tasks
     * {@link } can be checked for the result.
     * @param
     * @return
     * @throws
     */
    int selectCountbasicTasks(Map<String, Object> param);

    /**
     *
     * insert or update by taskKey
     * {@link } can be checked for the result.
     * @param
     * @return
     * @throws
     */
    int insertOrUpdateByTaskKey(BasicTask basicTask);

    /**
     *
     * return tasks in job
     * {@link } can be checked for the result.
     * @param
     * @return
     * @throws
     */
    List<JobMTask> selectTaskInJob(Map<String, Object> param);

    /**
     *
     * return list of appNames by groupName
     * {@link } can be checked for the result.
     * @param
     * @return
     * @throws
     */
    List<String> selectAppsByGroup(String groupName);

    /**
     *
     * return taskKeys by appName and groupName
     * {@link } can be checked for the result.
     * @param
     * @return
     * @throws
     */
    List<String> selectTaskKeysByGroupAndApp(@Param("groupName") String groupName, @Param("appName") String appName);

    /**
     *
     * return list of groupNames by roleNames
     * {@link } can be checked for the result.
     * @param
     * @return
     * @throws
     */
    List<String> selectGroupsByAuth(List<String> roleNames);

    /**
     *
     * return task according to appName and groupName
     * {@link } can be checked for the result.
     * @param
     * @return
     * @throws
     */
    BasicTask selectTasksByAppNameOrGroupName(BasicTask basicTask);

    /**
     *
     * update task by taskKey
     * {@link } can be checked for the result.
     * @param
     * @return
     * @throws
     */
    int updateByTaskKey(BasicTask basicTask);

    /**
     *
     * return all the groupNames and the according task count
     * {@link } can be checked for the result.
     * @param
     * @return
     * @throws
     */
    List<Map<String, Integer>> selectGroupAndCountI(Map<String, Object> param);

}
