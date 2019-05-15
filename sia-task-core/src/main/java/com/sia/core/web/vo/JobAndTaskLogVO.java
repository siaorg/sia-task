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

package com.sia.core.web.vo;

import com.sia.core.entity.JobLog;
import com.sia.core.entity.TaskLog;
import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 *
 *
 * @description JobAndTaskLogVO
 * @see
 * @author MAOZW
 * @date 2018-04-18 11:10
 * @version V1.0.0
 **/
@Data
public class JobAndTaskLogVO {

    private Integer jobLogId;

    private Integer jobId;

    private String jobKey;

    private String jobTriggerCode;

    private String jobTriggerMsg;

    private Date jobTriggerTime;

    private String jobHandleCode;

    private String jobHandleMsg;

    private Date jobHandleTime;

    private Date jobHandleFinishedTime;

    private Date createTime;

    private List<TaskLog> taskLogList;

}
