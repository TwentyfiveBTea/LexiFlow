package com.btea.lexiflow.article.dto.resp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Author: TwentyfiveBTea
 * @Date: 2026/7/6 10:04
 * @Description: 文章翻译实体信息
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ArticleTranslationEntityDTO {

    /**
     * 实体名称
     */
    private String name;

    /**
     * 实体类型：person/organization/location/concept
     */
    private String type;

    /**
     * 实体说明
     */
    private String description;
}
