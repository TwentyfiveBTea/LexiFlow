package com.btea.lexiflow.vocab.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.btea.lexiflow.article.constant.ArticleConstant;
import com.btea.lexiflow.article.dao.entity.BizArticlesDO;
import com.btea.lexiflow.article.dao.entity.RelArticleVocabDO;
import com.btea.lexiflow.article.dao.mapper.BizArticlesMapper;
import com.btea.lexiflow.article.dao.mapper.RelArticleVocabMapper;
import com.btea.lexiflow.article.nlp.ArticleVocabAnalyzer;
import com.btea.lexiflow.common.context.UserContext;
import com.btea.lexiflow.common.cache.AfterCommitExecutor;
import com.btea.lexiflow.common.convention.errorcode.BaseErrorCode;
import com.btea.lexiflow.common.convention.exception.ClientException;
import com.btea.lexiflow.learning.dao.entity.RelUserWordProgressDO;
import com.btea.lexiflow.learning.dao.mapper.RelUserWordProgressMapper;
import com.btea.lexiflow.vocab.constant.VocabConstant;
import com.btea.lexiflow.vocab.cache.VocabQueryCache;
import com.btea.lexiflow.vocab.cache.VocabWordCacheEntry;
import com.btea.lexiflow.vocab.cache.VocabWordCacheLoader;
import com.btea.lexiflow.vocab.cache.VocabWordLevelCacheEntry;
import com.btea.lexiflow.vocab.dao.entity.BizVocabLibraryDO;
import com.btea.lexiflow.vocab.dao.entity.RelVocabLibraryWordDO;
import com.btea.lexiflow.vocab.dao.mapper.BizVocabLibraryMapper;
import com.btea.lexiflow.vocab.dao.mapper.RelVocabLibraryWordMapper;
import com.btea.lexiflow.vocab.dto.req.VocabLibraryCreateReqDTO;
import com.btea.lexiflow.vocab.dto.req.VocabLibraryWordAddReqDTO;
import com.btea.lexiflow.vocab.dto.resp.VocabLibraryRespDTO;
import com.btea.lexiflow.vocab.dto.resp.VocabLibraryStatisticsRespDTO;
import com.btea.lexiflow.vocab.dto.resp.VocabLibraryWordRespDTO;
import com.btea.lexiflow.vocab.service.VocabService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @Author: TwentyfiveBTea
 * @Date: 2026/7/12
 * @Description: 词汇库服务实现类
 */
@Service
@RequiredArgsConstructor
public class VocabServiceImpl implements VocabService {

    private final BizVocabLibraryMapper bizVocabLibraryMapper;
    private final RelVocabLibraryWordMapper relVocabLibraryWordMapper;
    private final RelUserWordProgressMapper relUserWordProgressMapper;
    private final BizArticlesMapper bizArticlesMapper;
    private final RelArticleVocabMapper relArticleVocabMapper;
    private final ArticleVocabAnalyzer articleVocabAnalyzer;
    private final VocabQueryCache vocabQueryCache;
    private final VocabWordCacheLoader vocabWordCacheLoader;
    private final AfterCommitExecutor afterCommitExecutor;

