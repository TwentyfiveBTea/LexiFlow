package com.btea.lexiflow.article.cache;

import com.btea.lexiflow.article.dto.resp.ArticleDetailRespDTO;
import com.btea.lexiflow.article.dto.resp.ArticleListRespDTO;
import com.btea.lexiflow.article.dto.resp.ArticleProcessingDetailRespDTO;
import com.btea.lexiflow.article.dto.resp.ArticleVocabOccurrenceRespDTO;
import com.btea.lexiflow.article.dto.resp.ArticleVocabRespDTO;
import com.btea.lexiflow.common.cache.RedisJsonCache;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.List;

import static com.btea.lexiflow.article.constant.ArticleRedisConstant.CACHE_KEY_PREFIX;
import static com.btea.lexiflow.article.constant.ArticleRedisConstant.DETAIL_CACHE_TTL;
import static com.btea.lexiflow.article.constant.ArticleRedisConstant.EMPTY_CACHE_TTL;
import static com.btea.lexiflow.article.constant.ArticleRedisConstant.LIST_CACHE_TTL;
import static com.btea.lexiflow.article.constant.ArticleRedisConstant.PROCESSING_CACHE_TTL;
import static com.btea.lexiflow.article.constant.ArticleRedisConstant.VOCAB_CACHE_TTL;

/**
 * @Author: TwentyfiveBTea
 * @Date: 2026/7/24
 * @Description: 文章查询Redis缓存
 */
@Component
@RequiredArgsConstructor
public class ArticleQueryCache {

    private final RedisJsonCache cache;

    /**
     * 获取用户文章缓存版本
     *
     * @param userId 用户ID
     * @return 缓存版本号
     */
    public long getUserVersion(String userId) {
        return cache.getVersion(userVersionKey(userId));
    }

    /**
     * 获取指定文章的缓存版本
     *
     * @param userId 用户ID
     * @param articleId 文章ID
     * @return 缓存版本号
     */
    public long getArticleVersion(String userId, String articleId) {
        return cache.getVersion(articleVersionKey(userId, articleId));
    }

    /**
     * 获取文章列表缓存
     *
     * @param userId 用户ID
     * @param version 缓存版本号
     * @return 文章列表，不存在时返回null
     */
    public List<ArticleListRespDTO> getArticleList(String userId, long version) {
        return cache.getList(userKey(userId, version, "list"), ArticleListRespDTO.class);
    }

    /**
     * 写入文章列表缓存
     *
     * @param userId 用户ID
     * @param version 缓存版本号
     * @param value 文章列表
     */
    public void putArticleList(String userId, long version, List<ArticleListRespDTO> value) {
        cache.put(userKey(userId, version, "list"), value, ttl(value, LIST_CACHE_TTL));
    }

    /**
     * 获取最近文章缓存
     *
     * @param userId 用户ID
     * @param version 缓存版本号
     * @return 最近文章列表，不存在时返回null
     */
    public List<ArticleListRespDTO> getRecentArticles(String userId, long version) {
        return cache.getList(userKey(userId, version, "recent"), ArticleListRespDTO.class);
    }

    /**
     * 写入最近文章缓存
     *
     * @param userId 用户ID
     * @param version 缓存版本号
     * @param value 最近文章列表
     */
    public void putRecentArticles(String userId, long version, List<ArticleListRespDTO> value) {
        cache.put(userKey(userId, version, "recent"), value, ttl(value, LIST_CACHE_TTL));
    }

    /**
     * 获取文章详情缓存
     *
     * @param userId 用户ID
     * @param articleId 文章ID
     * @param version 缓存版本号
     * @return 文章详情，不存在时返回null
     */
    public ArticleDetailRespDTO getArticleDetail(String userId, String articleId, long version) {
        return cache.get(articleKey(userId, articleId, version, "detail"), ArticleDetailRespDTO.class);
    }

    /**
     * 写入文章详情缓存
     *
     * @param userId 用户ID
     * @param articleId 文章ID
     * @param version 缓存版本号
     * @param value 文章详情
     */
    public void putArticleDetail(String userId, String articleId, long version, ArticleDetailRespDTO value) {
        cache.put(articleKey(userId, articleId, version, "detail"), value, DETAIL_CACHE_TTL);
    }

    /**
     * 获取文章处理详情缓存
     *
     * @param userId 用户ID
     * @param articleId 文章ID
     * @param version 缓存版本号
     * @return 文章处理详情，不存在时返回null
     */
    public ArticleProcessingDetailRespDTO getProcessingDetail(String userId, String articleId, long version) {
        return cache.get(articleKey(userId, articleId, version, "processing"), ArticleProcessingDetailRespDTO.class);
    }

