package com.btea.lexiflow.vocab.dto.resp;

import lombok.Builder;
import lombok.Data;

/**
 * @Author: TwentyfiveBTea
 * @Date: 2026/7/12
 * @Description: 词汇库学习统计响应参数
 */
@Data
@Builder
public class VocabLibraryStatisticsRespDTO {

    /**
     * 词汇库ID
     */
    private String libraryId;

    /**
     * 单词总数
     */
    private Long totalCount;

    /**
     * 未学习单词数
     */
    private Long newCount;

    /**
     * 学习中单词数
     */
    private Long learningCount;

    /**
     * 已掌握单词数
     */
    private Long masteredCount;

    /**
     * 待复习单词数
     */
    private Long dueCount;
}
