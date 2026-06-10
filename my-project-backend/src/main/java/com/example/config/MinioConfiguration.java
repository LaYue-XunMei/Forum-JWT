package com.example.config;

import io.minio.MinioClient;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class MinioConfiguration {

    @Value("${spring.minio.endpoint}")
    String endpoint;
    @Value("${spring.minio.username}")
    String username;
    @Value("${spring.minio.password}")
    String password;

    @Bean
    public MinioClient minioClient() {
        try {
            log.info("初始化 MinIO 客户端，端点: {}", endpoint);

            MinioClient client = MinioClient.builder()
                    .endpoint(endpoint)
                    .credentials(username,password)
                    .build();

            // 验证连接
            client.listBuckets();
            log.info("MinIO 客户端初始化成功");

            return client;
        } catch (Exception e) {
            log.error("MinIO 客户端初始化失败", e);
            throw new RuntimeException("MinIO 客户端初始化失败", e);
        }
    }


}
