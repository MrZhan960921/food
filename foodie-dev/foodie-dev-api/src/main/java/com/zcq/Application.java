package com.zcq;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import tk.mybatis.spring.annotation.MapperScan;

@SpringBootApplication(exclude = {SecurityAutoConfiguration.class})
//扫描mybatis通用mapper
@MapperScan(basePackages = "com.zcq.mapper")
//@EnableTransactionManagement
// 扫描所有包以及相关组件包
@ComponentScan(basePackages = {"com.zcq", "org.n3r.idworker"})
@EnableScheduling       // 开启定时任务
@EnableRedisHttpSession //开启使用redis 作为Springsession
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

}
