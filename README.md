# HonMoon Backend

AI 를 위한 README 입니다.

## Project Overview

HonMoon is a Spring Boot backend application written in Kotlin. It provides a RESTful API with Swagger documentation and
uses PostgreSQL for data persistence.

When analyzing this codebase, pay attention to:

1. **Main Application Entry Point**: `src/main/kotlin/site/honmoon/Main.kt`
2. **Configuration Classes**: `src/main/kotlin/site/honmoon/config/` directory contains all application configurations
3. **Database Schema**: Check `src/main/resources/db/migration/V1__init.sql` for the database structure
4. **Application Properties**: `src/main/resources/application.yml` contains all application settings
5. **Build Configuration**: `build.gradle.kts` defines all dependencies and build settings
6. **Docker Setup**: `compose.yaml` defines all the containerized services

The project uses a version catalog (see `gradle/libs.versions.toml`) to declare and version dependencies.

## Project Structure

```
honmoon-backend/
├── src/
│   └── main/
│       ├── kotlin/
│       │   └── site/
│       │       └── honmoon/
│       │           ├── annotation/       # Custom annotations
│       │           ├── config/           # Application configuration
│       │           │   └── datasouce/    # Database configuration
│       │           └── Main.kt           # Application entry point
│       └── resources/
│           ├── application.yml           # Application configuration
│           └── db/
│               └── migration/            # Flyway database migrations
├── build.gradle.kts                      # Gradle build configuration
├── compose.yaml                          # Docker Compose configuration
├── gradle/
│   └── libs.versions.toml                # Dependency version catalog
└── README.md                             # This file
```

## Configuration Details

The application is configured using Spring Boot's application.yml file with the following key components:

- PostgreSQL database with JPA/Hibernate
- Flyway for database migrations
- Connection pooling with HikariCP

Once the application is running, you can access:

- Swagger UI: http://localhost:8080/swagger-ui.html
- OpenAPI Docs (JSON): http://localhost:8080/v3/api-docs
