package com.btea.lexiflow.user.constant;

/**
 * @Author: TwentyfiveBTea
 * @Date: 2026/7/13
 * @Description: 用户常量类
 */
public final class UserConstant {

    private UserConstant() {
    }

    /**
     * 账号状态：禁用
     */
    public static final int STATUS_DISABLED = 0;

    /**
     * 账号状态：正常
     */
    public static final int STATUS_NORMAL = 1;

    /**
     * 账号状态：已注销
     */
    public static final int STATUS_CANCELED = 2;

    /**
     * 普通用户角色
     */
    public static final String ROLE_USER = "USER";

    /**
     * 管理员角色
     */
    public static final String ROLE_ADMIN = "ADMIN";
}
