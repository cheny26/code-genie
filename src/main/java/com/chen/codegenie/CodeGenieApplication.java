package com.chen.codegenie;

import dev.langchain4j.community.store.embedding.redis.spring.RedisEmbeddingStoreAutoConfiguration;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author chen
 */
@SpringBootApplication(exclude = {RedisEmbeddingStoreAutoConfiguration.class})
@MapperScan("com.chen.codegenie.mapper")
public class CodeGenieApplication {

    public static void main(String[] args) {
        SpringApplication.run(CodeGenieApplication.class, args);
    }

}
