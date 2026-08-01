package com.btea.lexiflow.learning.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.btea.lexiflow.common.context.UserContext;
import com.btea.lexiflow.common.convention.errorcode.BaseErrorCode;
import com.btea.lexiflow.common.convention.exception.ClientException;
import com.btea.lexiflow.common.cache.AfterCommitExecutor;
import com.btea.lexiflow.learning.dao.entity.RelUserWordProgressDO;
import com.btea.lexiflow.learning.dao.mapper.RelUserWordProgressMapper;
import com.btea.lexiflow.learning.cache.LearningReviewQueueCache;
import com.btea.lexiflow.learning.dto.req.WordReviewReqDTO;
import com.btea.lexiflow.learning.dto.resp.DueWordRespDTO;
import com.btea.lexiflow.learning.service.LearningService;
import com.btea.lexiflow.vocab.constant.VocabConstant;
import com.btea.lexiflow.vocab.cache.VocabQueryCache;
import com.btea.lexiflow.vocab.cache.VocabWordCacheEntry;
import com.btea.lexiflow.vocab.cache.VocabWordCacheLoader;
import com.btea.lexiflow.vocab.cache.VocabWordLevelCacheEntry;
import com.btea.lexiflow.vocab.dao.entity.BizVocabLibraryDO;
import com.btea.lexiflow.vocab.dao.entity.RelVocabLibraryWordDO;
import com.btea.lexiflow.vocab.dao.mapper.BizVocabLibraryMapper;
import com.btea.lexiflow.vocab.dao.mapper.RelVocabLibraryWordMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @Author: TwentyfiveBTea
 * @Date: 2026/7/12
 * @Description: 单词学习服务实现类
 */
@Service
@RequiredArgsConstructor
public class LearningServiceImpl implements LearningService {

    private final RelUserWordProgressMapper progressMapper;
    private final LearningReviewQueueCache reviewQueueCache;
    private final RelVocabLibraryWordMapper libraryWordMapper;
    private final BizVocabLibraryMapper libraryMapper;
    private final VocabQueryCache vocabQueryCache;
    private final VocabWordCacheLoader vocabWordCacheLoader;
    private final AfterCommitExecutor afterCommitExecutor;

    /**
     * 获取当前用户指定词汇库的待复习单词列表
     *
     * @param libraryId 词汇库ID
     * @return 待复习单词列表
     */
    @Override
    public List<DueWordRespDTO> listDueWords(String libraryId) {
        String userId = getCurrentUserId();
        // 校验词汇库存在且属于当前用户，防止越权查询学习进度
        BizVocabLibraryDO library = getLibrary(libraryId, userId);
        List<RelVocabLibraryWordDO> relations = libraryWordMapper.selectList(
                new LambdaQueryWrapper<RelVocabLibraryWordDO>()
                        .eq(RelVocabLibraryWordDO::getLibraryId, libraryId)
                        .eq(RelVocabLibraryWordDO::getUserId, userId)
                        .eq(RelVocabLibraryWordDO::getLanguageCode, library.getLanguageCode())
                        .eq(RelVocabLibraryWordDO::getStatus, VocabConstant.STATUS_NORMAL));
        if (relations.isEmpty()) {
            return List.of();
        }
        Map<String, RelVocabLibraryWordDO> relationsByWord = relations.stream()
                .collect(java.util.stream.Collectors.toMap(
                        relation -> relation.getLanguageCode() + ":" + relation.getWordId(),
                        relation -> relation,
                        (first, ignored) -> first,
                        LinkedHashMap::new));
        List<RelUserWordProgressDO> progresses = progressMapper.selectList(new LambdaQueryWrapper<RelUserWordProgressDO>()
                        .eq(RelUserWordProgressDO::getUserId, userId)
                        .eq(RelUserWordProgressDO::getLanguageCode, library.getLanguageCode())
                        .eq(RelUserWordProgressDO::getLibraryStatus, VocabConstant.STATUS_NORMAL)
                        .in(RelUserWordProgressDO::getWordId,
                                relations.stream().map(RelVocabLibraryWordDO::getWordId).distinct().toList())
                        .and(q -> q.isNull(RelUserWordProgressDO::getNextReviewAt)
                                .or()
                                .le(RelUserWordProgressDO::getNextReviewAt, new Date()))
                        .orderByAsc(RelUserWordProgressDO::getNextReviewAt));
        return toDueResponses(userId, progresses, relationsByWord);
    }

