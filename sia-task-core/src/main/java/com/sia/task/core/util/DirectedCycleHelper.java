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

import com.sia.task.core.task.DagTask;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author maozhengwei
 * @version V1.0.0
 * @description
 * @data 2019-12-04 17:30
 * @see
 **/
@Slf4j
public class DirectedCycleHelper {

    /**
     * DAG 关系编码
     * @param jobMTasks
     * @return
     */
    public static List<DagTask> layoutTask(List<DagTask> jobMTasks) {

        List<DagTask> jobMTasksBk = new ArrayList<>();
        jobMTasks.forEach(jobMTask -> {
            DagTask jobMTaskClone = jobMTask.deepClone();
            jobMTasksBk.add(jobMTaskClone);
        });

        return analyticalTask(jobMTasksBk);
    }

    private static List<DagTask> analyticalTask(List<DagTask> jobMTaskList) {
        Map<String, DagTask> onlineTasksMap = new HashMap<>(jobMTaskList.size());
        List<DagTask> startTaskLists = new ArrayList<>();
        for (DagTask onlineTask : jobMTaskList) {
            onlineTasksMap.put(onlineTask.getTaskKey(), onlineTask);
        }
        //任务编排
        for (DagTask currentTask : jobMTaskList) {
            List<String> preTaskKeyList = StringHelper.isEmpty(currentTask.getPreTaskKey()) ? Collections.emptyList() : Arrays.asList(currentTask.getPreTaskKey().split(","));
            if (preTaskKeyList.size() == 0) {
                startTaskLists.add(currentTask);
                continue;
            }
            for (String preTaskKey : preTaskKeyList) {
                DagTask onlineTask = onlineTasksMap.get(preTaskKey);
                currentTask.getPreTask().add(onlineTask);
                onlineTask.getPostTask().add(onlineTasksMap.get(currentTask.getTaskKey()));
            }
        }
        //设置虚拟末节点
        DagTask endTask = new DagTask();
        endTask.setTaskKey(Constant.ENDTASK);
        endTask.setPreTaskCounter(new AtomicInteger(0));
        endTask.setJobKey(jobMTaskList.get(0).getJobKey());
        endTask.setJobGroup(jobMTaskList.get(0).getJobGroup());
        List<DagTask> preTask = new ArrayList<DagTask>();
        endTask.setPreTask(preTask);
        for (DagTask currentTask : jobMTaskList) {
            if (currentTask.getPostTask().size() == 0) {
                currentTask.getPostTask().add(endTask);
                endTask.getPreTask().add(currentTask);
            }
        }
        //任务校验
        jobMTaskList.add(endTask);
        if (doDAGCheck4Post(jobMTaskList)) {
            log.error("Job 的task存在环路 请检查配置的task关系....");
            return Collections.emptyList();
        }
        //获取起始任务
        return startTaskLists;
    }


    public static boolean doDAGCheck4Post(List<DagTask> jobMTaskList) {
        Map<String, List<String>> relyMap = new HashMap<>();
        for (DagTask jobMTask : jobMTaskList) {
            List<DagTask> postTask = jobMTask.getPostTask();
            List<String> tmp = new ArrayList<>();
            for (DagTask jobMTask1 : postTask) {
                tmp.add(jobMTask1.getTaskKey());
            }
            relyMap.put(jobMTask.getTaskKey(), tmp == null ? Collections.emptyList() : tmp);
        }
        return hasCycle(relyMap);
    }

    public static List<String> doDAGCheck4Pre(List<DagTask> jobMTasks) {
        Map<String, List<String>> relyMap = new HashMap<>();
        for (DagTask jobMTask : jobMTasks) {
            List<String> preTask = Arrays.asList(jobMTask.getPreTaskKey().split(","));
            relyMap.put(jobMTask.getTaskKey(), (preTask == null || preTask.contains("")) ? Collections.emptyList() : preTask);
        }
        return findACycle(relyMap);
    }

    private static List<String> findACycle(Map<String, List<String>> relyMap) {
        return new DirectedCycleCheck((new Digraph(relyMap))).cycle();
    }

    /**
     * 检测是否存在回环
     */
    private static boolean hasCycle(Map<String, List<String>> relyMap) {

        List<String> cycle = findACycle(relyMap);
        return !(cycle == null || cycle.size() == 0);

    }

    private DirectedCycleHelper() {
    }


    static class DirectedCycleCheck {

        /**
         * 以顶点为索引，值代表了该顶点是否标记过（是否可达）
         */
        private Map<String, Boolean> marked;
        /**
         * y=edgeTo.get(x)代表顶点y->x，指向x的顶点为y
         */
        private Map<String, String> edgeTo;
        /**
         * 用来存储有向环中所有顶点
         */
        private List<String> cycle;
        /**
         * 顶点为索引，值为该顶点是否参与dfs递归，参与为true
         */
        private Map<String, Boolean> onStack;

        public DirectedCycleCheck(Digraph g) {

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

    static class Digraph {
        /**
         * 邻接表
         */
        private Map<String, List<String>> adj;

        /**
         * 顶点
         */
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


}
