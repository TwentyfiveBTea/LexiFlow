package com.btea.lexiflow.common.constant;

/**
 * @Author: TwentyfiveBTea
 * @Date: 2026/7/2 23:38
 * @Description: 用户信息常量
 */
public class UserProfileConstant {

    /**
     * 头像访问地址前缀
     */
    public static final String AVATAR_BASE_URL = "https://lexi-flow.oss-cn-guangzhou.aliyuncs.com/";

    /**
     * 头像存储目录
     */
    public static final String AVATAR_DIR = "avatar/";

    /**
     * 头像文件后缀
     */
    public static final String AVATAR_SUFFIX = ".jpg";

    /**
     * 默认头像
     */
    public static final String DEFAULT_AVATAR = AVATAR_BASE_URL + AVATAR_DIR + "dufault.jpg";
}