    /**
     * 创建词汇库
     *
     * @param reqDTO 创建词汇库请求参数
     * @return 词汇库信息
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public VocabLibraryRespDTO createLibrary(VocabLibraryCreateReqDTO reqDTO) {
        String userId = getCurrentUserId();
        String languageCode = normalizeLanguage(reqDTO.getLanguageCode());
        String name = reqDTO.getName().trim();
        BizVocabLibraryDO existing = bizVocabLibraryMapper.selectOne(new LambdaQueryWrapper<BizVocabLibraryDO>()
                .eq(BizVocabLibraryDO::getUserId, userId)
                .eq(BizVocabLibraryDO::getName, name)
                .eq(BizVocabLibraryDO::getLanguageCode, languageCode));
        if (existing != null) {
            if (Integer.valueOf(VocabConstant.STATUS_NORMAL).equals(existing.getStatus())) {
                throw new ClientException(BaseErrorCode.VOCAB_LIBRARY_EXIST);
            }
            // 恢复同名的已删除词汇库，避免触发唯一键冲突。
            existing.setStatus(VocabConstant.STATUS_NORMAL);
            existing.setDescription(reqDTO.getDescription());
            existing.setDeletedAt(null);
            bizVocabLibraryMapper.updateById(existing);
            invalidateAfterCommit(userId);
            return toLibraryResp(existing, 0L);
        }
        BizVocabLibraryDO library = BizVocabLibraryDO.builder()
                .userId(userId)
                .name(name)
                .languageCode(languageCode)
                .description(reqDTO.getDescription() == null ? null : reqDTO.getDescription())
                .status(VocabConstant.STATUS_NORMAL)
                .build();
        bizVocabLibraryMapper.insert(library);
        invalidateAfterCommit(userId);
        return toLibraryResp(library, 0L);
    }

    /**
     * 查询当前用户的词汇库列表
     *
     * @param keyword 词汇库名称关键词
     * @param languageCode 语言标识：en/ja
     * @return 词汇库列表
     */
    @Override
    public List<VocabLibraryRespDTO> listLibraries(String keyword, String languageCode) {
        String userId = getCurrentUserId();
        String normalizedKeyword = keyword == null ? "" : keyword.trim();
        String normalizedLanguage = languageCode == null || languageCode.isBlank()
                ? null
                : normalizeLanguage(languageCode);
        long cacheVersion = vocabQueryCache.getUserVersion(userId);
        List<VocabLibraryRespDTO> libraries = vocabQueryCache.getLibraries(userId, cacheVersion);
        if (libraries == null) {
            List<BizVocabLibraryDO> libraryRows = bizVocabLibraryMapper.selectList(new LambdaQueryWrapper<BizVocabLibraryDO>()
                        .eq(BizVocabLibraryDO::getUserId, userId)
                        .eq(BizVocabLibraryDO::getStatus, VocabConstant.STATUS_NORMAL)
                        .orderByDesc(BizVocabLibraryDO::getCreatedAt));
            Map<String, Long> wordCounts = loadLibraryWordCounts(userId);
            libraries = libraryRows.stream()
                    .map(library -> toLibraryResp(library, wordCounts.getOrDefault(library.getId(), 0L)))
                    .toList();
            vocabQueryCache.putLibraries(userId, cacheVersion, libraries);
        }
        String normalizedKeywordLower = normalizedKeyword.toLowerCase(Locale.ROOT);
        return libraries.stream()
                .filter(library -> normalizedLanguage == null || normalizedLanguage.equals(library.getLanguageCode()))
                .filter(library -> normalizedKeywordLower.isEmpty()
                        || library.getName().toLowerCase(Locale.ROOT).contains(normalizedKeywordLower))
                .toList();
    }

