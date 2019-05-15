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

package com.sia.scheduler.http;

import com.sia.scheduler.http.enums.ResponseStatus;
import lombok.Data;

import java.io.Serializable;

/**
 *
 * The abstract class that returns the data returned by the http interface when defining an asynchronous call to a task.
 * Only the state information of the asynchronous return data is defined in this class.
 *
 * @see
 * @author maozhengwei
 * @date 2018-09-28 9:51
 * @version V1.0.0
 **/
public @Data abstract class AbstractResponse implements Serializable{

    protected ResponseStatus status;

}
