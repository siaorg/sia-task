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

package com.sia.config.web.controller;

import com.sia.core.web.vo.ResultBody;
import com.sia.config.web.constants.Constants;
import com.sia.config.web.filter.AuthInterceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * UI display control
 *
 * @author maozhengwei
 * @version V1.0.0
 * @date 2019-04-28 15:40
 **/
@RestController
@RequestMapping("/ui")
public class AuthController {
    private final Logger LOGGER = LoggerFactory.getLogger(AuthController.class);

    @Autowired
    private AuthInterceptor userService;

    @Autowired
    private HttpServletRequest httpServletRequest;

    /**
     * Get the role list
     *
     * @return the role list String
     */
    @RequestMapping(value = "/auth", method = {RequestMethod.POST, RequestMethod.GET})
    public String auth() {
        List<String> roleNames = userService.getCurrentUserAllRoles();
        LOGGER.info(Constants.LOG_PREFIX + "Page permission loading   roles {}", roleNames);
        return ResultBody.success(roleNames);
    }

    /**
     * login
     *
     * @return ResultBody String
     */
    @RequestMapping(value = "/login", method = {RequestMethod.POST, RequestMethod.GET})
    public String login(String userName, String roleName, String isAdmin) {
        LOGGER.info(Constants.LOG_PREFIX + "Page permission loading userName:{},roleName:{},isAdmin:{}", userName, roleName, isAdmin);
        HttpSession session = httpServletRequest.getSession();
        session.setAttribute("currentUser", userName);

        List<String> list = new ArrayList<>();
        list.addAll(Arrays.asList(roleName.split(",")));
        if ("true".equals(isAdmin)) {
            list.add("admin");
        }

        session.setAttribute("roleNames", list);

        List<String> roleNames = userService.getCurrentUserAllRoles();
        LOGGER.info(Constants.LOG_PREFIX + "Page permission loading   roles {}", roleNames);
        return ResultBody.success(roleNames);
    }

    /**
     * logout
     *
     * @return ResultBody String
     */
    @RequestMapping(value = "/logout", method = {RequestMethod.POST, RequestMethod.GET})
    public String logout() {

        HttpSession session = httpServletRequest.getSession();
        session.removeAttribute("currentUser");
        session.removeAttribute("roleNames");
        return ResultBody.success("logout");
    }

}
