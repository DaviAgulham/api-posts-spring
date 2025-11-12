# Build
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml ./
RUN mvn -q -DskipTests dependency:go-offline
COPY src ./src
RUN mvn -q -DskipTests package

# Runtime
FROM eclipse-temurin:21-jre
WORKDIR /app
RUN useradd -m appuser
COPY --from=build /app/target/*.jar app.jar
# dir para uploads
RUN mkdir -p /data/uploads && chown -R appuser:appuser /data
USER appuser
EXPOSE 8081
ENTRYPOINT ["java","-jar","/app/app.jar"]
