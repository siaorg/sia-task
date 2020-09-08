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

package com.sia.task.scheduler.task;

import com.sia.task.core.ModifyOnlineJobStatus;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * A simple class (structure) used for returning execution-time data from the
 * DagTask to the <code>SchedulerThread</code>.
 *
 * @author maozhengwei
 * @version V1.0.0
 * @description
 * @data 2019-10-12 10:23
 * @see
 **/
public class TriggerOnlineTaskBundle implements Serializable {

    private static final long serialVersionUID = -2311440310598862578L;

    private SiaTaskDetail task;

    @Setter
    @Getter
    private ModifyOnlineJobStatus modifyOnlineJobStatus;

    public TriggerOnlineTaskBundle(SiaTaskDetail task) {
        this.task = task;
    }

    public SiaTaskDetail getOnlineTaskDetail() {
        return task;
    }
}
