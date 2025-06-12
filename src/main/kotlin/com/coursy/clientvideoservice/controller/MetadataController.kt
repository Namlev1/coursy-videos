package com.coursy.clientvideoservice.controller

import com.coursy.clientvideoservice.service.VideoService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
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
//                content = [Content(
//                    mediaType = MediaType.APPLICATION_JSON_VALUE,
//                    schema = Schema(implementation = VideoUploadResponse::class)
//                )]
            ),
            ApiResponse(
                responseCode = "400",
                description = "Bad request - invalid page parameters",
            )
        ]
    )
    @GetMapping
    fun getAllVideos(): ResponseEntity<Any> {
        TODO()
    }

    @Operation(summary = "Get video metadata")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Fetched video metadata successfully",
//                content = [Content(
//                    mediaType = MediaType.APPLICATION_JSON_VALUE,
//                    schema = Schema(implementation = VideoUploadResponse::class)
//                )]
            ),
            ApiResponse(
                responseCode = "404",
                description = "Not found - invalid video id",
            )
        ]
    )
    @GetMapping("/{videoId}")
    fun getVideo(@PathVariable videoId: String): ResponseEntity<Any> {
        TODO()
    }


    @Operation(summary = "Update video metadata")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Video metadata updated successfully",
//                content = [Content(
//                    mediaType = MediaType.APPLICATION_JSON_VALUE,
//                    schema = Schema(implementation = VideoUploadResponse::class)
//                )]
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
//                content = [Content(
//                    mediaType = MediaType.APPLICATION_JSON_VALUE,
//                    schema = Schema(implementation = VideoUploadResponse::class)
//                )]
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
}