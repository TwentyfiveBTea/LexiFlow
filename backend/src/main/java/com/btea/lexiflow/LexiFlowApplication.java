package com.btea.lexiflow;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@MapperScan("com.btea.lexiflow.*.dao.mapper")
@EnableScheduling
public class LexiFlowApplication {

    public static void main(String[] args) {
        SpringApplication.run(LexiFlowApplication.class, args);
    }
}
