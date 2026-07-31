package com.interview.common.exception;

import com.interview.common.result.ResultCode;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 业务异常构造契约测试。
 */
class BusinessExceptionTest {

    @Test
    void 字符串构造默认错误码500() {
        BusinessException e = new BusinessException("面试任务不存在");

        assertThat(e.getCode()).isEqualTo(500);
        assertThat(e.getMessage()).isEqualTo("面试任务不存在");
    }

    @Test
    void 显式错误码构造() {
        BusinessException e = new BusinessException(400, "参数错误");

        assertThat(e.getCode()).isEqualTo(400);
        assertThat(e.getMessage()).isEqualTo("参数错误");
    }

    @Test
    void 枚举构造_透传枚举错误码与文案() {
        BusinessException e = new BusinessException(ResultCode.UNAUTHORIZED);

        assertThat(e.getCode()).isEqualTo(401);
        assertThat(e.getMessage()).isEqualTo(ResultCode.UNAUTHORIZED.getMessage());
    }
}