    /**
     * 写入文章处理详情缓存
     *
     * @param userId 用户ID
     * @param articleId 文章ID
     * @param version 缓存版本号
     * @param value 文章处理详情
     */
    public void putProcessingDetail(String userId, String articleId, long version,
                                    ArticleProcessingDetailRespDTO value) {
        cache.put(articleKey(userId, articleId, version, "processing"), value, PROCESSING_CACHE_TTL);
    }

    /**
     * 获取文章已解析词汇等级缓存
     *
     * @param userId 用户ID
     * @param articleId 文章ID
     * @param version 缓存版本号
     * @return 词汇等级列表，不存在时返回null
     */
    public List<String> getVocabLevels(String userId, String articleId, long version) {
        return cache.getList(articleKey(userId, articleId, version, "vocab-levels"), String.class);
    }

    /**
     * 写入文章已解析词汇等级缓存
     *
     * @param userId 用户ID
     * @param articleId 文章ID
     * @param version 缓存版本号
     * @param value 词汇等级列表
     */
    public void putVocabLevels(String userId, String articleId, long version, List<String> value) {
        cache.put(articleKey(userId, articleId, version, "vocab-levels"), value, ttl(value, VOCAB_CACHE_TTL));
    }

    /**
     * 获取指定等级的文章词汇缓存
     *
     * @param userId 用户ID
     * @param articleId 文章ID
     * @param version 缓存版本号
     * @param level 词汇等级
     * @return 文章词汇列表，不存在时返回null
     */
    public List<ArticleVocabRespDTO> getArticleVocabs(String userId, String articleId, long version, String level) {
        return cache.getList(articleKey(userId, articleId, version, "vocabs:" + level), ArticleVocabRespDTO.class);
    }

    /**
     * 写入指定等级的文章词汇缓存
     *
     * @param userId 用户ID
     * @param articleId 文章ID
     * @param version 缓存版本号
     * @param level 词汇等级
     * @param value 文章词汇列表
     */
    public void putArticleVocabs(String userId, String articleId, long version, String level,
                                 List<ArticleVocabRespDTO> value) {
        cache.put(articleKey(userId, articleId, version, "vocabs:" + level), value, ttl(value, VOCAB_CACHE_TTL));
    }

    /**
     * 获取文章词汇出现位置缓存
     *
     * @param userId 用户ID
     * @param articleId 文章ID
     * @param version 缓存版本号
     * @param articleVocabId 文章词汇ID
     * @return 词汇出现位置列表，不存在时返回null
     */
    public List<ArticleVocabOccurrenceRespDTO> getOccurrences(String userId, String articleId, long version,
                                                               String articleVocabId) {
        return cache.getList(articleKey(userId, articleId, version, "occurrences:" + articleVocabId),
                ArticleVocabOccurrenceRespDTO.class);
    }

    /**
     * 写入文章词汇出现位置缓存
     *
     * @param userId 用户ID
     * @param articleId 文章ID
     * @param version 缓存版本号
     * @param articleVocabId 文章词汇ID
     * @param value 词汇出现位置列表
     */
    public void putOccurrences(String userId, String articleId, long version, String articleVocabId,
                               List<ArticleVocabOccurrenceRespDTO> value) {
        cache.put(articleKey(userId, articleId, version, "occurrences:" + articleVocabId),
                value, ttl(value, VOCAB_CACHE_TTL));
    }

    /**
     * 使用户文章查询缓存失效
     *
     * @param userId 用户ID
     */
    public void invalidateUser(String userId) {
        cache.incrementVersion(userVersionKey(userId));
    }

    /**
     * 使指定文章查询缓存失效
     *
     * @param userId 用户ID
     * @param articleId 文章ID
     */
    public void invalidateArticle(String userId, String articleId) {
        cache.incrementVersion(articleVersionKey(userId, articleId));
    }

    private String userKey(String userId, long version, String suffix) {
        return CACHE_KEY_PREFIX + "user:" + userId + ":v" + version + ":" + suffix;
    }

    private String articleKey(String userId, String articleId, long version, String suffix) {
        return CACHE_KEY_PREFIX + "user:" + userId + ":id:" + articleId + ":v" + version + ":" + suffix;
    }

    private String userVersionKey(String userId) {
        return CACHE_KEY_PREFIX + "version:user:" + userId;
    }

    private String articleVersionKey(String userId, String articleId) {
        return CACHE_KEY_PREFIX + "version:user:" + userId + ":id:" + articleId;
    }

    private Duration ttl(List<?> value, Duration populatedTtl) {
        return value.isEmpty() ? EMPTY_CACHE_TTL : populatedTtl;
    }
}