    /**
     * 删除词汇库
     *
     * @param libraryId 词汇库ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteLibrary(String libraryId) {
        String userId = getCurrentUserId();
        BizVocabLibraryDO library = getLibrary(libraryId, userId);
        library.setStatus(VocabConstant.STATUS_DELETED);
        library.setDeletedAt(new Date());
        bizVocabLibraryMapper.updateById(library);
        relVocabLibraryWordMapper.update(null, new com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper<RelVocabLibraryWordDO>()
                .eq(RelVocabLibraryWordDO::getLibraryId, libraryId)
                .eq(RelVocabLibraryWordDO::getUserId, userId)
                .eq(RelVocabLibraryWordDO::getStatus, VocabConstant.STATUS_NORMAL)
                .set(RelVocabLibraryWordDO::getStatus, VocabConstant.STATUS_DELETED)
                .set(RelVocabLibraryWordDO::getDeletedAt, new Date()));
        invalidateAfterCommit(userId);
    }

    /**
     * 将文章命中词汇加入指定词汇库
     *
     * @param libraryId 词汇库ID
     * @param reqDTO 词汇加入请求参数
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addArticleVocab(String libraryId, VocabLibraryWordAddReqDTO reqDTO) {
        String userId = getCurrentUserId();
        BizVocabLibraryDO library = getLibrary(libraryId, userId);

        // 校验文章存在、属于当前用户且处于正常状态。
        BizArticlesDO article = bizArticlesMapper.selectOne(new LambdaQueryWrapper<BizArticlesDO>()
                .eq(BizArticlesDO::getId, reqDTO.getArticleId())
                .eq(BizArticlesDO::getUserId, userId)
                .eq(BizArticlesDO::getStatus, ArticleConstant.STATUS_NORMAL));
        if (article == null) {
            throw new ClientException(BaseErrorCode.ARTICLE_NOT_FOUND);
        }

        // 同时约束文章、文章词汇和当前用户，防止跨文章或跨用户添加。
        RelArticleVocabDO articleVocab = relArticleVocabMapper.selectOne(new LambdaQueryWrapper<RelArticleVocabDO>()
                .eq(RelArticleVocabDO::getId, reqDTO.getArticleVocabId())
                .eq(RelArticleVocabDO::getArticleId, reqDTO.getArticleId())
                .eq(RelArticleVocabDO::getUserId, userId));
        if (articleVocab == null) {
            throw new ClientException(BaseErrorCode.VOCAB_SOURCE_NOT_FOUND);
        }
        if (!library.getLanguageCode().equalsIgnoreCase(articleVocab.getLanguageCode())) {
            throw new ClientException(BaseErrorCode.VOCAB_LIBRARY_LANGUAGE_MISMATCH);
        }

        RelVocabLibraryWordDO relation = relVocabLibraryWordMapper.selectOne(new LambdaQueryWrapper<RelVocabLibraryWordDO>()
                .eq(RelVocabLibraryWordDO::getLibraryId, libraryId)
                .eq(RelVocabLibraryWordDO::getUserId, userId)
                .eq(RelVocabLibraryWordDO::getWordId, articleVocab.getWordId())
                .eq(RelVocabLibraryWordDO::getLanguageCode, articleVocab.getLanguageCode()));
        if (relation == null) {
            relation = RelVocabLibraryWordDO.builder()
                    .libraryId(libraryId)
                    .userId(userId)
                    .wordId(articleVocab.getWordId())
                    .languageCode(articleVocab.getLanguageCode())
                    .status(VocabConstant.STATUS_NORMAL)
                    .build();
            relVocabLibraryWordMapper.insert(relation);
        } else if (Integer.valueOf(VocabConstant.STATUS_DELETED).equals(relation.getStatus())) {
            // 已软删除的相同词条直接恢复，保留原关系记录。
            relation.setStatus(VocabConstant.STATUS_NORMAL);
            relation.setDeletedAt(null);
            relVocabLibraryWordMapper.updateById(relation);
        } else {
            throw new ClientException(BaseErrorCode.VOCAB_LIBRARY_WORD_EXIST);
        }
        restoreProgress(userId, articleVocab.getWordId(), articleVocab.getLanguageCode());
        invalidateAfterCommit(userId);
    }

    /**
     * 获取指定词汇库中的词条列表
     *
     * @param libraryId 词汇库ID
     * @param keyword 单词关键词
     * @param level 词汇等级
     * @return 词汇库词条列表
     */
    @Override
    public List<VocabLibraryWordRespDTO> listLibraryWords(String libraryId, String keyword, String level) {
        String userId = getCurrentUserId();
        BizVocabLibraryDO library = getReadableLibrary(libraryId, userId);
        String normalizedKeyword = keyword == null ? "" : keyword.trim().toLowerCase(Locale.ROOT);
        String normalizedLevel = normalizeLevel(library.getLanguageCode(), level);
        long cacheVersion = vocabQueryCache.getUserVersion(userId);
        List<VocabLibraryWordRespDTO> cached = vocabQueryCache.getLibraryWords(userId, cacheVersion, libraryId);
        if (cached != null) {
            return filterLibraryWords(cached, normalizedKeyword, library.getLanguageCode(), normalizedLevel);
        }
        List<RelVocabLibraryWordDO> relations = relVocabLibraryWordMapper.selectList(new LambdaQueryWrapper<RelVocabLibraryWordDO>()
                .eq(RelVocabLibraryWordDO::getLibraryId, libraryId)
                .eq(RelVocabLibraryWordDO::getUserId, userId)
                .eq(RelVocabLibraryWordDO::getStatus, VocabConstant.STATUS_NORMAL)
                .orderByAsc(RelVocabLibraryWordDO::getCreatedAt));
        if (relations.isEmpty()) {
            vocabQueryCache.putLibraryWords(userId, cacheVersion, libraryId, List.of());
            return List.of();
        }
        Set<Long> wordIds = relations.stream().map(RelVocabLibraryWordDO::getWordId).collect(Collectors.toSet());
        Set<String> levelWords = loadLevelWords(library.getLanguageCode(), normalizedLevel);
        Map<Long, VocabWordCacheEntry> wordDetails = vocabWordCacheLoader.loadWords(library.getLanguageCode(), wordIds);
        Map<Long, VocabWordLevelCacheEntry> wordLevels = vocabWordCacheLoader.loadLevels(
                userId, library.getLanguageCode(), wordIds, wordDetails);
        List<VocabLibraryWordRespDTO> words = relations.stream()
                .map(relation -> toWordResp(relation, library.getLanguageCode(), wordDetails.get(relation.getWordId()),
                        wordLevels.get(relation.getWordId())))
                .toList();
        vocabQueryCache.putLibraryWords(userId, cacheVersion, libraryId, words);
        return filterLibraryWords(words, normalizedKeyword, levelWords);
    }

