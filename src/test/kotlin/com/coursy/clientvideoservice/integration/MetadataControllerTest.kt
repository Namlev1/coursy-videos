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
class MetadataControllerTest : BehaviorSpec() {

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
            `when`("retrieving video metadata") {
                then("should return video metadata") {
                    TODO()
                }
            }
            
            `when`("retrieving videos list"){
                then("should return one element page"){
                    TODO()
                }
            }

            `when`("updating video metadata") {
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
    }
}
