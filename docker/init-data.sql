-- 使用 utf8mb4 编码
SET NAMES utf8mb4;
SET CHARACTER SET utf8mb4;

-- 清空旧数据
TRUNCATE TABLE job_position;
TRUNCATE TABLE hot_question;

-- 插入岗位数据
INSERT INTO job_position (name, description, category, level) VALUES
('Java高级开发工程师', '负责核心业务系统开发', 'Java后端', 'HARD'),
('Java中级开发工程师', '负责业务模块开发', 'Java后端', 'MEDIUM'),
('Java初级开发工程师', '负责基础功能开发', 'Java后端', 'EASY'),
('前端高级开发工程师', '负责前端架构设计', '前端开发', 'HARD'),
('前端中级开发工程师', '负责Web应用开发', '前端开发', 'MEDIUM'),
('前端初级开发工程师', '负责页面开发', '前端开发', 'EASY'),
('Python开发工程师', '负责Python后端开发', 'Python后端', 'MEDIUM'),
('算法工程师', '负责算法设计与优化', '算法', 'HARD'),
('数据分析师', '负责数据分析', '大数据', 'MEDIUM'),
('产品经理', '负责产品规划', '产品经理', 'MEDIUM'),
('测试开发工程师', '负责自动化测试', '测试开发', 'MEDIUM'),
('运维工程师', '负责系统运维', '运维', 'MEDIUM'),
('DBA数据库管理员', '负责数据库管理', '数据库', 'HARD'),
('项目经理', '负责项目管理', '项目管理', 'MEDIUM'),
('UI设计师', '负责界面设计', '设计', 'MEDIUM');

-- 插入热门题库数据
INSERT INTO hot_question (name, count, sort_order) VALUES
('Java核心技术八股文精选300题', '5.2万', 1),
('LeetCode高频算法面试题200题', '4.8万', 2),
('Spring全家桶深度解析', '3.5万', 3),
('前端面试必考知识点250题', '3.2万', 4),
('系统设计面试实战手册', '2.7万', 5);
