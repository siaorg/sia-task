package com.sia.intask;

import com.sia.hunter.constant.OnlineTaskConstant;
import org.mybatis.spring.annotation.MapperScan;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

/**
 *
 * @author jinghuali
 */
@SpringBootApplication(scanBasePackages = {"com.sia"})
@MapperScan({"com.sia.core.mapper"})
@EnableDiscoveryClient
public class InstrumentTaskApplication {

    private static final Logger LOGGER = LoggerFactory.getLogger(InstrumentTaskApplication.class);

    public static void main(String[] args) {

        SpringApplication.run(InstrumentTaskApplication.class, args);
        LOGGER.info(OnlineTaskConstant.LOGPREFIX + "InstrumentTaskApplication start OK!");
    }

    @Bean
    @LoadBalanced
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
