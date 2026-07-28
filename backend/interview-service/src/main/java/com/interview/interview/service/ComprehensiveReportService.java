package com.interview.interview.service;

import com.interview.interview.entity.AiAnalysisRecord;
import com.interview.interview.entity.EvaluationReport;
import com.interview.interview.entity.InterviewRecord;
import com.interview.interview.entity.InterviewTask;
import com.interview.interview.model.ComprehensiveReportVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 综合报告服务
 * <p>
 * 聚合评分、情绪分析、面试记录，生成多维度报告。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ComprehensiveReportService {

    private final InterviewTaskService interviewTaskService;
    private final EvaluationReportService evaluationReportService;
    private final AiAnalysisRecordService aiAnalysisRecordService;
    private final InterviewRecordService interviewRecordService;

    /** 情绪中文映射 */
    private static final Map<String, String> EMOTION_LABELS = Map.of(
            "happy", "自信", "neutral", "平静", "surprise", "惊讶",
            "sad", "低落", "angry", "紧张", "fear", "紧张", "disgust", "不适"
    );

    /** 情绪分数映射（给面试表现打分） */
    private static final Map<String, Integer> EMOTION_SCORES = Map.of(
            "happy", 90, "neutral", 75, "surprise", 70,
            "sad", 50, "angry", 40, "fear", 35, "disgust", 45
    );

    /**
     * 获取综合报告
     */
    public ComprehensiveReportVO getComprehensiveReport(Long taskId) {
        ComprehensiveReportVO vo = new ComprehensiveReportVO();

        // 1. 基础信息
        InterviewTask task = interviewTaskService.getById(taskId);
        if (task != null) {
            vo.setTaskId(task.getId());
            vo.setJobName(task.getJobName());
            vo.setMode(task.getMode());
            vo.setDifficulty(task.getDifficulty());
            if (task.getStartTime() != null && task.getEndTime() != null) {
                Duration d = Duration.between(task.getStartTime(), task.getEndTime());
                vo.setDuration(formatDuration(d));
            }
        }

        // 2. 评分
        EvaluationReport report = evaluationReportService.findByTaskId(taskId);
        if (report != null) {
            vo.setTotalScore(report.getTotalScore());
            vo.setProfessionalScore(report.getProfessionalScore());
            vo.setCommunicationScore(report.getCommunicationScore());
            vo.setLogicScore(report.getLogicScore());
            vo.setSummary(report.getSummary());
            vo.setSuggestion(report.getSuggestion());
        }

        // 3. 情绪分析
        List<AiAnalysisRecord> emotionRecords = aiAnalysisRecordService.findByTaskId(taskId).stream()
                .filter(r -> "EMOTION".equals(r.getAnalysisType()))
                .collect(Collectors.toList());

        ComprehensiveReportVO.EmotionSummary emotionSummary = buildEmotionSummary(emotionRecords);
        vo.setEmotionSummary(emotionSummary);

        if (emotionSummary != null) {
            // 情绪分数 = 主要情绪对应的分数
            Integer eScore = EMOTION_SCORES.get(emotionSummary.getDominantEmotion());
            vo.setEmotionScore(eScore != null ? eScore : 70);
            // 置信度分数：情绪变化越少越自信
            int changes = emotionSummary.getEmotionChanges();
            vo.setConfidenceScore(changes <= 2 ? 85 : changes <= 5 ? 70 : 55);
        }

        // 4. 问答记录
        List<ComprehensiveReportVO.QaRecord> qaRecords = buildQaRecords(taskId);
        vo.setRecords(qaRecords);
        vo.setTotalRounds(qaRecords.size());

        return vo;
    }

    /**
     * 构建情绪摘要
     */
    private ComprehensiveReportVO.EmotionSummary buildEmotionSummary(List<AiAnalysisRecord> records) {
        if (records == null || records.isEmpty()) return null;

        ComprehensiveReportVO.EmotionSummary summary = new ComprehensiveReportVO.EmotionSummary();
        summary.setTotalFrames(records.size());

        // 统计各情绪出现次数
        Map<String, Integer> emotionCount = new LinkedHashMap<>();
        String prevEmotion = null;
        int changes = 0;

        for (AiAnalysisRecord record : records) {
            String emotion = extractEmotion(record.getResultJson());
            if (emotion == null) continue;

            emotionCount.merge(emotion, 1, Integer::sum);
            if (prevEmotion != null && !prevEmotion.equals(emotion)) {
                changes++;
            }
            prevEmotion = emotion;
        }

        if (emotionCount.isEmpty()) return null;

        summary.setEmotionChanges(changes);

        // 主要情绪（出现次数最多的）
        String dominant = emotionCount.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("neutral");
        summary.setDominantEmotion(dominant);
        summary.setEmotionLabel(EMOTION_LABELS.getOrDefault(dominant, "未知"));

        // 情绪分布
        int total = emotionCount.values().stream().mapToInt(Integer::intValue).sum();
        List<ComprehensiveReportVO.EmotionDistribution> distribution = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : emotionCount.entrySet()) {
            ComprehensiveReportVO.EmotionDistribution d = new ComprehensiveReportVO.EmotionDistribution();
            d.setEmotion(entry.getKey());
            d.setLabel(EMOTION_LABELS.getOrDefault(entry.getKey(), entry.getKey()));
            d.setCount(entry.getValue());
            d.setPercentage(Math.round(entry.getValue() * 1000.0 / total) / 10.0);
            distribution.add(d);
        }
        distribution.sort((a, b) -> Integer.compare(b.getCount(), a.getCount()));
        summary.setDistribution(distribution);

        return summary;
    }

    /**
     * 从 resultJson 中提取情绪字段
     */
    private String extractEmotion(String resultJson) {
        if (resultJson == null || resultJson.isEmpty()) return null;
        try {
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            com.fasterxml.jackson.databind.JsonNode node = mapper.readTree(resultJson);
            return node.has("emotion") ? node.get("emotion").asText() : null;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 构建问答记录
     */
    private List<ComprehensiveReportVO.QaRecord> buildQaRecords(Long taskId) {
        List<InterviewRecord> records = interviewRecordService.findByTaskId(taskId);
        if (records == null || records.isEmpty()) return List.of();

        List<ComprehensiveReportVO.QaRecord> result = new ArrayList<>();
        for (int i = 0; i < records.size(); i++) {
            InterviewRecord r = records.get(i);
            ComprehensiveReportVO.QaRecord qa = new ComprehensiveReportVO.QaRecord();
            qa.setRound(r.getRoundNum() != null ? r.getRoundNum() : i + 1);
            qa.setQuestion(r.getQuestion());
            qa.setAnswer(""); // 可以从 interview_answer 表补充
            result.add(qa);
        }
        return result;
    }

    private String formatDuration(Duration d) {
        long minutes = d.toMinutes();
        long seconds = d.minusMinutes(minutes).getSeconds();
        if (minutes > 0) return minutes + "分" + seconds + "秒";
        return seconds + "秒";
    }
}