    /**
     * 获取指定词汇库的学习统计
     *
     * @param libraryId 词汇库ID
     * @return 词汇库学习统计
     */
    @Override
    public VocabLibraryStatisticsRespDTO getLibraryStatistics(String libraryId) {
        String userId = getCurrentUserId();
        BizVocabLibraryDO library = getReadableLibrary(libraryId, userId);
        long cacheVersion = vocabQueryCache.getUserVersion(userId);
        VocabLibraryStatisticsRespDTO cached = vocabQueryCache.getStatistics(userId, cacheVersion, libraryId);
        if (cached != null) {
            return cached;
        }
        List<RelVocabLibraryWordDO> relations = relVocabLibraryWordMapper.selectList(
                new LambdaQueryWrapper<RelVocabLibraryWordDO>()
                        .eq(RelVocabLibraryWordDO::getLibraryId, libraryId)
                        .eq(RelVocabLibraryWordDO::getUserId, userId)
                        .eq(RelVocabLibraryWordDO::getLanguageCode, library.getLanguageCode())
                        .eq(RelVocabLibraryWordDO::getStatus, VocabConstant.STATUS_NORMAL));
        if (relations.isEmpty()) {
            VocabLibraryStatisticsRespDTO result = statistics(libraryId, 0, 0, 0, 0, 0);
            vocabQueryCache.putStatistics(userId, cacheVersion, libraryId, result);
            return result;
        }
        List<RelUserWordProgressDO> progresses = relUserWordProgressMapper.selectList(
                new LambdaQueryWrapper<RelUserWordProgressDO>()
                        .eq(RelUserWordProgressDO::getUserId, userId)
                        .eq(RelUserWordProgressDO::getLanguageCode, library.getLanguageCode())
                        .eq(RelUserWordProgressDO::getLibraryStatus, VocabConstant.STATUS_NORMAL)
                        .in(RelUserWordProgressDO::getWordId,
                                relations.stream().map(RelVocabLibraryWordDO::getWordId).distinct().toList()));
        Date now = new Date();
        VocabLibraryStatisticsRespDTO result = statistics(libraryId,
                progresses.size(),
                progresses.stream().filter(progress -> progress.getStatus() == VocabConstant.WORD_STATUS_NEW).count(),
                progresses.stream().filter(progress -> progress.getStatus() == VocabConstant.WORD_STATUS_LEARNING).count(),
                progresses.stream().filter(progress -> progress.getStatus() == VocabConstant.WORD_STATUS_MASTERED).count(),
                progresses.stream().filter(progress -> progress.getNextReviewAt() == null
                        || !progress.getNextReviewAt().after(now)).count());
        vocabQueryCache.putStatistics(userId, cacheVersion, libraryId, result);
        return result;
    }

