package com.coursy.videos.config

import io.minio.MinioClient
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class MinIOConfig {

    @Bean
    fun minioClient(
        @Value("\${minio.endpoint:http://localhost:11000}") endpoint: String,
        @Value("\${minio.access-key:minioadmin}") accessKey: String,
        @Value("\${minio.secret-key:minioadmin}") secretKey: String
    ): MinioClient {
        return MinioClient.builder()
            .endpoint(endpoint)
            .credentials(accessKey, secretKey)
            .build()
    }
}