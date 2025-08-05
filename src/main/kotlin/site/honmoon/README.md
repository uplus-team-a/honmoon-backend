# HonMoon Backend Package Structure

This project follows a domain-driven design approach for package organization. Each domain has its own package containing controllers, services, repositories, and entities.

## Package Structure

```
site.honmoon/
├── common/                  # Common components used across domains
│   ├── dto/                 # Common DTOs
│   ├── entity/              # Base entity classes
│   └── exception/           # Global exception handling
├── config/                  # Application configuration
│   └── datasource/          # Database configuration
├── sample/                  # Sample domain (example)
│   ├── controller/          # REST controllers
│   ├── dto/                 # Domain-specific DTOs
│   ├── entity/              # JPA entities
│   ├── repository/          # Spring Data repositories
│   └── service/             # Business logic
└── Main.kt                  # Application entry point
```

## Domain Package Structure

Each domain package follows this structure:

- **controller/**: REST API endpoints
- **dto/**: Data Transfer Objects for API requests and responses
- **entity/**: JPA entities representing database tables
- **repository/**: Data access layer with Spring Data repositories
- **service/**: Business logic layer

## Common Package Structure

The common package contains shared components:

- **dto/**: Common response DTOs like ApiResponse and PageResponse
- **entity/**: Base entity classes like BaseEntity with audit fields
- **exception/**: Global exception handling with GlobalExceptionHandler

## Best Practices

1. Keep domain logic within its domain package
2. Use DTOs to transfer data between layers
3. Follow the Single Responsibility Principle
4. Use dependency injection for loose coupling
5. Add proper documentation with Swagger annotations