    /**
     * 从指定词汇库中删除词条
     *
     * @param libraryId 词汇库ID
     * @param wordId 词条ID
     * @param languageCode 语言标识
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteLibraryWord(String libraryId, Long wordId, String languageCode) {
        String userId = getCurrentUserId();
        BizVocabLibraryDO library = getLibrary(libraryId, userId);
        String language = normalizeLanguage(languageCode);
        if (!library.getLanguageCode().equals(language)) {
            throw new ClientException(BaseErrorCode.VOCAB_LIBRARY_LANGUAGE_MISMATCH);
        }
        RelVocabLibraryWordDO relation = relVocabLibraryWordMapper.selectOne(new LambdaQueryWrapper<RelVocabLibraryWordDO>()
                .eq(RelVocabLibraryWordDO::getLibraryId, libraryId)
                .eq(RelVocabLibraryWordDO::getUserId, userId)
                .eq(RelVocabLibraryWordDO::getWordId, wordId)
                .eq(RelVocabLibraryWordDO::getLanguageCode, language)
                .eq(RelVocabLibraryWordDO::getStatus, VocabConstant.STATUS_NORMAL));
        if (relation == null) {
            throw new ClientException(BaseErrorCode.VOCAB_LIBRARY_WORD_NOT_FOUND);
        }
        relation.setStatus(VocabConstant.STATUS_DELETED);
        relation.setDeletedAt(new Date());
        relVocabLibraryWordMapper.updateById(relation);
        invalidateAfterCommit(userId);
    }

    /**
     * 构建词汇库学习统计响应参数
     *
     * @param libraryId 词汇库ID
     * @param total 单词总数
     * @param newCount 未学习单词数
     * @param learning 学习中单词数
     * @param mastered 已掌握单词数
     * @param due 待复习单词数
     * @return 词汇库学习统计响应参数
     */
    private VocabLibraryStatisticsRespDTO statistics(String libraryId, long total, long newCount,
                                                      long learning, long mastered, long due) {
        return VocabLibraryStatisticsRespDTO.builder()
                .libraryId(libraryId)
                .totalCount(total)
                .newCount(newCount)
                .learningCount(learning)
                .masteredCount(mastered)
                .dueCount(due)
                .build();
    }

    private void restoreProgress(String userId, Long wordId, String languageCode) {
        RelUserWordProgressDO progress = relUserWordProgressMapper.selectOne(new LambdaQueryWrapper<RelUserWordProgressDO>()
                .eq(RelUserWordProgressDO::getUserId, userId)
                .eq(RelUserWordProgressDO::getWordId, wordId)
                .eq(RelUserWordProgressDO::getLanguageCode, languageCode));
        if (progress == null) {
            relUserWordProgressMapper.insert(RelUserWordProgressDO.builder()
                    .userId(userId)
                    .wordId(wordId)
                    .languageCode(languageCode)
                    .status(VocabConstant.WORD_STATUS_NEW)
                    .libraryStatus(VocabConstant.STATUS_NORMAL)
                    .reviewCount(0)
                    .easinessFactor(VocabConstant.DEFAULT_EASINESS_FACTOR)
                    .intervalDays(0)
                    .build());
        // 恢复曾从词汇库移除的学习进度，避免重复创建记录。
        } else if (Integer.valueOf(VocabConstant.STATUS_DELETED).equals(progress.getLibraryStatus())) {
            progress.setLibraryStatus(VocabConstant.STATUS_NORMAL);
            progress.setDeletedAt(null);
            relUserWordProgressMapper.updateById(progress);
        }
    }

