package com.coursy.clientvideoservice.integration

import io.kotest.core.spec.style.BehaviorSpec
//import jakarta.transaction.Transactional
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
//@Transactional
class ContentControllerTest : BehaviorSpec() {

    val url = "/v1/videos"

    init {
        given("no saved videos") {
            `when`("retrieving videos list"){
                then("should return empty page"){
                    TODO()
                }
            }
        }

        given("video with given ID in system") {
            `when`("retrieving video details"){
                then("should return video details"){
                    TODO()
                }
            }
            
            `when`("retrieving videos list"){
                then("should return one element page"){
                    TODO()
                }
            }
            
            `when`("updating video details"){
                then("should return 200 with new metadata"){
                    TODO()
                }
            }

            `when`("deleting video"){
                then("should return 204"){
                    TODO()
                }
            }
        }
        
        given("video with given ID not in system"){
            `when`("retrieving video details"){
                then("should return 404 with IdNotExists"){
                    TODO()
                }
            }

            `when`("updating video file"){
                then("should return 404 with IdNotExists"){
                    TODO()
                }
            }

            `when`("deleting video"){
                then("should return 404 with IdNotExists"){
                    TODO()
                }
            }
        }
        
        given("user is sending correct file"){
            `when`("sending POST /videos/upload"){
                then("the file is saved, metadata is returned"){
                    TODO()
                }
            }
        }

        given("user is sending incorrect file extension"){
            `when`("sending POST /videos/upload"){
                then("should return 400 with InvalidFileFormat"){
                    TODO()
                }
            }
        }

        given("user is sending malicious content with .mp4 extension"){
            `when`("sending POST /videos/upload"){
                then("should return 400 with InvalidFileContent"){
                    TODO()
                }
            }
        }

        given("user is sending too big file"){
            `when`("sending POST /videos/upload"){
                then("should return 413 with FileTooBig"){
                    TODO()
                }
            }
        }

        given("platform has 50MB free space"){
            `when`("user sends 100MB file"){
                then("should return 403"){
                    TODO()
                }
            }
        }
    }
}
