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
 INDEX idx_user_id(user_id)
);

CREATE TABLE interview_record (
 id BIGINT PRIMARY KEY AUTO_INCREMENT,
 task_id BIGINT NOT NULL,
 round_num INT,
 question TEXT,
 create_time DATETIME DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE interview_answer (
 id BIGINT PRIMARY KEY AUTO_INCREMENT,
 record_id BIGINT NOT NULL,
 answer_text TEXT,
 audio_url VARCHAR(255),
 video_url VARCHAR(255),
 create_time DATETIME DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE ai_analysis_record (
 id BIGINT PRIMARY KEY AUTO_INCREMENT,
 task_id BIGINT,
 analysis_type VARCHAR(50),
 result_json JSON,
 score DECIMAL(5,2),
 create_time DATETIME DEFAULT CURRENT_TIMESTAMP
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
