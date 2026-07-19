package com.btea.lexiflow.article.dto.resp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * @Author: TwentyfiveBTea
 * @Date: 2026/7/4 17:10
 * @Description: 文章详情响应参数
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ArticleDetailRespDTO {

    /**
     * 文章ID
     */
    private String articleId;

    /**
     * 文章标题
     */
    private String title;

    /**
     * 解析后的文章正文内容
     */
    private String parsedContent;

    /**
     * 文章主语言标识
     */
    private String languageCode;

    /**
     * 词数统计
     */
    private Integer wordCount;

    /**
     * 字符数统计
     */
    private Integer charCount;

    /**
     * 创建时间
     */
    private Date createdAt;
}
