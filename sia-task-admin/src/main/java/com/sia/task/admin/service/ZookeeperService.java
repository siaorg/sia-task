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

package com.sia.task.admin.service;

import com.sia.task.core.util.Constant;
import com.sia.task.integration.curator.Curator4Scheduler;
import com.sia.task.integration.curator.ZkNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

/**
 * zookeeper 管理
 *
 * @author maozhengwei
 * @version V1.0.0
 * @data 2020/4/27 4:39 下午
 **/
@Slf4j
@Service
public class ZookeeperService {

    @Resource
    protected Curator4Scheduler curator4Scheduler;

    /**
     * 返回节点信息
     *
     * @param path
     * @return
     */
    public ZkNode loadNodeJson(String path) throws Exception {
        return curator4Scheduler.loadNodeJson(path);
    }

    /**
     * 按照存文本保存更新节点信息(传入Json字符串,节点信息放入content节点)
     *
     * @param request
     * @return
     */
    public boolean updateNodeJson(Map<String, String> request) throws Exception {
        curator4Scheduler.updateNodeJson(request.get("path"), request.get("nodeData"));
        return true;
    }

    /**
     * 根据path获取对应zk树。
     *
     * @return zk tree json
     */
    @Deprecated
    public ZkNode loadNodeTreeOfPath(String path) throws Exception {
        return curator4Scheduler.loadZkTreeOfPath(path);
    }

    /**
     * 根据path获取对应节点的zk子树。
     *
     * @return zk tree json
     */
    @Deprecated
    public List<ZkNode> loadSubTreeOfPath(String path) throws Exception {
        return curator4Scheduler.loadSubTreeOfPath(path);
    }

    /**
     * 根据path获取对应节点的zk子树。
     * 每次获取一层目录
     *
     * @return zk tree json
     */
    public ZkNode loadLevelOfPath(String path) throws Exception {
        log.info(Constant.LOG_PREFIX + " 获取zk一层子树信息 path [{}]", path);
        return curator4Scheduler.loadLevelOfPath(path);
    }

    /**
     * 根据path,添加新节点到对应zk树。
     *
     * @return
     */
    public boolean addNode4Path(@RequestBody Map<String, String> request) throws Exception {
        String path = request.get("path");
        String nodeName = request.get("nodeName");
        String nodeData = request.get("nodeData");
        curator4Scheduler.addZkNodeOfPath(path, nodeName, nodeData);
        log.info(Constant.LOG_PREFIX + " 新增节点 >>> 成功 :  path is{},nodeName is{} ", path, nodeName);
        return true;
    }

    /**
     * 根据path,删除对应zk节点。
     *
     * @return
     */
    public boolean deleteZkNodeOfPath(@RequestParam String path) throws Exception {
        return curator4Scheduler.deleteZkNodeOfPath(path) == null;
    }
}
