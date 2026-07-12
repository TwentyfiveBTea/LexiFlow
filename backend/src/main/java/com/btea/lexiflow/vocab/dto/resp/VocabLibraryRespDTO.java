package com.btea.lexiflow.vocab.dto.resp;

import lombok.Builder;
import lombok.Data;

import java.util.Date;

/**
 * @Author: TwentyfiveBTea
 * @Date: 2026/7/12
 * @Description: 词汇库响应参数
 */
@Data
@Builder
public class VocabLibraryRespDTO {

    /**
     * 词汇库ID
     */
    private String libraryId;

    /**
     * 词汇库名称
     */
    private String name;

    /**
     * 语言标识：en/ja
     */
    private String languageCode;

    /**
     * 词汇库描述
     */
    private String description;

    /**
     * 词汇库中的正常词条数量
     */
    private Long wordCount;

    /**
     * 创建时间
     */
    private Date createdAt;

    /**
     * 更新时间
     */
    private Date updatedAt;
}
