package com.acg.community;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.acg.community.mapper")
public class AcgCommunityApplication {

    public static void main(String[] args) {
        SpringApplication.run(AcgCommunityApplication.class, args);
    }
}
