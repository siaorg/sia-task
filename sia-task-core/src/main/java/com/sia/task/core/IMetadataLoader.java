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

package com.sia.task.core;

import com.sia.task.core.entity.BasicJob;
import com.sia.task.core.entity.BasicTask;
import com.sia.task.core.task.DagTask;

import java.util.List;

/**
 * 加载器
 *
 * @author maozhengwei
 * @version V1.0.0
 * @data 2020/4/29 12:06 下午
 **/
public interface IMetadataLoader {

    /**
     * 加载Job的元数据，该接口具体有实现定义，元数据具体是从何处进行加载，(如：db, redid, ram, zk, ...)
     *
     * @param jobGroup 项目组
     * @param jobKey   job唯一标识
     * @return
     */
    BasicJob loadJob(String jobGroup, String jobKey);

    /**
     * 加载指定Job的Dag任务集合，由于该接口返回数据是存储在某一指定媒介中，可能在存储期间数据发生变化，改变其dag的合法性，
     * 建议在使用期间进行DAG检验；
     *
     * @param jobGroup 项目组
     * @param jobKey   job唯一标识
     * @return
     */
    List<DagTask> loadDagTask4Job(String jobGroup, String jobKey);

    /**
     * PersistentMetadata
     *
     * @param basicTask
     * @return
     */
    int saveTaskMetadata(BasicTask basicTask);
}
