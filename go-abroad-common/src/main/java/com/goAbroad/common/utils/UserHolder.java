package com.goAbroad.common.utils;

/**
 * 用户上下文 holder，用于在当前线程中存储和获取用户信息
 */
public class UserHolder {

    private static final ThreadLocal<Long> USER_ID_HOLDER = new ThreadLocal<>();

    private UserHolder() {
    }

    /**
     * 设置当前线程的用户ID
     */
    public static void setUserId(Long userId) {
        USER_ID_HOLDER.set(userId);
    }

    /**
     * 获取当前线程的用户ID
     */
    public static Long getUserId() {
        return USER_ID_HOLDER.get();
    }

    /**
     * 清除当前线程的用户ID
     */
    public static void clear() {
        USER_ID_HOLDER.remove();
    }
}
