package com.btea.lexiflow.vocab.dto.req;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * @Author: TwentyfiveBTea
 * @Date: 2026/7/12
 * @Description: 创建词汇库请求参数
 */
@Data
public class VocabLibraryCreateReqDTO {

    /**
     * 词汇库名称
     */
    @NotBlank(message = "词汇库名称不能为空")
    @Size(max = 128, message = "词汇库名称不能超过128个字符")
    private String name;

    /**
     * 语言标识：en/ja
     */
    @NotBlank(message = "语言标识不能为空")
    @Pattern(regexp = "en|ja", message = "语言标识仅支持en或ja")
    private String languageCode;

    /**
     * 词汇库描述
     */
    @Size(max = 500, message = "词汇库描述不能超过500个字符")
    private String description;
}
