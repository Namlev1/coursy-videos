package com.coursy.videos

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableAsync

@SpringBootApplication
@EnableAsync
class VideosApplication

fun main(args: Array<String>) {
    runApplication<VideosApplication>(*args)
}
