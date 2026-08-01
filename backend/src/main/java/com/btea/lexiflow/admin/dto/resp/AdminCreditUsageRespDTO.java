package com.btea.lexiflow.admin.dto.resp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * @Author: TwentyfiveBTea
 * @Date: 2026/7/24
 * @Description: 管理员Credits使用记录响应参数
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminCreditUsageRespDTO {

    /**
     * 用户ID
     */
    private String userId;

    /**
     * 用户名
     */
    private String username;

    /**
     * 文章名称
     */
    private String articleTitle;

    /**
     * 总使用Credits
     */
    private Long totalCredits;

    /**
     * OCR使用Credits
     */
    private Long ocrCredits;

    /**
     * 翻译使用Credits
     */
    private Long translationCredits;

    /**
     * 完成时间
     */
    private Date completedAt;
}
