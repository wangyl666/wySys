package com.community.property;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.community.property.mapper")
public class PropertyApplication {
    public static void main(String[] args) {
        SpringApplication.run(PropertyApplication.class, args);
    }
}