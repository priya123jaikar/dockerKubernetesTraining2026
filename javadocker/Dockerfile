FROM eclipse-temurin:17-jdk-jammy AS builder

WORKDIR /app

COPY Main.java .
RUN javac Main.java

FROM eclipse-temurin:17-jre-jammy AS runtime

WORKDIR /app

COPY --from=builder /app/Main.class .

EXPOSE 8060

CMD ["java", "Main"]
