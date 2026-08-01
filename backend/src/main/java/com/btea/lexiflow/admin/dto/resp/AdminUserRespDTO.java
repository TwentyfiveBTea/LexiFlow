/**
 * @Author: TwentyfiveBTea
 * @Date: 2026/7/24
 * @Description: 管理员用户列表响应参数
 */
package com.btea.lexiflow.admin.dto.resp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * 管理员用户列表响应参数。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminUserRespDTO {

    /**
     * 用户ID
     */
    private String userId;

    /**
     * 用户名
     */
    private String username;

    /**
     * 注册时间
     */
    private Date registeredAt;
}
