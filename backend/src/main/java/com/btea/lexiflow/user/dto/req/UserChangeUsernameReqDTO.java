package com.btea.lexiflow.user.dto.req;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * @Author: TwentyfiveBTea
 * @Date: 2026/7/3 14:53
 * @Description: 用户修改用户名请求参数
 */
@Data
public class UserChangeUsernameReqDTO {

    /**
     * 用户名
     */
    @NotBlank(message = "用户名不能为空")
    private String username;
}
