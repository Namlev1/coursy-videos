package com.coursy.clientvideoservice.service

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import com.coursy.clientvideoservice.failure.MinIoFailure
import io.minio.BucketExistsArgs
import io.minio.MakeBucketArgs
import io.minio.MinioClient
import io.minio.PutObjectArgs
import jakarta.annotation.PostConstruct
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.io.InputStream

@Service
class MinIOService(
    private val minioClient: MinioClient,
    @Value("\${minio.bucket-name:videos}")
    private val bucketName: String,
    @Value("\${minio.endpoint:http://localhost:11000}")
    private val endpoint: String
) {
    private val logger = LoggerFactory.getLogger(MinIOService::class.java)

    @PostConstruct
    fun initializeBucket() {
        try {
            val bucketExists = minioClient.bucketExists(
                BucketExistsArgs.builder()
                    .bucket(bucketName)
                    .build()
            )

            if (!bucketExists) {
                minioClient.makeBucket(
                    MakeBucketArgs.builder()
                        .bucket(bucketName)
                        .build()
                )
                logger.info("Created bucket: ${bucketName}")
            }
        } catch (e: Exception) {
            logger.error("Error initializing MinIO bucket", e)
        }
    }

    fun uploadFile(
        fileName: String,
        inputStream: InputStream,
        contentType: String,
        size: Long
    ): Either<MinIoFailure, Unit> = runCatching {
        minioClient.putObject(
            PutObjectArgs.builder()
                .bucket(bucketName)
                .`object`(fileName)
                .stream(inputStream, size, -1)
                .contentType(contentType)
                .build()
        )
    }.fold(
        onSuccess = { Unit.right() },
        onFailure = { exception ->
            logger.error("Error uploading file: $fileName", exception)
            MinIoFailure(exception.message).left()
        }
    )
}