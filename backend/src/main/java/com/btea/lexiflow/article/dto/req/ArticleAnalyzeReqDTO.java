package com.btea.lexiflow.article.dto.req;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Author: TwentyfiveBTea
 * @Date: 2026/7/4 16:41
 * @Description: 文章词汇分析请求参数
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ArticleAnalyzeReqDTO {

    /**
     * 词汇分析等级：CET4/CET6/POSTGRADUATE/TOEFL/IELTS 等
     */
    private String analysisLevel;
}
