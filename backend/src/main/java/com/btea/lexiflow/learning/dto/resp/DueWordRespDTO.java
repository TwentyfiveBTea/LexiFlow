package com.btea.lexiflow.learning.dto.resp;

import lombok.Builder;
import lombok.Data;

/**
 * @Author: TwentyfiveBTea
 * @Date: 2026/7/12
 * @Description: 待复习单词响应参数
 */
@Data
@Builder
public class DueWordRespDTO {

    /**
     * 词汇库词条关系ID
     */
    private String libraryWordId;

    /**
     * 单词ID
     */
    private Long wordId;

    /**
     * 语言标识
     */
    private String languageCode;

    /**
     * 词汇等级
     */
    private String level;

    /**
     * 单词
     */
    private String word;

    /**
     * 日语假名
     */
    private String kana;

    /**
     * 美式音标
     */
    private String us;

    /**
     * 英式音标
     */
    private String uk;

    /**
     * 释义
     */
    private String translations;
}
