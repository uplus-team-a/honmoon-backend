## Project Overview

HonMoon is a Spring Boot backend application written in Kotlin. It provides a RESTful API with Swagger documentation and
uses PostgreSQL for data persistence. The project follows a domain-driven design approach with clean architecture principles.

When analyzing this codebase, pay attention to:

1. **Main Application Entry Point**: `src/main/kotlin/site/honmoon/Main.kt`
2. **Configuration Classes**: `src/main/kotlin/site/honmoon/config/` directory contains all application configurations
3. **Database Schema**: Check `src/main/resources/db/migration/V1__init.sql` for the database structure
4. **Application Properties**: `src/main/resources/application.yml` contains all application settings
5. **Build Configuration**: `build.gradle.kts` defines all dependencies and build settings
6. **Docker Setup**: `compose.yaml` defines all the containerized services
7. **Domain Structure**: Each domain follows controller/service/repository/entity pattern
8. **Common Components**: Shared utilities, DTOs, and base classes in the common package
9. **Exception Handling**: Global exception handling with custom error responses

The project uses a version catalog (see `gradle/libs.versions.toml`) to declare and version dependencies.

## Project Structure

```
honmoon-backend/
├── src/
│   └── main/
│       ├── kotlin/
│       │   └── site/
│       │       └── honmoon/
│       │           ├── annotation/           # Custom annotations
│       │           ├── common/               # Shared components
│       │           │   ├── dto/              # Common DTOs (ApiResponse, PageResponse)
│       │           │   ├── entity/           # Base entity classes (BaseEntity)
│       │           │   └── exception/        # Global exception handling
│       │           ├── config/               # Application configuration
│       │           │   └── datasource/       # Database configuration
│       │           ├── sample/               # Sample domain (example implementation)
│       │           │   ├── controller/       # REST controllers
│       │           │   ├── dto/              # Domain-specific DTOs
│       │           │   ├── entity/           # JPA entities
│       │           │   ├── repository/       # Spring Data repositories
│       │           │   └── service/          # Business logic
│       │           ├── Main.kt               # Application entry point
│       │           └── README.md             # Package structure documentation
│       └── resources/
│           ├── application.yml               # Application configuration
│           └── db/
│               └── migration/                # Flyway database migrations
├── build.gradle.kts                          # Gradle build configuration
├── compose.yaml                              # Docker Compose configuration
├── gradle/
│   └── libs.versions.toml                    # Dependency version catalog
└── README.md                                 # Project documentation
```

## Domain Architecture

The project follows a domain-driven design approach where each business domain is organized in its own package:

### Domain Package Structure
Each domain package contains:
- **controller/**: REST API endpoints with Swagger documentation
- **dto/**: Request/Response DTOs for API communication
- **entity/**: JPA entities representing database tables
- **repository/**: Spring Data repositories for data access
- **service/**: Business logic and transaction management

### Common Components
- **BaseEntity**: Provides audit fields (createdBy, createdAt, modifiedBy, modifiedAt)
- **ApiResponse**: Standardized API response wrapper
- **PageResponse**: Pagination response structure
- **GlobalExceptionHandler**: Centralized exception handling
- **ResourceNotFoundException**: Custom exception for not found resources

### Sample Domain Implementation
The project includes a complete sample domain demonstrating the architecture:
- **SampleEntity**: JPA entity with audit fields
- **SampleRepository**: Spring Data repository with custom queries
- **SampleService**: Business logic with CRUD operations
- **SampleController**: REST endpoints with full CRUD API
- **SampleDto**: Request/Response DTOs (SampleResponse, SampleCreateRequest, SampleUpdateRequest)

## API Endpoints

### Sample API (`/api/samples`)
- `GET /api/samples` - Get paginated list of samples
- `GET /api/samples/{id}` - Get sample by ID
- `GET /api/samples/by-name?name={name}` - Get samples by name
- `POST /api/samples` - Create new sample
- `PUT /api/samples/{id}` - Update existing sample
- `DELETE /api/samples/{id}` - Delete sample

## Configuration Details

The application is configured using Spring Boot's application.yml file with the following key components:

- PostgreSQL database with JPA/Hibernate
- Flyway for database migrations
- Connection pooling with HikariCP
- JPA Auditing for automatic audit field population
- QueryDSL for type-safe queries
- Swagger/OpenAPI for API documentation

Once the application is running, you can access:

- Swagger UI: http://localhost:8080/swagger-ui.html
- OpenAPI Docs (JSON): http://localhost:8080/v3/api-docs
