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

package com.sia.scheduler.util.tools;

import java.text.MessageFormat;

/**
 *
 * StringFormat
 * @description
 * @see
 * @author maozhengwei
 * @date 2018-10-08 20:12
 * @version V1.0.0
 **/
public class StringFormat {

    /**
     *
     * @param args
     * @return
     */
    public static String logMessFormat(String ... args){
        if (args.length>1) {
            return MessageFormat.format("Scheduling task :{0},Scheduling information : {1}",args);
        }
        return MessageFormat.format(" Scheduling task :{0}",args);

    }
}
