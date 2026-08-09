# Mock-Interview · RAG 知识库 + Agent 面试官 改造说明

> 生成时间：2026-08-09
> 用途：记录本次对 ai-service 的 RAG + Agent 改造，供工作区迁移 / 新人接手时快速恢复上下文。

## 一、改造目标

在原「AI 模拟面试平台」（DeepSeek LLM + 手写 RestTemplate 调用）基础上，新增两层能力：

1. **RAG 检索增强**：把「大厂面试真题」建成向量知识库，面试官出题/追问时先检索相关真题再作答。
2. **Agent 面试官**：基于 langchain4j `AiServices` + 工具调用（function calling），让 LLM 能自主调用工具（查题库 / 检索知识库）后再出题，实现"先查再答"。

**技术选型**：langchain4j 0.33.0（与根 pom 版本一致）+ Ollama 本地向量模型 + Qdrant 向量库 + DeepSeek（OpenAI 兼容，已验证 function calling 可用）。

## 二、新增 / 修改文件清单

### 1. 知识源（新增）`backend/ai-service/src/main/resources/knowledge/`

| 文件 | 内容 |
|------|------|
| `alibaba.md` | 阿里 Java 后端真题（JVM/并发/秒杀/缓存） |
| `tencent.md` | 腾讯 Java 后端真题（JVM/GC/Redis 集群） |
| `bytedance.md` | 字节后端真题（分布式锁/索引失效/秒杀/幂等） |
| `meituan.md` | 美团后端真题（caffeine 缓存/秒杀优惠券/Redis 集群） |
| `huawei.md` | 华为 OD Java 真题（重写重载/单例/String） |
| `iflytek.md` | 科大讯飞 Java 真题（集合/JUC/Redis/布隆过滤器） |

> 这些是 RAG 的知识源。**后续扩知识 = 往这个目录加 md 文件**，再调 `/api/ai/rag/rebuild` 重建索引。

### 2. 依赖（修改）

- `backend/pom.xml`：`dependencyManagement` 新增 `langchain4j-open-ai` / `langchain4j-ollama` / `langchain4j-qdrant`（版本 ${langchain4j.version}=0.33.0）。
- `backend/ai-service/pom.xml`：新增 `langchain4j`（聚合包，含 AiServices/ChatMemory）+ 上述三个模块。

> ⚠️ 注意：`AiServices` 和 `MessageWindowChatMemory` 在 `langchain4j` 聚合包里，只引子模块编译不过。
> ⚠️ 注意：langchain4j 0.33 的 `@Tool` 注解包路径是 `dev.langchain4j.agent.tool.Tool`（不是 `service.tool`）。

### 3. RAG 服务（新增）`backend/ai-service/src/main/java/com/interview/ai/service/RagService.java`

核心类，流程：加载 knowledge 目录 → 按 `## ` 二级标题切分 → Ollama embedding 向量化 → 写入 Qdrant → 检索 top-k。

> ⚠️ 已改用 **Qdrant REST API**（避免 gRPC HTTP/2 在 Windows 上的兼容性问题）。

- `init()`：初始化 `OllamaEmbeddingModel`（HTTP）+ RestTemplate
- `rebuildIndex()`：删旧 collection → 重建（768 维 Cosine）→ 批量向量化 + REST 写入，返回写入段数
- `retrieve(query)`：向量化 query → 本地余弦相似度排序 → 返回 top-k 文档文本
- 私有 `recreateCollection()` / `batchUpsert()` / `loadKnowledgeSegments()` / `splitByHeadings()`
- `VectorEntry` 内部类：缓存向量 + 文本 + metadata

### 4. Agent 服务（新增）

- `AgentInterviewService.java`：`AiServices.builder()` + `MessageWindowChatMemory`（每会话 20 条）+ `InterviewTools`，按 sessionId 维护独立面试官实例。
  - `chat(sessionId, userMessage)` → 会话首次创建 Interviewer，之后复用
  - `clearSession(sessionId)` → 释放会话记忆
