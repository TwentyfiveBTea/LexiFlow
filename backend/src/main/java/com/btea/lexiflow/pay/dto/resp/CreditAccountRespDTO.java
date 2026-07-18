package com.btea.lexiflow.pay.dto.resp;

import lombok.Builder;
import lombok.Data;

import java.util.Date;

/**
 * @Author: TwentyfiveBTea
 * @Date: 2026/7/16
 * @Description: Credits账户响应参数
 */
@Data
@Builder
public class CreditAccountRespDTO {

    /**
     * 当前可用Credits
     */
    private Long availableCredits;

    /**
     * 当前冻结Credits
     */
    private Long frozenCredits;

    /**
     * Credits账户状态：0=冻结，1=正常
     */
    private Integer status;

    /**
     * 更新时间
     */
    private Date updatedAt;
}
