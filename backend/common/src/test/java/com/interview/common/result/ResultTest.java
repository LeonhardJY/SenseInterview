package com.interview.common.result;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 统一返回结构 Result 的契约测试：作为接口联调 / 断言的依据。
 */
class ResultTest {

    @Test
    void success_默认返回200与操作成功() {
        Result<Void> r = Result.success();

        assertThat(r.getCode()).isEqualTo(200);
        assertThat(r.getMessage()).isEqualTo("操作成功");
        assertThat(r.getData()).isNull();
    }

    @Test
    void success_携带返回数据() {
        Result<String> r = Result.success("hello");

        assertThat(r.getCode()).isEqualTo(200);
        assertThat(r.getData()).isEqualTo("hello");
    }

    @Test
    void success_自定义提示语() {
        Result<String> r = Result.success("创建成功", "id=1");

        assertThat(r.getMessage()).isEqualTo("创建成功");
    }

    @Test
    void error_字符串形式默认返回500() {
        Result<Void> r = Result.error("系统繁忙");

        assertThat(r.getCode()).isEqualTo(500);
        assertThat(r.getMessage()).isEqualTo("系统繁忙");
    }

    @Test
    void error_支持自定义错误码() {
        Result<Void> r = Result.error(403, "没有相关权限");

        assertThat(r.getCode()).isEqualTo(403);
    }

    @Test
    void error_枚举映射错误码与文案() {
        Result<Void> r = Result.error(ResultCode.VALIDATE_FAILED);

        assertThat(r.getCode()).isEqualTo(400);
        assertThat(r.getMessage()).isEqualTo("参数检验失败");
    }
}
