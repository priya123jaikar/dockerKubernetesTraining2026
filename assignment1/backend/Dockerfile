FROM maven:3.9.8-eclipse-temurin-17 AS builder

WORKDIR /app

COPY pom.xml .
COPY src ./src

RUN mvn -q -DskipTests package dependency:copy-dependencies -DoutputDirectory=target/lib

FROM eclipse-temurin:17-jre-jammy AS runtime

WORKDIR /app

COPY --from=builder /app/target/backend.jar app.jar
COPY --from=builder /app/target/lib ./lib

EXPOSE 5000

CMD ["java", "-cp", "app.jar:lib/*", "com.assignment1.App"]
