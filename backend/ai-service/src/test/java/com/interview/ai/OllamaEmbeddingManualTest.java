package com.interview.ai;

import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.model.ollama.OllamaEmbeddingModel;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 手动验证 Ollama embedding（不启动 Spring 上下文，排查 JDK HttpClient 问题）
 */
public class OllamaEmbeddingManualTest {

    @Test
    void embeddingWorks() {
        OllamaEmbeddingModel model = OllamaEmbeddingModel.builder()
                .baseUrl("http://localhost:11434")
                .modelName("nomic-embed-text")
                .build();

        Embedding embedding = model.embed("ConcurrentHashMap 如何保证线程安全").content();
        System.out.println("==== embedding 维度: " + embedding.vector().length + " ====");
        assertTrue(embedding.vector().length > 0, "应返回非空向量");
    }
}
