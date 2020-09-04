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

package com.sia.task.core.task;

import java.util.List;

/**
 * SiaTaskMeta
 *
 * @author maozhengwei
 * @version V1.0.0
 * @date 2020/7/20 3:18 下午
 * @see
 **/
public class SiaTaskMeta {

    /**
     * 业务应用所在的组名
     */
    private String groupName;

    /**
     * 业务应用名称
     */
    private String applicationName;

    /**
     * 应用请求的http路径
     */
    private String httpPath;

    /**
     * IP地址和端口
     */
    private String ipAndPort;

    /**
     * 输出参数
     */
    private String output;

    /**
     * 输入参数
     */
    private List<String> input;

    /**
     * 任务方法名称
     */
    private String methodName;

    /**
     * 任务类名
     */
    private String className;

    /**
     * bean名称
     */
    private String beanName;

    /**
     * http方法支持的请求方式（GET/POST）
     */
    private List<String> httpMethod;

    /**
     * 任务方法描述
     */
    private String description;

    public void setDescription(String description) {

        this.description = description;
    }

    public String getDescription() {

        return this.description;
    }

    public void setGroupName(String groupName) {

        this.groupName = groupName;
    }

    public String getGroupName() {

        return this.groupName;
    }

    public void setApplicationName(String applicationName) {

        this.applicationName = applicationName;
    }

    public String getApplicationName() {

        return this.applicationName;
    }

    public void setHttpPath(String httpPath) {

        this.httpPath = httpPath;
    }

    public String getHttpPath() {

        return this.httpPath;
    }

    public void setIpAndPort(String ipAndPort) {

        this.ipAndPort = ipAndPort;
    }

    public String getIpAndPort() {

        return this.ipAndPort;
    }

    public void setOutput(String output) {

        this.output = output;
    }

    public String getOutput() {

        return this.output;
    }

    public void setInput(List<String> input) {

        this.input = input;
    }

    public List<String> getInput() {

        return this.input;
    }

    public void setMethodName(String methodName) {

        this.methodName = methodName;
    }

    public String getMethodName() {

        return this.methodName;
    }

    public void setClassName(String className) {

        this.className = className;
    }

    public String getClassName() {

        return this.className;
    }

    public void setBeanName(String beanName) {

        this.beanName = beanName;
    }

    public String getBeanName() {

        return this.beanName;
    }

    public void setHttpMethod(List<String> httpMethod) {

        this.httpMethod = httpMethod;
    }

    public List<String> getHttpMethod() {

        return this.httpMethod;
    }

}
