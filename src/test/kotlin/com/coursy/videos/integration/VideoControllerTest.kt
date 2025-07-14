//package com.coursy.videos.integration
//
//import io.kotest.core.spec.style.BehaviorSpec
////import jakarta.transaction.Transactional
//import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
//import org.springframework.boot.test.context.SpringBootTest
//import org.springframework.test.context.ActiveProfiles
//
//@SpringBootTest
//@AutoConfigureMockMvc
//@ActiveProfiles("test")
////@Transactional
//class VideoControllerTest : BehaviorSpec() {
//
//    val url = "/v1/videos"
//
//    init {
//
//        given("user is uploading correct file") {
//            `when`("POST /videos/upload") {
//                then("the file is saved, metadata is returned") {
//                    TODO()
//                }
//            }
//        }
//
//        given("user is uploading incorrect file extension") {
//            `when`("POST /videos/upload") {
//                then("should return 400 with InvalidFileFormat") {
//                    TODO()
//                }
//            }
//        }
//
//        given("user is uploading malicious content with .mp4 extension") {
//            `when`("POST /videos/upload") {
//                then("should return 400 with InvalidFileContent") {
//                    TODO()
//                }
//            }
//        }
//
//        given("user is uploading too big file") {
//            `when`("POST /videos/upload") {
//                then("should return 413 with FileTooBig") {
//                    TODO()
//                }
//            }
//        }
//
//        given("platform has 50MB free space") {
//            `when`("user is uploading 100MB file") {
//                then("should return 403") {
//                    TODO()
//                }
//            }
//        }
//        
//        given("requested video is not in the system") {
//            `when`("streaming video") {
//                then("should return 404 with IdNotExists") {
//                    TODO()
//                }
//            }
//
//            `when`("downloading video") {
//                then("should return 404 with IdNotExists") {
//                    TODO()
//                }
//            }
//
//            `when`("downloading thumbnail") {
//                then("should return 404 with IdNotExists") {
//                    TODO()
//                }
//            }
//        }
//
//        given("requested video is present in the system") {
//            `when`("streaming video") {
//                then("should return 200 with video stream") {
//                    // Test that response contains proper video streaming response
//                    // Could be redirect to MinIO presigned URL or direct stream
//                    TODO()
//                }
//                then("should return proper Content-Type header") {
//                    // Content-Type: video/mp4
//                    TODO()
//                }
//                then("should support range requests for video seeking") {
//                    // Accept-Ranges: bytes
//                    // Support for HTTP 206 Partial Content
//                    TODO()
//                }
//            }
//
//            `when`("downloading video") {
//                then("should return 200 with video file") {
//                    // Full video file download
//                    TODO()
//                }
//                then("should include Content-Disposition header for download") {
//                    // Content-Disposition: attachment; filename="video.mp4"
//                    TODO()
//                }
//                then("should return correct Content-Length") {
//                    // Proper file size in headers
//                    TODO()
//                }
//            }
//
//            `when`("downloading thumbnail") {
//                then("should return 200 with thumbnail image") {
//                    // Thumbnail image (usually JPEG/PNG)
//                    TODO()
//                }
//                then("should return image Content-Type") {
//                    // Content-Type: image/jpeg or image/png
//                    TODO()
//                }
//            }
//        }
//    }
//
//}
