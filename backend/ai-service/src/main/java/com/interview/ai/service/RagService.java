package com.interview.ai.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import dev.langchain4j.data.document.Metadata;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.ollama.OllamaEmbeddingModel;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * RAG 检索服务：大厂面试知识库（classpath:knowledge/*.md）
 * <p>
 * 使用 Qdrant REST API（避免 gRPC HTTP/2 在 Windows 上的兼容性问题）
 * 流程：知识源 → 按标题切分 → Ollama embedding 向量化 → 写入 Qdrant → 检索 top-k
 */
@Slf4j
@Service
public class RagService {

    @Value("${ai.rag.embedding.base-url}")
    private String embeddingBaseUrl;

    @Value("${ai.rag.embedding.model}")
    private String embeddingModelName;

    @Value("${ai.rag.qdrant.host}")
    private String qdrantHost;

    @Value("${ai.rag.qdrant.port}")
    private int qdrantPort;

    @Value("${ai.rag.qdrant.collection}")
    private String collectionName;

    @Value("${ai.rag.qdrant.dimension}")
    private int dimension;

    @Value("${ai.rag.knowledge-dir}")
    private String knowledgeDir;

    @Value("${ai.rag.top-k}")
    private int topK;

    private EmbeddingModel embeddingModel;
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    /** 本地向量缓存：pointId → {embedding, text, metadata} */
    private final ConcurrentHashMap<String, VectorEntry> vectorStore = new ConcurrentHashMap<>();
    private long pointIdCounter = 0;

    @PostConstruct
    public void init() {
        embeddingModel = OllamaEmbeddingModel.builder()
                .baseUrl(embeddingBaseUrl)
                .modelName(embeddingModelName)
                .build();

        log.info("RAG 组件初始化完成（REST 模式）: embedding={}, qdrant={}:{}, collection={}",
                embeddingModelName, qdrantHost, qdrantPort, collectionName);
    }

    /**
     * 重建索引：删除旧 collection → 创建新 collection → 从知识源向量化写入
     *
     * @return 写入的文档段数量
     */
    public int rebuildIndex() {
        vectorStore.clear();
        pointIdCounter = 0;

        List<TextSegment> segments = loadKnowledgeSegments();
        if (segments.isEmpty()) {
            log.warn("知识源为空，跳过索引重建");
            return 0;
        }

        recreateCollection();

        // 批量向量化
        List<Embedding> embeddings = embeddingModel.embedAll(segments).content();

        // 批量写入 Qdrant REST API
        batchUpsert(segments, embeddings);

        log.info("RAG 索引重建完成: {} 个文档段写入 collection={}", segments.size(), collectionName);
        return segments.size();
    }

    /**
     * 向量化检索 top-k 相关文档，仅返回文本（RAG 测试接口用）
     */
    public List<String> retrieve(String query) {
        return retrieveDocs(query).stream().map(RagDoc::text).toList();
    }

    /**
     * 向量化检索 top-k 相关文档（含来源）
     * 优先从 Qdrant REST API 查询（持久化存储），本地缓存兜底（Qdrant 不可用时）
     */
    public List<RagDoc> retrieveDocs(String query) {
        Embedding queryEmbedding = embeddingModel.embed(query).content();
        float[] queryVector = queryEmbedding.vector();

        // 优先从 Qdrant 检索
        try {
            List<RagDoc> qdrantResults = searchFromQdrant(queryVector, query);
            if (!qdrantResults.isEmpty()) {
                log.info("RAG 检索: query={}, 命中 {} 条（Qdrant）", query, qdrantResults.size());
                return qdrantResults;
            }
        } catch (Exception e) {
            log.warn("Qdrant 检索失败，使用本地缓存: {}", e.getMessage());
        }

        // 兜底：本地缓存余弦相似度（服务重启后缓存为空，需先 rebuildIndex）
        List<VectorEntry> entries = new ArrayList<>(vectorStore.values());
        entries.sort((a, b) -> Double.compare(
                cosineSimilarity(queryVector, b.embedding),
                cosineSimilarity(queryVector, a.embedding)));

        List<RagDoc> results = new ArrayList<>();
        for (int i = 0; i < Math.min(topK, entries.size()); i++) {
            VectorEntry entry = entries.get(i);
            String source = entry.metadata != null && entry.metadata.get("source") != null
                    ? String.valueOf(entry.metadata.get("source")) : null;
            results.add(new RagDoc(source, entry.text));
        }

        log.info("RAG 检索: query={}, 命中 {} 条（本地缓存）", query, results.size());
        return results;
    }

