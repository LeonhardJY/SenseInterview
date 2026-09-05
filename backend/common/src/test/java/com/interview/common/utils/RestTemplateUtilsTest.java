package com.interview.common.utils;

import org.junit.jupiter.api.Test;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * RestTemplate 工厂测试：验证超时被正确设置。
 * <p>
 * 背景：项目中调用 LLM / Qdrant / Ollama / question-service 的 RestTemplate 若无超时，
 * 下游卡死会导致调用线程无限阻塞、线程堆积——这是 AI 应用（LLM 调用天然长耗时）的稳定性底线。
 */
class RestTemplateUtilsTest {

    @Test
    void build_为RestTemplate设置连接与读取超时_避免下游卡死时无限阻塞() {
        RestTemplate rt = RestTemplateUtils.build(5000, 60000);

        assertThat(rt.getRequestFactory()).isInstanceOf(SimpleClientHttpRequestFactory.class);
        SimpleClientHttpRequestFactory factory = (SimpleClientHttpRequestFactory) rt.getRequestFactory();
        assertThat(ReflectionTestUtils.getField(factory, "connectTimeout")).isEqualTo(5000);
        assertThat(ReflectionTestUtils.getField(factory, "readTimeout")).isEqualTo(60000);
    }
}
