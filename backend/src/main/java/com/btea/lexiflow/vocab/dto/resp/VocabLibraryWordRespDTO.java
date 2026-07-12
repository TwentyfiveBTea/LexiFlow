package com.btea.lexiflow.vocab.dto.resp;

import lombok.Builder;
import lombok.Data;

import java.util.Date;

/**
 * @Author: TwentyfiveBTea
 * @Date: 2026/7/12
 * @Description: 词汇库词条响应参数
 */
@Data
@Builder
public class VocabLibraryWordRespDTO {

    /**
     * 词汇库词条关系ID
     */
    private String libraryWordId;

    /**
     * 公共词典词条ID
     */
    private Long wordId;

    /**
     * 语言标识：en/ja
     */
    private String languageCode;

    /**
     * 单词或日语词条
     */
    private String word;

    /**
     * 日语假名读音
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
     * 词条释义 JSON
     */
    private String translations;

    /**
     * 英语短语 JSON
     */
    private String phrases;

    /**
     * 学习状态：0=新词，1=学习中，2=已掌握
     */
    private Integer learningStatus;

    /**
     * 累计复习次数
     */
    private Integer reviewCount;

    /**
     * 下次复习时间
     */
    private Date nextReviewAt;

    /**
     * 加入词汇库时间
     */
    private Date addedAt;
}
