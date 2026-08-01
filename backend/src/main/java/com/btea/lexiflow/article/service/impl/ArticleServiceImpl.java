package com.btea.lexiflow.article.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.btea.lexiflow.article.dao.entity.BizArticlesDO;
import com.btea.lexiflow.article.dao.entity.RelArticleVocabDO;
import com.btea.lexiflow.article.dao.entity.RelArticleVocabOccurrenceDO;
import com.btea.lexiflow.article.dao.mapper.BizArticlesMapper;
import com.btea.lexiflow.article.dao.mapper.RelArticleVocabMapper;
import com.btea.lexiflow.article.dao.mapper.RelArticleVocabOccurrenceMapper;
import com.btea.lexiflow.article.cache.ArticleQueryCache;
import com.btea.lexiflow.article.cache.ArticleUploadSession;
import com.btea.lexiflow.article.cache.ArticleUploadSessionCache;
import com.btea.lexiflow.article.dto.req.ArticleAnalyzeReqDTO;
import com.btea.lexiflow.article.dto.req.ArticleUploadInitReqDTO;
import com.btea.lexiflow.article.dto.resp.*;
import com.btea.lexiflow.article.nlp.ArticleVocabAnalyzer;
import com.btea.lexiflow.article.nlp.ArticleVocabMatch;
import com.btea.lexiflow.article.nlp.ArticleVocabOccurrence;
import com.btea.lexiflow.article.nlp.ArticleSourceRangeUtil;
import com.btea.lexiflow.article.nlp.ArticleSourceRangeUtil.SourceSegment;
import com.btea.lexiflow.article.service.ArticleProcessingService;
import com.btea.lexiflow.article.service.ArticleService;
import com.btea.lexiflow.common.context.UserContext;
import com.btea.lexiflow.common.convention.errorcode.BaseErrorCode;
import com.btea.lexiflow.common.convention.exception.ClientException;
import com.btea.lexiflow.infrastructure.s3.S3Util;
import com.btea.lexiflow.pay.model.AiProcessingContext;
import com.btea.lexiflow.pay.service.CreditReservationService;
import com.btea.lexiflow.vocab.cache.VocabQueryCache;
import com.btea.lexiflow.vocab.cache.VocabWordCacheEntry;
import com.btea.lexiflow.vocab.cache.VocabWordCacheLoader;
import com.btea.lexiflow.vocab.constant.VocabConstant;
import com.btea.lexiflow.vocab.dao.entity.BizVocabLibraryDO;
import com.btea.lexiflow.vocab.dao.entity.RelVocabLibraryWordDO;
import com.btea.lexiflow.vocab.dao.mapper.BizVocabLibraryMapper;
import com.btea.lexiflow.vocab.dao.mapper.RelVocabLibraryWordMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.Executor;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static com.btea.lexiflow.article.constant.ArticleConstant.*;

