package com.starwar;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.starwar.mapper")
public class StarWarApplication {

    public static void main(String[] args) {
        SpringApplication.run(StarWarApplication.class, args);
    }
}


