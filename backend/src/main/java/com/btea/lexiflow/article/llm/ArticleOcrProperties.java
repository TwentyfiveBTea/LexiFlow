package com.btea.lexiflow.article.llm;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @Author: TwentyfiveBTea
 * @Date: 2026/7/7 15:00
 * @Description: 文章 OCR 配置
 */
@Data
@Component
@ConfigurationProperties(prefix = "lexiflow.article.ocr")
public class ArticleOcrProperties {

    /**
     * 是否启用 OCR 兜底解析
     */
    private Boolean enabled = true;

    /**
     * Gemini API Key
     */
    private String apiKey;

    /**
     * Gemini OpenAI-compatible 基础地址
     */
    private String baseUrl;

    /**
     * OCR 使用的模型名称
     */
    private String modelName;

    /**
     * Tika 提取有效字符少于该值时触发 OCR
     */
    private Integer minTextLength;

    /**
     * OCR 最大 PDF 页数
     */
    private Integer maxPages;

    /**
     * PDF 页面渲染 DPI
     */
    private Integer renderDpi;

    /**
     * 页面图片格式
     */
    private String imageFormat;

    /**
     * 单页图片最大字节数
     */
    private Integer maxImageBytes;

    /**
     * OCR 模型输出 token 上限
     */
    private Integer maxOutputTokens;

    /**
     * OCR 调用失败重试次数
     */
    private Integer maxRetryTimes;
}
