package com.coursy.videos.controller

import com.coursy.videos.dto.MetadataResponse
import com.coursy.videos.dto.VideoUploadRequest
import com.coursy.videos.service.VideoService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.*
import org.springframework.web.bind.annotation.*
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody
import java.util.*

@CrossOrigin(origins = ["*"])
@RestController
@RequestMapping("/api/videos")
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
                    schema = Schema(implementation = MetadataResponse::class)
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
                { failure ->
                    ResponseEntity
                        .badRequest()
                        .body(failure.message())
                },
                { metadataResponse ->
                    ResponseEntity
                        .status(HttpStatus.CREATED)
                        .body(metadataResponse)
                }
            )
    }

//    @Operation(summary = "Stream video content")
//    @ApiResponses(
//        value = [
//            ApiResponse(
//                responseCode = "200",
//                description = "Video stream started successfully",
//                content = [Content(
//                    mediaType = "video/mp4"
//                )]
//            ),
//            ApiResponse(
//                responseCode = "206",
//                description = "Partial content - range request fulfilled",
//                content = [Content(
//                    mediaType = "video/mp4"
//                )]
//            ),
//            ApiResponse(
//                responseCode = "404",
//                description = "Video not found"
//            ),
//            ApiResponse(
//                responseCode = "416",
//                description = "Range not satisfiable"
//            )
//        ]
//    )
//    @GetMapping("/{id}/stream")
//    fun streamVideo(
//        @Parameter(description = "Video ID", example = "123")
//        @PathVariable id: UUID,
//        @RequestHeader(value = "Range", required = true) rangeHeader: String,
//    ): ResponseEntity<StreamingResponseBody> {
//        return videoService
//            .streamVideo(id, rangeHeader)
//            .fold(
//                { failure ->
//                    // TODO KISS
//                    // I must keep ResponseEntity<StreamingResponseBody> and not <Any>,
//                    // so this is a workaround.
//                    val errorBody = StreamingResponseBody { outputStream ->
//                        outputStream.write(failure.message().toByteArray())
//                    }
//                    ResponseEntity.badRequest()
//                        .contentType(MediaType.TEXT_PLAIN)
//                        .body(errorBody)
//                },
//                { streamingResult ->
//
//                    ResponseEntity.ok()
//                        .header("Accept-Ranges", "bytes")
//                        .header("Content-Length", streamingResult.fileSize.toString())
//                        .contentType(MediaType.parseMediaType("video/mp4"))
//                        .body(streamingResult.streamingBody)
//                }
//
//            )
//    }

    @GetMapping("/{videoId}/stream")
    fun getMasterPlaylist(@PathVariable videoId: UUID): ResponseEntity<String> {
        return videoService
            .getMasterPlaylist(videoId)
            .fold(
                { failure ->
                    ResponseEntity.badRequest()
                        .contentType(MediaType.TEXT_PLAIN)
                        .body(failure.message())
                },
                { playlistContent ->
                    ResponseEntity.ok()
                        .contentType(MediaType.parseMediaType("application/vnd.apple.mpegurl"))
                        .body(playlistContent)  // Return String directly
                }
            )
    }
    @GetMapping("/{videoId}/stream/{quality}/playlist.m3u8")
    fun getQualityPlaylist(
        @PathVariable videoId: UUID,
        @PathVariable quality: String
    ): ResponseEntity<String> {
        TODO()
    }

    @GetMapping("/{videoId}/stream/{quality}/{segmentName}")
    fun getSegment(
        @PathVariable videoId: UUID,
        @PathVariable quality: String,
        @PathVariable segmentName: String
    ): ResponseEntity<StreamingResponseBody> {
        TODO()
    }

    @Operation(summary = "Download video file")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Video download started successfully",
                content = [Content(
                    mediaType = "video/mp4"
                )]
            ),
            ApiResponse(
                responseCode = "404",
                description = "Video not found"
            )
        ]
    )
    @GetMapping("/{id}/download")
    fun downloadVideo(
        @Parameter(
            description = "Video ID",
            required = true,
            example = "intro-kotlin.mp4" //todo docs
        )
        @PathVariable id: UUID,

        @Parameter(
            description = "ID of the user possessing the video",
            required = true,
            example = "123"
        )
        @RequestParam userId: Long,

        @Parameter(
            description = "Name of the course this video belongs to",
            required = true,
            example = "Spring Boot Fundamentals"
        )
        @RequestParam courseName: String,
    ): ResponseEntity<StreamingResponseBody> {
        return videoService
            .getVideoStream(id, userId, courseName)
            .fold(
                { failure ->
                    // I must keep ResponseEntity<StreamingResponseBody> and not <Any>,
                    // so this is a workaround.
                    val errorBody = StreamingResponseBody { outputStream ->
                        outputStream.write(failure.message().toByteArray())
                    }
                    ResponseEntity.badRequest()
                        .contentType(MediaType.TEXT_PLAIN)
                        .body(errorBody)
                },
                { downloadResult ->
                    val headers = HttpHeaders()
                    headers.contentType = MediaType.parseMediaType("video/mp4")
                    headers.contentLength = downloadResult.fileSize
                    headers.contentDisposition = ContentDisposition
                        .attachment()
                        .filename(downloadResult.fileName.value)
                        .build()

                    ResponseEntity(downloadResult.streamingBody, headers, HttpStatus.OK)
                }
            )
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