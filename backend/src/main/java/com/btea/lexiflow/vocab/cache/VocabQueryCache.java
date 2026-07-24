package com.btea.lexiflow.vocab.cache;

import com.btea.lexiflow.common.cache.RedisJsonCache;
import com.btea.lexiflow.vocab.dto.resp.VocabLibraryRespDTO;
import com.btea.lexiflow.vocab.dto.resp.VocabLibraryStatisticsRespDTO;
import com.btea.lexiflow.vocab.dto.resp.VocabLibraryWordRespDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.btea.lexiflow.vocab.constant.VocabRedisConstant.CACHE_KEY_PREFIX;
import static com.btea.lexiflow.vocab.constant.VocabRedisConstant.EMPTY_CACHE_TTL;
import static com.btea.lexiflow.vocab.constant.VocabRedisConstant.LIBRARY_CACHE_TTL;
import static com.btea.lexiflow.vocab.constant.VocabRedisConstant.LIBRARY_WORD_CACHE_TTL;
import static com.btea.lexiflow.vocab.constant.VocabRedisConstant.STATISTICS_CACHE_TTL;
import static com.btea.lexiflow.vocab.constant.VocabRedisConstant.WORD_CACHE_TTL;
import static com.btea.lexiflow.vocab.constant.VocabRedisConstant.WORD_LEVEL_CACHE_TTL;

/**
 * @Author: TwentyfiveBTea
 * @Date: 2026/7/24
 * @Description: 词汇查询Redis缓存
 */
@Component
@RequiredArgsConstructor
public class VocabQueryCache {

    private final RedisJsonCache cache;

    /**
     * 获取用户词汇缓存版本
     *
     * @param userId 用户ID
     * @return 缓存版本号
     */
    public long getUserVersion(String userId) {
        return cache.getVersion(userVersionKey(userId));
    }

    /**
     * 获取用户词汇库列表缓存
     *
     * @param userId 用户ID
     * @param version 缓存版本号
     * @return 词汇库列表，不存在时返回null
     */
    public List<VocabLibraryRespDTO> getLibraries(String userId, long version) {
        return cache.getList(userKey(userId, version, "libraries"), VocabLibraryRespDTO.class);
    }

    /**
     * 写入用户词汇库列表缓存
     *
     * @param userId 用户ID
     * @param version 缓存版本号
     * @param value 词汇库列表
     */
    public void putLibraries(String userId, long version, List<VocabLibraryRespDTO> value) {
        cache.put(userKey(userId, version, "libraries"), value, ttl(value, LIBRARY_CACHE_TTL));
    }

    /**
     * 获取词汇库详情缓存
     *
     * @param userId 用户ID
     * @param version 缓存版本号
     * @param libraryId 词汇库ID
     * @return 词汇库详情，不存在时返回null
     */
    public VocabLibraryRespDTO getLibrary(String userId, long version, String libraryId) {
        return cache.get(userKey(userId, version, "library:" + libraryId), VocabLibraryRespDTO.class);
    }

    /**
     * 写入词汇库详情缓存
     *
     * @param userId 用户ID
     * @param version 缓存版本号
     * @param value 词汇库详情
     */
    public void putLibrary(String userId, long version, VocabLibraryRespDTO value) {
        cache.put(userKey(userId, version, "library:" + value.getLibraryId()), value, LIBRARY_CACHE_TTL);
    }

    /**
     * 获取词汇库单词列表缓存
     *
     * @param userId 用户ID
     * @param version 缓存版本号
     * @param libraryId 词汇库ID
     * @return 词汇库单词列表，不存在时返回null
     */
    public List<VocabLibraryWordRespDTO> getLibraryWords(String userId, long version, String libraryId) {
        return cache.getList(userKey(userId, version, "library:" + libraryId + ":words"),
                VocabLibraryWordRespDTO.class);
    }

    /**
     * 写入词汇库单词列表缓存
     *
     * @param userId 用户ID
     * @param version 缓存版本号
     * @param libraryId 词汇库ID
     * @param value 词汇库单词列表
     */
    public void putLibraryWords(String userId, long version, String libraryId,
                                List<VocabLibraryWordRespDTO> value) {
        cache.put(userKey(userId, version, "library:" + libraryId + ":words"),
                value, ttl(value, LIBRARY_WORD_CACHE_TTL));
    }

    /**
     * 获取词汇库统计数据缓存
     *
     * @param userId 用户ID
     * @param version 缓存版本号
     * @param libraryId 词汇库ID
     * @return 统计数据，不存在时返回null
     */
    public VocabLibraryStatisticsRespDTO getStatistics(String userId, long version, String libraryId) {
        return cache.get(userKey(userId, version, "library:" + libraryId + ":statistics"),
                VocabLibraryStatisticsRespDTO.class);
    }

