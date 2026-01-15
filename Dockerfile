# Build stage
FROM eclipse-temurin:17-jdk AS builder
WORKDIR /build

# Install Maven once
RUN apt-get update && apt-get install -y maven && rm -rf /var/lib/apt/lists/*

# Copy only pom.xml first (to cache dependencies)
COPY pom.xml .

# Download dependencies (cached layer)
RUN mvn dependency:go-offline -DskipTests -q || true

# Copy source code
COPY src ./src

# Build application
RUN mvn clean package -q -DskipTests

# Runtime stage
FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=builder /build/target/ebook-store-*.jar app.jar
EXPOSE 8080
ENV SPRING_PROFILES_ACTIVE=default
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
