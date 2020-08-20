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

package com.sia.config.web.filter;

import com.sia.config.web.constants.Constants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.Collections;
import java.util.List;

/**
 * Login user information processing class
 * @see
 * @author maozhengwei
 * @date 2019-04-28 15:40
 * @version V1.0.0
 **/
@Service
public class AuthInterceptor {

    @Autowired
    private HttpServletRequest request;

    /**
     * Get the role information of the current user and null the admin role
     * (for data retrieval field matching, the administrator defaults to null and returns all)
     * @return
     */
    public List<String> getCurrentUserRoles(){
        List<String> roleNames;
        HttpSession session = request.getSession();
        roleNames = (List<String>) session.getAttribute("roleNames");
        if (roleNames == null) {
            roleNames = Collections.emptyList();
        }
        if (roleNames.contains(Constants.ADMIN_ROLE)) {
            roleNames = null;
        }
        return roleNames;
    }
    /**
     * Gets the role information for the current user
     * @return
     */
    public List<String> getCurrentUserAllRoles(){
        List<String> roleNames;
        HttpSession session = request.getSession();
        roleNames = (List<String>) session.getAttribute("roleNames");
        return roleNames;
    }
    /**
     * Gets the current user information
     * @return
     */
    public String getCurrentUser(){
        String currentUser;
        HttpSession session = request.getSession();
        currentUser = (String) session.getAttribute("currentUser");
        return currentUser;
    }
}
