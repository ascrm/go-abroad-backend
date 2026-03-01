package com.goAbroad.common.result;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

/**
 * 分页返回结果
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PageR<T> {

    private Integer code;           // 状态码
    private String message;         // 提示信息
    private Long total;             // 总记录数
    private Integer pageNum;        // 当前页码
    private Integer pageSize;       // 每页大小
    private List<T> rows;           // 数据列表

    /**
     * 成功返回分页数据
     */
    public static <T> PageR<T> ok(Long total, List<T> rows) {
        return new PageR<>(200, "success", total, 1, rows.size(), rows);
    }

    public static <T> PageR<T> ok(Long total, List<T> rows, Integer pageNum, Integer pageSize) {
        return new PageR<>(200, "success", total, pageNum, pageSize, rows);
    }

    /**
     * 失败返回
     */
    public static <T> PageR<T> fail() {
        return new PageR<>(500, "error", 0L, 1, 0, null);
    }

    public static <T> PageR<T> fail(String message) {
        return new PageR<>(500, message, 0L, 1, 0, null);
    }
}