    @Override
    public List<DueWordRespDTO> listDueWords() {
        String userId = getCurrentUserId();
        List<RelVocabLibraryWordDO> relations = libraryWordMapper.selectList(
                new LambdaQueryWrapper<RelVocabLibraryWordDO>()
                        .eq(RelVocabLibraryWordDO::getUserId, userId)
                        .eq(RelVocabLibraryWordDO::getStatus, VocabConstant.STATUS_NORMAL));
        if (relations.isEmpty()) {
            return List.of();
        }
        Map<String, RelVocabLibraryWordDO> relationsByWord = relations.stream()
                .collect(java.util.stream.Collectors.toMap(
                        relation -> relation.getLanguageCode() + ":" + relation.getWordId(),
                        relation -> relation,
                        (first, ignored) -> first,
                        LinkedHashMap::new));
        List<RelUserWordProgressDO> progresses = progressMapper.selectList(new LambdaQueryWrapper<RelUserWordProgressDO>()
                        .eq(RelUserWordProgressDO::getUserId, userId)
                        .eq(RelUserWordProgressDO::getLibraryStatus, VocabConstant.STATUS_NORMAL)
                        .in(RelUserWordProgressDO::getWordId,
                                relations.stream().map(RelVocabLibraryWordDO::getWordId).distinct().toList())
                        .and(q -> q.isNull(RelUserWordProgressDO::getNextReviewAt)
                                .or()
                                .le(RelUserWordProgressDO::getNextReviewAt, new Date()))
                        .orderByAsc(RelUserWordProgressDO::getNextReviewAt));
        return toDueResponses(userId, progresses, relationsByWord);
    }

    /**
     * 查询当前用户尚未完成的复习会话队列。
     *
     * @return 当前复习会话队列，无会话时返回空列表
     */
    @Override
    public List<DueWordRespDTO> listReviewQueue() {
        String userId = getCurrentUserId();
        return toReviewQueueResponses(userId, reviewQueueCache.getQueue(userId));
    }

