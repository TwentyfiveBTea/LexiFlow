package com.btea.lexiflow.article.dto.resp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * @Author: TwentyfiveBTea
 * @Date: 2026/8/1
 * @Description: 文章直传初始化响应参数
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ArticleUploadInitRespDTO {

    /**
     * 文章ID
     */
    private String articleId;

    /**
     * S3预签名上传地址
     */
    private String uploadUrl;

    /**
     * 上传时必须使用的MIME类型
     */
    private String contentType;

    /**
     * 上传地址过期时间
     */
    private Date expiresAt;
}
