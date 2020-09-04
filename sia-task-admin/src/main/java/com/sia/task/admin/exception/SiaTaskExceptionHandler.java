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

package com.sia.task.admin.exception;

import com.sia.task.admin.vo.SiaResponseBody;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.servlet.http.HttpServletRequest;

//import org.springframework.security.access.AccessDeniedException;

/**
 * @author maozhengwei
 * @version V1.0.0
 * @description
 * @data 2019-08-30 14:57
 * @see
 **/
@RestControllerAdvice
@Slf4j
public class SiaTaskExceptionHandler {

    @ExceptionHandler(SiaTaskException.class)
    @ResponseBody
    public String handleAmsException(HttpServletRequest request, SiaTaskException ex) {
        String responseBody = ex.getResponseBody();
        log.error("AmsException {}", responseBody);
        return responseBody;
    }

    @ExceptionHandler(Exception.class)
    @ResponseBody
    public String handleException(HttpServletRequest request, Exception ex) {
        log.info("handleException : ", ex);
        String responseBody;
        if (ex instanceof DuplicateKeyException) {
            responseBody = SiaResponseBody.failure(null, "已添加，无须重复操作", ex.getMessage());
        } //else if (ex instanceof AccessDeniedException){
          //  resultBody = new ResultBody(401, ex.getMessage(), "没有操作权限");
        //}
        else if (ex instanceof MissingServletRequestParameterException) {
            responseBody = SiaResponseBody.failure(null, "请求参数不能匹配，请检查输入参数", ex.getMessage());
        }else {
            responseBody = SiaResponseBody.failure(null, "输入参数不合法或者为空，请检查输入参数", ex.getMessage());
        }
        return responseBody;
    }

}
