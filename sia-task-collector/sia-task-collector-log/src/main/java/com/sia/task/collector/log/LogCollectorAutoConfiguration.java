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

package com.sia.task.collector.log;

import com.sia.task.core.util.Constant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.type.AnnotationMetadata;

/**
 * LogCollectorConfigurator
 *
 * @author maozhengwei
 * @version V1.0.0
 * @data 2020/5/13 11:03 上午
 **/
@Slf4j
public class LogCollectorAutoConfiguration implements ImportBeanDefinitionRegistrar {

    @Override
    public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {

        String logCollectorAutoConfiguration = LogCollectorBeansRegistrar.class.getName();
        boolean containsBeanDefinition = registry.containsBeanDefinition(logCollectorAutoConfiguration);
        log.info(Constant.LOG_PREFIX + " registerBeanDefinition - [{}]", logCollectorAutoConfiguration);
        if (!containsBeanDefinition) {
            registry.registerBeanDefinition(logCollectorAutoConfiguration, new RootBeanDefinition(LogCollectorBeansRegistrar.class));
        }
    }

}
