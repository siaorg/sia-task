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

package com.sia.hunter.exception;

import org.springframework.web.client.RestClientException;

/**
 * OnlineTask Call Exception wrap class
 * @see
 * @author huangqian
 * @date 2018-06-29 11:13
 * @version V1.0.0
 **/
public class OnlineTaskCallerException extends RestClientException {

    private RestClientException restClientException;
    private String body;

    public RestClientException getRestClientException() {
        return restClientException;
    }

    public void setRestClientException(RestClientException restClientException) {
        this.restClientException = restClientException;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }


    public OnlineTaskCallerException(String msg, RestClientException restClientException, String body) {
        super(msg);
        this.restClientException = restClientException;
        this.body = body;
    }
}
