# SenseInterview

AI 模拟面试评测平台，融合语音交互、面部表情分析与大语言模型，从专业知识、语言表达、逻辑思维、情绪控制、自信程度五个维度对面试表现进行多源综合评估与实时反馈。

[![Java 17](https://img.shields.io/badge/Java-17-F89820?logo=openjdk&logoColor=white)](https://openjdk.org/projects/jdk/17/)
[![Spring Boot 3.2](https://img.shields.io/badge/Spring%20Boot-3.2-6DB33F?logo=spring&logoColor=white)](https://spring.io/projects/spring-boot)
[![Vue 3.4](https://img.shields.io/badge/Vue-3.4-4FC08D?logo=vuedotjs&logoColor=white)](https://vuejs.org/)
[![DeepSeek LLM](https://img.shields.io/badge/DeepSeek-LLM-4D6BFE)](https://platform.deepseek.com/)
[![License MIT](https://img.shields.io/badge/License-MIT-blue)](https://opensource.org/licenses/MIT)

## 目录

- [核心亮点](#核心亮点)
- [技术栈](#技术栈)
- [系统架构](#系统架构)
- [项目结构](#项目结构)
- [快速开始](#快速开始)
- [环境变量配置](#环境变量配置)
- [文档](#文档)
- [License](#license)

## 核心亮点

| 亮点 | 说明 |
|------|------|
| 多维度评分 | 综合文本、语音、画面三种数据源，覆盖专业能力、表达能力、逻辑能力、情绪控制、自信程度五个维度 |
| 语音输入 | 基于浏览器 [Web Speech API](https://developer.mozilla.org/docs/Web/API/Web_Speech_API) 实时语音转文字，语音模式下支持自动提交 |
| 面部情绪识别 | 部署 DeepFace Python 微服务，视频面试模式下实时分析面部表情，识别自信、紧张、平静、惊讶等情绪 |
| 流式输出 | 所有 LLM 交互采用 SSE（Server-Sent Events）逐字推送，首 token 延迟由约 5 秒降至约 2.1 秒 |
| 多轮对话管理 | 基于 LangChain4j 对话记忆组件与 Redis 会话缓存，维持连贯的多轮面试追问 |
| 面试模式切换 | 支持 `TEXT`（文字）、`VOICE`（语音识别 + 自动提交）、`VIDEO`（摄像头 + 实时情绪分析）三种模式 |
| 综合报告 | 面试结束自动生成五维评分、情绪分布趋势、LLM 改进建议的完整报告 |

## 技术栈

| 类别 | 技术 |
|------|------|
| 后端 | [![Java 17](https://img.shields.io/badge/Java-17-F89820?logo=openjdk&logoColor=white)](https://openjdk.org/projects/jdk/17/) [![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2-6DB33F?logo=spring&logoColor=white)](https://spring.io/projects/spring-boot) [![Spring Cloud](https://img.shields.io/badge/Spring%20Cloud-2023-6DB33F?logo=spring&logoColor=white)](https://spring.io/projects/spring-cloud) [![MyBatis-Plus](https://img.shields.io/badge/MyBatis--Plus-3.5-E2615F?logo=apachemaven&logoColor=white)](https://baomidou.com/) |
| 前端 | [![Vue](https://img.shields.io/badge/Vue-3.4-4FC08D?logo=vuedotjs&logoColor=white)](https://vuejs.org/) [![Vite](https://img.shields.io/badge/Vite-5-646CFF?logo=vite&logoColor=white)](https://vitejs.dev/) [![Element Plus](https://img.shields.io/badge/Element%20Plus-2.4-409EFF?logo=element&logoColor=white)](https://element-plus.org/) [![Pinia](https://img.shields.io/badge/Pinia-2.1-FFD859?logo=pinia&logoColor=black)](https://pinia.vuejs.org/) |
| AI / 大模型 | [![DeepSeek](https://img.shields.io/badge/DeepSeek-API-4D6BFE)](https://platform.deepseek.com/) [![LangChain4j](https://img.shields.io/badge/LangChain4j-0.33-1C3C3C?logo=langchain&logoColor=white)](https://docs.langchain4j.dev/) |
| 语音 / 视觉 | [![Web Speech API](https://img.shields.io/badge/Web%20Speech-API-FF6B6B)](https://developer.mozilla.org/docs/Web/API/Web_Speech_API) [![DeepFace](https://img.shields.io/badge/DeepFace-Python-3776AB?logo=python&logoColor=white)](https://github.com/serengil/deepface) [![Flask](https://img.shields.io/badge/Flask-2.3-000000?logo=flask&logoColor=white)](https://flask.palletsprojects.com/) |
| 存储 / 基础设施 | [![MySQL](https://img.shields.io/badge/MySQL-8.0-4479A1?logo=mysql&logoColor=white)](https://www.mysql.com/) [![Redis](https://img.shields.io/badge/Redis-7-FF4438?logo=redis&logoColor=white)](https://redis.io/) [![Docker](https://img.shields.io/badge/Docker-Compose-2496ED?logo=docker&logoColor=white)](https://www.docker.com/) |

## 系统架构

```
浏览器 (Vue 3)
    |
    ├── /api/* ---- 网关 (Spring Cloud Gateway) ---- 后端服务模块
    │                          |
    ├── /ws/* ---- 网关 ---- WebSocket 处理器 ---- 面试实时状态推送
    │                          |
    ├── SSE ---- /api/ai/* ---- LLM 流式 ---- DeepSeek API
    │                          |
    └── 摄像头 ---- /api/ai/analyze-emotion ---- FaceAnalysisService ---- DeepFace (Python)
```

- 统一网关：所有 API 请求经网关转发，负责 JWT 鉴权与路由分发。
- 实时通信：每场面试建立独立 WebSocket 连接，推送面试进度与实时状态。
- 流式 LLM：SSE 逐字渲染，首 token 延迟约 2.1 秒。
- 视觉分析：DeepFace 以独立 Python Flask 进程运行，AI 服务通过 HTTP 异步调用。

## 项目结构

```
backend/
├── common/                    # 公共模块：DTO、异常、统一返回
├── gateway-service/           # 网关服务：路由转发、JWT 鉴权
├── user-service/              # 用户服务：登录注册、简历管理
├── interview-service/         # 面试服务：任务管理、问答记录、会话缓存、
│                               WebSocket、报告生成、LangChain4j 记忆
├── question-service/          # 题库服务：题目 CRUD
├── evaluation-service/        # 评测服务：评测记录
├── report-service/            # 报告服务：报告存储
└── ai-service/                # AI 服务：LLM 编排、SSE 流式、情绪分析客户端

emotion-service/               # DeepFace 情绪识别 Python 微服务

frontend/
├── src/
│   ├── views/                 # 面试房间、大厅、历史记录、报告、管理后台
│   ├── utils/                 # SSE 客户端、WebSocket 客户端、语音识别
│   ├── store/                 # Pinia 状态管理
│   └── api/                   # Axios 实例与拦截器
```

## 快速开始

### 环境要求

- Java 17+
- Node.js 18+
- Python 3.10+（运行 DeepFace 情绪识别时需要）
- MySQL 8.0
- Redis
- DeepSeek API Key（获取地址：https://platform.deepseek.com/api_keys）

### 启动步骤

```bash
# 1. 克隆仓库
git clone https://github.com/LeonhardJY/SenseInterview.git
cd SenseInterview

# 2. 配置环境变量（见下方「环境变量配置」）

# 3. 启动 MySQL 和 Redis，并初始化数据库
#    执行 docs/database/ai_interview.sql

# 4. 启动后端服务（在 backend/ 目录下）
cd backend
mvn spring-boot:run -pl gateway-service    # 端口 8080
mvn spring-boot:run -pl ai-service         # 端口 8086
mvn spring-boot:run -pl interview-service  # 端口 8082

# 5. 启动前端（在 frontend/ 目录下）
cd frontend
npm install
npm run dev                                 # 端口 3000

# 6. （可选）启动 DeepFace 情绪识别服务
cd backend/emotion-service
pip install -r requirements.txt
python download_model.py                    # 下载约 500MB 模型权重
python app.py                               # 端口 5000
```

### 服务端口

| 服务 | 端口 | 服务 | 端口 |
|------|------|------|------|
| 网关服务 | 8080 | 评测服务 | 8084 |
| 用户服务 | 8081 | 报告服务 | 8085 |
| 面试服务 | 8082 | AI 服务 | 8086 |
| 题库服务 | 8083 | 前端开发服务器 | 3000 |
| DeepFace 情绪服务 | 5000 |  |  |

### Docker 部署

本地一键部署方式见 [docker/README.md](docker/README.md)，敏感配置通过 `docker/.env` 注入。

## 环境变量配置

所有敏感配置均通过环境变量注入，仓库中不包含任何真实密钥。可复制项目根目录的 `.env.example` 为 `.env` 并按需修改。

| 变量 | 说明 | 必填 |
|------|------|------|
| `SENSE_LLM_KEY` | DeepSeek API Key（获取：https://platform.deepseek.com/api_keys） | 是 |
| `SPRING_DATASOURCE_USERNAME` | MySQL 用户名（默认 `root`） | 否 |
| `SPRING_DATASOURCE_PASSWORD` | MySQL 密码 | 是 |
| `JWT_SECRET` | JWT 签名密钥，各微服务须一致，需 ≥32 字符（生成：`openssl rand -base64 48`） | 是 |
| `SENSE_NLP_APP_KEY` | 阿里云 NLP AppKey（可选） | 否 |
| `SENSE_NLP_ACCESS_KEY_ID` | 阿里云 AccessKey ID（可选） | 否 |
| `SENSE_NLP_ACCESS_KEY_SECRET` | 阿里云 AccessKey Secret（可选） | 否 |

```bash
# Windows（全局生效）
setx SENSE_LLM_KEY sk-your-deepseek-api-key-here
setx SPRING_DATASOURCE_PASSWORD your-mysql-password
setx JWT_SECRET your-random-jwt-secret-at-least-32-chars

# Linux / Mac（临时生效，关闭终端后失效）
export SENSE_LLM_KEY=sk-your-deepseek-api-key-here
export SPRING_DATASOURCE_PASSWORD=your-mysql-password
export JWT_SECRET=your-random-jwt-secret-at-least-32-chars
```

> IDEA 用户可在启动配置中设置：`Run` → `Edit Configurations` → `Environment variables` 添加上述变量。
>
> 注意：配置文件中的 `${VAR:默认值}` 会优先读取环境变量，未设置时使用占位值（如 `sk-placeholder`、`your-mysql-password`），会导致对应功能不可用。生产环境务必设置真实密钥，并保持所有服务的 `JWT_SECRET` 一致。

## 文档

- [API 接口设计](docs/api/AI-Interview-API接口设计.md)
- [系统架构设计](docs/architecture/04-系统架构设计.md)
- [数据库设计](docs/database/01-数据库设计.md)
- [需求设计文档](docs/product/02-需求设计文档.md)
- [Docker 部署指南](docker/README.md)

## License

本项目基于 [MIT](https://opensource.org/licenses/MIT) 协议开源。
