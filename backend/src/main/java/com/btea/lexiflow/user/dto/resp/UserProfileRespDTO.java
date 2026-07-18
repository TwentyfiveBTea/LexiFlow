package com.btea.lexiflow.user.dto.resp;

import lombok.Builder;
import lombok.Data;

/**
 * 用户资料弹窗响应参数
 */
@Data
@Builder
public class UserProfileRespDTO {

    /**
     * 头像地址
     */
    private String avatar;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 注册天数，注册当天为第 1 天
     */
    private long registeredDays;
}