    /**
     * 获取或创建当前用户的复习会话队列。
     *
     * @return 当前复习会话队列
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<DueWordRespDTO> startReviewSession() {
        String userId = getCurrentUserId();
        List<String> queue = reviewQueueCache.getQueue(userId);
        if (queue.isEmpty()) {
            List<DueWordRespDTO> dueWords = listDueWords();
            queue = reviewQueueCache.initializeIfAbsent(userId, dueWords.stream()
                    .map(word -> queueEntry(word.getLanguageCode(), word.getWordId()))
                    .toList());
        }
        return toReviewQueueResponses(userId, queue);
    }

    /**
     * 提交单词复习按钮结果并按照SM-2规则更新学习进度

     * @param wordId 单词ID
     * @param reqDTO 复习按钮请求参数
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void reviewWord(Long wordId, WordReviewReqDTO reqDTO) {
        String userId = getCurrentUserId();
        updateWordProgress(userId, wordId, reqDTO);
        afterCommitExecutor.execute(() -> vocabQueryCache.invalidateUser(userId));
    }

    /**
     * 提交当前复习会话队列的单词结果，并更新队列顺序。
     *
     * @param wordId 单词ID
     * @param reqDTO 复习结果请求参数
     * @return 更新后的复习会话队列
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<DueWordRespDTO> reviewSessionWord(Long wordId, WordReviewReqDTO reqDTO) {
        String userId = getCurrentUserId();
        String language = normalizeLanguage(reqDTO.getLanguageCode());
        List<String> queue = reviewQueueCache.getQueue(userId);
        String entry = queueEntry(language, wordId);
        if (queue.isEmpty() || !entry.equals(queue.get(0))) {
            throw new ClientException(BaseErrorCode.WORD_REVIEW_QUEUE_CONFLICT);
        }
        int quality = updateWordProgress(userId, wordId, reqDTO);
        if (!reviewQueueCache.advanceHead(userId, entry, quality < 5)) {
            throw new ClientException(BaseErrorCode.WORD_REVIEW_QUEUE_CONFLICT);
        }
        afterCommitExecutor.execute(() -> vocabQueryCache.invalidateUser(userId));
        return toReviewQueueResponses(userId, reviewQueueCache.getQueue(userId));
    }

    /**
     * 更新用户单词学习进度。
     *
     * @param userId 用户ID
     * @param wordId 单词ID
     * @param reqDTO 复习结果请求参数
     * @return SM-2质量评分
     */
    private int updateWordProgress(String userId, Long wordId, WordReviewReqDTO reqDTO) {
        String language = normalizeLanguage(reqDTO.getLanguageCode());
        // 在事务内锁定当前用户的进度行，防止并发复习相互覆盖
        RelUserWordProgressDO progress = progressMapper.selectForUpdate(userId, wordId, language);
        if (progress == null) {
            throw new ClientException(BaseErrorCode.WORD_PROGRESS_NOT_FOUND);
        }
        int quality = resolveQuality(reqDTO.getRating());
        Date now = new Date();
        BigDecimal oldEf = progress.getEasinessFactor() == null ? VocabConstant.DEFAULT_EASINESS_FACTOR : progress.getEasinessFactor();
        // 按照SM-2评分公式计算难度因子与下一次复习间隔
        BigDecimal newEf = calculateEf(oldEf, quality);
        int oldReviewCount = progress.getReviewCount() == null ? 0 : progress.getReviewCount();
        int intervalDays;
        if (quality < 3) {
            intervalDays = 1;
        } else if (oldReviewCount == 0) {
            intervalDays = 1;
        } else if (oldReviewCount == 1) {
            intervalDays = 6;
        } else {
            intervalDays = Math.max(1, (int) Math.round(
                    (progress.getIntervalDays() == null ? 1 : progress.getIntervalDays()) * newEf.doubleValue()));
        }
        int reviewCount = oldReviewCount + 1;
        progress.setReviewCount(reviewCount);
        progress.setLastReviewedAt(now);
        progress.setNextReviewAt(new Date(now.getTime() + intervalDays * 24L * 60L * 60L * 1000L));
        progress.setEasinessFactor(newEf);
        progress.setIntervalDays(intervalDays);
        progress.setStatus(quality == 5 && reviewCount >= VocabConstant.MASTERED_REVIEW_COUNT
                ? VocabConstant.WORD_STATUS_MASTERED : VocabConstant.WORD_STATUS_LEARNING);
        progressMapper.updateById(progress);
        return quality;
    }