- `InterviewTools.java`：三个 `@Tool`：
  - `searchKnowledgeBase(keyword)` → 调 `RagService.retrieve`
  - `searchQuestionBank(keyword)` → HTTP 调 question-service 的 `/api/question/search`
  - `getInterviewEnvironment()` → 返回面试设置（演示多工具编排）

### 5. 接口（修改）`backend/ai-service/src/main/java/com/interview/ai/controller/AiController.java`

新增 4 个接口（网关 `/api/ai/**` 自动覆盖，无需改网关）：

| 接口 | 方法 | 说明 |
|------|------|------|
| `/api/ai/agent-chat` | POST | Agent 对话，body `{sessionId, message}`，返回 String |
| `/api/ai/agent-end` | POST | 结束会话，body `{sessionId}` |
| `/api/ai/rag/rebuild` | POST | 重建知识库索引，返回写入段数 |
| `/api/ai/rag/retrieve` | POST | 检索测试，body `{query}`，返回 List<String> |

### 6. 题库关键词搜索（修改）`backend/question-service/`

- `service/QuestionBankService.java` + `impl/QuestionBankServiceImpl.java`：新增 `findByKeyword(String)`（title/answer LIKE，LIMIT 10）
- `controller/QuestionBankController.java`：新增 `GET /api/question/search?keyword=`

### 7. 配置（修改）`backend/ai-service/src/main/resources/application.yml`

新增 `ai.rag` 段（embedding/qdrant/知识目录/top-k）+ `ai.question-service-url`。

### 8. 测试（新增）`backend/ai-service/src/test/java/com/interview/ai/`

| 文件 | 作用 |
|------|------|
| `SmokeToolCallTest.java` | DeepSeek function calling 冒烟（验证 langchain4j 0.33 工具调用兼容性） |
| `OllamaEmbeddingManualTest.java` | 手动验证 Ollama embedding（不启动 Spring 上下文） |
| `RagManualTest.java` | RAG 全链路（手动注入字段，避开 @SpringBootTest） |

> ⚠️ `RagSmokeTest.java`（@SpringBootTest 版）已删除：在受限环境下启动完整 Spring 上下文会因 NIO Selector loopback 限制失败，改用手动注入测试。

## 三、依赖环境（迁移新工作区必备）

| 依赖 | 版本/端口 | 启动方式 |
|------|-----------|---------|
| Ollama | `nomic-embed-text` 模型，11434 端口 | `ollama serve`，确认 `ollama list` 有该模型 |
| Qdrant | 6333 端口（grpc）| `docker run -d --name qdrant -p 6333:6333 -p 6334:6334 qdrant/qdrant` |
| DeepSeek API key | — | 环境变量 `SENSE_LLM_KEY`（配置 `deepseek-v4-flash` 模型） |
| MySQL / Redis | 项目 docker-compose | `docker compose up -d` |

> Qdrant 的 Java grpc 客户端（netty）需要本地 loopback 连接：**部分受限沙箱环境下跑不了，普通本机无此问题**。

## 四、验证步骤（新环境复现）

```bash
# 1. 设环境变量（Windows）
setx SENSE_LLM_KEY "你的 DeepSeek key"

# 2. 起本地服务
ollama serve            # 确认 nomic-embed-text
docker start qdrant     # 6333

# 3. 跑 RAG 全链路测试（验证 ollama + qdrant + 检索）
cd backend
mvn test -pl ai-service -Dtest=RagManualTest

# 4. 启动 ai-service（idea 或 mvn spring-boot:run -pl ai-service）

# 5. 重建索引 + Agent 对话
curl -X POST http://localhost:8086/api/ai/rag/rebuild
curl -X POST http://localhost:8086/api/ai/agent-chat \
  -H "Content-Type: application/json" \
  -d '{"sessionId":"test1","message":"请根据大厂真题库给我出一道 ConcurrentHashMap 相关的题"}'
```

## 五、已确认 / 待确认

