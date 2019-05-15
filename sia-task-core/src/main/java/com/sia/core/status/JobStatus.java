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

package com.sia.core.status;

/**
 * <table>
 * <tr>
 * <th>用户操作</th>
 * <th>JOB状态</th>
 * <th>页面显示</th>
 * </tr>
 * <tbody>
 * <tr>
 * <td>删除操作</td>
 * <td>无数据</td>
 * <td>——</td>
 * </tr>
 * <tr>
 * <td>停止操作</td>
 * <td>无数据</td>
 * <td>已停止</td>
 * </tr>
 * <tr>
 * <td>激活操作</td>
 * <td>READY</td>
 * <td>准备中</td>
 * </tr>
 * </tbody>
 * </table>
 *
 * <table>
 *
 * <tr>
 * <th>JOB状态</th>
 * <th>JOB状态解释</th>
 * <th>页面显示</th>
 * </tr>
 * <tbody>
 * <tr>
 * <td>READY</td>
 * <td>Job执行结束</td>
 * <td>准备中</td>
 * </tr>
 * <tr>
 * <td>RUNNING</td>
 * <td>Job正在运行</td>
 * <td>正在运行</td>
 * </tr>
 * <tr>
 * <td>STOP</td>
 * <td>Job异常停止</td>
 * <td>异常停止</td>
 * </tr>
 * <tr>
 * <td>——</td>
 * <td>Job不再运行</td>
 * <td>已停止</td>
 * </tr>
 * </tbody>
 * </table>
 *
 * @author: pengfeili23
 * @Description: JOB执行状态
 * @date: 2018年6月27日 下午3:46:05
 */
public enum JobStatus {

    /**
     * 任务异常停止状态
     */
    STOP("stop"),

    /**
     * 任务就绪状态
     */
    READY("ready"),

    /**
     * 任务运行状态
     */
    RUNNING("running"),

    /**
     * 任务暂停状态，暂时不用
     */
    PAUSE("pause");

    private String status;

    JobStatus(String status) {

        this.status = status;
    }

    @Override
    public String toString() {

        return status;
    }
}
