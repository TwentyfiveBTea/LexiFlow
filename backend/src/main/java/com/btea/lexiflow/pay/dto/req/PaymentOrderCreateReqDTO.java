package com.btea.lexiflow.pay.dto.req;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * @Author: TwentyfiveBTea
 * @Date: 2026/7/16
 * @Description: 支付订单创建请求参数
 */
@Data
public class PaymentOrderCreateReqDTO {

    /**
     * 充值金额，单位为元，仅支持1到100之间的整数
     */
    @NotNull(message = "充值金额不能为空")
    @Min(value = 1, message = "充值金额不能小于1元")
    @Max(value = 100, message = "充值金额不能大于100元")
    private Integer amountYuan;

    /**
     * 客户端创建订单幂等请求号
     */
    @NotBlank(message = "客户端请求号不能为空")
    @Size(max = 64, message = "客户端请求号长度不能超过64个字符")
    private String clientRequestNo;

    /**
     * 支付设备类型
     */
    @Size(max = 16, message = "支付设备类型长度不能超过16个字符")
    private String deviceType;
}
