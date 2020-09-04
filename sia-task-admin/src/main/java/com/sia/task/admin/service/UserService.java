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

package com.sia.task.admin.service;

import com.sia.task.core.util.Constant;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author: MAOZW
 * @Description: 登陆用户信息处理类
 * @date 2018/9/19 15:58
 */
@Slf4j
public class UserService {
    /**
     * 获取当前用户的角色信息
     *
     * @return
     */
    public static List<String> getCurrentUserAllRoles() {
        // 此处返回的是用户的数据权限，
        // 返回不同的角色信息可查看不同的用户组的任务数据；
        // admin 权限是用户管理员使用：调度器管理和zookeeper管理 - 可参照页面
        // eg.
        List<String> roles = Arrays.asList("admin", "simple", "apple923", "sia", "sianew", "apple924");
        log.info(Constant.LOG_PREFIX + " userName: [ {} ], authority: [ {} ] ", UserService.getCurrentUser(), roles);
        return new ArrayList<>(roles);
    }

    /**
     * 获取当前用户信息
     *
     * @return
     */
    public static String getCurrentUser() {
        return "admin";
    }
}
