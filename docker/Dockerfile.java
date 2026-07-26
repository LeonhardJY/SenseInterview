# Java微服务通用Dockerfile
FROM openjdk:17-jdk-slim

WORKDIR /app

# 复制jar包（构建时传入服务名参数）
ARG SERVICE_NAME
COPY backend/${SERVICE_NAME}/target/${SERVICE_NAME}-1.0.0.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]