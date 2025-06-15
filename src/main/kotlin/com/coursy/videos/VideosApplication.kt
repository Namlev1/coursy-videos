package com.coursy.videos

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class VideosApplication

fun main(args: Array<String>) {
    runApplication<VideosApplication>(*args)
}
