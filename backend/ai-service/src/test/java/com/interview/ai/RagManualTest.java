package com.interview.ai;

import com.interview.ai.service.RagService;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * RAG 全链路手动测试：注入字段 + 初始化组件 + 重建索引 + 检索
 * 依赖本地服务：Ollama(nomic-embed-text) + Qdrant(:6333)
 */
public class RagManualTest {

    @Test
    void rebuildIndexAndRetrieve() throws Exception {
        RagService ragService = new RagService();
        ReflectionTestUtils.setField(ragService, "embeddingBaseUrl", "http://localhost:11434");
        ReflectionTestUtils.setField(ragService, "embeddingModelName", "nomic-embed-text");
        ReflectionTestUtils.setField(ragService, "qdrantHost", "localhost");
        ReflectionTestUtils.setField(ragService, "qdrantPort", 6333);
        ReflectionTestUtils.setField(ragService, "collectionName", "interview_knowledge");
        ReflectionTestUtils.setField(ragService, "dimension", 768);
        ReflectionTestUtils.setField(ragService, "knowledgeDir", "classpath:knowledge");
        ReflectionTestUtils.setField(ragService, "topK", 4);

        ragService.init();

        int count = ragService.rebuildIndex();
        System.out.println("==== RAG 索引写入段数: " + count + " ====");
        assertTrue(count > 0, "知识库索引应写入文档段");

        List<String> docs = ragService.retrieve("ConcurrentHashMap 如何保证线程安全");
        System.out.println("==== 检索命中: " + docs.size() + " 条 ====");
        docs.forEach(d -> System.out.println("---\n" + d));
        assertFalse(docs.isEmpty(), "检索应命中知识库文档");
    }
}
