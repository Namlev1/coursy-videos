package com.coursy.videos.controller

import com.coursy.videos.dto.MetadataResponse
import com.coursy.videos.service.VideoService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.data.domain.PageRequest
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/videos")
@Tag(
    name = "Video Metadata Management",
    description = "API for managing video files and metadata"
)
class MetadataController(
    private val videoService: VideoService,
) {

    @Operation(summary = "Get paginated video metadata list")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Fetched video page successfully",
                content = [Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = Schema(implementation = MetadataResponse::class)
                )]
            ),
            ApiResponse(
                responseCode = "400",
                description = "Bad request - invalid page parameters",
            )
        ]
    )
    @GetMapping
    fun getVideoPage(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "10") size: Int,
    ): ResponseEntity<Any> {
        return when {
            arePageParamsInvalid(page, size) -> ResponseEntity.badRequest().build()
            else -> PageRequest.of(page, size)
                .let { page -> videoService.getPage(page) }
                .let { response -> ResponseEntity.ok(response) }
        }
    }

    @Operation(summary = "Get video metadata")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Fetched video metadata successfully",
                content = [Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = Schema(implementation = MetadataResponse::class)
                )]
            ),
            ApiResponse(
                responseCode = "404",
                description = "Not found - invalid video id",
            )
        ]
    )
    @GetMapping("/{videoId}")
    fun getVideo(@PathVariable videoId: Long) =
        videoService
            .getVideo(videoId)
            .fold(
                { error -> ResponseEntity.status(HttpStatus.NOT_FOUND).body(error.message()) },
                { response -> ResponseEntity.ok(response) }
            )

    @Operation(summary = "Update video metadata")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Video metadata updated successfully",
                content = [Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = Schema(implementation = MetadataResponse::class)
                )]
            ),
            ApiResponse(
                responseCode = "404",
                description = "Not found - invalid video id",
            ),
            ApiResponse(
                responseCode = "400",
                description = "Bad request - invalid request body",
            )
        ]
    )
    @PutMapping("/{videoId}")
    fun updateVideo(@PathVariable videoId: String): ResponseEntity<Any> {
        TODO()
    }


    @Operation(summary = "Delete video file with its metadata")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "204",
                description = "Video deleted successfully",
            ),
            ApiResponse(
                responseCode = "404",
                description = "Not found - invalid video id",
            )
        ]
    )
    @DeleteMapping("/{videoId}")
    fun deleteVideo(@PathVariable videoId: String): ResponseEntity<Any> {
        TODO()
    }

    private fun arePageParamsInvalid(page: Int, size: Int) =
        page < 0 || size <= 0
}