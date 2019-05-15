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

import com.sia.core.entity.BasicJob;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 *
 *
 * @description BasicJobMapper
 * @see
 * @author MAOZW
 * @date 2018-04-18 11:10
 * @version V1.0.0
 **/
@Mapper
public interface BasicJobMapper {

    /**
     *
     * delete job
     * {@link } can be checked for the result.
     * @param
     * @return
     * @throws
     */
    int deleteByJobKeyAndJobGroup(Map<String, String> param);

    /**
     *
     * insert job
     * {@link } can be checked for the result.
     * @param
     * @return
     * @throws
     */
    int insertSelective(BasicJob record);

    /**
     *
     * lookup job
     * {@link } can be checked for the result.
     * @param
     * @return
     * @throws
     */
    List<BasicJob> selectByJobKeyAndJobGroupList(Map<String, Object> param);

    /**
     *
     * update job by primary key
     * {@link } can be checked for the result.
     * @param
     * @return
     * @throws
     */
    int updateByPrimaryKey(BasicJob record);

    /**
     *
     * acquire list of auth
     * {@link } can be checked for the result.
     * @param
     * @return
     * @throws
     */
    List<BasicJob> selectAuth(List<String> roleNames);

    /**
     *
     * return the count of job
     * {@link } can be checked for the result.
     * @param
     * @return
     * @throws
     */
    int selectCountBasicJobs(Map<String, Object> param);

    /**
     *
     * acquire job according jobKey and jobGroup
     * {@link } can be checked for the result.
     * @param
     * @return
     * @throws
     */
    BasicJob selectByJobKeyAndJobGroup(Map<String, String> param);

    /**
     *
     * check if job has post task
     * {@link } can be checked for the result.
     * @param
     * @return
     * @throws
     */
    BasicJob selectChilds(@Param("jobKey") String jobKey);

    /**
     *
     * return list of jobs
     * {@link } can be checked for the result.
     * @param
     * @return
     * @throws
     */
    List<BasicJob> selectByJobKeyList(Map<String, Object> param);

    /**
     *
     * return group name and the according job count
     * {@link } can be checked for the result.
     * @param
     * @return
     * @throws
     */
    List<Map<String, Integer>> selectGroupAndJobNum(Map<String, Object> param);

    /**
     *
     * return list of jobs according the field jobPlan
     * {@link } can be checked for the result.
     * @param
     * @return
     * @throws
     */
    List<BasicJob> selectJobsByPlan(String jobPlan);
}
