package com.btea.lexiflow.common.convention.exception;

import lombok.Getter;

/**
 * @Author: TwentyfiveBTea
 * @Date: 2026/7/2 15:24
 * @Description: 基础异常类
 */
@Getter
public class BaseException extends RuntimeException {

    private final String code;

    public BaseException(String code, String message) {
        super(message);
        this.code = code;
    }

    public BaseException(String message) {
        super(message);
        this.code = null;
    }
}
