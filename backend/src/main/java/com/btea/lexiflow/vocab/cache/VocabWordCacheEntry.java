package com.btea.lexiflow.vocab.cache;

/**
 * @Author: TwentyfiveBTea
 * @Date: 2026/7/24
 * @Description: 词典词条缓存数据
 */
public record VocabWordCacheEntry(Long wordId,
                                  String languageCode,
                                  String word,
                                  String kana,
                                  String us,
                                  String uk,
                                  String translations,
                                  String level) {
}
