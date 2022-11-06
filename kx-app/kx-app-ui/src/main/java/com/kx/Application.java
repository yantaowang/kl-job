package com.kx;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import tk.mybatis.spring.annotation.MapperScan;

@SpringBootApplication
@MapperScan(basePackages = "com.kx.service.mapper")
@EnableMongoRepositories//开启mongodb
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
