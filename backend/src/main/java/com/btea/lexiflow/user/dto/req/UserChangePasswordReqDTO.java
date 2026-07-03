package com.btea.lexiflow.user.dto.req;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/**
 * @Author: TwentyfiveBTea
 * @Date: 2026/7/3 14:53
 * @Description: 用户修改密码请求参数
 */
@Data
public class UserChangePasswordReqDTO {

    /**
     * 旧密码
     */
    @NotBlank(message = "旧密码不能为空")
    private String oldPassword;

    /**
     * 新密码
     */
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z]).{6,}$", message = "新密码长度不少于6位，且必须包含大小写字母")
    @NotBlank(message = "新密码不能为空")
    private String newPassword;

    /**
     * 二次确认密码
     */
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z]).{6,}$", message = "二次确认密码长度不少于6位，且必须包含大小写字母")
    @NotBlank(message = "二次确认密码不能为空")
    private String confirmPassword;
}
