package com.huzhu;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.mybatis.spring.annotation.MapperScan;

@SpringBootApplication
@MapperScan("com.huzhu.mapper")
public class BarleyWineSystemApplication {
    public static void main(String[] args) {
        SpringApplication.run(BarleyWineSystemApplication.class, args);
    }
}