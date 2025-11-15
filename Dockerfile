FROM eclipse-temurin:11-jdk-alpine AS builder
WORKDIR /app
COPY Example.java .
RUN javac Example.java
RUN jlink --module-path /opt/java/openjdk/jmods --add-modules java.base,jdk.httpserver --output /opt/jre-min

FROM alpine:3.20
WORKDIR /app
COPY --from=builder /opt/jre-min /opt/jre-min
COPY --from=builder /app/Example.class .
ENV PATH="/opt/jre-min/bin:${PATH}"
CMD ["java", "Example"]