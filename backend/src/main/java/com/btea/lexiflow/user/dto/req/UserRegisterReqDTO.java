package com.btea.lexiflow.user.dto.req;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/**
 * @Author: TwentyfiveBTea
 * @Date: 2026/7/2 22:30
 * @Description: 用户注册请求参数
 */
@Data
public class UserRegisterReqDTO {

    /**
     * 邮箱
     */
    @Email(message = "邮箱格式错误")
    @NotBlank(message = "邮箱不能为空")
    private String email;

    /**
     * 用户名
     */
    private String username;

    /**
     * 密码
     */
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z]).{6,}$", message = "密码长度不少于6位，且必须包含大小写字母")
    @NotBlank(message = "密码不能为空")
    private String password;

    /**
     * 二次确认密码
     */
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z]).{6,}$", message = "二次确认密码长度不少于6位，且必须包含大小写字母")
    @NotBlank(message = "二次确认密码不能为空")
    private String confirmPassword;
}
