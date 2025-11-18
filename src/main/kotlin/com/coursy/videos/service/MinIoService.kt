package com.coursy.videos.service

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import com.coursy.videos.failure.MinIoFailure
import io.minio.*
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
    private val endpoint: String,
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
        path: String,
        inputStream: InputStream,
        contentType: String,
        size: Long,
    ): Either<MinIoFailure, String> = runCatching {
        minioClient.putObject(
            PutObjectArgs.builder()
                .bucket(bucketName)
                .`object`(path)
                .stream(inputStream, size, -1)
                .contentType(contentType)
                .build()
        )
    }.fold(
        onSuccess = { path.right() },
        onFailure = { exception ->
            logger.error("Error uploading file: $path", exception)
            MinIoFailure(exception.message).left()
        }
    )

    fun deleteFolder(folderPath: String): Either<MinIoFailure, Unit> = runCatching {
        val objects = minioClient.listObjects(
            ListObjectsArgs.builder()
                .bucket(bucketName)
                .prefix(folderPath)
                .recursive(true)
                .build()
        )

        objects.forEach { item ->
            minioClient.removeObject(
                RemoveObjectArgs.builder()
                    .bucket(bucketName)
                    .`object`(item.get().objectName())
                    .build()
            )
        }
    }.fold(
        onSuccess = { Unit.right() },
        onFailure = { exception ->
            logger.error("Error deleting folder: $folderPath", exception)
            MinIoFailure(exception.message).left()
        }
    )

    fun getFileStream(
        path: String,
    ): Either<MinIoFailure, InputStream> = runCatching {
        minioClient.getObject(
            GetObjectArgs.builder()
                .bucket(bucketName)
                .`object`(path)
                .build()
        )
    }.fold(
        onSuccess = { inputStream -> inputStream.right() },
        onFailure = { exception ->
            logger.error("Error downloading file: $path", exception)
            MinIoFailure(exception.message).left()
        }
    )
}