**已验证：**
- ✅ 全量模块 `mvn compile` 通过
- ✅ DeepSeek function calling 兼容 langchain4j 0.33（`SmokeToolCallTest` 实测自动调用 2 个工具）
- ✅ Ollama embedding 返回 768 维向量
- ✅ Qdrant REST API 接受 768/Cosine collection
- ✅ `RagManualTest` 全链路通过（改用 REST API 后）
- ✅ `/api/ai/rag/rebuild` 返回 28 个文档段
- ✅ `/api/ai/rag/retrieve` 检索返回相关面试题
- ✅ 前端 Agent 面试页面（`AgentInterview.vue`）编译通过
- ✅ `/api/ai/agent-chat` 端到端对话正常（含工具调用 + 多轮记忆）
- ✅ `/api/ai/agent-end` 会话释放正常
- ✅ 中文 / 英文消息均可正常交互

## 六、前端 Agent 面试页面（新增）

### 新增文件

- `frontend/src/views/AgentInterview.vue`：Agent 面试页面（**取代原 Interview.vue**，唯一面试入口）
- `frontend/src/utils/markdown.js`：轻量安全 markdown 渲染

### 修改文件

- `frontend/src/router/index.js`：新增 `/agent-interview` 路由，删除 `/interview/:taskId`
- `frontend/src/api/index.js`：新增 `agentChat` / `agentEnd` API 方法
- `frontend/src/views/Lobby.vue`：「开始 Agent 面试」直达 Agent 页，删除创建弹窗
- `frontend/src/views/History.vue`：删除「继续」，报告按钮更显眼
- `frontend/src/views/Layout.vue`：顶部导航增加「✦ Agent」入口

### 页面功能

- 聊天界面：消息气泡 + 打字动画 + 来源徽标
- **三种模式**：文字 / 语音（Web Speech API 自动发） / 视频（摄像头 + 情绪分析）
- **评分接入**：结束面试 → 创建任务 → 生成报告 → 自动跳转 `/report/:taskId`
- 快速开始：预设方向 + 公司真题（阿里/腾讯/字节/美团/华为）
- 面试聚焦：侧边栏主题标签点击切换出题方向
- 人文设计：中式印章「问」签名元素 + 《中庸》语录

## 七、清理记录（取代旧流程）

为消除「普通面试 / Agent 面试」双模块写同一批记录的逻辑冲突，做了以下清理：

### 前端
- 删除 `views/Interview.vue`（旧 SSE 轮次制面试页）
- 删除孤儿工具：`utils/sse.js` / `utils/websocket.js` / `utils/audioRecorder.js`
- 移除 `api/index.js` 中无引用的 `ragRebuild` / `ragRetrieve` 导出

### 后端
- ai-service 删除旧 SSE/轮次制端点：`/generate-question*`、`/generate-follow-up*`、`/generate-evaluation`、`/analyze-text`
- 删除死服务：`LlmService.java`、`NlpService.java`、`AsrService.java`
- `FaceAnalysisService` 移除无调用的 `isHealthy`
- interview-service 删除孤儿 `LangChainMemoryService`、`InterviewSessionService` 中 5 个死方法、`InterviewContext` 中死字段
- 删除死模块：`evaluation-service/`、`report-service/`（功能并入 interview-service）

## 八、注意事项

1. **SENSE_LLM_KEY**：本次冒烟测试用了临时 key，未写入任何项目文件；`.env.example` 仍是占位符，请勿提交真实 key。
2. **langchain4j 0.33 API 差异**（踩过的坑）：
   - `@Tool` → `dev.langchain4j.agent.tool.Tool`
   - `EmbeddingStore.search` → 需 `EmbeddingSearchRequest`
   - `QdrantEmbeddingStore` 的 gRPC 客户端在 Windows 上有 HTTP/2 兼容问题 → **已改用 Qdrant REST API**
   - `TextSegment.from(text, Metadata.from(...))` 而非 `Map.of`
   - `Metadata` 无 `forEach` 方法 → 用 `Metadata.toMap().entrySet()` 遍历
3. **知识源扩容**：往 `knowledge/` 加 md 文件 → `curl POST /api/ai/rag/rebuild` 即可，无需改代码。
4. **DeepSeek 无 embedding 接口**：向量化必须走 Ollama 本地模型，不能复用 DeepSeek key。
