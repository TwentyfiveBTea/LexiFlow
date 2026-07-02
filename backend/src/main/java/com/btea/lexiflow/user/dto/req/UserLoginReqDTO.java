package com.btea.lexiflow.user.dto.req;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * @Author: TwentyfiveBTea
 * @Date: 2026/7/2 22:30
 * @Description: 用户登录请求参数
 */
@Data
public class UserLoginReqDTO {

    /**
     * 邮箱
     */
    @Email(message = "邮箱格式错误")
    @NotBlank(message = "邮箱不能为空")
    private String email;

    /**
     * 密码
     */
    @NotBlank(message = "密码不能为空")
    private String password;
}
