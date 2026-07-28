# SenseInterview — AI 模拟面试评测平台

融合语音交互、面部表情分析与大语言模型的 AI 面试助手。针对纯文本评估维度单一的痛点，接入语音与画面分析，实现面试表现的多维度评估与实时反馈。

## 项目背景

传统模拟面试工具仅依赖文字问答，无法评估求职者的表达能力、情绪稳定性与整体气场。SenseInterview 将语音识别与计算机视觉引入面试流程，从专业知识、语言表达、逻辑思维、情绪控制、自信程度五个维度进行综合评估。

## 核心能力

- **多维度评分** — 覆盖专业能力、表达能力、逻辑能力、情绪控制、自信程度五个维度，综合文本、语音、画面三种数据源。
- **语音输入** — 基于浏览器 Web Speech API 实现实时语音转文字，语音模式下支持自动提交。
- **面部情绪识别** — 部署 DeepFace Python 微服务，在视频面试模式下实时分析面试者面部表情，识别自信、紧张、平静、惊讶等情绪。
- **流式输出** — 所有 LLM 交互均采用 SSE（Server-Sent Events）逐字推送，用户感知响应时间由约 5 秒降至约 2.1 秒。
- **多轮对话管理** — 基于 LangChain4j 对话记忆组件与 Redis 会话缓存，维持连贯的多轮面试对话。
- **面试模式切换** — 支持 TEXT（文字输入）、VOICE（语音识别 + 自动提交）、VIDEO（摄像头画面 + 实时情绪分析）三种模式。
- **综合报告** — 面试结束后生成包含五维评分、情绪分布趋势、LLM 改进建议的完整报告。

## 技术栈

| 类别 | 技术 |
|------|------|
| 后端 | Java 17, Spring Boot 3.2, Spring Cloud 2023, MyBatis-Plus 3.5 |
| 前端 | Vue 3.4, Vite 5, Element Plus 2.4, Pinia 2.1 |
| AI / 大模型 | DeepSeek API, LangChain4j 0.33, SSE 流式输出 |
| 语音 | Web Speech API（浏览器原生） |
| 视觉 | DeepFace + Flask（Python 微服务） |
| 存储 | MySQL 8.0, Redis, Druid 连接池 |
| 基础设施 | Spring Cloud Gateway, JWT 鉴权, WebSocket |

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

### API Key 配置

系统通过环境变量 `SENSE_LLM_KEY` 读取 DeepSeek API Key，可按以下方式配置：

```bash
# Windows（全局生效）
setx SENSE_LLM_KEY sk-your-deepseek-api-key-here

# Linux / Mac（临时生效，关闭终端后失效）
export SENSE_LLM_KEY=sk-your-deepseek-api-key-here

# Linux / Mac（永久生效）
echo 'export SENSE_LLM_KEY=sk-your-deepseek-api-key-here' >> ~/.bashrc
```

IDEA 用户可在启动配置中设置：`Run` → `Edit Configurations` → `Environment variables` 添加 `SENSE_LLM_KEY=your-key`。

> 注意：API Key 已从代码仓库中移除，配置文件中的 `${SENSE_LLM_KEY:sk-placeholder}` 会读取环境变量，未设置时使用占位值 `sk-placeholder` 会导致 LLM 调用失败。

### 启动步骤

```bash
# 1. 克隆仓库
git clone https://github.com/LeonhardJY/SenseInterview.git
cd SenseInterview

# 2. 配置 API Key（见上方说明）

# 3. 启动 MySQL 和 Redis

# 4. 初始化数据库
#    执行 docs/database/ai_interview.sql

# 5. 启动后端服务（在 backend/ 目录下）
cd backend
mvn spring-boot:run -pl gateway-service    # 端口 8080
mvn spring-boot:run -pl ai-service         # 端口 8086
mvn spring-boot:run -pl interview-service  # 端口 8082

# 6. 启动前端（在 frontend/ 目录下）
cd frontend
npm install
npm run dev                                 # 端口 3000

# 7. （可选）启动 DeepFace 情绪识别服务
cd backend/emotion-service
pip install -r requirements.txt
python download_model.py                    # 下载约 500MB 模型权重
python app.py                               # 端口 5000
```

### 服务端口

| 服务 | 端口 |
|------|------|
| 网关服务 | 8080 |
| 用户服务 | 8081 |
| 面试服务 | 8082 |
| 题库服务 | 8083 |
| 评测服务 | 8084 |
| 报告服务 | 8085 |
| AI 服务 | 8086 |
| 前端开发服务器 | 3000 |
| DeepFace 情绪服务 | 5000 |

## 架构说明

```
浏览器 (Vue 3)
    |
    ├── /api/* ---- 网关 (Spring Cloud) ---- 后端服务模块
    │                                              |
    ├── /ws/* ----- 网关 ----- WebSocket 处理器      |
    |                                               |
    ├── SSE ------ /api/ai/* --- LLM 流式 --- DeepSeek API
    |                                               |
    └── 摄像头 --- /api/ai/analyze-emotion -- FaceAnalysisService -- DeepFace (Python)
```

- 所有 API 请求经网关统一转发，网关负责 JWT 鉴权与路由分发。
- 每场面试建立独立 WebSocket 连接，用于推送面试进度与实时状态更新。
- LLM 响应通过 SSE 流式传输，前端逐字渲染，首 token 延迟约 2.1 秒。
- DeepFace 微服务以独立 Python Flask 进程运行，AI 服务通过 HTTP 异步调用。

## License

MIT