    private BizVocabLibraryDO getLibrary(String libraryId, String userId) {
        // 将用户条件并入查询，防止跨用户访问词汇库。
        BizVocabLibraryDO library = bizVocabLibraryMapper.selectOne(new LambdaQueryWrapper<BizVocabLibraryDO>()
                .eq(BizVocabLibraryDO::getId, libraryId)
                .eq(BizVocabLibraryDO::getUserId, userId)
                .eq(BizVocabLibraryDO::getStatus, VocabConstant.STATUS_NORMAL));
        if (library == null) {
            throw new ClientException(BaseErrorCode.VOCAB_LIBRARY_NOT_FOUND);
        }
        return library;
    }

    private BizVocabLibraryDO getReadableLibrary(String libraryId, String userId) {
        long cacheVersion = vocabQueryCache.getUserVersion(userId);
        VocabLibraryRespDTO cachedLibrary = vocabQueryCache.getLibrary(userId, cacheVersion, libraryId);
        if (cachedLibrary != null) {
            return toLibraryDO(cachedLibrary, userId);
        }
        List<VocabLibraryRespDTO> cachedLibraries = vocabQueryCache.getLibraries(userId, cacheVersion);
        if (cachedLibraries != null) {
            return cachedLibraries.stream()
                    .filter(library -> libraryId.equals(library.getLibraryId()))
                    .findFirst()
                    .map(library -> {
                        vocabQueryCache.putLibrary(userId, cacheVersion, library);
                        return toLibraryDO(library, userId);
                    })
                    .orElseThrow(() -> new ClientException(BaseErrorCode.VOCAB_LIBRARY_NOT_FOUND));
        }
        BizVocabLibraryDO library = getLibrary(libraryId, userId);
        vocabQueryCache.putLibrary(userId, cacheVersion, toLibraryResp(library, 0L));
        return library;
    }

    private BizVocabLibraryDO toLibraryDO(VocabLibraryRespDTO library, String userId) {
        return BizVocabLibraryDO.builder()
                .id(library.getLibraryId())
                .userId(userId)
                .name(library.getName())
                .languageCode(library.getLanguageCode())
                .description(library.getDescription())
                .status(VocabConstant.STATUS_NORMAL)
                .createdAt(library.getCreatedAt())
                .updatedAt(library.getUpdatedAt())
                .build();
    }

    private VocabLibraryRespDTO toLibraryResp(BizVocabLibraryDO library, long wordCount) {
        return VocabLibraryRespDTO.builder()
                .libraryId(library.getId())
                .name(library.getName())
                .languageCode(library.getLanguageCode())
                .description(library.getDescription())
                .wordCount(wordCount)
                .createdAt(library.getCreatedAt())
                .updatedAt(library.getUpdatedAt())
                .build();
    }

    private VocabLibraryWordRespDTO toWordResp(RelVocabLibraryWordDO relation, String languageCode,
                                               VocabWordCacheEntry word, VocabWordLevelCacheEntry level) {
        VocabLibraryWordRespDTO.VocabLibraryWordRespDTOBuilder builder = VocabLibraryWordRespDTO.builder()
                .libraryWordId(relation.getId())
                .wordId(relation.getWordId())
                .languageCode(languageCode)
                .level(level == null ? null : level.level())
                .addedAt(relation.getCreatedAt());
        if (word != null) {
            builder.word(word.word())
                    .kana(word.kana())
                    .us(word.us())
                    .uk(word.uk())
                    .translations(word.translations());
        }
        return builder.build();
    }

