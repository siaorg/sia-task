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

package com.sia.task.register.zookeeper.impl;

import com.sia.task.core.IExecutorSelector;
import com.sia.task.core.entity.BasicTask;
import com.sia.task.core.task.DagTask;
import com.sia.task.core.util.Constant;
import com.sia.task.integration.curator.Curator4Scheduler;
import com.sia.task.register.zookeeper.annotations.EnableZkRegister4Scheduler;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * <p>
 * The default implementation of IExecutorSelector.
 * </p>
 *
 * <p>
 * If the display uses the <code>EnableZk</code> annotation,
 * the implementation is used to get the dispatcher instance,
 * based on the zookeeper implementation.
 * </p>
 *
 * @author maozhengwei
 * @version V1.0.0
 * @data 2019-10-18 14:39
 * @see EnableZkRegister4Scheduler
 **/
public class DagTaskExecutorSelector implements IExecutorSelector {

    private static final String sql = "select sbt.* from skyworld_basic_task sbt where sbt.task_key = ?";

    @Resource
    private JdbcTemplate jdbcTemplate;
    @Resource
    private Curator4Scheduler curator4Scheduler;

    /**
     * get an executor instance
     *
     * @param task
     * @return
     */
    @Override
    public List<String> getTaskExecutor(DagTask task) throws Exception {
        if (Constant.ENDTASK.equals(task.getTaskKey())) {
            return Collections.EMPTY_LIST;
        }
        if (Constant.TASK_SOURCE_ZK.equals(task.getTaskSource())) {
            List<String> executors = curator4Scheduler.getExecutors(task.getTaskKey());
            return executors == null ? Collections.EMPTY_LIST : executors;
        }
        BasicTask basicTask = jdbcTemplate.queryForObject(sql, new BeanPropertyRowMapper<>(BasicTask.class), task.getTaskKey());
        return basicTask == null ? Collections.EMPTY_LIST : Arrays.asList(basicTask.getTaskAppIpPort().split(Constant.REGEX_COMMA));
    }
}
