# AI智能模拟面试评测平台 API接口设计文档

## 1. 接口规范

基础路径：`/api`

通信方式：
- RESTful API
- WebSocket实时通信

认证方式：
```
Authorization: Bearer {token}
```

---

# 2. 用户认证模块

## 用户登录

POST `/api/auth/login`

请求：
```json
{
 "username":"admin",
 "password":"123456"
}
```

响应：
```json
{
 "code":200,
 "data":{
  "token":"xxxxx",
  "userId":1
 }
}
```

## 用户注册

POST `/api/auth/register`

---

# 3. 面试模块

## 创建面试任务

POST `/api/interview/create`

请求：
```json
{
 "jobName":"Java开发工程师",
 "mode":"VIDEO",
 "difficulty":"MEDIUM"
}
```

## 开始面试

POST `/api/interview/start/{taskId}`

## 提交回答

POST `/api/interview/answer`

请求：
```json
{
 "taskId":10001,
 "questionId":1,
 "answer":"SpringBoot自动配置原理..."
}
```

## 结束面试

POST `/api/interview/end/{taskId}`

---

# 4. WebSocket实时通信

连接：
```
ws://host/ws/interview/{taskId}
```

消息类型：

AI提问：
```json
{
 "type":"QUESTION",
 "content":"介绍Redis"
}
```

用户回答：
```json
{
 "type":"ANSWER",
 "content":"Redis是一种缓存数据库"
}
```

---

# 5. 语音模块

上传语音：

POST `/api/speech/upload`

参数：
- taskId
- audio file

返回ASR识别文本。

---

# 6. 视频模块

创建视频房间：

POST `/api/video/createRoom`

返回：
```json
{
 "roomId":"room001",
 "token":"xxxx"
}
```

录制：

POST `/api/video/startRecord`

POST `/api/video/endRecord`

---

# 7. 题库模块

获取题目：

GET `/api/question/list`

新增题目：

POST `/api/question/add`

---

# 8. AI评测模块

生成评价：

POST `/api/evaluation/generate/{taskId}`

返回：
```json
{
 "score":88,
 "summary":"技术基础扎实"
}
```

---

# 9. 报告模块

获取报告：

GET `/api/report/{taskId}`

导出报告：

POST `/api/report/export`

---

# 10. 管理员模块

查询面试记录：

GET `/api/admin/interview/list`

审核结果：

POST `/api/admin/interview/audit`

---

# 11. 错误码

|Code|说明|
|-|-|
|200|成功|
|400|参数错误|
|401|未认证|
|403|无权限|
|404|不存在|
|500|服务器异常|

---

# 12. 设计原则

- RESTful接口设计
- JWT认证
- WebSocket实时通信
- AI任务异步处理
- 文件服务独立存储
