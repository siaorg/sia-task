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

package com.sia.task.scheduler.util;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

/**
 * LogSummary
 *
 * @author maozhengwei
 * @version V1.0.0
 * @date 2020/6/10 11:31 上午
 * @see
 **/
@Slf4j
@Data
@AllArgsConstructor
@NoArgsConstructor
public class LogSummary {

    private String title;

    private String description;

    private Map<String, String> logMeteData;

    @Override
    public String toString() {
        try {
            return buildSummary();
        } catch (Exception se) {
            return "logMeteData: undeterminable.";
        }
    }

    /**
     * Returns a formatted (human readable) String describing all the <code>Scheduler</code>'s
     * meta-data values.
     *
     * @return
     */
    private String buildSummary() throws Exception {

        StringBuilder str = new StringBuilder("\n");
        str.append("----------------------------------------------------------");
        str.append("\n");
        str.append("  ");
        str.append(getTitle());
        str.append("\n");
        str.append("  ");
        str.append(getDescription());
        str.append("\n");
        logMeteData.forEach((k, v) -> {
            str.append("  ");
            str.append(k).append(": ");
            str.append(v).append("\n");
        });
        str.append("----------------------------------------------------------");
        return str.toString();
    }
}
