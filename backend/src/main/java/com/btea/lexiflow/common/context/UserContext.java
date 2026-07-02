package com.btea.lexiflow.common.context;

/**
 * @Author: TwentyfiveBTea
 * @Date: 2026/7/2 12:29
 * @Description: 用户上下文
 */
public class UserContext {

    private static final ThreadLocal<String> USER_ID_THREAD_LOCAL = new ThreadLocal<>();

    /**
     * 设置当前用户ID
     */
    public static void setCurrentUserId(String userId) {
        USER_ID_THREAD_LOCAL.set(userId);
    }

    /**
     * 获取当前用户ID
     */
    public static String getCurrentUserId() {
        return USER_ID_THREAD_LOCAL.get();
    }

    /**
     * 清理ThreadLocal
     */
    public static void clear() {
        USER_ID_THREAD_LOCAL.remove();
    }
}