    /**
     * 将当前复习队列转换为包含词汇信息的响应列表。
     *
     * @param userId 用户ID
     * @param queue 当前复习队列
     * @return 待复习单词响应列表
     */
    private List<DueWordRespDTO> toReviewQueueResponses(String userId, List<String> queue) {
        if (queue.isEmpty()) {
            return List.of();
        }
        List<QueueWord> queueWords = queue.stream().map(this::parseQueueEntry).filter(Objects::nonNull).toList();
        if (queueWords.isEmpty()) {
            return List.of();
        }
        List<Long> wordIds = queueWords.stream().map(QueueWord::wordId).distinct().toList();
        List<RelVocabLibraryWordDO> relations = libraryWordMapper.selectList(
                new LambdaQueryWrapper<RelVocabLibraryWordDO>()
                        .eq(RelVocabLibraryWordDO::getUserId, userId)
                        .eq(RelVocabLibraryWordDO::getStatus, VocabConstant.STATUS_NORMAL)
                        .in(RelVocabLibraryWordDO::getWordId, wordIds));
        Map<String, RelVocabLibraryWordDO> relationsByWord = relations.stream()
                .collect(Collectors.toMap(
                        relation -> relation.getLanguageCode() + ":" + relation.getWordId(),
                        relation -> relation,
                        (first, ignored) -> first,
                        LinkedHashMap::new));
        Map<String, RelUserWordProgressDO> progressesByWord = progressMapper.selectList(
                        new LambdaQueryWrapper<RelUserWordProgressDO>()
                                .eq(RelUserWordProgressDO::getUserId, userId)
                                .eq(RelUserWordProgressDO::getLibraryStatus, VocabConstant.STATUS_NORMAL)
                                .in(RelUserWordProgressDO::getWordId, wordIds))
                .stream()
                .collect(Collectors.toMap(
                        progress -> progress.getLanguageCode() + ":" + progress.getWordId(),
                        progress -> progress));
        List<QueueWord> validQueue = queueWords.stream()
                .filter(item -> {
                    String key = item.languageCode() + ":" + item.wordId();
                    return relationsByWord.containsKey(key) && progressesByWord.containsKey(key);
                })
                .toList();
        List<RelUserWordProgressDO> orderedProgresses = validQueue.stream()
                .map(item -> progressesByWord.get(item.languageCode() + ":" + item.wordId()))
                .toList();
        return toDueResponses(userId, orderedProgresses, relationsByWord);
    }

    /**
     * 生成Redis复习队列中的词汇标识。
     *
     * @param languageCode 语言标识
     * @param wordId 单词ID
     * @return 词汇标识
     */
    private String queueEntry(String languageCode, Long wordId) {
        return languageCode + ":" + wordId;
    }

    /**
     * 解析Redis复习队列中的词汇标识。
     *
     * @param entry 词汇标识
     * @return 词汇队列元素，格式不合法时返回null
     */
    private QueueWord parseQueueEntry(String entry) {
        if (entry == null) {
            return null;
        }
        int separator = entry.indexOf(':');
        if (separator <= 0 || separator >= entry.length() - 1) {
            return null;
        }
        try {
            return new QueueWord(normalizeLanguage(entry.substring(0, separator)),
                    Long.parseLong(entry.substring(separator + 1)));
        } catch (RuntimeException ignored) {
            return null;
        }
    }

    private record QueueWord(String languageCode, Long wordId) {
    }

    /**
     * 将复习按钮结果转换为 SM-2 质量评分
     *
     * @param rating 复习按钮结果
     * @return SM-2 质量评分
     */
    private int resolveQuality(String rating) {
        return switch (rating.trim().toUpperCase(Locale.ROOT)) {
            case "UNKNOWN" -> 0;
            case "VAGUE" -> 3;
            case "KNOWN" -> 5;
            default -> throw new ClientException(BaseErrorCode.WORD_REVIEW_QUALITY_INVALID);
        };
    }

