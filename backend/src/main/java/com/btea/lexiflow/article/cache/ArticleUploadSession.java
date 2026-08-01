package com.btea.lexiflow.article.cache;

/**
 * @Author: TwentyfiveBTea
 * @Date: 2026/8/1
 * @Description: 文章直传会话数据
 */
public record ArticleUploadSession(String articleId,
                                   String userId,
                                   String objectKey,
                                   String filename,
                                   String fileType,
                                   String contentType,
                                   long fileSize,
                                   String status) {
}
