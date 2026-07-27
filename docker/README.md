# Docker Desktop 本地部署指南（Windows）

## 一、环境准备

### 1. 安装 Docker Desktop

1. 下载 Docker Desktop for Windows：
   - 官网：https://www.docker.com/products/docker-desktop/
   - 或使用镜像源下载

2. 安装注意事项：
   - 需要开启 WSL2（Windows Subsystem for Linux）
   - 安装完成后重启电脑
   - 打开 Docker Desktop，确保左下角显示绿色图标

3. 验证安装：
```bash
docker --version
docker-compose --version
```

### 2. 确认端口未被占用

```bash
# 检查以下端口是否被占用
netstat -ano | findstr "80 3306 6379 8081 8082 8083 8086"
```

如果有端口被占用，先关闭对应服务。

---

## 二、项目构建

### 1. 构建后端 jar 包

打开终端，进入项目后端目录：

```bash
cd D:\java\item\mock-Interview\backend

# 使用 Maven 构建所有服务
mvn clean package -DskipTests
```

验证构建成功：
```bash
# 检查各服务的 jar 包是否生成
dir user-service\target\*.jar
dir interview-service\target\*.jar
dir question-service\target\*.jar
dir ai-service\target\*.jar
```

### 2. 构建前端静态文件

```bash
cd D:\java\item\mock-Interview\frontend

# 安装依赖
npm install

# 构建生产版本
npm run build
```

验证构建成功：
```bash
dir dist\
```

---

## 三、启动 Docker 服务

### 方式一：使用 docker-compose（推荐）

[//]: # (后端镜像构建)
# user-service
docker build -t docker-user-service \
-f docker/Dockerfile.java \
--build-arg SERVICE_NAME=user-service \
.

# interview-service
docker build -t docker-interview-service \
-f docker/Dockerfile.java \
--build-arg SERVICE_NAME=interview-service \
.

# question-service
docker build -t docker-question-service \
-f docker/Dockerfile.java \
--build-arg SERVICE_NAME=question-service \
.

# ai-service
docker build -t docker-ai-service \
-f docker/Dockerfile.java \
--build-arg SERVICE_NAME=ai-service \
.

[//]: # (前端镜像构建)
docker build -t docker-frontend \
-f docker/Dockerfile.frontend \
.



```bash
cd D:\java\item\mock-Interview\docker

# 一键启动所有服务
docker-compose up -d --build
```



查看启动状态：
```bash
docker-compose ps
```

查看日志：
```bash
docker-compose logs -f
```

### 方式二：手动启动（逐个启动）

#### 1. 启动 MySQL

```bash
docker run -d \
  --name ai-interview-mysql \
  -p 3306:3306 \
  -e MYSQL_ROOT_PASSWORD=123456 \
  -e MYSQL_DATABASE=ai_interview \
  -e MYSQL_CHARSET=utf8mb4 \
  -v D:\java\item\mock-Interview\docs\database\ai_interview.sql:/docker-entrypoint-initdb.d/init.sql \
  mysql:8.0 \
  --character-set-server=utf8mb4 \
  --collation-server=utf8mb4_unicode_ci
```

#### 2. 启动 Redis

```bash
docker run -d \
  --name ai-interview-redis \
  -p 6379:6379 \
  redis:7-alpine
```

#### 3. 启动后端服务（需要先构建镜像）

```bash
# 构建 user-service
docker build -t ai-user-service -f docker/Dockerfile.java --build-arg SERVICE_NAME=user-service .

# 启动 user-service
docker run -d \
  --name ai-user-service \
  -p 8081:8080 \
  -e SPRING_DATASOURCE_URL=jdbc:mysql://host.docker.internal:3306/ai_interview?useUnicode=true&characterEncoding=utf-8&useSSL=false&serverTimezone=Asia/Shanghai \
  -e SPRING_DATASOURCE_USERNAME=root \
  -e SPRING_DATASOURCE_PASSWORD=123456 \
  -e SPRING_REDIS_HOST=host.docker.internal \
  -e JWT_SECRET=REPLACED-JWT-SECRET \
  ai-user-service
```

重复以上步骤启动其他服务。

---

## 四、访问测试

### 1. 检查服务状态

```bash
# 查看所有容器
docker ps

# 查看日志
docker logs ai-interview-mysql
docker logs ai-user-service
```

### 2. 测试接口

```bash
# 测试登录接口
curl http://localhost:8081/api/auth/login -X POST -H "Content-Type: application/json" -d "{\"username\":\"admin\",\"password\":\"123456\"}"

# 或使用浏览器访问
# http://localhost:8081/swagger-ui.html
```

### 3. 访问前端

打开浏览器访问：
```
http://localhost
```

账号：`admin`
密码：`123456`

---

## 五、常用命令

```bash
# 查看所有运行中的容器
docker ps

# 停止所有服务
docker-compose down

# 重启所有服务
docker-compose restart

# 重启单个服务
docker-compose restart user-service

# 查看服务日志
docker-compose logs -f user-service

# 重新构建并启动
docker-compose up -d --build

# 清理所有容器和镜像
docker-compose down -rmi all

# 进入容器内部
docker exec -it ai-interview-mysql bash
docker exec -it ai-interview-redis sh
```

---

## 六、常见问题

### Q1: MySQL 启动失败
```bash
# 检查端口是否被占用
netstat -ano | findstr 3306

# 如果被占用，关闭占用的进程
taskkill /PID <进程ID> /F
```

### Q2: 后端服务连接数据库失败
```bash
# 检查 MySQL 是否启动
docker ps | findstr mysql

# 检查 MySQL 日志
docker logs ai-interview-mysql
```

### Q3: 前端页面打不开
```bash
# 检查前端容器是否运行
docker ps | findstr frontend

# 检查端口是否被占用
netstat -ano | findstr 80
```

### Q4: Redis 连接失败
```bash
# 检查 Redis 是否启动
docker ps | findstr redis

# 测试 Redis 连接
docker exec -it ai-interview-redis redis-cli ping
```

### Q5: 如何修改数据库密码
```bash
# 进入 MySQL 容器
docker exec -it ai-interview-mysql mysql -u root -p123456

# 修改密码
ALTER USER 'root'@'%' IDENTIFIED BY '新密码';
FLUSH PRIVILEGES;
```

---

## 七、数据备份

```bash
# 备份 MySQL 数据
docker exec ai-interview-mysql mysqldump -u root -p123456 ai_interview > backup.sql

# 恢复 MySQL 数据
docker exec -i ai-interview-mysql mysql -u root -p123456 ai_interview < backup.sql
```

---

## 八、服务端口汇总

| 服务 | 端口 | 说明 |
|------|------|------|
| 前端 | 80 | http://localhost |
| user-service | 8081 | 用户服务 |
| interview-service | 8082 | 面试服务 |
| question-service | 8083 | 题库服务 |
| ai-service | 8086 | AI服务 |
| MySQL | 3306 | 数据库 |
| Redis | 6379 | 缓存 |
