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

package com.sia.task.register.zookeeper;

import com.sia.task.register.zookeeper.core.JobMonitor;
import com.sia.task.register.zookeeper.core.TaskMonitor;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportSelector;
import org.springframework.core.type.AnnotationMetadata;

/**
 * ZookeeperMonitorSelector
 * 向Spring容器注册两个核心组件，TaskMonitor和JobMonitor
 *
 * @author maozhengwei
 * @version V1.0.0
 * @data 2019-10-23 14:50
 * @see TaskMonitor
 * @see JobMonitor
 **/
public class ZookeeperMonitorSelector implements ImportSelector {

    /**
     * Select and return the names of which class(es) should be imported based on
     * the {@link AnnotationMetadata} of the importing @{@link Configuration} class.
     *
     * @param importingClassMetadata
     */
    @Override
    public String[] selectImports(AnnotationMetadata importingClassMetadata) {

        return new String[]{TaskMonitor.class.getName(), JobMonitor.class.getName()};
    }
}
