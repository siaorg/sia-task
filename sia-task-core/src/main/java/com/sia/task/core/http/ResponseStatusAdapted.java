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

import lombok.NoArgsConstructor;

/**
 * @author: MAOZW
 * @Description: ResponseStatus
 * @date 2018/9/29 15:03
 */
@NoArgsConstructor
public class ResponseStatusAdapted {

    private static final String success = "success";

    ResponseStatus status;

    public ResponseStatus getStatus() {
        return status;
    }

    ResponseStatusAdapted(String res) {
        if (success.equals(res.toLowerCase())) {
            this.status = ResponseStatus.success;
        } else {
            this.status = ResponseStatus.failed;
        }
    }

    ResponseStatusAdapted(ResponseStatus status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "ResponseStatusAdapted{" +
                "status=" + status +
                '}';
    }
}
