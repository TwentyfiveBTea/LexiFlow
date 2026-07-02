package com.btea.lexiflow.user.dto.resp;

import lombok.Builder;
import lombok.Data;

/**
 * @Author: TwentyfiveBTea
 * @Date: 2026/7/2 22:30
 * @Description: 用户登录返回参数
 */
@Data
@Builder
public class UserLoginRespDTO {

    /**
     * 用户ID
     */
    private String userId;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 头像
     */
    private String avatar;

    /**
     * Token
     */
    private String token;
}
