package com.sia.task.executordemo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

//务必覆盖扫描包的范围
@SpringBootApplication(scanBasePackages = { "com.sia"})
public class SiaTaskExecutorDemoApplication {

	private static final Logger LOGGER = LoggerFactory.getLogger(SiaTaskExecutorDemoApplication.class);

	public static void main(String[] args) {

		SpringApplication.run(SiaTaskExecutorDemoApplication.class, args);
		LOGGER.info("SiaTaskExecutorDemoApplication启动！");

	}

}