    private List<VocabLibraryWordRespDTO> filterLibraryWords(List<VocabLibraryWordRespDTO> words,
                                                              String keyword, String languageCode, String level) {
        Set<String> levelWords = loadLevelWords(languageCode, level);
        return filterLibraryWords(words, keyword, levelWords);
    }

    private List<VocabLibraryWordRespDTO> filterLibraryWords(List<VocabLibraryWordRespDTO> words,
                                                              String keyword, Set<String> levelWords) {
        return words.stream()
                .filter(word -> matchesWord(word, keyword, levelWords))
                .toList();
    }

    private Map<String, Long> loadLibraryWordCounts(String userId) {
        Map<String, Long> result = new HashMap<>();
        List<Map<String, Object>> rows = relVocabLibraryWordMapper.selectMaps(new QueryWrapper<RelVocabLibraryWordDO>()
                .select("library_id", "COUNT(*) AS word_count")
                .eq("user_id", userId)
                .eq("status", VocabConstant.STATUS_NORMAL)
                .groupBy("library_id"));
        for (Map<String, Object> row : rows) {
            Object libraryId = row.getOrDefault("library_id", row.get("libraryId"));
            Object count = row.getOrDefault("word_count", row.get("wordCount"));
            if (libraryId != null && count instanceof Number number) {
                result.put(String.valueOf(libraryId), number.longValue());
            }
        }
        return result;
    }

    private void invalidateAfterCommit(String userId) {
        afterCommitExecutor.execute(() -> vocabQueryCache.invalidateUser(userId));
    }

    private String normalizeLevel(String languageCode, String level) {
        if (level == null || level.isBlank()) {
            return null;
        }
        String normalizedLevel = level.trim().toUpperCase(Locale.ROOT);
        Set<String> supportedLevels = "ja".equals(languageCode)
                ? VocabConstant.JAPANESE_LEVELS
                : VocabConstant.ENGLISH_LEVELS;
        if (!supportedLevels.contains(normalizedLevel)) {
            throw new ClientException(BaseErrorCode.VOCAB_LEVEL_NOT_SUPPORTED);
        }
        return normalizedLevel;
    }

    private Set<String> loadLevelWords(String languageCode, String level) {
        if (level == null) {
            return null;
        }
        try {
            return articleVocabAnalyzer.loadLevelWords(languageCode, level);
        } catch (ClientException e) {
            throw e;
        } catch (Exception e) {
            throw new ClientException(BaseErrorCode.VOCAB_NOT_FOUND);
        }
    }

    private boolean matchesWord(VocabLibraryWordRespDTO word, String keyword, Set<String> levelWords) {
        boolean matchesLevel = levelWords == null || levelWords.contains(normalizeWord(word.getWord()));
        if (!matchesLevel) {
            return false;
        }
        if (keyword.isEmpty()) {
            return true;
        }
        String searchable = String.join(" ",
                valueOrEmpty(word.getWord()), valueOrEmpty(word.getKana()), valueOrEmpty(word.getUs()),
                valueOrEmpty(word.getUk()), valueOrEmpty(word.getTranslations()), valueOrEmpty(word.getPhrases()))
                .toLowerCase(Locale.ROOT);
        return searchable.contains(keyword);
    }

    private String normalizeWord(String word) {
        return valueOrEmpty(word).trim().toLowerCase(Locale.ROOT);
    }

    private String valueOrEmpty(String value) {
        return value == null ? "" : value;
    }

    private String normalizeLanguage(String languageCode) {
        String language = languageCode == null ? "" : languageCode.trim().toLowerCase(Locale.ROOT);
        if (!VocabConstant.SUPPORTED_LANGUAGES.contains(language)) {
            throw new ClientException(BaseErrorCode.VOCAB_LANGUAGE_NOT_SUPPORTED);
        }
        return language;
    }

    private String getCurrentUserId() {
        String userId = UserContext.getCurrentUserId();
        if (userId == null) {
            throw new ClientException(BaseErrorCode.USER_NOT_LOGIN);
        }
        return userId;
    }
}
