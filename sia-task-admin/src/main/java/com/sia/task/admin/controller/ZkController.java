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

package com.sia.task.admin.controller;

import com.sia.task.admin.service.ZookeeperService;
import com.sia.task.admin.vo.SiaResponseBody;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.Map;

/**
 * 编排中心zookeeper的API类
 *
 * @author maozhengwei
 * @version V1.0.0
 * @date 2020/9/4 6:36 下午
 **/
@RestController
@RequestMapping("/zkapi")
public class ZkController {

    @Resource
    private ZookeeperService zookeeperService;

    /**
     * 按照字符串格式返回节点信息
     *
     * @param path
     * @return
     */
    @GetMapping("/loadZKNodeJson")
    public String loadZKNodeJson(@RequestParam String path) throws Exception {
        return SiaResponseBody.success(zookeeperService.loadNodeJson(path));
    }

    /**
     * 更新节点content信息
     *
     * @param request
     * @return
     */
    @PostMapping(path = "/updateZKNodeJson", consumes = "application/json")
    public String updateZKNodeJson(@RequestBody Map<String, String> request) throws Exception {
        return SiaResponseBody.success(zookeeperService.updateNodeJson(request));
    }


    /**
     * 根据path获取对应zk树。
     *
     * @return zk tree json
     */
    @GetMapping("/loadZkTreeOfPath")
    @Deprecated
    public String loadZkTreeOfPath(@RequestParam String path) throws Exception {
        return SiaResponseBody.success(zookeeperService.loadNodeTreeOfPath(path));
    }

    /**
     * 根据path获取对应节点的node子树。
     *
     * @return zk tree json
     */
    @GetMapping("/loadSubTreeOfPath")
    @Deprecated
    public String loadSubTreeOfPath(@RequestParam String path) throws Exception {
        return SiaResponseBody.success(zookeeperService.loadSubTreeOfPath(path));
    }

    /**
     * 根据path获取对应节点的zk子树。
     * 每次获取一层目录
     *
     * @return zk tree json
     */
    @GetMapping("/loadLevelOfPath")
    public String loadLevelOfPath(@RequestParam String path) throws Exception {
        return SiaResponseBody.success(zookeeperService.loadLevelOfPath(path));
    }


    /**
     * 根据path,添加新节点到对应zk树。
     *
     * @return
     */
    @PostMapping(path = "/addZkNodeOfPath")
    public String addZkNodeOfPath(@RequestBody Map<String, String> request) throws Exception {
        return SiaResponseBody.success(zookeeperService.addNode4Path(request));
    }


    /**
     * 根据path,删除对应zk节点。
     *
     * @return
     */
    @GetMapping("/deleteZkNodeOfPath")
    public String deleteZkNodeOfPath(@RequestParam String path) throws Exception {
        return SiaResponseBody.success(zookeeperService.deleteZkNodeOfPath(path));
    }
}
