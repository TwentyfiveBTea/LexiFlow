package com.btea.lexiflow.learning.dto.req;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/**
 * @Author: TwentyfiveBTea
 * @Date: 2026/7/12
 * @Description: 单词复习按钮请求参数
 */
@Data
public class WordReviewReqDTO {

    /**
     * 语言标识：en/ja
     */
    @NotBlank(message = "语言标识不能为空")
    @Pattern(regexp = "en|ja", message = "语言标识仅支持en或ja")
    private String languageCode;

    /**
     * 复习结果：UNKNOWN=不认识，VAGUE=有点模糊，KNOWN=完全知道
     */
    @NotBlank(message = "复习结果不能为空")
    private String rating;
}
