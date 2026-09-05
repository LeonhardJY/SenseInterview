CREATE DATABASE IF NOT EXISTS ai_interview DEFAULT CHARACTER SET utf8mb4;

USE ai_interview;

CREATE TABLE sys_user (
 id BIGINT PRIMARY KEY AUTO_INCREMENT,
 username VARCHAR(50) NOT NULL,
 password VARCHAR(255) NOT NULL,
 email VARCHAR(100),
 role VARCHAR(20) DEFAULT 'USER',
 status TINYINT DEFAULT 1,
 create_time DATETIME DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE question_bank (
 id BIGINT PRIMARY KEY AUTO_INCREMENT,
 title TEXT NOT NULL,
 category VARCHAR(50),
 level VARCHAR(20),
 answer TEXT COMMENT '参考答案',
 deleted TINYINT DEFAULT 0 COMMENT '逻辑删除',
 create_time DATETIME DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE interview_task (
 id BIGINT PRIMARY KEY AUTO_INCREMENT,
 user_id BIGINT NOT NULL,
 job_name VARCHAR(100),
 mode VARCHAR(20),
 difficulty VARCHAR(20),
 status VARCHAR(20),
 start_time DATETIME,
 end_time DATETIME,
 create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
 INDEX idx_user_id(user_id),
 INDEX idx_create_time(create_time),
 INDEX idx_job_name(job_name),
 INDEX idx_status(status)
);

CREATE TABLE interview_record (
 id BIGINT PRIMARY KEY AUTO_INCREMENT,
 task_id BIGINT NOT NULL,
 round_num INT,
 question TEXT,
 create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
 INDEX idx_task_id(task_id)
);

CREATE TABLE interview_answer (
 id BIGINT PRIMARY KEY AUTO_INCREMENT,
 record_id BIGINT NOT NULL,
 answer_text TEXT,
 audio_url VARCHAR(255),
 video_url VARCHAR(255),
 create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
 INDEX idx_record_id(record_id)
);

CREATE TABLE ai_analysis_record (
 id BIGINT PRIMARY KEY AUTO_INCREMENT,
 task_id BIGINT,
 analysis_type VARCHAR(50),
 result_json JSON,
 score DECIMAL(5,2),
 create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
 INDEX idx_task_type(task_id, analysis_type)
);

CREATE TABLE evaluation_report (
 id BIGINT PRIMARY KEY AUTO_INCREMENT,
 task_id BIGINT UNIQUE,
 total_score DECIMAL(5,2),
 professional_score INT,
 communication_score INT,
 logic_score INT,
 summary TEXT,
 suggestion TEXT,
 create_time DATETIME DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE video_record (
 id BIGINT PRIMARY KEY AUTO_INCREMENT,
 task_id BIGINT,
 video_url VARCHAR(255),
 create_time DATETIME DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE speech_record (
 id BIGINT PRIMARY KEY AUTO_INCREMENT,
 task_id BIGINT,
 audio_url VARCHAR(255),
 text_result TEXT,
 create_time DATETIME DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE job_position (
 id BIGINT PRIMARY KEY AUTO_INCREMENT,
 name VARCHAR(100) NOT NULL COMMENT '职位名称',
 description TEXT COMMENT '职位描述',
 category VARCHAR(50) COMMENT '职位类别',
 level VARCHAR(20) COMMENT '职位级别',
 deleted TINYINT DEFAULT 0 COMMENT '逻辑删除 0-未删除 1-已删除',
 create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间'
) COMMENT='职位表';

-- 职位初始数据
INSERT INTO job_position (name, description, category, level) VALUES
-- 后端开发
('Java开发工程师', '负责Java后端系统的设计与开发', '后端开发', '初级'),
('Java高级开发工程师', '负责核心系统架构设计与开发', '后端开发', '高级'),
('Python开发工程师', '负责Python后端服务开发', '后端开发', '初级'),
('Go开发工程师', '负责Go语言微服务开发', '后端开发', '中级'),

-- 前端开发
('前端开发工程师', '负责Web前端页面开发', '前端开发', '初级'),
('Vue开发工程师', '负责Vue.js项目开发', '前端开发', '中级'),
('React开发工程师', '负责React项目开发', '前端开发', '中级'),

-- 移动开发
('Android开发工程师', '负责Android应用开发', '移动开发', '初级'),
('iOS开发工程师', '负责iOS应用开发', '移动开发', '初级'),

-- 数据相关
('数据分析师', '负责数据分析与报表', '数据', '初级'),
('数据工程师', '负责数据平台建设', '数据', '中级'),
('算法工程师', '负责机器学习算法开发', '数据', '高级'),

-- 测试
('测试工程师', '负责软件测试工作', '测试', '初级'),
('自动化测试工程师', '负责自动化测试框架搭建', '测试', '中级'),

-- 运维/DevOps
('运维工程师', '负责系统运维工作', '运维', '初级'),
('DevOps工程师', '负责CI/CD流程建设', '运维', '中级'),

-- 产品/设计
('产品经理', '负责产品规划与设计', '产品', '中级'),
('UI设计师', '负责用户界面设计', '设计', '初级');

CREATE TABLE resume (
 id BIGINT PRIMARY KEY AUTO_INCREMENT,
 user_id BIGINT NOT NULL COMMENT '用户ID',
 title VARCHAR(100) COMMENT '简历标题',
 name VARCHAR(50) COMMENT '姓名',
 phone VARCHAR(20) COMMENT '电话',
 email VARCHAR(100) COMMENT '邮箱',
 education VARCHAR(20) COMMENT '学历',
 school VARCHAR(100) COMMENT '学校',
 major VARCHAR(100) COMMENT '专业',
 work_years INT COMMENT '工作年限',
 job_position VARCHAR(100) COMMENT '期望职位',
 skills TEXT COMMENT '技能',
 experience TEXT COMMENT '工作经历',
 self_introduction TEXT COMMENT '自我介绍',
 file_url VARCHAR(255) COMMENT '文件地址',
 status TINYINT DEFAULT 1 COMMENT '状态 1-正常 0-禁用',
 deleted TINYINT DEFAULT 0 COMMENT '逻辑删除',
 create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
 update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
 INDEX idx_user_id(user_id)
) COMMENT='简历表';

CREATE TABLE hot_question (
 id BIGINT PRIMARY KEY AUTO_INCREMENT,
 name VARCHAR(200) NOT NULL COMMENT '题目名称',
 count VARCHAR(50) COMMENT '热度 count',
 sort_order INT DEFAULT 0 COMMENT '排序',
 deleted TINYINT DEFAULT 0 COMMENT '逻辑删除',
 create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间'
) COMMENT='热门题目表';

INSERT INTO hot_question (name, count, sort_order) VALUES
('自我介绍', '12800', 1),
('你的优缺点是什么', '9600', 2),
('为什么选择我们公司', '8200', 3),
('你的职业规划是什么', '7500', 4),
('描述一个你的项目经历', '6800', 5);
