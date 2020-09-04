/*-
 * <<
 * sia-task
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

import com.sia.task.admin.service.BasicTask4Service;
import com.sia.task.admin.service.UserService;
import com.sia.task.admin.vo.SiaResponseBody;
import com.sia.task.core.util.Constant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * UI菜单显示控制
 *
 * @author maozhengwei
 * @version V1.0.0
 * @date 2020/7/1 5:49 下午
 **/
@RestController
@RequestMapping("/ui")
public class IndexController {
    private final Logger LOGGER = LoggerFactory.getLogger(IndexController.class);

    @Autowired
    private BasicTask4Service task4Service;

    @Deprecated
    @RequestMapping("/auth")
    public String auth() {
        List<String> roleNames = UserService.getCurrentUserAllRoles();
        LOGGER.info(Constant.LOG_PREFIX + "页面权限加载   roles {}", roleNames);
        return SiaResponseBody.success(roleNames);
    }

    /**
     * 如果如要定制页面，卡在此处进行替换处理；
     * 以下是返回给前端的menuList Json 数据；
     */
    String menuList = "{\"code\":0,\"error\":null,\"message\":\"成功\",\"data\":[" +
            "{\"auth\":{\"menuName\":\"调度器管理\",\"menuOrder\":4,\"pageUrl\":\"/dispatch-manage\"}}," +
            "{\"auth\":{\"menuName\":\"zookeeper管理\",\"menuOrder\":7,\"pageUrl\":\"/zk-manage\"}}," +
            "{\"auth\":{\"menuName\":\"用户首页\",\"menuOrder\":2,\"pageUrl\":\"/user-home\"}}," +
            "{\"auth\":{\"menuName\":\"调度日志-本地\",\"menuOrder\":8,\"pageUrl\":\"/task-log-list\"}}," +
            "{\"auth\":{\"menuName\":\"上帝视角\",\"menuOrder\":1,\"pageUrl\":\"/admin-home\"}}," +
            "{\"auth\":{\"menuName\":\"JOB管理\",\"menuOrder\":6,\"pageUrl\":\"/job-manage-list\"}}," +
            "{\"auth\":{\"menuName\":\"TASK管理\",\"menuOrder\":5,\"pageUrl\":\"/task-manage-list\"}}," +
            "{\"auth\":{\"menuName\":\"调度监控\",\"menuOrder\":3,\"pageUrl\":\"/monitor-manage\"}}],\"extra\":null}";
    /**
     * 返回左侧菜单列表
     * 不同用户组权限返回权限内的列表信息
     *
     * @return
     */
    @RequestMapping("/menuList")
    public String menuList() {
        LOGGER.info(Constant.LOG_PREFIX + "菜单权限加载  menuList {}", menuList);
        return menuList;
    }

    /**
     * 返回导航页项目列表
     * 不同用户组权限返回权限内的列表信息
     *
     * @return
     */
    @RequestMapping("/groupList")
    public String groupList() {
        return SiaResponseBody.success(task4Service.getGroupList());
    }

    @RequestMapping("/currentUserName")
    public String getCurrentUserName() {
        return SiaResponseBody.success(UserService.getCurrentUser());
    }

}
