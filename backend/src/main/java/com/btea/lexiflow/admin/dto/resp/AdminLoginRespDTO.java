package com.btea.lexiflow.admin.dto.resp;

import lombok.Builder;
import lombok.Data;

/**
 * @Author: TwentyfiveBTea
 * @Date: 2026/7/24
 * @Description: 管理员登录响应参数
 */
@Data
@Builder
public class AdminLoginRespDTO {

    /**
     * 管理员用户ID
     */
    private String userId;

    /**
     * 管理员用户名
     */
    private String username;

    /**
     * 访问令牌
     */
    private String token;
}
