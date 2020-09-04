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

package com.sia.task.admin.vo;

import com.sia.task.core.util.JsonHelper;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Map;

/**
 * 1、对于成功的返回，有data的返回data，message是"成功"，没有data的data为null，message是"成功"；
 * 2、对于失败的返回，一般失败返回，message是"失败"，特殊失败返回，message是失败原因。
 *
 * @author maozhengwei
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Data
public class SiaResponseBody<T extends Object> implements Serializable {

    /**
     * 返回码
     */
    private int code = 0;

    /**
     * 错误
     */
    private String error;

    /**
     * 返回消息
     */
    private String message;

    /**
     * 返回数据
     */
    private T data;

    /**
     * 元数据
     */
    private Map<String, Object> extra;

    private SiaResponseBody(T data) {
        this(data, null);
    }

    private SiaResponseBody(T data, String message) {
        this(data, message, null);
    }

    private SiaResponseBody(T data, String message, String error) {
        this(data, message, error, null);
    }

    private SiaResponseBody(T data, String message, String error, Map<String, Object> extra) {
        this.code = 0;
        if ("error".equals(error)) {
            this.code = 1;
        }
        this.data = data;
        this.message = message;
        this.error = error;
        this.extra = extra;
    }

    public static String isOk(boolean isSuccess) {
        return isSuccess ? success() : failure();
    }

    public static String isOk(boolean isSuccess, Object data) {
        return isSuccess ? success(data) : failure(data);
    }

    public static String isOk(boolean isSuccess, Object data, String message) {
        return isSuccess ? success(data, message) : failure(data, message);
    }

    public static String isOk(boolean isSuccess, Object data, String message, String error) {
        return isSuccess ? success(data, message, error) : failure(data, message, error);
    }

    public static String isOk(boolean isSuccess, Object data, String message, String error, Map extra) {
        return isSuccess ? success(data, message, error, extra) : failure(data, message, error, extra);
    }

    public static String success() {
        return success(null, ResponseCodeEnum.SUCCESS.message);
    }

    public static <T> String success(T data) {
        return success(data, ResponseCodeEnum.SUCCESS.message);
    }

    public static <T> String success(T data, String message) {
        return success(data, message, null);
    }

    public static <T> String success(T data, String message, String error) {
        return success(data, message, error, null);
    }

    public static <T> String success(T data, String message, String error, Map extra) {
        return JsonHelper.toString(onSuccess(data, message, error, extra));
    }

    public static String failure() {
        return failure(null, ResponseCodeEnum.FAIL.message);
    }

    public static String failure(ResponseCodeEnum responseCodeEnum) {
        return failure(null, responseCodeEnum.message);
    }

    public static <T> String failure(T data) {
        return failure(data, ResponseCodeEnum.FAIL.message);
    }

    public static <T> String failure(T data, String message) {
        return failure(data, message, "error");
    }

    public static <T> String failure(T data, String message, String error) {
        return failure(data, message, error, null);
    }

    public static <T> String failure(T data, String message, String error, Map extra) {
        return JsonHelper.toString(onFailure(data, message, error, extra));
    }

    private static SiaResponseBody onSuccess(Object data, String message, String error, Map extra) {
        return new SiaResponseBody(data, message, error, extra);
    }

    private static SiaResponseBody onFailure(Object data, String message, String error, Map extra) {
        return new SiaResponseBody(data, message, error, extra);
    }

    /**
     * 定义返回码
     */
    public enum ResponseCodeEnum {

        /**
         * 返回码 类型
         */
        SUCCESS(1000, "成功"),
        FAIL(4000, "失败"),
        WARNING(5000, "警告"),

        /**
         * 客服端错误
         */
        REQUEST_FAIL_PARAM(4001, "参数输入不正确"),
        REQUEST_FAIL_NOT_AUTH(4002, "没有权限"),

        /**
         * 服务端错误
         */
        ERROR(5000, "异常"),
        SERVICE_FAIL_NOT_FOUND(5001, "服务未找到"),
        TASK_CHECK(5002, "该TASK已被JOB引用，不能删除"),
        DAG_CHECK(5003, "该TASK配置存在环路"),
        JOB_NO_TASK_CONFIG(5004, "该JOB没有配置TASK"),
        TASK_ALREADY_EXISTED(5005, "Task_Key已存在"),
        JOB_ALREADY_EXISTED(5006, "Job_Key已存在");


        int code;

        String message;

        ResponseCodeEnum() {
        }

        ResponseCodeEnum(int code, String message) {
            this.code = code;
            this.message = message;
        }

        public int getCode() {
            return code;
        }

        public String getMessage() {
            return message;
        }
    }

}