/**
 * @Author: TwentyfiveBTea
 * @Date: 2026/7/4 16:47
 * @Description: 文章服务实现类
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class ArticleServiceImpl implements ArticleService {

    private final BizArticlesMapper bizArticlesMapper;
    private final RelArticleVocabMapper relArticleVocabMapper;
    private final RelArticleVocabOccurrenceMapper relArticleVocabOccurrenceMapper;
    private final S3Util s3Util;
    private final ArticleVocabAnalyzer articleVocabAnalyzer;
    private final ArticleProcessingService articleProcessingService;
    private final CreditReservationService creditReservationService;
    private final ArticleQueryCache articleQueryCache;
    private final ArticleUploadSessionCache articleUploadSessionCache;
    private final VocabQueryCache vocabQueryCache;
    private final VocabWordCacheLoader vocabWordCacheLoader;
    private final BizVocabLibraryMapper vocabLibraryMapper;
    private final RelVocabLibraryWordMapper vocabLibraryWordMapper;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Map<String, Boolean> activeAnalyses = new ConcurrentHashMap<>();

    @Autowired
    @Qualifier("articleTaskExecutor")
    private Executor articleTaskExecutor;

    /**
     * 上传文章
     *
     * @param file 文章文件
     * @return 上传响应参数
     */
    @Override
    public ArticleUploadRespDTO uploadArticle(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ClientException(BaseErrorCode.FILE_NOT_FOUND);
        }

        String userId = getCurrentUserId();
        String articleId = IdWorker.getIdStr();
        String processingNo = IdWorker.getIdStr();
        AiProcessingContext processingContext = new AiProcessingContext(userId, articleId, processingNo);
        String originalFilename = getOriginalFilename(file);
        String fileType = getFileType(originalFilename);
        validateUpload(originalFilename, fileType, file.getSize());
        String contentType = normalizeContentType(file.getContentType());
        String title = getBaseName(originalFilename);
        String filePath = ARTICLE_DIR + userId + "/" + articleId + "_original." + fileType;
        log.info("开始上传文章: userId={}, articleId={}, filename={}, fileType={}, fileSize={}",
                userId, articleId, originalFilename, fileType, file.getSize());

        BizArticlesDO article = buildPendingArticle(
                articleId, userId, title, originalFilename, fileType, contentType, file.getSize(), filePath);

        try {
            creditReservationService.createInitialReservation(processingContext);
            s3Util.uploadFile(file, filePath);
            log.info("文章原文件上传成功: userId={}, articleId={}, filePath={}", userId, articleId, filePath);
            articleProcessingService.processUploadedArticle(article, processingContext);
            log.info("文章异步处理任务提交成功: userId={}, articleId={}", userId, articleId);
            return toUploadResp(articleId, title);
        } catch (ClientException e) {
            creditReservationService.release(processingNo);
            article.setParseStatus(PARSE_STATUS_FAILED);
            bizArticlesMapper.updateById(article);
            throw e;
        } catch (Exception e) {
            creditReservationService.release(processingNo);
            article.setParseStatus(PARSE_STATUS_FAILED);
            bizArticlesMapper.updateById(article);
            log.error("文章上传失败: userId={}, articleId={}", userId, articleId, e);
            throw new ClientException(BaseErrorCode.FILE_UPLOAD_FAILED);
        }
    }

    /**
     * 初始化文章直传会话
     *
     * @param reqDTO 上传文件信息
     * @return 预签名上传信息
     */
    @Override
    public ArticleUploadInitRespDTO initializeArticleUpload(ArticleUploadInitReqDTO reqDTO) {
        String userId = getCurrentUserId();
        String originalFilename = reqDTO.getFilename().trim();
        String fileType = getFileType(originalFilename);
        validateUpload(originalFilename, fileType, reqDTO.getFileSize());
        String contentType = normalizeContentType(reqDTO.getContentType());
        String articleId = IdWorker.getIdStr();
        String objectKey = ARTICLE_UPLOAD_DIR + userId + "/" + articleId + "_original." + fileType;
        articleUploadSessionCache.create(new ArticleUploadSession(
                articleId,
                userId,
                objectKey,
                originalFilename,
                fileType,
                contentType,
                reqDTO.getFileSize(),
                "PENDING"));
        String uploadUrl = s3Util.createPresignedUploadUrl(
                objectKey, contentType, com.btea.lexiflow.article.constant.ArticleRedisConstant.PRESIGNED_UPLOAD_TTL);
        return ArticleUploadInitRespDTO.builder()
                .articleId(articleId)
                .uploadUrl(uploadUrl)
                .contentType(contentType)
                .expiresAt(Date.from(Instant.now().plus(
                        com.btea.lexiflow.article.constant.ArticleRedisConstant.PRESIGNED_UPLOAD_TTL)))
                .build();
    }

    /**
     * 确认文章直传完成并提交异步处理
     *
     * @param articleId 文章ID
     * @return 上传响应参数
     */
    @Override
    public ArticleUploadRespDTO completeArticleUpload(String articleId) {
        String userId = getCurrentUserId();
        ArticleUploadSession session = getUploadSession(articleId, userId);
        if ("COMPLETED".equals(session.status())) {
            return toUploadResp(articleId, getBaseName(session.filename()));
        }
        long actualSize = s3Util.headObject(session.objectKey()).contentLength();
        if (actualSize != session.fileSize() || actualSize <= 0L || actualSize > MAX_UPLOAD_FILE_SIZE) {
            s3Util.deleteFile(session.objectKey());
            articleUploadSessionCache.delete(articleId);
            throw new ClientException(BaseErrorCode.ARTICLE_UPLOAD_INVALID);
        }
        long claimResult = articleUploadSessionCache.claim(articleId);
        if (claimResult == 2L) {
            return toUploadResp(articleId, getBaseName(session.filename()));
        }
        if (claimResult != 1L) {
            throw new ClientException(claimResult == 0L
                    ? BaseErrorCode.ARTICLE_UPLOAD_SESSION_NOT_FOUND
                    : BaseErrorCode.ARTICLE_UPLOAD_CONFLICT);
        }

        String processingNo = IdWorker.getIdStr();
        AiProcessingContext context = new AiProcessingContext(userId, articleId, processingNo);
        String finalObjectKey = ARTICLE_DIR + userId + "/" + articleId + "_original." + session.fileType();
        BizArticlesDO article = buildPendingArticle(
                articleId,
                userId,
                getBaseName(session.filename()),
                session.filename(),
                session.fileType(),
                session.contentType(),
                session.fileSize(),
                finalObjectKey);
        boolean reservationCreated = false;
        boolean objectCopied = false;
        try {
            creditReservationService.createInitialReservation(context);
            reservationCreated = true;
            s3Util.copyFile(session.objectKey(), finalObjectKey);
            objectCopied = true;
            articleUploadSessionCache.markCompleted(articleId);
            articleProcessingService.processUploadedArticle(article, context);
            log.info("文章直传完成并提交异步处理: userId={}, articleId={}, filePath={}",
                    userId, articleId, finalObjectKey);
            s3Util.deleteFile(session.objectKey());
            return toUploadResp(articleId, article.getTitle());
        } catch (RuntimeException e) {
            if (reservationCreated) {
                creditReservationService.release(processingNo);
            }
            if (objectCopied) {
                s3Util.deleteFile(finalObjectKey);
            }
            articleUploadSessionCache.releaseClaim(articleId);
            throw e;
        }
    }

    /**
     * 分析文章词汇
     *
     * @param articleId 文章ID
     * @param reqDTO 分析请求参数
     * @return 分析响应参数
     */
    @Override
    public ArticleAnalyzeRespDTO analyzeArticle(String articleId, ArticleAnalyzeReqDTO reqDTO) {
        String userId = getCurrentUserId();
        String analysisLevel = reqDTO.getAnalysisLevel();
        BizArticlesDO article = getUserArticle(articleId, userId);
        log.info("开始分析文章词汇: userId={}, articleId={}, analysisLevel={}, languageCode={}",
                userId, articleId, analysisLevel, article.getLanguageCode());
        if (!Integer.valueOf(PARSE_STATUS_SUCCESS).equals(article.getParseStatus())) {
            throw new ClientException(BaseErrorCode.ARTICLE_PARSE_FAILED);
        }

        List<ArticleVocabRespDTO> existingVocabs = listArticleVocabs(articleId, analysisLevel);
        if (!existingVocabs.isEmpty() && isAnalysisReusable(article, userId, analysisLevel)) {
            log.info("复用文章词汇分析结果: userId={}, articleId={}, analysisLevel={}, matchedWordCount={}",
                    userId, articleId, analysisLevel, existingVocabs.size());
            return ArticleAnalyzeRespDTO.builder()
                    .articleId(articleId)
                    .analysisLevel(analysisLevel)
                    .analysisStatus(ANALYSIS_STATUS_SUCCESS)
                    .reused(true)
                    .matchedWordCount(existingVocabs.size())
                    .vocabs(existingVocabs)
                    .build();
        }
        if (!existingVocabs.isEmpty()) {
            log.info("文章词汇分析版本已更新，重新生成结果: userId={}, articleId={}, analysisLevel={}",
                    userId, articleId, analysisLevel);
        }

        String analysisKey = articleId + ":" + analysisLevel;
        if (Integer.valueOf(ANALYSIS_STATUS_PROCESSING).equals(article.getAnalysisStatus())
                || activeAnalyses.putIfAbsent(analysisKey, Boolean.TRUE) != null) {
            return acceptedAnalysis(articleId, analysisLevel);
        }

        deleteAnalysisResults(articleId, userId, analysisLevel);
        article.setAnalysisStatus(ANALYSIS_STATUS_PROCESSING);
        bizArticlesMapper.updateById(article);
        articleQueryCache.invalidateArticle(userId, articleId);
        submitAnalysisTask(article, userId, analysisLevel, analysisKey);
        return acceptedAnalysis(articleId, analysisLevel);
    }

    private ArticleAnalyzeRespDTO acceptedAnalysis(String articleId, String analysisLevel) {
        return ArticleAnalyzeRespDTO.builder()
                .articleId(articleId)
                .analysisLevel(analysisLevel)
                .analysisStatus(ANALYSIS_STATUS_PROCESSING)
                .reused(false)
                .matchedWordCount(0)
                .vocabs(List.of())
                .build();
    }

    private void submitAnalysisTask(BizArticlesDO article,
                                    String userId,
                                    String analysisLevel,
                                    String analysisKey) {
        Runnable task = () -> {
            try {
                performArticleAnalysis(article, userId, analysisLevel);
            } finally {
                activeAnalyses.remove(analysisKey);
            }
        };
        Executor executor = articleTaskExecutor == null ? ForkJoinPool.commonPool() : articleTaskExecutor;
        try {
            executor.execute(task);
        } catch (RuntimeException e) {
            activeAnalyses.remove(analysisKey);
            article.setAnalysisStatus(ANALYSIS_STATUS_FAILED);
            bizArticlesMapper.updateById(article);
            articleQueryCache.invalidateArticle(userId, article.getId());
            throw new ClientException(BaseErrorCode.ARTICLE_ANALYSIS_FAILED);
        }
    }

    private void performArticleAnalysis(BizArticlesDO article, String userId, String analysisLevel) {
        String articleId = article.getId();
        try {
            String text = article.getParsedContent();
            if (text == null || text.isBlank()) {
                throw new ClientException(BaseErrorCode.ARTICLE_PARSE_FAILED);
            }
            boolean translated = Integer.valueOf(TRANSLATION_STATUS_SUCCESS).equals(article.getTranslationStatus());
            List<ArticleVocabMatch> matches = articleVocabAnalyzer.analyzeText(
                    text, article.getLanguageCode(), analysisLevel, translated);
            log.info("文章词汇匹配完成: userId={}, articleId={}, analysisLevel={}, matchedWordCount={}",
                    userId, articleId, analysisLevel, matches.size());
            saveMatches(article, analysisLevel, matches);
            log.info("文章词汇匹配结果保存成功: userId={}, articleId={}, analysisLevel={}", userId, articleId, analysisLevel);

            article.setAnalysisStatus(ANALYSIS_STATUS_SUCCESS);
            article.setAnalyzedAt(new Date());
            bizArticlesMapper.updateById(article);
            articleQueryCache.invalidateArticle(userId, articleId);
            vocabQueryCache.invalidateWordLevels(userId, article.getLanguageCode(), matches.stream()
                    .map(ArticleVocabMatch::getWordId)
                    .collect(Collectors.toSet()));

            List<ArticleVocabRespDTO> vocabs = listArticleVocabs(article, userId, analysisLevel);
            log.info("文章词汇分析成功: userId={}, articleId={}, analysisLevel={}, matchedWordCount={}",
                    userId, articleId, analysisLevel, vocabs.size());
        } catch (ClientException e) {
            article.setAnalysisStatus(ANALYSIS_STATUS_FAILED);
            bizArticlesMapper.updateById(article);
            articleQueryCache.invalidateArticle(userId, articleId);
            log.error("文章词汇分析失败: userId={}, articleId={}, analysisLevel={}", userId, articleId, analysisLevel, e);
        } catch (Exception e) {
            article.setAnalysisStatus(ANALYSIS_STATUS_FAILED);
            bizArticlesMapper.updateById(article);
            articleQueryCache.invalidateArticle(userId, articleId);
            log.error("文章词汇分析失败: userId={}, articleId={}, analysisLevel={}", userId, articleId, analysisLevel, e);
        }
    }

    /**
     * 获取文章详情
     *
     * @param articleId 文章ID
     * @return 文章详情
     */
    @Override
    public ArticleDetailRespDTO getArticleDetail(String articleId) {
        String userId = getCurrentUserId();
        long cacheVersion = articleQueryCache.getArticleVersion(userId, articleId);
        ArticleDetailRespDTO cached = articleQueryCache.getArticleDetail(userId, articleId, cacheVersion);
        if (cached != null) {
            return cached;
        }
        BizArticlesDO article = getUserArticle(articleId, userId);
        ArticleDetailRespDTO result = ArticleDetailRespDTO.builder()
                .articleId(article.getId())
                .title(article.getTitle())
                .parsedContent(article.getParsedContent())
                .languageCode(article.getLanguageCode())
                .translationStatus(article.getTranslationStatus())
                .wordCount(article.getWordCount())
                .charCount(article.getCharCount())
                .createdAt(article.getCreatedAt())
                .build();
        articleQueryCache.putArticleDetail(userId, articleId, cacheVersion, result);
        log.info("获取文章详情成功: userId={}, articleId={}", userId, articleId);
        return result;
    }

    /**
     * 查询文章列表
     *
     * @param keyword 文章标题关键词
     * @param languageCode 语言标识：en/ja
     * @return 文章列表
     */
    @Override
    public List<ArticleListRespDTO> listArticles(String keyword, String languageCode) {
        String userId = getCurrentUserId();
        String normalizedKeyword = keyword == null ? "" : keyword.trim();
        String normalizedLanguage = normalizeOptionalLanguage(languageCode);
        long cacheVersion = articleQueryCache.getUserVersion(userId);
        List<ArticleListRespDTO> articles = articleQueryCache.getArticleList(userId, cacheVersion);
        if (articles == null) {
            articles = queryArticleList(userId);
            articleQueryCache.putArticleList(userId, cacheVersion, articles);
        }
        String normalizedKeywordLower = normalizedKeyword.toLowerCase(Locale.ROOT);
        return articles.stream()
                .filter(each -> normalizedLanguage == null || normalizedLanguage.equals(each.getLanguageCode()))
                .filter(each -> normalizedKeywordLower.isEmpty()
                        || Optional.ofNullable(each.getTitle()).orElse("").toLowerCase(Locale.ROOT).contains(normalizedKeywordLower))
                .toList();
    }

    @Override
    public List<ArticleListRespDTO> listRecentArticles() {
        String userId = getCurrentUserId();
        long cacheVersion = articleQueryCache.getUserVersion(userId);
        List<ArticleListRespDTO> cached = articleQueryCache.getRecentArticles(userId, cacheVersion);
        if (cached != null) {
            return cached;
        }
        List<ArticleListRespDTO> recent = queryArticleList(userId, 2);
        articleQueryCache.putRecentArticles(userId, cacheVersion, recent);
        return recent;
    }

    private List<ArticleListRespDTO> queryArticleList(String userId) {
        return queryArticleList(userId, null);
    }

    private List<ArticleListRespDTO> queryArticleList(String userId, Integer limit) {
        LambdaQueryWrapper<BizArticlesDO> query = new LambdaQueryWrapper<BizArticlesDO>()
                .eq(BizArticlesDO::getUserId, userId)
                .eq(BizArticlesDO::getStatus, STATUS_NORMAL)
                .orderByDesc(BizArticlesDO::getCreatedAt);
        if (limit != null) {
            query.last("LIMIT " + limit);
        }
        return bizArticlesMapper.selectList(query).stream()
                .map(each -> ArticleListRespDTO.builder()
                        .articleId(each.getId())
                        .title(each.getTitle())
                        .languageCode(each.getLanguageCode())
                        .wordCount(each.getWordCount())
                        .createdAt(each.getCreatedAt())
                        .build())
                .toList();
    }

    /**
     * 获取文章处理详情
     *
     * @param articleId 文章ID
     * @return 文章处理详情
     */
    @Override
    public ArticleProcessingDetailRespDTO getArticleProcessingDetail(String articleId) {
        String userId = getCurrentUserId();
        long cacheVersion = articleQueryCache.getArticleVersion(userId, articleId);
        ArticleProcessingDetailRespDTO cached = articleQueryCache.getProcessingDetail(userId, articleId, cacheVersion);
        if (cached != null) {
            return cached;
        }
        BizArticlesDO article = getUserArticle(articleId, userId);
        ArticleProcessingDetailRespDTO result = ArticleProcessingDetailRespDTO.builder()
                .wordCount(article.getWordCount())
                .parseStatus(article.getParseStatus())
                .translationStatus(article.getTranslationStatus())
                .analysisStatus(article.getAnalysisStatus())
                .parsedAt(article.getParsedAt())
                .translatedAt(article.getTranslatedAt())
                .analyzedAt(article.getAnalyzedAt())
                .build();
        articleQueryCache.putProcessingDetail(userId, articleId, cacheVersion, result);
        return result;
    }

    /**
     * 获取文章命中词汇列表
     *
     * @param articleId 文章ID
     * @param analysisLevel 词汇分析等级
     * @return 命中词汇列表
     */
    @Override
    public List<ArticleVocabRespDTO> listArticleVocabs(String articleId, String analysisLevel) {
        String userId = getCurrentUserId();
        BizArticlesDO article = getUserArticle(articleId, userId);
        String normalizedLevel = analysisLevel.trim().toUpperCase(Locale.ROOT);
        long cacheVersion = articleQueryCache.getArticleVersion(userId, articleId);
        List<ArticleVocabRespDTO> cached = articleQueryCache.getArticleVocabs(
                userId, articleId, cacheVersion, normalizedLevel);
        if (cached != null) {
            return cached;
        }
        List<ArticleVocabRespDTO> vocabs = listArticleVocabs(article, userId, normalizedLevel);
        articleQueryCache.putArticleVocabs(userId, articleId, cacheVersion, normalizedLevel, vocabs);
        log.info("获取文章命中词汇列表成功: userId={}, articleId={}, analysisLevel={}, vocabCount={}",
                userId, articleId, analysisLevel, vocabs.size());
        return vocabs;
    }

    /**
     * 获取文章已解析的词汇等级
     *
     * @param articleId 文章ID
     * @return 已解析的词汇等级列表
     */
    @Override
    public List<String> listArticleVocabLevels(String articleId) {
        String userId = getCurrentUserId();
        BizArticlesDO article = getUserArticle(articleId, userId);
        long cacheVersion = articleQueryCache.getArticleVersion(userId, articleId);
        List<String> cached = articleQueryCache.getVocabLevels(userId, articleId, cacheVersion);
        if (cached != null && !"ja".equals(article.getLanguageCode())) {
            return cached;
        }
        List<String> levels = listReusableAnalysisLevels(article, userId).stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(each -> !each.isEmpty())
                .map(each -> each.toUpperCase(Locale.ROOT))
                .distinct()
                .sorted()
                .toList();
        log.info("获取文章词汇等级成功: userId={}, articleId={}, levels={}", userId, articleId, levels);
        articleQueryCache.putVocabLevels(userId, articleId, cacheVersion, levels);
        return levels;
    }

    private List<String> listReusableAnalysisLevels(BizArticlesDO article, String userId) {
        if ("ja".equals(article.getLanguageCode())) {
            return relArticleVocabOccurrenceMapper.selectList(
                            new LambdaQueryWrapper<RelArticleVocabOccurrenceDO>()
                                    .select(RelArticleVocabOccurrenceDO::getAnalysisLevel)
                                    .eq(RelArticleVocabOccurrenceDO::getArticleId, article.getId())
                                    .eq(RelArticleVocabOccurrenceDO::getUserId, userId)
                                    .eq(RelArticleVocabOccurrenceDO::getAnalysisProvider,
                                            ArticleVocabAnalyzer.JAPANESE_ANALYSIS_PROVIDER)
                                    .eq(RelArticleVocabOccurrenceDO::getAnalysisVersion,
                                            ArticleVocabAnalyzer.JAPANESE_ANALYSIS_VERSION)
                                    .groupBy(RelArticleVocabOccurrenceDO::getAnalysisLevel)
                                    .orderByAsc(RelArticleVocabOccurrenceDO::getAnalysisLevel))
                    .stream()
                    .map(RelArticleVocabOccurrenceDO::getAnalysisLevel)
                    .toList();
        }
        return relArticleVocabMapper.selectList(
                        new LambdaQueryWrapper<RelArticleVocabDO>()
                                .select(RelArticleVocabDO::getAnalysisLevel)
                                .eq(RelArticleVocabDO::getArticleId, article.getId())
                                .eq(RelArticleVocabDO::getUserId, userId)
                                .groupBy(RelArticleVocabDO::getAnalysisLevel)
                                .orderByAsc(RelArticleVocabDO::getAnalysisLevel))
                .stream()
                .map(RelArticleVocabDO::getAnalysisLevel)
                .toList();
    }

    /**
     * 获取词汇出现位置列表
     *
     * @param articleId 文章ID
     * @param articleVocabId 文章命中词汇汇总ID
     * @return 词汇出现位置列表
     */
    @Override
    public List<ArticleVocabOccurrenceRespDTO> listArticleVocabOccurrences(String articleId, String articleVocabId) {
        String userId = getCurrentUserId();
        long cacheVersion = articleQueryCache.getArticleVersion(userId, articleId);
        List<ArticleVocabOccurrenceRespDTO> cached = articleQueryCache.getOccurrences(
                userId, articleId, cacheVersion, articleVocabId);
        if (cached != null) {
            return cached;
        }
        // 校验文章是否存在且属于当前用户，防止越权查询词汇出现位置
        BizArticlesDO article = getUserArticle(articleId, userId);
        RelArticleVocabDO articleVocab = relArticleVocabMapper.selectOne(new LambdaQueryWrapper<RelArticleVocabDO>()
                .eq(RelArticleVocabDO::getId, articleVocabId)
                .eq(RelArticleVocabDO::getArticleId, articleId)
                .eq(RelArticleVocabDO::getUserId, userId));
        if (articleVocab == null) {
            throw new ClientException(BaseErrorCode.VOCAB_NOT_FOUND);
        }

        List<RelArticleVocabOccurrenceDO> occurrences = querySourceOccurrences(
                article, userId, articleVocab.getAnalysisLevel(), articleVocabId);
        log.info("获取词汇出现位置列表成功: userId={}, articleId={}, articleVocabId={}, occurrenceCount={}",
                userId, articleId, articleVocabId, occurrences.size());
        List<ArticleVocabOccurrenceRespDTO> result = occurrences.stream()
                .map(this::toOccurrenceResp)
                .collect(Collectors.toList());
        articleQueryCache.putOccurrences(userId, articleId, cacheVersion, articleVocabId, result);
        return result;
    }

    /**
     * 获取指定词汇等级的全部原文出现位置
     *
     * @param articleId 文章ID
     * @param analysisLevel 词汇分析等级
     * @return 原文出现位置列表
     */
    @Override
    public List<ArticleVocabOccurrenceRespDTO> listArticleVocabLevelOccurrences(String articleId,
                                                                                String analysisLevel) {
        String userId = getCurrentUserId();
        BizArticlesDO article = getUserArticle(articleId, userId);
        String normalizedLevel = analysisLevel.trim().toUpperCase(Locale.ROOT);
        return querySourceOccurrences(article, userId, normalizedLevel, null).stream()
                .map(this::toOccurrenceResp)
                .toList();
    }

    /**
     * 删除文章
     *
     * @param articleId 文章ID
     */
    @Override
    public void deleteArticle(String articleId) {
        String userId = getCurrentUserId();
        BizArticlesDO article = getUserArticle(articleId, userId);
        article.setStatus(STATUS_DELETED);
        article.setDeletedAt(new Date());
        bizArticlesMapper.updateById(article);
        articleQueryCache.invalidateUser(userId);
        articleQueryCache.invalidateArticle(userId, articleId);
        log.info("文章软删除成功: userId={}, articleId={}", userId, articleId);
    }

    private BizArticlesDO getUserArticle(String articleId, String userId) {
        BizArticlesDO article = bizArticlesMapper.selectOne(new LambdaQueryWrapper<BizArticlesDO>()
                .eq(BizArticlesDO::getId, articleId)
                .eq(BizArticlesDO::getUserId, userId)
                .eq(BizArticlesDO::getStatus, STATUS_NORMAL));
        if (article == null) {
            throw new ClientException(BaseErrorCode.ARTICLE_NOT_FOUND);
        }
        return article;
    }

    private String normalizeOptionalLanguage(String languageCode) {
        if (languageCode == null || languageCode.isBlank()) {
            return null;
        }
        String normalizedLanguage = languageCode.trim().toLowerCase(Locale.ROOT);
        if (!SUPPORTED_LANGUAGES.contains(normalizedLanguage)) {
            throw new ClientException(BaseErrorCode.ARTICLE_LANGUAGE_NOT_SUPPORTED);
        }
        return normalizedLanguage;
    }

    private void saveMatches(BizArticlesDO article, String analysisLevel, List<ArticleVocabMatch> matches) throws Exception {
        for (ArticleVocabMatch match : matches) {
            ArticleVocabOccurrence firstOccurrence = match.getOccurrences().get(0);
            RelArticleVocabDO articleVocab = RelArticleVocabDO.builder()
                    .articleId(article.getId())
                    .userId(article.getUserId())
                    .analysisLevel(analysisLevel)
                    .wordId(match.getWordId())
                    .languageCode(article.getLanguageCode())
                    .baseWord(match.getBaseWord())
                    .matchedForms(objectMapper.writeValueAsString(match.getMatchedForms()))
                    .occurrenceCount(match.getOccurrences().size())
                    .firstMatchedText(firstOccurrence.getMatchedText())
                    .firstSentence(firstOccurrence.getSentence())
                    .firstStartOffset(firstOccurrence.getStartOffset())
                    .firstEndOffset(firstOccurrence.getEndOffset())
                    .build();
            relArticleVocabMapper.insert(articleVocab);

            for (ArticleVocabOccurrence occurrence : match.getOccurrences()) {
                RelArticleVocabOccurrenceDO occurrenceDO = RelArticleVocabOccurrenceDO.builder()
                        .articleId(article.getId())
                        .articleVocabId(articleVocab.getId())
                        .userId(article.getUserId())
                        .analysisLevel(analysisLevel)
                        .wordId(match.getWordId())
                        .languageCode(article.getLanguageCode())
                        .normalizedText(occurrence.getNormalizedText())
                        .posTag(occurrence.getPosTag())
                        .posType(occurrence.getPosType())
                        .morphFeatures(occurrence.getMorphFeatures())
                        .sentence(occurrence.getSentence())
                        .sentenceStartOffset(occurrence.getSentenceStartOffset())
                        .sentenceEndOffset(occurrence.getSentenceEndOffset())
                        .startOffset(occurrence.getStartOffset())
                        .endOffset(occurrence.getEndOffset())
                        .analysisProvider(occurrence.getAnalysisProvider())
                        .analysisVersion(occurrence.getAnalysisVersion())
                        .build();
                relArticleVocabOccurrenceMapper.insert(occurrenceDO);
            }
        }
    }

    private List<ArticleVocabRespDTO> listArticleVocabs(BizArticlesDO article, String userId,
                                                        String analysisLevel) {
        String articleId = article.getId();
        List<RelArticleVocabDO> articleVocabs = relArticleVocabMapper.selectList(new LambdaQueryWrapper<RelArticleVocabDO>()
                .eq(RelArticleVocabDO::getArticleId, articleId)
                .eq(RelArticleVocabDO::getUserId, userId)
                .eq(RelArticleVocabDO::getAnalysisLevel, analysisLevel)
                .orderByAsc(RelArticleVocabDO::getFirstStartOffset));
        if (articleVocabs.isEmpty()) {
            return List.of();
        }

        Map<String, List<RelArticleVocabOccurrenceDO>> sourceOccurrences = querySourceOccurrences(
                article, userId, analysisLevel, null).stream()
                .collect(Collectors.groupingBy(RelArticleVocabOccurrenceDO::getArticleVocabId));
        articleVocabs = articleVocabs.stream()
                .filter(each -> sourceOccurrences.containsKey(each.getId()))
                .toList();
        if (articleVocabs.isEmpty()) {
            return List.of();
        }

        Map<Long, ArticleVocabDetail> vocabDetailMap = getVocabDetailMap(article.getLanguageCode(), articleVocabs.stream()
                .map(RelArticleVocabDO::getWordId)
                .collect(Collectors.toSet()));
        Map<Long, List<String>> addedLibraryIdsByWord = getAddedLibraryIdsByWord(
                userId,
                article.getLanguageCode(),
                articleVocabs.stream().map(RelArticleVocabDO::getWordId).collect(Collectors.toSet()));
        Map<String, String> libraryNamesById = getLibraryNamesById(userId, addedLibraryIdsByWord.values().stream()
                .flatMap(Collection::stream)
                .collect(Collectors.toSet()));
        return articleVocabs.stream()
                .map(each -> {
                    ArticleVocabDetail vocabDetail = vocabDetailMap.get(each.getWordId());
                    List<RelArticleVocabOccurrenceDO> validOccurrences = sourceOccurrences.get(each.getId());
                    RelArticleVocabOccurrenceDO firstOccurrence = validOccurrences.get(0);
                    List<String> addedLibraryIds = addedLibraryIdsByWord.getOrDefault(each.getWordId(), List.of()).stream()
                            .filter(libraryNamesById::containsKey)
                            .toList();
                    return ArticleVocabRespDTO.builder()
                            .articleVocabId(each.getId())
                            .wordId(each.getWordId())
                            .languageCode(each.getLanguageCode())
                            .baseWord(each.getBaseWord())
                            .matchedForms(each.getMatchedForms())
                            .occurrenceCount(validOccurrences.size())
                            .firstMatchedText(article.getParsedContent().substring(
                                    firstOccurrence.getStartOffset(), firstOccurrence.getEndOffset()))
                            .firstSentence(firstOccurrence.getSentence())
                            .translations(vocabDetail == null ? null : vocabDetail.translations())
                            .us(vocabDetail == null ? null : vocabDetail.us())
                            .uk(vocabDetail == null ? null : vocabDetail.uk())
                            .kana(vocabDetail == null ? null : vocabDetail.kana())
                            .addedLibraryIds(addedLibraryIds)
                            .addedLibraryNames(addedLibraryIds.stream().map(libraryNamesById::get).toList())
                            .build();
                })
                .collect(Collectors.toList());
    }

    /**
     * 批量查询文章词汇已加入的有效词汇库ID。
     *
     * @param userId 用户ID
     * @param languageCode 语言标识
     * @param wordIds 词汇ID集合
     * @return 以词汇ID为键的词汇库ID列表
     */
    private Map<Long, List<String>> getAddedLibraryIdsByWord(String userId, String languageCode, Set<Long> wordIds) {
        if (wordIds.isEmpty()) {
            return Map.of();
        }
        return vocabLibraryWordMapper.selectList(new LambdaQueryWrapper<RelVocabLibraryWordDO>()
                        .eq(RelVocabLibraryWordDO::getUserId, userId)
                        .eq(RelVocabLibraryWordDO::getLanguageCode, languageCode)
                        .eq(RelVocabLibraryWordDO::getStatus, VocabConstant.STATUS_NORMAL)
                        .in(RelVocabLibraryWordDO::getWordId, wordIds))
                .stream()
                .collect(Collectors.groupingBy(
                        RelVocabLibraryWordDO::getWordId,
                        LinkedHashMap::new,
                        Collectors.mapping(RelVocabLibraryWordDO::getLibraryId, Collectors.toList())));
    }

    /**
     * 批量查询当前用户有效词汇库的名称。
     *
     * @param userId 用户ID
     * @param libraryIds 词汇库ID集合
     * @return 以词汇库ID为键的词汇库名称
     */
    private Map<String, String> getLibraryNamesById(String userId, Set<String> libraryIds) {
        if (libraryIds.isEmpty()) {
            return Map.of();
        }
        return vocabLibraryMapper.selectList(new LambdaQueryWrapper<BizVocabLibraryDO>()
                        .eq(BizVocabLibraryDO::getUserId, userId)
                        .eq(BizVocabLibraryDO::getStatus, VocabConstant.STATUS_NORMAL)
                        .in(BizVocabLibraryDO::getId, libraryIds))
                .stream()
                .collect(Collectors.toMap(BizVocabLibraryDO::getId, BizVocabLibraryDO::getName));
    }

    private List<RelArticleVocabOccurrenceDO> querySourceOccurrences(BizArticlesDO article, String userId,
                                                                      String analysisLevel,
                                                                      String articleVocabId) {
        LambdaQueryWrapper<RelArticleVocabOccurrenceDO> query = new LambdaQueryWrapper<RelArticleVocabOccurrenceDO>()
                .eq(RelArticleVocabOccurrenceDO::getArticleId, article.getId())
                .eq(RelArticleVocabOccurrenceDO::getUserId, userId)
                .eq(RelArticleVocabOccurrenceDO::getAnalysisLevel, analysisLevel)
                .orderByAsc(RelArticleVocabOccurrenceDO::getStartOffset);
        if (articleVocabId != null) {
            query.eq(RelArticleVocabOccurrenceDO::getArticleVocabId, articleVocabId);
        }
        List<RelArticleVocabOccurrenceDO> occurrences = relArticleVocabOccurrenceMapper.selectList(query);
        boolean translated = Integer.valueOf(TRANSLATION_STATUS_SUCCESS).equals(article.getTranslationStatus());
        List<SourceSegment> sourceSegments = ArticleSourceRangeUtil.extract(article.getParsedContent(), translated);
        return occurrences.stream()
                .filter(each -> each.getStartOffset() != null && each.getEndOffset() != null)
                .filter(each -> sourceSegments.stream().anyMatch(segment ->
                        each.getStartOffset() >= segment.startOffset()
                                && each.getEndOffset() <= segment.endOffset()))
                .toList();
    }

    private ArticleVocabOccurrenceRespDTO toOccurrenceResp(RelArticleVocabOccurrenceDO occurrence) {
        return ArticleVocabOccurrenceRespDTO.builder()
                .occurrenceId(occurrence.getId())
                .articleVocabId(occurrence.getArticleVocabId())
                .wordId(occurrence.getWordId())
                .normalizedText(occurrence.getNormalizedText())
                .posTag(occurrence.getPosTag())
                .posType(occurrence.getPosType())
                .sentence(occurrence.getSentence())
                .startOffset(occurrence.getStartOffset())
                .endOffset(occurrence.getEndOffset())
                .build();
    }

    private void deleteAnalysisResults(String articleId, String userId, String analysisLevel) {
        relArticleVocabOccurrenceMapper.delete(new LambdaQueryWrapper<RelArticleVocabOccurrenceDO>()
                .eq(RelArticleVocabOccurrenceDO::getArticleId, articleId)
                .eq(RelArticleVocabOccurrenceDO::getUserId, userId)
                .eq(RelArticleVocabOccurrenceDO::getAnalysisLevel, analysisLevel));
        relArticleVocabMapper.delete(new LambdaQueryWrapper<RelArticleVocabDO>()
                .eq(RelArticleVocabDO::getArticleId, articleId)
                .eq(RelArticleVocabDO::getUserId, userId)
                .eq(RelArticleVocabDO::getAnalysisLevel, analysisLevel));
    }

    private boolean isAnalysisReusable(BizArticlesDO article, String userId, String analysisLevel) {
        if (!"ja".equals(article.getLanguageCode())) {
            return true;
        }
        LambdaQueryWrapper<RelArticleVocabOccurrenceDO> baseQuery = new LambdaQueryWrapper<RelArticleVocabOccurrenceDO>()
                .eq(RelArticleVocabOccurrenceDO::getArticleId, article.getId())
                .eq(RelArticleVocabOccurrenceDO::getUserId, userId)
                .eq(RelArticleVocabOccurrenceDO::getAnalysisLevel, analysisLevel);
        long totalCount = relArticleVocabOccurrenceMapper.selectCount(baseQuery);
        long currentCount = relArticleVocabOccurrenceMapper.selectCount(
                new LambdaQueryWrapper<RelArticleVocabOccurrenceDO>()
                        .eq(RelArticleVocabOccurrenceDO::getArticleId, article.getId())
                        .eq(RelArticleVocabOccurrenceDO::getUserId, userId)
                        .eq(RelArticleVocabOccurrenceDO::getAnalysisLevel, analysisLevel)
                        .eq(RelArticleVocabOccurrenceDO::getAnalysisProvider,
                                ArticleVocabAnalyzer.JAPANESE_ANALYSIS_PROVIDER)
                        .eq(RelArticleVocabOccurrenceDO::getAnalysisVersion,
                                ArticleVocabAnalyzer.JAPANESE_ANALYSIS_VERSION));
        return totalCount > 0 && totalCount == currentCount;
    }

    private Map<Long, ArticleVocabDetail> getVocabDetailMap(String languageCode, Set<Long> wordIds) {
        Map<Long, ArticleVocabDetail> result = new HashMap<>();
        if (wordIds.isEmpty()) {
            return result;
        }
        Map<Long, VocabWordCacheEntry> words = vocabWordCacheLoader.loadWords(languageCode, wordIds);
        words.values().forEach(word -> result.put(word.wordId(), new ArticleVocabDetail(
                word.translations(), word.us(), word.uk(), word.kana())));
        return result;
    }

    /**
     * 文章词汇的词典详情
     *
     * @param translations 翻译列表 JSON
     * @param us 美式音标
     * @param uk 英式音标
     * @param kana 日语假名读音
     */
    private record ArticleVocabDetail(String translations, String us, String uk, String kana) {
    }

    private String getCurrentUserId() {
        String userId = UserContext.getCurrentUserId();
        if (userId == null) {
            throw new ClientException(BaseErrorCode.USER_NOT_LOGIN);
        }
        return userId;
    }

    private String getOriginalFilename(MultipartFile file) {
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.isBlank()) {
            return "untitled";
        }
        return originalFilename;
    }

    private String getFileType(String filename) {
        int index = filename.lastIndexOf('.');
        if (index < 0 || index == filename.length() - 1) {
            return "bin";
        }
        return filename.substring(index + 1).toLowerCase(Locale.ROOT);
    }

    private String getBaseName(String filename) {
        int index = filename.lastIndexOf('.');
        return index < 0 ? filename : filename.substring(0, index);
    }

    private void validateUpload(String filename, String fileType, long fileSize) {
        if (filename.isBlank() || !SUPPORTED_FILE_TYPES.contains(fileType)
                || fileSize <= 0L || fileSize > MAX_UPLOAD_FILE_SIZE) {
            throw new ClientException(BaseErrorCode.ARTICLE_UPLOAD_INVALID);
        }
    }

    private String normalizeContentType(String contentType) {
        return contentType == null || contentType.isBlank()
                ? "application/octet-stream"
                : contentType.trim();
    }

    private ArticleUploadSession getUploadSession(String articleId, String userId) {
        ArticleUploadSession session = articleUploadSessionCache.get(articleId);
        if (session == null || !userId.equals(session.userId())) {
            throw new ClientException(BaseErrorCode.ARTICLE_UPLOAD_SESSION_NOT_FOUND);
        }
        return session;
    }

    private BizArticlesDO buildPendingArticle(String articleId,
                                              String userId,
                                              String title,
                                              String originalFilename,
                                              String fileType,
                                              String contentType,
                                              long fileSize,
                                              String filePath) {
        return BizArticlesDO.builder()
                .id(articleId)
                .userId(userId)
                .title(title)
                .originalFilename(originalFilename)
                .fileType(fileType)
                .mimeType(contentType)
                .fileSize(fileSize)
                .filePath(filePath)
                .parseStatus(PARSE_STATUS_PROCESSING)
                .translationStatus(TRANSLATION_STATUS_PENDING)
                .analysisStatus(ANALYSIS_STATUS_PENDING)
                .status(STATUS_NORMAL)
                .build();
    }

    private ArticleUploadRespDTO toUploadResp(String articleId, String title) {
        return ArticleUploadRespDTO.builder()
                .articleId(articleId)
                .title(title)
                .languageCode("unknown")
                .parseStatus(PARSE_STATUS_PROCESSING)
                .translationStatus(TRANSLATION_STATUS_PENDING)
                .build();
    }

}
