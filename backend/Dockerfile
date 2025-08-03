FROM eclipse-temurin:21-jdk-alpine:latest

# Update Alpine packages to fix vulnerabilities
RUN apk update && apk upgrade && rm -rf /var/cache/apk/*

# Set working directory
WORKDIR /app

# Copy Maven wrapper and pom.xml
COPY mvnw pom.xml ./
COPY .mvn .mvn

# Download dependencies
RUN ./mvnw dependency:go-offline -B

# Copy source code
COPY src src

# Build the application
RUN ./mvnw clean package -DskipTests

# Create a non-root user
RUN addgroup -g 1001 -S appgroup && \
    adduser -u 1001 -S appuser -G appgroup

# Change ownership of the app directory
RUN chown -R appuser:appgroup /app

# Create uploads directory
RUN mkdir -p uploads && chown -R appuser:appgroup uploads

# Switch to non-root user
USER appuser

# Expose port
EXPOSE 8081

# Run the application
CMD ["java", "-jar", "target/p2p-file-share-1.0.0.jar"]