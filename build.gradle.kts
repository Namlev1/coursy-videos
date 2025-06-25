plugins {
    kotlin("jvm") version "2.1.0"
    kotlin("plugin.spring") version "2.1.0"
    id("org.springframework.boot") version "3.5.0"
    id("io.spring.dependency-management") version "1.1.7"
    kotlin("plugin.jpa") version "2.0.0"
}

group = "com.coursy"
version = "0.0.1-SNAPSHOT"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.springframework.boot:spring-boot-starter-hateoas")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.9.0")
    
    // Swagger
    implementation("io.swagger.core.v3:swagger-annotations:2.2.31")
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.8.9")
    // Security
//    implementation("org.springframework.boot:spring-boot-starter-security")

    // Authentication
//    implementation("com.auth0:java-jwt:4.5.0")

    // DB
    runtimeOnly("org.postgresql:postgresql")
    testImplementation("com.h2database:h2:2.3.232")

    // minIo
    implementation("io.minio:minio:8.5.17")

    // FP / Error Handling
    implementation("io.arrow-kt:arrow-core:2.0.1")

    // Testing - Spring & JUnit
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
    testImplementation("org.springframework.security:spring-security-test")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    // Testing - Kotest
    testImplementation("io.kotest:kotest-runner-junit5:5.9.0")
    testImplementation("io.kotest:kotest-assertions-core:5.9.0")
    testImplementation("io.kotest:kotest-property:5.9.0")
    implementation("io.kotest.extensions:kotest-extensions-spring:1.3.0")

    // Testing - Mocking
    testImplementation("io.mockk:mockk:1.13.17")

    // Testing - Additional Assertions
    testImplementation("io.kotest.extensions:kotest-assertions-arrow:2.0.0")
}
kotlin {
    compilerOptions {
        freeCompilerArgs.addAll("-Xjsr305=strict")
    }
}
tasks.withType<Test> {
    useJUnitPlatform()
}