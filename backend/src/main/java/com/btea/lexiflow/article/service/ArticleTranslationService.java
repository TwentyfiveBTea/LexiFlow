package com.btea.lexiflow.article.service;

import com.btea.lexiflow.article.dto.resp.ArticleTranslationResultDTO;
import com.btea.lexiflow.pay.model.AiProcessingContext;

/**
 * @Author: TwentyfiveBTea
 * @Date: 2026/7/6 10:08
 * @Description: 文章翻译服务接口
 */
public interface ArticleTranslationService {

    /**
     * 翻译文章正文
     *
     * @param originalContent 原始正文内容
     * @param context AI处理计费上下文
     * @return 翻译结果
     */
    ArticleTranslationResultDTO translate(String originalContent, AiProcessingContext context);
}
