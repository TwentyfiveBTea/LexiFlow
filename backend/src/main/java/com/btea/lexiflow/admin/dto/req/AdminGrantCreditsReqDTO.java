package com.btea.lexiflow.admin.dto.req;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * @Author: TwentyfiveBTea
 * @Date: 2026/7/24
 * @Description: 管理员赠送Credits请求参数
 */
@Data
public class AdminGrantCreditsReqDTO {

    /**
     * 目标用户ID
     */
    @NotBlank(message = "用户ID不能为空")
    private String userId;

    /**
     * 赠送Credits数量
     */
    @NotNull(message = "赠送Credits数量不能为空")
    @Min(value = 1, message = "赠送Credits数量不能小于1")
    @Max(value = 10_000_000, message = "赠送Credits数量不能超过10000000")
    private Long credits;
}
