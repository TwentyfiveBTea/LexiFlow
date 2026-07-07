package com.btea.lexiflow.article.service;

import com.btea.lexiflow.article.dao.entity.BizArticlesDO;

/**
 * @Author: TwentyfiveBTea
 * @Date: 2026/7/7 15:00
 * @Description: 文章异步处理服务
 */
public interface ArticleProcessingService {

    /**
     * 异步处理已上传文章
     *
     * @param article 文章记录
     * @param fileBytes 文件字节数组
     * @param originalFilename 原始文件名
     * @param contentType 文件 MIME 类型
     */
    void processUploadedArticle(BizArticlesDO article, byte[] fileBytes, String originalFilename, String contentType);
}