    /**
     * 通过 Qdrant REST API 检索相似向量（含来源），采用向量 + 关键词加权的混合重排。
     * <p>
     * 纯向量检索对中文短语的区分度有限（语料同质化），故先取 Top-N 候选，
     * 再按查询关键词命中数加权，兼顾语义相关与精确命中，提升召回相关度。
     */
    private List<RagDoc> searchFromQdrant(float[] queryVector, String queryText) {
        try {
            ObjectNode body = objectMapper.createObjectNode();
            ArrayNode vectorArray = objectMapper.createArrayNode();
            for (float v : queryVector) vectorArray.add(v);
            body.set("vector", vectorArray);
            // 候选取全量（知识库规模小），让关键词重排在全部文档段上生效，
            // 避免中文向量检索把相关段挤出 Top-K 候选
            body.put("limit", 500);
            body.put("with_payload", true);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<String> request = new HttpEntity<>(objectMapper.writeValueAsString(body), headers);

            ResponseEntity<JsonNode> response = restTemplate.postForEntity(
                    qdrantUrl("/collections/{name}/points/search"), request, JsonNode.class, collectionName);

            // 候选列表：{向量得分, 来源, 文本}
            List<Object[]> candidates = new ArrayList<>();
            if (response.getBody() != null && response.getBody().has("result")) {
                for (JsonNode hit : response.getBody().get("result")) {
                    JsonNode payload = hit.get("payload");
                    if (payload == null || !payload.has("text")) continue;
                    double vectorScore = hit.has("score") ? hit.get("score").asDouble() : 0;
                    String source = payload.has("source") ? payload.get("source").asText() : null;
                    candidates.add(new Object[]{vectorScore, source, payload.get("text").asText()});
                }
            }

            // 混合重排：最终得分 = 向量相似度 + 关键词命中加权
            List<String> keywords = extractKeywords(queryText);
            candidates.sort((a, b) -> Double.compare(
                    hybridScore((double) b[0], (String) b[2], keywords),
                    hybridScore((double) a[0], (String) a[2], keywords)));

            List<RagDoc> results = new ArrayList<>();
            for (int i = 0; i < Math.min(topK, candidates.size()); i++) {
                Object[] c = candidates.get(i);
                results.add(new RagDoc((String) c[1], (String) c[2]));
            }
            return results;
        } catch (Exception e) {
            log.error("Qdrant REST 检索失败: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    /**
     * 从查询文本中提取关键词：
     * - 英文：按非字母分隔，保留 >=2 字母的词
     * - 中文：对连续中文串切 bi-gram / tri-gram 滑动窗口（如「秒杀系统」→ 秒杀/系统/秒杀系/杀系统），
     *   使关键词能命中知识库中的短语，避免整句作为一个词无法匹配
     */
    private List<String> extractKeywords(String queryText) {
        if (queryText == null || queryText.isBlank()) return Collections.emptyList();
        List<String> keywords = new ArrayList<>();

        // 英文词
        for (String w : queryText.split("[^A-Za-z]+")) {
            if (w.length() >= 2) keywords.add(w);
        }

        // 连续中文串的 bi-gram / tri-gram
        for (String zh : queryText.split("[^\\u4e00-\\u9fa5]+")) {
            if (zh.length() < 2) continue;
            for (int i = 0; i < zh.length() - 1; i++) {
                String bg = zh.substring(i, i + 2);
                if (isMeaningfulGram(bg)) keywords.add(bg);
                if (i + 2 < zh.length()) {
                    String tg = zh.substring(i, i + 3);
                    if (isMeaningfulGram(tg)) keywords.add(tg);
                }
            }
        }
        return keywords;
    }

    /** 过滤掉无语义的虚词组合 */
    private boolean isMeaningfulGram(String gram) {
        if (gram == null || gram.length() < 2) return false;
        String[] stop = {"如何", "怎么", "什么", "为什么", "一个", "一下", "这个", "以及", "那么", "同时", "还有"};
        for (String s : stop) {
            if (gram.contains(s)) return false;
        }
        return true;
    }

    /**
     * 混合评分：向量得分 + 关键词命中数加权（命中越多权重越高，但不超过向量得分的主导）
     */
    private double hybridScore(double vectorScore, String text, List<String> keywords) {
        if (text == null || keywords.isEmpty()) return vectorScore;
        int hits = 0;
        for (String kw : keywords) {
            if (text.contains(kw)) hits++;
        }
        return vectorScore + hits * 0.15;
    }

    // ========== Qdrant REST 操作 ==========

    private void recreateCollection() {
        try {
            // 删除旧 collection（忽略 404）
            try {
                restTemplate.delete(qdrantUrl("/collections/{name}"), collectionName);
                log.info("Qdrant collection 已删除: {}", collectionName);
            } catch (Exception ignored) {}

            // 创建新 collection
            ObjectNode body = objectMapper.createObjectNode();
            ObjectNode vectors = objectMapper.createObjectNode();
            vectors.put("size", dimension);
            vectors.put("distance", "Cosine");
            body.set("vectors", vectors);

            restTemplate.put(qdrantUrl("/collections/{name}"), body, collectionName);
            log.info("Qdrant collection 已创建: {} (dim={}, Cosine)", collectionName, dimension);
        } catch (Exception e) {
            log.error("Qdrant collection 操作失败: {}", e.getMessage());
        }
    }

    private void batchUpsert(List<TextSegment> segments, List<Embedding> embeddings) {
        try {
            ArrayNode pointsArray = objectMapper.createArrayNode();

            for (int i = 0; i < segments.size(); i++) {
                long pointId = ++pointIdCounter;
                TextSegment segment = segments.get(i);
                float[] vector = embeddings.get(i).vector();

                ObjectNode point = objectMapper.createObjectNode();
                point.put("id", pointId);

                // 向量
                ArrayNode vectorArray = objectMapper.createArrayNode();
                for (float v : vector) vectorArray.add(v);
                point.set("vector", vectorArray);

                // Payload
                ObjectNode payload = objectMapper.createObjectNode();
                payload.put("text", segment.text());
                if (segment.metadata() != null && segment.metadata().toMap() != null) {
                    for (Map.Entry<String, Object> entry : segment.metadata().toMap().entrySet()) {
                        payload.put(entry.getKey(), String.valueOf(entry.getValue()));
                    }
                }
                point.set("payload", payload);

                pointsArray.add(point);

                // 同时写入本地缓存
                vectorStore.put(String.valueOf(pointId), new VectorEntry(vector, segment.text(), segment.metadata()));
            }

            ObjectNode body = objectMapper.createObjectNode();
            body.set("points", pointsArray);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<String> request = new HttpEntity<>(objectMapper.writeValueAsString(body), headers);

            restTemplate.put(qdrantUrl("/collections/{name}/points"), request, collectionName);
            log.info("Qdrant 批量写入完成: {} 条", pointsArray.size());
        } catch (Exception e) {
            log.error("Qdrant 批量写入失败: {}", e.getMessage());
        }
    }

    private String qdrantUrl(String path) {
        return "http://" + qdrantHost + ":" + qdrantPort + path;
    }

    // ========== 知识源加载 ==========

    private List<TextSegment> loadKnowledgeSegments() {
        List<TextSegment> segments = new ArrayList<>();
        try {
            Resource[] resources = new PathMatchingResourcePatternResolver()
                    .getResources(knowledgeDir + "/*.md");
            for (Resource resource : resources) {
                String content = new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
                String source = resource.getFilename();
                for (String chunk : splitByHeadings(content)) {
                    if (!chunk.isBlank()) {
                        segments.add(TextSegment.from(chunk, Metadata.from("source", String.valueOf(source))));
                    }
                }
            }
        } catch (IOException e) {
            log.error("加载知识源失败: {}", e.getMessage());
        }
        return segments;
    }

    /**
     * 按二/三/四级标题（## / ### / ####）切分 markdown。
     * 细化切分粒度并合并父级标题上下文，同时丢弃仅含标题的空白段，提升向量检索相关度。
     */
    private List<String> splitByHeadings(String content) {
        List<String> chunks = new ArrayList<>();
        String[] lines = content.split("\n");
        StringBuilder current = new StringBuilder();
        boolean hasContent = false;
        for (String line : lines) {
            if (line.matches("^#{2,4} .*")) {
                // 仅当累积到实际内容时才落段，避免产出纯标题空段
                if (hasContent) {
                    chunks.add(current.toString().trim());
                }
                current = new StringBuilder();
                hasContent = false;
            }
            current.append(line).append("\n");
            if (!line.matches("^#{1,4} .*") && !line.isBlank()) {
                hasContent = true;
            }
        }
        if (hasContent) {
            chunks.add(current.toString().trim());
        }
        return chunks;
    }

    // ========== 工具方法 ==========

    private static double cosineSimilarity(float[] a, float[] b) {
        if (a.length != b.length) return 0;
        double dot = 0, normA = 0, normB = 0;
        for (int i = 0; i < a.length; i++) {
            dot += a[i] * b[i];
            normA += a[i] * a[i];
            normB += b[i] * b[i];
        }
        return normA == 0 || normB == 0 ? 0 : dot / (Math.sqrt(normA) * Math.sqrt(normB));
    }

    /** 向量条目 */
    private static class VectorEntry {
        final float[] embedding;
        final String text;
        final Metadata metadata;

        VectorEntry(float[] embedding, String text, Metadata metadata) {
            this.embedding = embedding;
            this.text = text;
            this.metadata = metadata;
        }
    }

    /** 检索结果：来源 + 文本 */
    public record RagDoc(String source, String text) {
        /** 将来源文件名映射为中文公司名 */
        public String sourceName() {
            if (source == null) return "";
            return switch (source) {
                case "alibaba.md" -> "阿里巴巴";
                case "tencent.md" -> "腾讯";
                case "bytedance.md" -> "字节跳动";
                case "meituan.md" -> "美团";
                case "huawei.md" -> "华为";
                case "iflytek.md" -> "科大讯飞";
                default -> source.replace(".md", "");
            };
        }
    }
}
