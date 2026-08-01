/**
 * @Author: TwentyfiveBTea
 * @Date: 2026/7/24
 * @Description: 管理员登录请求参数
 */
package com.btea.lexiflow.admin.dto.req;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 管理员登录请求参数。
 */
@Data
public class AdminLoginReqDTO {

    /**
     * 管理员账号
     */
    @NotBlank(message = "管理员账号不能为空")
    private String account;

    /**
     * 管理员密码
     */
    @NotBlank(message = "管理员密码不能为空")
    private String password;
}
