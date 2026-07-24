package com.btea.lexiflow.article.dto.resp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @Author: TwentyfiveBTea
 * @Date: 2026/7/4 16:42
 * @Description: 文章词汇分析响应参数
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ArticleAnalyzeRespDTO {

    /**
     * 文章ID
     */
    private String articleId;

    /**
     * 词汇分析等级
     */
    private String analysisLevel;

    /**
     * 词汇分析状态：0=待分析, 1=分析中, 2=分析成功, 3=分析失败
     */
    private Integer analysisStatus;

    /**
     * 是否复用已有分析结果
     */
    private Boolean reused;

    /**
     * 命中词汇数量
     */
    private Integer matchedWordCount;

    /**
     * 命中词汇列表
     */
    private List<ArticleVocabRespDTO> vocabs;
}
