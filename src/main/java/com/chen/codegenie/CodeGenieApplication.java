package com.chen.codegenie;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author chen
 */
@SpringBootApplication
@MapperScan("com.chen.codegenie.mapper")
public class CodeGenieApplication {

    public static void main(String[] args) {
        SpringApplication.run(CodeGenieApplication.class, args);
    }

}
