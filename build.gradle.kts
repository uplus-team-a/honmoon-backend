plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.spring)
    alias(libs.plugins.kotlin.jpa)
    alias(libs.plugins.kotlin.noarg)
    alias(libs.plugins.kotlin.allopen)
    alias(libs.plugins.kotlin.kapt)
    alias(libs.plugins.spring.boot)
    alias(libs.plugins.spring.dependency.management)
}

group = "com"
version = "0.0.1-SNAPSHOT"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

kotlin {
    jvmToolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
    compilerOptions {
        freeCompilerArgs.addAll("-Xjsr305=strict")
    }
}

repositories {
    mavenCentral()
    maven {
        url = uri("https://plugins.gradle.org/m2/")
    }
    maven("https://jitpack.io")
    maven {
        url = uri("https://repo.spring.io/milestone")
    }
    google()
}

extra["springCloudVersion"] = libs.versions.springCloud.get()
extra["springAiVersion"] = libs.versions.springAi.get()

configurations {
    compileOnly {
        extendsFrom(configurations.annotationProcessor.get())
    }
    all {
        resolutionStrategy {
            force(libs.guava)
            force(libs.protobuf.java)
        }
        // Exclude legacy Swagger annotations to avoid conflicts with Jakarta variant
        exclude(group = "io.swagger.core.v3", module = "swagger-annotations")
    }
}

dependencies {
    implementation(libs.spring.boot.starter.aop)
    implementation(libs.spring.boot.starter.actuator)
    implementation(libs.spring.boot.starter.data.jdbc)
    implementation(libs.spring.boot.starter.data.jpa)
    implementation(libs.spring.boot.starter.web)
    implementation(libs.spring.cloud.starter.circuitbreaker.resilience4j)
    implementation(libs.spring.cloud.starter.openfeign)
    implementation(libs.jackson.module.kotlin)
    implementation(libs.kotlin.reflect)
    implementation(libs.kotlin.logging.jvm)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.reactor)

    // Security
    implementation(libs.spring.boot.starter.security)
    implementation(libs.spring.boot.starter.oauth2.client)

    // Prometheus
    implementation(libs.micrometer.registry.prometheus)

    // Querydsl
    implementation("com.querydsl:querydsl-jpa:${libs.versions.querydsl.get()}:jakarta")
    kapt(libs.querydsl.apt)
    implementation(libs.hibernateTypes60)

    // Flyway + Database
    implementation(libs.flyway.core)
    implementation(libs.flyway.database.postgresql)
    implementation(libs.postgresql)

    // 카페인 캐시 (인메모리)
    implementation(libs.caffeine)

    // Swagger/OpenAPI
    implementation(libs.springdoc.openapi.starter.webmvc.ui)
    implementation(libs.commons.lang3)
    runtimeOnly(libs.h2)

    // Mail
    implementation(libs.spring.boot.starter.mail)
    implementation(libs.spring.boot.starter.thymeleaf)

    // Spring Cloud GCP
    implementation(libs.spring.cloud.gcp.starter)
    implementation(libs.google.cloud.storage)

    // Spring AI, OpenAI
    implementation(libs.spring.ai.openai)
    
    // Google Gen AI (Gemini) - Spring AI ChatClient
    implementation(libs.spring.boot.starter.webflux)

    developmentOnly(libs.spring.boot.docker.compose)

    annotationProcessor(libs.spring.boot.configuration.processor)
    testImplementation(libs.spring.restdocs.mockmvc)
    testImplementation(libs.spring.boot.starter.test)
    testImplementation(libs.kotlin.test.junit5)
    testImplementation(libs.kotest.assertions.core)
    testImplementation(libs.kotest.runner.junit5)
    testImplementation(libs.kotest.extensions.spring)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.mockk)

    // JWT
    implementation("io.jsonwebtoken:jjwt-api:0.12.7")
    runtimeOnly("io.jsonwebtoken:jjwt-impl:0.12.7")
    runtimeOnly("io.jsonwebtoken:jjwt-jackson:0.12.7")
}

dependencyManagement {
    imports {
        mavenBom("org.springframework.cloud:spring-cloud-dependencies:${property("springCloudVersion")}")
        mavenBom("org.springframework.ai:spring-ai-bom:${property("springAiVersion")}")
    }
}

noArg {
    annotation("jakarta.persistence.Entity")
    annotation("jakarta.persistence.MappedSuperclass")
    annotation("jakarta.persistence.Embeddable")
    annotation("site.honmoon.annotation.NoArgsConstructor")
}

allOpen {
    annotation("jakarta.persistence.Entity")
    annotation("jakarta.persistence.MappedSuperclass")
    annotation("jakarta.persistence.Embeddable")
    annotation("site.honmoon.annotation.NoArgsConstructor")
}

springBoot {
    mainClass.set("site.honmoon.MainKt")
}

sourceSets {
    main {
        java {
            srcDirs("build/generated/source/kapt/main")
        }
    }
}

tasks.jar {
    enabled = false
}

tasks.bootJar {
    enabled = true
}

tasks.test {
    useJUnitPlatform()
    // Kotest autoscan 비활성화로 테스트 시작 시간 단축 및 경고 제거
    systemProperty("kotest.framework.classpath.scanning.autoscan.disable", "true")
    testLogging {
        events("passed", "skipped", "failed")
        showStandardStreams = true
    }
}
