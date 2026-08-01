package com.btea.lexiflow.article.dto.req;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Author: TwentyfiveBTea
 * @Date: 2026/8/1
 * @Description: 文章直传初始化请求参数
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ArticleUploadInitReqDTO {

    /**
     * 原始文件名
     */
    @NotBlank
    @Size(max = 255)
    private String filename;

    /**
     * 文件MIME类型
     */
    @Size(max = 255)
    private String contentType;

    /**
     * 文件大小
     */
    @NotNull
    @Positive
    private Long fileSize;
}
