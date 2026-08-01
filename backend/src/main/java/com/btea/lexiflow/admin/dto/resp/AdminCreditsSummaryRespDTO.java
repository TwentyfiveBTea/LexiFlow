/**
 * @Author: TwentyfiveBTea
 * @Date: 2026/7/24
 * @Description: 管理员Credits用量汇总响应参数
 */
package com.btea.lexiflow.admin.dto.resp;

import lombok.Builder;
import lombok.Data;

/**
 * 管理员Credits用量汇总响应参数。
 */
@Data
@Builder
public class AdminCreditsSummaryRespDTO {

    /**
     * 最近一天使用量
     */
    private Long lastDayCredits;

    /**
     * 最近三天使用量
     */
    private Long lastThreeDaysCredits;

    /**
     * 最近七天使用量
     */
    private Long lastSevenDaysCredits;

    /**
     * 最近三十天使用量
     */
    private Long lastThirtyDaysCredits;
}
