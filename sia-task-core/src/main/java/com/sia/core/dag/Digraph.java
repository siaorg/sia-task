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

package com.sia.core.dag;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 *
 *
 * @description mode of Directed Acyclic Graph
 * @see
 * @author pengfeili23
 * @date 2018-06-27 5:48:51
 * @version V1.0.0
 **/
public class Digraph {

    /** 邻接表*/
    private Map<String, List<String>> adj;

    /** 顶点*/
    private List<String> v;

    public Digraph(Map<String, List<String>> relyMap) {

        this.adj = new HashMap<String, List<String>>();
        this.v = new LinkedList<String>();

        for (String taskKey : relyMap.keySet()) {

            List<String> pre = relyMap.get(taskKey);
            // 收集邻接表
            adj.put(taskKey, pre);
            // 收集顶点
            v.add(taskKey);
        }

    }

    /**
     * 获取顶点的邻接表
     * 
     * @param v
     * @return
     */
    public List<String> adj(String v) {

        return adj.get(v);
    }

    /**
     * 获取有向图的所有顶点
     * 
     * @return
     */
    public List<String> getAllVertex() {

        return v;
    }

}
