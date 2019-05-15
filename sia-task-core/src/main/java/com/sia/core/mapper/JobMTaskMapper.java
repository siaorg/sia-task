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

import com.sia.core.entity.JobMTask;
import com.sia.core.web.vo.JobMTaskVO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Map;

/**
 *
 *
 * @description JobMTaskMapper
 * @see
 * @author MAOZW
 * @date 2018-04-18 11:10
 * @version V1.0.0
 **/
@Mapper
public interface JobMTaskMapper {

    /**
     *
     * delete configurated tasks of job
     * {@link } can be checked for the result.
     * @param
     * @return
     * @throws
     */
    int deleteByJobKeyAndJobGroup(Map<String, String> param);

    /**
     *
     * configurate tasks of job
     * {@link } can be checked for the result.
     * @param
     * @return
     * @throws
     */
    int insertSelective(JobMTask jobMTask);

    /**
     *
     * lookup configurated task info of job
     * {@link } can be checked for the result.
     * @param
     * @return
     * @throws
     */
    List<JobMTask> selectByJobGroupAndKey(Map<String, String> param);

    /**
     * 查询某个job配置的task信息 增加深度
     * @param param
     * @return
     */
    List<JobMTaskVO> selectTaskMJobAndIPListByJobGroupAndKeyVO(Map<String, String> param);

    /**
     *
     * whether task has been referenced
     * {@link } can be checked for the result.
     * @param
     * @return
     * @throws
     */
    List<JobMTask> selectJobMTaskTaskKey(String taskKey);

    /**
     *
     * acquire group count and job count
     * {@link } can be checked for the result.
     * @param
     * @return
     * @throws
     */
    Map<String, String> selectActuatorsAndJobCount();

    /**
     *
     * lookup task config info of job
     * {@link } can be checked for the result.
     * @param
     * @return
     * @throws
     */
    List<JobMTask> selectTaskMJobAndIPListByJobGroupAndKey(Map<String,String> param);

    /**
     *
     * acquire job count, task count and emails of jobGroup
     * {@link } can be checked for the result.
     * @param
     * @return
     * @throws
     */
    List<Map<String, Object>> selectJobGroupDetails(Map<String, Object> param);
}
