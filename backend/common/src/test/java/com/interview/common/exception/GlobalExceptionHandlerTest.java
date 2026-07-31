package com.interview.common.exception;

import com.interview.common.result.Result;
import com.interview.common.result.ResultCode;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.lang.reflect.Method;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 全局异常处理器测试：验证业务异常、参数校验异常、未知异常到统一返回结构的映射。
 */
class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void 业务异常转为对应错误码() {
        BusinessException e = new BusinessException(ResultCode.NOT_FOUND, "面试任务不存在");

        Result<Void> r = handler.handleBusinessException(e);

        assertThat(r.getCode()).isEqualTo(404);
        assertThat(r.getMessage()).isEqualTo("面试任务不存在");
    }

    @Test
    void 参数校验异常拼接全部字段错误信息() throws Exception {
        Method method = getClass().getDeclaredMethod("dummy", String.class);
        MethodParameter parameter = new MethodParameter(method, 0);
        BeanPropertyBindingResult binding = new BeanPropertyBindingResult(new Object(), "form");
        binding.addError(new FieldError("form", "name", "名称不能为空"));
        binding.addError(new FieldError("form", "age", "年龄必须大于0"));

        Result<Void> r = handler.handleMethodArgumentNotValidException(
                new MethodArgumentNotValidException(parameter, binding));

        assertThat(r.getCode()).isEqualTo(ResultCode.VALIDATE_FAILED.getCode());
        assertThat(r.getMessage()).contains("名称不能为空", "年龄必须大于0");
    }

    @Test
    void 未知异常统一降级为500() {
        Result<Void> r = handler.handleException(new IllegalStateException("boom"));

        assertThat(r.getCode()).isEqualTo(500);
        assertThat(r.getMessage()).isEqualTo(ResultCode.INTERNAL_SERVER_ERROR.getMessage());
    }

    private void dummy(String name) {
    }
}
