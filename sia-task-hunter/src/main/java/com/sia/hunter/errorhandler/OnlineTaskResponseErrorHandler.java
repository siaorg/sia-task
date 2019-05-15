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

package com.sia.hunter.errorhandler;

import com.sia.hunter.exception.OnlineTaskCallerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.ResponseErrorHandler;
import org.springframework.web.client.RestClientException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * user-defined ErrorHandler
 * @see
 * @author huangqian
 * @date 2018-06-29 14:33
 * @version V1.0.0
 **/
public class OnlineTaskResponseErrorHandler implements ResponseErrorHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(OnlineTaskResponseErrorHandler.class);

    private ResponseErrorHandler errorHandler = new DefaultResponseErrorHandler();

    /**
     * Exception handling method
     *
     * @param response
     * @throws IOException
     */
    @Override
    public void handleError(ClientHttpResponse response) throws IOException {

        String body = convertStreamToString(response.getBody());
        try {
            errorHandler.handleError(response);
        } catch (RestClientException scx) {
            throw new OnlineTaskCallerException(scx.getMessage(), scx, body);
        }
    }

    /**
     * @param response
     * @return
     * @throws IOException
     */
    @Override
    public boolean hasError(ClientHttpResponse response) throws IOException {
        return errorHandler.hasError(response);
    }

    /**
     * Stream conversion
     *
     * @param is
     * @return
     */
    private String convertStreamToString(InputStream is) {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();
        String line;
        try {
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
        } catch (IOException ex) {
            LOGGER.error("", ex);
        } finally {
            try {
                is.close();
            } catch (IOException ex) {
                LOGGER.error("", ex);
            }
        }
        return sb.toString();
    }

}
