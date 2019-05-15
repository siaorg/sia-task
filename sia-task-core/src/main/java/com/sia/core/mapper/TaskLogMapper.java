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

import com.sia.core.entity.TaskLog;
import org.apache.ibatis.annotations.Mapper;

import java.util.Map;

/**
 *
 *
 * @description TaskLogMapper
 * @see
 * @author MAOZW
 * @date 2018-04-18 11:10
 * @version V1.0.0
 **/
@Mapper
public interface TaskLogMapper {

    /**
     *
     * insert taskLog
     * {@link } can be checked for the result.
     * @param
     * @return
     * @throws
     */
    int insertSelective(TaskLog record);

    /**
     *
     * delete taskLog
     * {@link } can be checked for the result.
     * @param
     * @return
     * @throws
     */
    int deleteTaskLogByDate(Map<String, String> param);


}
