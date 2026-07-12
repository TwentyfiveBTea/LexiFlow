package com.btea.lexiflow.vocab.dto.req;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * @Author: TwentyfiveBTea
 * @Date: 2026/7/12
 * @Description: 文章词汇加入词汇库请求参数
 */
@Data
public class VocabLibraryWordAddReqDTO {

    /**
     * 文章ID
     */
    @NotBlank(message = "文章ID不能为空")
    private String articleId;

    /**
     * 文章命中词汇汇总ID
     */
    @NotBlank(message = "文章词汇ID不能为空")
    private String articleVocabId;
}
