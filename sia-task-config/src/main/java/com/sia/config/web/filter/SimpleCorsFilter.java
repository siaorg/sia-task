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

import com.sia.core.helper.StringHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * SimpleCORSFilter
 * @author maozhengwei
 * @date 2019-04-28 15:40
 * @version V1.0.0
 **/
@Component
public class SimpleCorsFilter implements Filter {

    private final static Logger LOGGER = LoggerFactory.getLogger(SimpleCorsFilter.class);
    private static final String METHOD_OPTIONS = "OPTIONS";

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest httpServletRequest = (HttpServletRequest) servletRequest;
        HttpServletResponse httpServletResponse = (HttpServletResponse) servletResponse;
        httpServletResponse.setHeader("Access-Control-Allow-Origin",  httpServletRequest.getHeader("Origin"));
        httpServletResponse.setHeader("Access-Control-Allow-Methods", "POST, GET, OPTIONS, DELETE，PUT");
        httpServletResponse.setHeader("Access-Control-Max-Age", "3600");
        httpServletResponse.setHeader("Access-Control-Allow-Headers", "Authentication,Origin,X-Requested-With,Content-Type,Accept");
        httpServletResponse.setHeader("Access-Control-Allow-Credentials", "true");
        httpServletResponse.setHeader("XDomainRequestAllowed","1");

        String method = httpServletRequest.getMethod();

        if (METHOD_OPTIONS.equals(method)){
            filterChain.doFilter(httpServletRequest, servletResponse);
        } if (httpServletRequest.getRequestURI().contains("login")){
            filterChain.doFilter(httpServletRequest, httpServletResponse);
        }
        else{
            filterChain.doFilter(httpServletRequest, httpServletResponse);
        }
    }


    boolean checkLogin(HttpServletRequest httpServletRequest){
        HttpSession session = httpServletRequest.getSession();
        String user = (String) session.getAttribute("currentUser");
        if (StringHelper.isEmpty(user)) {
            return false;
        }
        return true;
    }

    @Override
    public void destroy() {

    }
}
