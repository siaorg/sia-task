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

package com.sia.task.core.util;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author
 * @Description:
 * @date 2018/4/1718:15
 */
public class ListHelper {
    
    /**
     * 合并List Zk和DB 去重
     * @param zkList
     * @param dbList
     * @return
     */
    public static List<String> mergeList(List<String> zkList, List<String> dbList) {
        Set<String> set = new HashSet<String>();
        if (zkList != null && zkList.size() > 0) {
            for (String zk : zkList) {
                if (!StringHelper.isEmpty(zk)) {
                    set.add(zk);
                }
            }
        }
        if (dbList != null && dbList.size() > 0) {
            for (String db : dbList) {
                if (!StringHelper.isEmpty(db)) {
                    set.add(db);
                }
            }
        }
        return new ArrayList<>(set);
    }
}