    /**
     * 写入词汇库统计数据缓存
     *
     * @param userId 用户ID
     * @param version 缓存版本号
     * @param libraryId 词汇库ID
     * @param value 统计数据
     */
    public void putStatistics(String userId, long version, String libraryId, VocabLibraryStatisticsRespDTO value) {
        cache.put(userKey(userId, version, "library:" + libraryId + ":statistics"), value, STATISTICS_CACHE_TTL);
    }

    /**
     * 批量获取词典词条缓存
     *
     * @param languageCode 语言编码
     * @param wordIds 单词ID集合
     * @return 已缓存的词典词条映射
     */
    public Map<Long, VocabWordCacheEntry> getWords(String languageCode, Set<Long> wordIds) {
        List<String> keys = wordIds.stream().map(id -> wordKey(languageCode, id)).toList();
        Map<String, VocabWordCacheEntry> cached = cache.multiGet(keys, VocabWordCacheEntry.class);
        Map<Long, VocabWordCacheEntry> result = new LinkedHashMap<>();
        cached.values().forEach(value -> result.put(value.wordId(), value));
        return result;
    }

    /**
     * 批量写入词典词条缓存
     *
     * @param languageCode 语言编码
     * @param words 词典词条列表
     */
    public void putWords(String languageCode, List<VocabWordCacheEntry> words) {
        Map<String, VocabWordCacheEntry> values = new LinkedHashMap<>();
        words.forEach(word -> values.put(wordKey(languageCode, word.wordId()), word));
        cache.putAll(values, WORD_CACHE_TTL);
    }

    /**
     * 批量获取用户词汇等级缓存
     *
     * @param userId 用户ID
     * @param languageCode 语言编码
     * @param wordIds 单词ID集合
     * @return 已缓存的用户词汇等级映射
     */
    public Map<Long, VocabWordLevelCacheEntry> getWordLevels(String userId, String languageCode, Set<Long> wordIds) {
        List<String> keys = wordIds.stream().map(id -> wordLevelKey(userId, languageCode, id)).toList();
        Map<String, VocabWordLevelCacheEntry> cached = cache.multiGet(keys, VocabWordLevelCacheEntry.class);
        Map<Long, VocabWordLevelCacheEntry> result = new LinkedHashMap<>();
        for (Long wordId : wordIds) {
            VocabWordLevelCacheEntry value = cached.get(wordLevelKey(userId, languageCode, wordId));
            if (value != null) {
                result.put(wordId, value);
            }
        }
        return result;
    }

    /**
     * 批量写入用户词汇等级缓存
     *
     * @param userId 用户ID
     * @param languageCode 语言编码
     * @param wordLevels 用户词汇等级映射
     */
    public void putWordLevels(String userId, String languageCode,
                              Map<Long, VocabWordLevelCacheEntry> wordLevels) {
        Map<String, VocabWordLevelCacheEntry> populated = new LinkedHashMap<>();
        Map<String, VocabWordLevelCacheEntry> empty = new LinkedHashMap<>();
        wordLevels.forEach((wordId, value) -> {
            Map<String, VocabWordLevelCacheEntry> destination = value.level() == null ? empty : populated;
            destination.put(wordLevelKey(userId, languageCode, wordId), value);
        });
        cache.putAll(populated, WORD_LEVEL_CACHE_TTL);
        cache.putAll(empty, EMPTY_CACHE_TTL);
    }

    /**
     * 使用户词汇查询缓存失效
     *
     * @param userId 用户ID
     */
    public void invalidateUser(String userId) {
        cache.incrementVersion(userVersionKey(userId));
    }

    /**
     * 使指定用户词汇等级及相关查询缓存失效
     *
     * @param userId 用户ID
     * @param languageCode 语言编码
     * @param wordIds 单词ID集合
     */
    public void invalidateWordLevels(String userId, String languageCode, Set<Long> wordIds) {
        cache.delete(wordIds.stream().map(wordId -> wordLevelKey(userId, languageCode, wordId)).toList());
        invalidateUser(userId);
    }

    private String userKey(String userId, long version, String suffix) {
        return CACHE_KEY_PREFIX + "user:" + userId + ":v" + version + ":" + suffix;
    }

    private String userVersionKey(String userId) {
        return CACHE_KEY_PREFIX + "version:user:" + userId;
    }

    private String wordKey(String languageCode, Long wordId) {
        return CACHE_KEY_PREFIX + "word:" + languageCode + ":" + wordId;
    }

    private String wordLevelKey(String userId, String languageCode, Long wordId) {
        return CACHE_KEY_PREFIX + "user-word-level:" + userId + ":" + languageCode + ":" + wordId;
    }

    private Duration ttl(List<?> value, Duration populatedTtl) {
        return value.isEmpty() ? EMPTY_CACHE_TTL : populatedTtl;
    }
}
