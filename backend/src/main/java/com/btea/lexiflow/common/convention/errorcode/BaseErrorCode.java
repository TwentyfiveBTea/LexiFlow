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
    USER_DISABLED("0002308", "账号已被禁用"),
    USER_CANCELED("0002309", "账号已注销"),

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
    ARTICLE_LANGUAGE_NOT_SUPPORTED("0004305", "暂不支持该文章语言"),

    // 词汇处理（0005xxx）
    VOCAB_NOT_FOUND("0005300", "词汇不存在"),
    VOCAB_ANALYSIS_FAILED("0005301", "词汇识别失败"),
    VOCAB_LIBRARY_NOT_FOUND("0005302", "词汇库不存在或无权访问"),
    VOCAB_LIBRARY_EXIST("0005303", "同名词汇库已存在"),
    VOCAB_LIBRARY_LANGUAGE_MISMATCH("0005304", "词汇与词汇库语言不一致"),
    VOCAB_LIBRARY_WORD_NOT_FOUND("0005305", "词汇库中不存在该词汇"),
    VOCAB_SOURCE_NOT_FOUND("0005306", "文章词汇不存在或无权访问"),
    VOCAB_LANGUAGE_NOT_SUPPORTED("0005307", "暂不支持该词汇语言"),
    VOCAB_LIBRARY_WORD_EXIST("0005308", "该词汇已存在于当前词汇库"),

    // 单词学习（0006xxx）
    WORD_PROGRESS_NOT_FOUND("0006300", "单词学习进度不存在"),
    WORD_REVIEW_QUALITY_INVALID("0006301", "复习结果无效"),

    // 支付与Credits（0007xxx）
    PAYMENT_CONFIG_INVALID("0007300", "支付配置无效"),
    PAYMENT_AMOUNT_INVALID("0007301", "充值金额必须为1到100之间的整数"),
    PAYMENT_DEVICE_INVALID("0007302", "暂不支持当前支付设备"),
    PAYMENT_ORDER_NOT_FOUND("0007303", "支付订单不存在或无权访问"),
    PAYMENT_ORDER_CONFLICT("0007304", "支付订单请求冲突"),
    PAYMENT_PROVIDER_ERROR("0007305", "支付平台调用失败"),
    PAYMENT_PROVIDER_RESPONSE_INVALID("0007306", "支付平台响应无效"),
    PAYMENT_NOTIFY_INVALID("0007307", "支付通知验证失败"),
    PAYMENT_AMOUNT_MISMATCH("0007308", "支付金额不一致"),
    CREDIT_ACCOUNT_NOT_FOUND("0007309", "Credits账户不存在"),
    CREDIT_ACCOUNT_FROZEN("0007310", "Credits账户已冻结"),
    CREDIT_BALANCE_INSUFFICIENT("0007311", "Credits余额不足"),
    CREDIT_RESERVATION_NOT_FOUND("0007312", "Credits预占记录不存在"),
    CREDIT_RESERVATION_CONFLICT("0007313", "Credits预占状态冲突"),
    AI_USAGE_NOT_FOUND("0007314", "AI用量记录不存在"),
    AI_USAGE_INVALID("0007315", "AI用量数据无效");

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
