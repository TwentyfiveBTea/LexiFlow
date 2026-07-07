package com.btea.lexiflow.article.dto.resp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Author: TwentyfiveBTea
 * @Date: 2026/7/4 16:40
 * @Description: 文章上传响应参数
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ArticleUploadRespDTO {

    /**
     * 文章ID
     */
    private String articleId;

    /**
     * 文章标题
     */
    private String title;

    /**
     * 文章主语言标识
     */
    private String languageCode;

    /**
     * 解析状态
     */
    private Integer parseStatus;

    /**
     * 翻译状态
     */
    private Integer translationStatus;
}
