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

package com.sia.task.core.http;

import com.sia.task.core.util.JsonHelper;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author: MAOZW
 * @Description: AsyncResponse
 * @date 2018/9/28 11:19
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Data
public class SiaHttpResponse extends AbstractResponse /*implements IResponse*/ {

    private static final long serialVersionUID = 5201675651800011504L;

    private String result;

    private String mess;

    private String ext;

    private SiaHttpResponse(String json) {
        SiaHttpResponse response = JsonHelper.toObject(json, SiaHttpResponse.class);
        this.result = response.result;
        this.status = response.status;
        this.mess = response.mess;
        this.ext = response.ext;
    }

    private SiaHttpResponse(String result, ResponseStatus status) {
        this(result, status, null);
    }

    private SiaHttpResponse(String result, ResponseStatus status, String mess) {
        this(result, status, null,null);
    }

    private SiaHttpResponse(String result, ResponseStatus status, String mess, String ext) {
        //super(status);
        super(new ResponseStatusAdapted(status));
        this.result = result;
        this.mess = mess;
        this.ext = ext;
    }

    public static String isOK(boolean isSuccess) {
        return isSuccess ? success() : failure();
    }

    public static String isOK(boolean isSuccess, String mess) {
        return isSuccess ? success(mess) : failure(mess);
    }

    public static String isOK(boolean isSuccess, String result, String mess) {
        return isSuccess ? success(result, mess) : failure(result, mess);
    }

    public static String isOK(boolean isSuccess, String result, String mess, String ext) {
        return isSuccess ? success(result, mess, ext) : failure(result, mess, ext);
    }

    public static String success() {
        return success(null);
    }

    public static String success(String mess){
        return success(null, mess);
    }

    public static String success(String result, String mess){
        return success(result, mess,null);
    }

    public static String success(String result, String mess, String ext){
        return JsonHelper.toString(onSuccess(result, mess, ext));
    }

    public static String failure() {
        return failure(null);
    }

    public static String failure(String mess){
        return failure(null, mess);
    }

    public static String failure(String result, String mess){
        return failure(result, mess, null);
    }

    public static String failure(String result, String mess, String ext){
        return JsonHelper.toString(onFailure(result, mess, ext));
    }

    private static SiaHttpResponse onSuccess(String result, String mess, String ext) {
        return new SiaHttpResponse(result, ResponseStatus.success, mess, ext);
    }

    private static SiaHttpResponse onFailure(String data, String mess, String ext) {
        return new SiaHttpResponse(data, ResponseStatus.failed, mess, ext);
    }

    @Override
    public String toString() {
        return "OnlineResponse{" +
                "result=" + result +
                ", status=" + status +
                ", mess=" + mess +
                '}';
    }
}
