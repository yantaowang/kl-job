package com.kx.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import tk.mybatis.spring.annotation.MapperScan;

@SpringBootApplication
@MapperScan(basePackages = "com.kx.mapper")
@ComponentScan(basePackages = {"com.kx", "org.n3r.idworker"})//扫描这两个包下的类
@EnableMongoRepositories//开启mongodb
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
