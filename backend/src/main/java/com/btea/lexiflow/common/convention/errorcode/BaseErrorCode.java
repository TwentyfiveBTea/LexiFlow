package com.btea.lexiflow.common.convention.errorcode;

import lombok.AllArgsConstructor;

/**
 * @Author: TwentyfiveBTea
 * @Date: 2026/7/2 14:10
 * @Description: 基础错误码
 */
@AllArgsConstructor
public enum BaseErrorCode implements IErrorCode {

    // 系统级响应码
    SUCCESS("0000000", "操作成功"),
    CLIENT_ERROR("0000100", "客户端异常"),
    SERVICE_ERROR("0000200", "服务端异常"),

    // 用户认证与注册（0001xxx）
    EMAIL_EXIST("0001300", "该邮箱已被使用"),
    AVATAR_FILE_EMPTY("0001301", "头像文件不能为空"),

    // 用户登录与 Token（0002xxx）
    USER_NOT_LOGIN("0002300", "用户未登录"),
    USER_NOT_FOUND("0002301", "用户不存在"),
    TOKEN_INVALID("0002302", "Token已失效，请重新登录"),
    PASSWORD_ERROR("0002303", "密码错误"),
    PASSWORD_NOT_MATCH("0002304", "两次密码不一致"),
    USER_NOT_FOUND_OR_PASSWORD_ERROR("0002305", "用户不存在或密码错误"),
    NEW_USERNAME_SAME_AS_OLD_USERNAME("0002306", "新用户名不能与原用户名相同"),
    NEW_PASSWORD_SAME_AS_OLD_PASSWORD("0002307", "新密码不能与原密码相同"),

    // 文件存储与解析（0003xxx）
    FILE_UPLOAD_FAILED("0003300", "文件上传失败"),
    FILE_PARSE_FAILED("0003301", "文件解析失败"),
    FILE_NOT_FOUND("00033302", "文件不存在"),

    // 文章处理（0004xxx）
    ARTICLE_NOT_FOUND("0004300", "文章不存在或无权访问"),
    ARTICLE_PARSE_FAILED("0004301", "文章解析失败"),
    ARTICLE_ANALYSIS_FAILED("0004302", "文章词汇分析失败"),
    ARTICLE_TRANSLATION_FAILED("0004303", "文章翻译失败"),
    AI_RESPONSE_PARSE_FAILED("0004304", "AI响应解析失败"),

    // 词汇处理（0005xxx）
    VOCAB_NOT_FOUND("0005300", "词汇不存在"),
    VOCAB_ANALYSIS_FAILED("0005301", "词汇识别失败");

    // 错误码
    private final String code;

    // 错误信息
    private final String message;

    @Override
    public String code() {
        return code;
    }

    @Override
    public String message() {
        return message;
    }
}
