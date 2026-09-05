package com.interview.gateway.filter;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;

import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * 网关鉴权过滤器单元测试。
 * <p>
 * 聚焦两块可纯单测的逻辑：白名单判定、token 解析（含 WebSocket 握手用 query 参数 token）。
 * 反应式过滤链（Mono 插桩）不在此覆盖。
 */
class AuthFilterTest {

    private final AuthFilter filter = new AuthFilter();

    @Test
    void isWhiteListed_放行登录注册与文档_但不再放行WebSocket() {
        assertThat(filter.isWhiteListed("/api/auth/login")).isTrue();
        assertThat(filter.isWhiteListed("/api/auth/register")).isTrue();
        assertThat(filter.isWhiteListed("/swagger-ui/index.html")).isTrue();
        // 关键：WebSocket 握手必须鉴权，不能在白名单里裸奔
        assertThat(filter.isWhiteListed("/ws/interview/1")).isFalse();
        assertThat(filter.isWhiteListed("/api/interview/list")).isFalse();
    }

    @Test
    void resolveToken_优先从Authorization头解析Bearer令牌() {
        ServerHttpRequest request = mock(ServerHttpRequest.class);
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.AUTHORIZATION, "Bearer header-token");
        when(request.getHeaders()).thenReturn(headers);

        assertThat(filter.resolveToken(request)).isEqualTo("header-token");
    }

    @Test
    void resolveToken_无请求头时从query参数token解析_供WebSocket握手使用() {
        ServerHttpRequest request = mock(ServerHttpRequest.class);
        when(request.getHeaders()).thenReturn(new HttpHeaders());
        when(request.getURI()).thenReturn(URI.create("ws://host/ws/interview/1?userId=3&token=ws-token"));

        assertThat(filter.resolveToken(request)).isEqualTo("ws-token");
    }
}
