package com.coursy.clientvideoservice.controller

import com.coursy.clientvideoservice.dto.VideoUploadRequest
import com.coursy.clientvideoservice.dto.VideoUploadResponse
import com.coursy.clientvideoservice.service.VideoService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/videos")
@Tag(
    name = "Video Content",
    description = "API for uploading, downloading, streaming and accessing video content"
)
class VideoController(
    private val videoService: VideoService,
) {

    @Operation(summary = "Upload video file")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "201",
                description = "Video uploaded successfully",
                content = [Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = Schema(implementation = VideoUploadResponse::class)
                )]
            ),
            ApiResponse(
                responseCode = "400",
                description = "Bad request - invalid file format, missing parameters, or upload failed",
            )
        ]
    )
    @PostMapping(
        "/upload",
        consumes = [MediaType.MULTIPART_FORM_DATA_VALUE],
        produces = [MediaType.APPLICATION_JSON_VALUE]
    )
    fun uploadVideo(
        @ModelAttribute request: VideoUploadRequest,
    ): ResponseEntity<Any> {
        return videoService
            .saveVideo(
                request.file,
                request.userId,
                request.courseName
            )
            .fold(
                { failure -> ResponseEntity.badRequest().body(failure.message()) },
                { path ->
                    ResponseEntity
                        .status(HttpStatus.CREATED)
                        .body(VideoUploadResponse(path))
                }
            )
    }

    @Operation(summary = "Stream video content")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Video stream started successfully",
                content = [Content(
                    mediaType = "video/mp4"
                )]
            ),
            ApiResponse(
                responseCode = "206",
                description = "Partial content - range request fulfilled",
                content = [Content(
                    mediaType = "video/mp4"
                )]
            ),
            ApiResponse(
                responseCode = "404",
                description = "Video not found"
            ),
            ApiResponse(
                responseCode = "416",
                description = "Range not satisfiable"
            )
        ]
    )
    @GetMapping("/{id}/stream")
    fun streamVideo(@PathVariable id: String): ResponseEntity<Any> {
        TODO()
    }

    @Operation(summary = "Download video file")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Video download started successfully",
                content = [Content(
                    mediaType = MediaType.APPLICATION_OCTET_STREAM_VALUE
                )]
            ),
            ApiResponse(
                responseCode = "404",
                description = "Video not found"
            )
        ]
    )
    @GetMapping("/{id}/download")
    fun downloadVideo(@PathVariable id: String): ResponseEntity<Any> {
        TODO()
    }

    @Operation(summary = "Get video thumbnail")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Thumbnail retrieved successfully",
                content = [Content(
                    mediaType = MediaType.IMAGE_JPEG_VALUE
                )]
            ),
            ApiResponse(
                responseCode = "404",
                description = "Video or thumbnail not found"
            )
        ]
    )
    @GetMapping("/{id}/thumbnail")
    fun getVideoThumbnail(@PathVariable id: String): ResponseEntity<Any> {
        TODO()
    }
}