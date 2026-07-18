package com.btea.lexiflow.pay.model;

/**
 * @Author: TwentyfiveBTea
 * @Date: 2026/7/16
 * @Description: AI文章处理计费上下文
 */
public record AiProcessingContext(
        String userId,
        String articleId,
        String processingNo) {
}
