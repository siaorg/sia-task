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

package com.sia.hunter.taskstatus;

/**
 *
 *
 * @description task status
 * @see
 * @author huangqian
 * @date 2018-06-29 11:16
 * @version V1.0.0
 **/
public enum TaskStatus {

    /**
     * 任务运行状态
     */
    RUNNING("running"),

    /**
     * 任务停止状态
     */
    STOP("stop");

    private String status;

    TaskStatus(String status) {

        this.status = status;
    }

    @Override
    public String toString() {

        return status;
    }
}
