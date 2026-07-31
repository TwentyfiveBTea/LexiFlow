package com.btea.lexiflow.vocab.cache;

import com.btea.lexiflow.vocab.dao.mapper.BizVocabEnMapper;
import com.btea.lexiflow.vocab.dao.mapper.BizVocabJpMapper;
import com.btea.lexiflow.vocab.util.VocabLevelUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.btea.lexiflow.article.constant.ArticleConstant.VOCAB_QUERY_BATCH_SIZE;

/**
 * @Author: TwentyfiveBTea
 * @Date: 2026/7/24
 * @Description: 词典与数据库词汇等级批量缓存加载器
 */
@Component
@RequiredArgsConstructor
public class VocabWordCacheLoader {

    private final VocabQueryCache vocabQueryCache;
    private final BizVocabEnMapper bizVocabEnMapper;
    private final BizVocabJpMapper bizVocabJpMapper;

    /**
     * 批量加载词典词条，优先读取缓存并查询缺失数据
     *
     * @param languageCode 语言编码
     * @param wordIds 单词ID集合
     * @return 单词ID与词典词条的映射
     */
    public Map<Long, VocabWordCacheEntry> loadWords(String languageCode, Set<Long> wordIds) {
        Map<Long, VocabWordCacheEntry> result = new LinkedHashMap<>(vocabQueryCache.getWords(languageCode, wordIds));
        Set<Long> missing = new HashSet<>(wordIds);
        missing.removeAll(result.keySet());
        List<VocabWordCacheEntry> loaded = new ArrayList<>();
        for (List<Long> batch : partition(new ArrayList<>(missing))) {
            if ("ja".equals(languageCode)) {
                bizVocabJpMapper.selectBatchIds(batch).forEach(word -> loaded.add(new VocabWordCacheEntry(
                        word.getId(), languageCode, word.getWord(), word.getKana(), null, null,
                        word.getTranslations(), VocabLevelUtil.toApiLevel(languageCode, word.getLevel()))));
            } else {
                bizVocabEnMapper.selectBatchIds(batch).forEach(word -> loaded.add(new VocabWordCacheEntry(
                        word.getId(), languageCode, word.getWord(), null, word.getUs(), word.getUk(),
                        word.getTranslations(), VocabLevelUtil.toApiLevel(languageCode, word.getLevel()))));
            }
        }
        vocabQueryCache.putWords(languageCode, loaded);
        loaded.forEach(word -> result.put(word.wordId(), word));
        return result;
    }

    /**
     * 批量加载词汇等级，优先读取缓存并使用词典等级补齐缺失数据
     *
     * @param userId 用户ID
     * @param languageCode 语言编码
     * @param wordIds 单词ID集合
     * @param wordDetails 词典词条映射
     * @return 单词ID与词汇等级的映射
     */
    public Map<Long, VocabWordLevelCacheEntry> loadLevels(String userId, String languageCode,
                                                           Set<Long> wordIds,
                                                           Map<Long, VocabWordCacheEntry> wordDetails) {
        Map<Long, VocabWordLevelCacheEntry> result = new LinkedHashMap<>(
                vocabQueryCache.getWordLevels(userId, languageCode, wordIds));
        Set<Long> missing = new HashSet<>(wordIds);
        missing.removeAll(result.keySet());
        if (missing.isEmpty()) {
            return result;
        }
        for (Long wordId : missing) {
            VocabWordCacheEntry word = wordDetails.get(wordId);
            String level = word == null ? null : word.level();
            result.put(wordId, new VocabWordLevelCacheEntry(level));
        }
        vocabQueryCache.putWordLevels(userId, languageCode, result);
        return result;
    }

    private <T> List<List<T>> partition(List<T> values) {
        List<List<T>> result = new ArrayList<>();
        for (int index = 0; index < values.size(); index += VOCAB_QUERY_BATCH_SIZE) {
            result.add(values.subList(index, Math.min(index + VOCAB_QUERY_BATCH_SIZE, values.size())));
        }
        return result;
    }
}
