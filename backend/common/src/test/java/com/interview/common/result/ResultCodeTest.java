package com.interview.common.result;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 错误码语义约定测试，保证前端 / 调用方按 code 判断的契约稳定。
 */
class ResultCodeTest {

    @Test
    void 成功错误码为200() {
        assertThat(ResultCode.SUCCESS.getCode()).isEqualTo(200);
    }

    @Test
    void 客户端类错误码为4xx() {
        assertThat(ResultCode.VALIDATE_FAILED.getCode()).isEqualTo(400);
        assertThat(ResultCode.UNAUTHORIZED.getCode()).isEqualTo(401);
        assertThat(ResultCode.FORBIDDEN.getCode()).isEqualTo(403);
        assertThat(ResultCode.NOT_FOUND.getCode()).isEqualTo(404);
    }

    @Test
    void 服务端类错误码为5xx() {
        assertThat(ResultCode.FAILED.getCode()).isEqualTo(500);
        assertThat(ResultCode.INTERNAL_SERVER_ERROR.getCode()).isEqualTo(500);
    }
}
