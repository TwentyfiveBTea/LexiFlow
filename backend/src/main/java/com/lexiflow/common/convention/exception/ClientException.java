package com.lexiflow.common.convention.exception;

import com.lexiflow.common.convention.errorcode.BaseErrorCode;
import com.lexiflow.common.convention.errorcode.IErrorCode;

/**
 * @Author: TwentyfiveBTea
 * @Date: 2026/7/2 12:45
 * @Description: 客户端异常
 */
public class ClientException extends AbstractException {

    public ClientException(String message) {
        super(message, null, BaseErrorCode.CLIENT_ERROR);
    }

    public ClientException(IErrorCode errorCode) {
        super(null, null, errorCode);
    }

    public ClientException(String message, Throwable throwable) {
        super(message, throwable, BaseErrorCode.CLIENT_ERROR);
    }
}
