package com.btea.lexiflow.vocab.dto.resp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * @Author: TwentyfiveBTea
 * @Date: 2026/7/12
 * @Description: 词汇库词条响应参数
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
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
     * 词汇等级，例如 CET4、CET6、N3
     */
    private String level;

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
     * 加入词汇库时间
     */
    private Date addedAt;
}
