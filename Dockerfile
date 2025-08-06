FROM eclipse-temurin:21-jdk

COPY build/libs/honmoon-0.0.1-SNAPSHOT.jar /app.jar

# Expose port 8080
EXPOSE 8080

# Run the application
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
