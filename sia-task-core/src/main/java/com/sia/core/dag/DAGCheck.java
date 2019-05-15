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
 * @description Directed Acyclic Graph
 * @see
 * @author pengfeili23
 * @date 2018-06-27 5:50:08
 * @version V1.0.0
 **/
public class DAGCheck {

    /** 以顶点为索引，值代表了该顶点是否标记过（是否可达）*/
    private Map<String, Boolean> marked;
    /** y=edgeTo.get(x)代表顶点y->x，指向x的顶点为y*/
    private Map<String, String> edgeTo;
    /** 用来存储有向环中所有顶点*/
    private List<String> cycle;
    /** 顶点为索引，值为该顶点是否参与dfs递归，参与为true*/
    private Map<String, Boolean> onStack;

    public DAGCheck(Digraph g) {

        // 初始化成员变量
        onStack = new HashMap<String, Boolean>();
        edgeTo = new HashMap<String, String>();
        marked = new HashMap<String, Boolean>();

        for (String v : g.getAllVertex()) {
            marked.put(v, false);
        }
        // 检查是否有环
        for (String v : g.getAllVertex()) {
            // 只有顶点未被标记，而且暂时没有找到环，才继续dfs（这个判断只是加快算法结束，对正确性没有影响）
            if (!marked.get(v) && !hasCycle()) {
                dfs(g, v);
            }
        }
    }

    /**
     * 深度优先搜索
     * 
     * @param g
     * @param v
     */
    private void dfs(Digraph g, String v) {

        // 递归开始，顶点入栈
        onStack.put(v, true);
        // 标记顶点已被搜索
        marked.put(v, true);
        // 遍历顶点v的每一条边，v-> w
        for (String w : g.adj(v)) {
            // 终止条件：找到有向环
            if (this.hasCycle()) {
                return;
            }
            // 顶点w未被搜索
            else if (!marked.get(w)) {
                // 记录顶点v->w
                edgeTo.put(w, v);
                // 从顶点w开始，继续dfs
                dfs(g, w);
            }
            // 如果找到了已标记的顶点，且该顶点在递归栈上。（栈上都是出发点，而找到了已标记的顶点是终点，说明出发点和终点相同了，有环！）
            else if (onStack.get(w)) {
                cycle = new LinkedList<String>();

                for (String x = v; !x.equals(w); x = edgeTo.get(x)) {
                    // 将由v出发，w结束的环上中间的结点遍历添加到cycle中
                    cycle.add(x);
                }
                // 添加终点w
                cycle.add(w);
                // 额外添加起点v，表明首尾相接，是一个环
                cycle.add(v);
            }
        }
        // 当递归开始结算退出时，顶点出栈
        onStack.put(v, false);
    }

    /**
     * 判断是否有环
     * 
     * @return
     */
    public boolean hasCycle() {

        return cycle != null;
    }

    /**
     * 返回找到的一个环
     * 
     * @return
     */
    public List<String> cycle() {

        return cycle;
    }
}
