FROM eclipse-temurin:26-jdk-jammy AS build
WORKDIR /workspace

COPY .mvn/wrapper/maven-wrapper.properties .mvn/wrapper/maven-wrapper.properties
COPY mvnw pom.xml ./
# Git checkouts on Windows may contain CRLF line endings.
RUN sed -i 's/\r$//' mvnw && chmod +x mvnw

COPY src/main src/main
RUN ./mvnw --batch-mode --no-transfer-progress -Dmaven.test.skip=true package

FROM eclipse-temurin:26-jre-jammy AS runtime
WORKDIR /app

RUN groupadd --system app && useradd --system --gid app app
COPY --from=build --chown=app:app /workspace/target/personal-finance-backend-0.0.1-SNAPSHOT.jar app.jar

ENV SPRING_PROFILES_ACTIVE=prod
# Keep room for JVM native memory on Render Free's 512 MB instance.
ENV JAVA_TOOL_OPTIONS="-Xms64m -Xmx256m -XX:+UseSerialGC -XX:ReservedCodeCacheSize=48m -Xss512k"

USER app
EXPOSE 10000
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
