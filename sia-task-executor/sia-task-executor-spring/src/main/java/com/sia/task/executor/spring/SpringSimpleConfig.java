package com.sia.task.executor.spring;

import com.sia.task.hunter.annotation.EnableTaskClient;
import org.springframework.context.annotation.Configuration;

/**
 * 导入组件<code>{@link EnableTaskClient}</code>注册Spring容器
 *
 * @see
 * @author maozhengwei
 * @date 2020/9/8 9:48 上午
 * @version V1.0.0
 **/
@Configuration
@EnableTaskClient
public class SpringSimpleConfig {
}