    /**
     * 根据SM-2评分公式计算新的难度因子
     *
     * @param ef      原难度因子
     * @param quality 复习评分
     * @return 新难度因子
     */
    private BigDecimal calculateEf(BigDecimal ef, int quality) {
        BigDecimal q = BigDecimal.valueOf(quality);
        BigDecimal delta = BigDecimal.valueOf(0.1).subtract(BigDecimal.valueOf(5).subtract(q)
                .multiply(BigDecimal.valueOf(0.08).add(BigDecimal.valueOf(5).subtract(q).multiply(BigDecimal.valueOf(0.02)))));
        BigDecimal result = ef.add(delta);
        return result.max(VocabConstant.MIN_EASINESS_FACTOR).setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * 转换待复习单词响应参数
     *
     * @param progress 用户单词学习进度
     * @return 待复习单词响应参数
     */
    private List<DueWordRespDTO> toDueResponses(String userId, List<RelUserWordProgressDO> progresses,
                                                 Map<String, RelVocabLibraryWordDO> relationsByWord) {
        Map<String, Set<Long>> wordIdsByLanguage = progresses.stream()
                .collect(Collectors.groupingBy(RelUserWordProgressDO::getLanguageCode,
                        Collectors.mapping(RelUserWordProgressDO::getWordId, Collectors.toSet())));
        Map<String, Map<Long, VocabWordCacheEntry>> wordsByLanguage = new HashMap<>();
        Map<String, Map<Long, VocabWordLevelCacheEntry>> levelsByLanguage = new HashMap<>();
        wordIdsByLanguage.forEach((languageCode, wordIds) -> {
            Map<Long, VocabWordCacheEntry> words = vocabWordCacheLoader.loadWords(languageCode, wordIds);
            wordsByLanguage.put(languageCode, words);
            levelsByLanguage.put(languageCode,
                    vocabWordCacheLoader.loadLevels(userId, languageCode, wordIds, words));
        });
        return progresses.stream()
                .filter(progress -> relationsByWord.containsKey(progress.getLanguageCode() + ":" + progress.getWordId()))
                .map(progress -> toDueResp(
                        progress,
                        relationsByWord.get(progress.getLanguageCode() + ":" + progress.getWordId()),
                        wordsByLanguage.getOrDefault(progress.getLanguageCode(), Map.of()).get(progress.getWordId()),
                        levelsByLanguage.getOrDefault(progress.getLanguageCode(), Map.of()).get(progress.getWordId())))
                .toList();
    }

    private DueWordRespDTO toDueResp(RelUserWordProgressDO progress, RelVocabLibraryWordDO relation,
                                     VocabWordCacheEntry word, VocabWordLevelCacheEntry level) {
        DueWordRespDTO.DueWordRespDTOBuilder builder = DueWordRespDTO.builder()
                .libraryWordId(relation == null ? null : relation.getId())
                .wordId(progress.getWordId())
                .languageCode(progress.getLanguageCode())
                .level(level == null ? null : level.level());
        if (word != null) {
            builder.word(word.word())
                    .kana(word.kana())
                    .us(word.us())
                    .uk(word.uk())
                    .translations(word.translations());
        }
        return builder.build();
    }

    /**
     * 获取当前用户拥有的有效词汇库
     *
     * @param libraryId 词汇库ID
     * @param userId    用户ID
     * @return 词汇库实体
     */
    private BizVocabLibraryDO getLibrary(String libraryId, String userId) {
        BizVocabLibraryDO library = libraryMapper.selectOne(new LambdaQueryWrapper<BizVocabLibraryDO>()
                .eq(BizVocabLibraryDO::getId, libraryId)
                .eq(BizVocabLibraryDO::getUserId, userId)
                .eq(BizVocabLibraryDO::getStatus, VocabConstant.STATUS_NORMAL));
        if (library == null) {
            throw new ClientException(BaseErrorCode.VOCAB_LIBRARY_NOT_FOUND);
        }
        return library;
    }

    /**
     * 标准化并校验语言标识
     *
     * @param languageCode 语言标识
     * @return 标准化语言标识
     */
    private String normalizeLanguage(String languageCode) {
        String language = languageCode == null ? "" : languageCode.trim().toLowerCase(Locale.ROOT);
        if (!VocabConstant.SUPPORTED_LANGUAGES.contains(language)) {
            throw new ClientException(BaseErrorCode.VOCAB_LANGUAGE_NOT_SUPPORTED);
        }
        return language;
    }

    /**
     * 获取当前登录用户ID
     *
     * @return 当前登录用户ID
     */
    private String getCurrentUserId() {
        String userId = UserContext.getCurrentUserId();
        if (userId == null) {
            throw new ClientException(BaseErrorCode.USER_NOT_LOGIN);
        }
        return userId;
    }
}
