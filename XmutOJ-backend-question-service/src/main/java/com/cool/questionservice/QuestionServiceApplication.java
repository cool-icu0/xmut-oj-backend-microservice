package com.cool.questionservice;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication()
@MapperScan("com.cool.questionservice.mapper")
@EnableDiscoveryClient
@EnableFeignClients(basePackages = {"com.cool.serviceclient.service"})
//这里如果要扫到全局异常处理器就要加上com.cool
@ComponentScan("com.cool")
@EnableScheduling
@EnableAspectJAutoProxy(proxyTargetClass = true, exposeProxy = true)
public class QuestionServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(QuestionServiceApplication.class, args);
    }

}
