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

package com.sia.scheduler.service;

import com.sia.core.entity.BasicTask;
import com.sia.core.mapper.BasicTaskMapper;
import com.sia.scheduler.util.constant.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * BasicTaskService
 *
 * @see
 * @author maozhengwei
 * @date 2018-04-18 10:22
 * @version V1.0.0
 **/
@Service
public class BasicTaskService {

    private static final Logger LOGGER = LoggerFactory.getLogger(BasicTaskService.class);

    @Autowired
    private BasicTaskMapper basicTaskMapper;


    /**
     * Insert or update Task information
     * @param basicTask
     * @return
     */
    public int insertOrUpdateByTaskKey(BasicTask basicTask) {
        BasicTask taskOld = basicTaskMapper.selectTasksByAppNameOrGroupName(basicTask);
        if (taskOld != null) {
            LOGGER.info(Constants.LOG_PREFIX + " 更新Task {}",basicTask);
            return basicTaskMapper.updateByTaskKey(basicTask);
        }
        LOGGER.info(Constants.LOG_PREFIX + " 插入 Task {}",basicTask);
        return basicTaskMapper.insertOrUpdateByTaskKey(basicTask);
    }

}
