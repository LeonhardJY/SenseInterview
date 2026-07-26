SET NAMES utf8mb4;
SET CHARACTER SET utf8mb4;

-- 清空并重建题库数据
TRUNCATE TABLE question_bank;
INSERT INTO question_bank (title, category, level, answer) VALUES
('请介绍一下Java中的多态性是什么？', 'Java', 'EASY', '多态是面向对象编程的核心概念，允许不同类的对象对同一消息做出响应。'),
('HashMap和ConcurrentHashMap的区别？', 'Java', 'MEDIUM', 'HashMap非线程安全，ConcurrentHashMap通过CAS+synchronized实现线程安全。'),
('请解释JVM的内存模型和垃圾回收机制。', 'Java', 'HARD', 'JVM内存分为堆、栈、方法区、程序计数器。'),
('Spring Boot自动配置原理是什么？', 'Java', 'MEDIUM', '通过@EnableAutoConfiguration利用spring.factories加载配置类。'),
('Vue3 Composition API有什么优势？', '前端', 'MEDIUM', '更灵活的代码组织，支持逻辑复用。'),
('请解释JavaScript事件循环机制。', '前端', 'HARD', '同步代码在主线程执行，异步任务分宏任务和微任务。'),
('Redis常用数据结构有哪些？', 'Java', 'EASY', 'String、List、Hash、Set、Sorted Set。'),
('什么是数据库索引？如何优化？', '数据库', 'MEDIUM', '索引是用于快速查找的数据结构。'),
('Docker和Kubernetes的区别？', 'Java', 'MEDIUM', 'Docker是容器化平台，K8s是容器编排系统。'),
('如何设计一个短链接系统？', '算法', 'HARD', '发号器+Base62编码，Redis缓存热点数据。'),
('Python中列表和元组的区别？', 'Python', 'EASY', '列表可变用[]，元组不可变用()。'),
('如何进行接口自动化测试？', '测试', 'MEDIUM', '使用Postman/JMeter，集成CI/CD流程。'),
('分布式系统中的一致性问题？', '算法', 'HARD', 'CAP定理，Paxos/Raft等一致性协议。'),
('产品经理如何进行需求分析？', '产品', 'MEDIUM', '用户调研、竞品分析、KANO模型。'),
('请介绍一下微服务架构。', 'Java', 'MEDIUM', '将单体拆分为多个小型服务，独立部署扩展。');

-- 清空并重建面试任务数据
TRUNCATE TABLE interview_task;
INSERT INTO interview_task (user_id, job_name, mode, difficulty, status, start_time, end_time) VALUES
(1, 'Java高级开发工程师', 'TEXT', 'HARD', 'FINISHED', '2026-07-24 14:00:00', '2026-07-24 14:30:00'),
(1, '前端中级开发工程师', 'TEXT', 'MEDIUM', 'FINISHED', '2026-07-24 15:00:00', '2026-07-24 15:25:00'),
(1, 'Python开发工程师', 'TEXT', 'MEDIUM', 'CREATED', NULL, NULL);

-- 清空并重建评价报告
TRUNCATE TABLE evaluation_report;
INSERT INTO evaluation_report (task_id, total_score, professional_score, communication_score, logic_score, summary, suggestion) VALUES
(1, 82.5, 85, 80, 83, '技术基础扎实，表达清晰，逻辑思维良好。', '建议加强对分布式系统和微服务架构的理解。'),
(2, 75.0, 70, 80, 75, '前端基础扎实，对框架理解较深。', '建议加强对浏览器底层原理的理解。');
