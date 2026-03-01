package com.goAbroad.common.result;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 统一返回结果
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class R<T> {

    private Integer code;       // 业务状态码
    private String message;     // 提示信息
    private T data;             // 数据

    /**
     * 业务状态码常量
     */
    public static final class Code {
        public static final int SUCCESS = 20000;    // 成功
        public static final int FAIL = 50000;        // 失败
    }

    /**
     * 成功返回
     */
    public static <T> R<T> ok() {
        return new R<>(Code.SUCCESS, "success", null);
    }

    public static <T> R<T> ok(T data) {
        return new R<>(Code.SUCCESS, "success", data);
    }

    public static <T> R<T> ok(String message, T data) {
        return new R<>(Code.SUCCESS, message, data);
    }

    /**
     * 失败返回
     */
    public static <T> R<T> fail() {
        return new R<>(Code.FAIL, "error", null);
    }

    public static <T> R<T> fail(String message) {
        return new R<>(Code.FAIL, message, null);
    }

    public static <T> R<T> fail(Integer code, String message) {
        return new R<>(code, message, null);
    }
}
