# Java微服务通用Dockerfile
FROM eclipse-temurin:17-jdk

WORKDIR /app

# 复制jar包（构建时传入服务名参数）
ARG SERVICE_NAME
COPY backend/${SERVICE_NAME}/target/${SERVICE_NAME}-1.0.0.jar app.jar

EXPOSE 8080

# 绑定到所有网络接口
ENTRYPOINT ["java", "-Dserver.address=0.0.0.0", "-jar", "app.jar"]