# Multi-stage build is unnecessary here because CI builds the JAR.
# This image expects the bootable JAR to be present in
# apps/backend/build/libs/ before docker build (downloaded from CI artifact).

FROM eclipse-temurin:25-jre-alpine

WORKDIR /app

# Default heap tuning; can be overridden at runtime via JAVA_TOOL_OPTIONS
ENV JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=75 -XX:+UseG1GC -XX:+UseStringDeduplication" \
    TZ=UTC

# Copy the Spring Boot fat JAR built in CI. The pattern matches the boot JAR
# (and not the -plain classifier). Adjust if your versioning changes.
COPY apps/backend/build/libs/*-SNAPSHOT.jar /app/app.jar

EXPOSE 8080

ENTRYPOINT ["java","-jar","/app/app.jar"]

