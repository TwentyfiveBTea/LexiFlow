package com.btea.lexiflow.article.service;

import com.btea.lexiflow.article.dto.resp.ArticleTranslationResultDTO;

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
     * @return 翻译结果
     */
    ArticleTranslationResultDTO translate(String originalContent);
}
