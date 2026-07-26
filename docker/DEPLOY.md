# Docker 部署指南

## 前提条件

- Linux 服务器
- Docker 20.10+
- Docker Compose 2.0+

## 部署步骤

### 1. 上传项目到服务器

```bash
# 将项目上传到服务器
scp -r mock-Interview/ user@your-server:/opt/
```

### 2. 进入部署目录

```bash
cd /opt/mock-Interview/docker
```

### 3. 先构建后端服务

```bash
# 构建所有Java服务的jar包
cd ../backend
mvn clean package -DskipTests
cd ../docker
```

### 4. 启动所有服务

```bash
docker-compose up -d --build
```

### 5. 查看服务状态

```bash
docker-compose ps
```

### 6. 查看日志

```bash
# 查看所有服务日志
docker-compose logs -f

# 查看某个服务日志
docker-compose logs -f user-service
```

## 服务端口

| 服务 | 端口 | 说明 |
|------|------|------|
| 前端 | 80 | http://your-server |
| user-service | 8081 | 用户服务 |
| interview-service | 8082 | 面试服务 |
| question-service | 8083 | 题库服务 |
| ai-service | 8086 | AI服务 |
| MySQL | 3306 | 数据库 |
| Redis | 6379 | 缓存 |

## 访问地址

```
http://your-server-ip
```

账号：`admin` / 密码：`123456`

## 常用命令

```bash
# 停止所有服务
docker-compose down

# 重启所有服务
docker-compose restart

# 重启某个服务
docker-compose restart user-service

# 查看服务日志
docker-compose logs -f user-service

# 清理所有容器和镜像
docker-compose down -rmi all
```

## 数据持久化

- MySQL数据存储在 `mysql-data` 卷
- Redis数据存储在 `redis-data` 卷

## 注意事项

1. 首次启动会自动导入数据库表和初始数据
2. 如需修改数据库，编辑 `docs/database/ai_interview.sql` 后重启MySQL
3. 修改后端代码后需要重新构建：`docker-compose up -d --build`