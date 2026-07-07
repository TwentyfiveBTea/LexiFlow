package com.btea.lexiflow.article.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.btea.lexiflow.article.dao.entity.BizArticlesDO;
import com.btea.lexiflow.article.dao.entity.RelArticleVocabDO;
import com.btea.lexiflow.article.dao.entity.RelArticleVocabOccurrenceDO;
import com.btea.lexiflow.article.dao.mapper.BizArticlesMapper;
import com.btea.lexiflow.article.dao.mapper.RelArticleVocabMapper;
import com.btea.lexiflow.article.dao.mapper.RelArticleVocabOccurrenceMapper;
import com.btea.lexiflow.article.dto.req.ArticleAnalyzeReqDTO;
import com.btea.lexiflow.article.dto.resp.*;
import com.btea.lexiflow.article.nlp.ArticleVocabAnalyzer;
import com.btea.lexiflow.article.nlp.ArticleVocabMatch;
import com.btea.lexiflow.article.nlp.ArticleVocabOccurrence;
import com.btea.lexiflow.article.service.ArticleProcessingService;
import com.btea.lexiflow.article.service.ArticleService;
import com.btea.lexiflow.common.context.UserContext;
import com.btea.lexiflow.common.convention.errorcode.BaseErrorCode;
import com.btea.lexiflow.common.convention.exception.ClientException;
import com.btea.lexiflow.infrastructure.s3.S3Util;
import com.btea.lexiflow.vocab.dao.entity.BizVocabEnDO;
import com.btea.lexiflow.vocab.dao.entity.BizVocabJpDO;
import com.btea.lexiflow.vocab.dao.mapper.BizVocabEnMapper;
import com.btea.lexiflow.vocab.dao.mapper.BizVocabJpMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.security.MessageDigest;
import java.util.*;
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
    private final BizVocabEnMapper bizVocabEnMapper;
    private final BizVocabJpMapper bizVocabJpMapper;
    private final S3Util s3Util;
    private final ArticleVocabAnalyzer articleVocabAnalyzer;
    private final ArticleProcessingService articleProcessingService;

    private final ObjectMapper objectMapper = new ObjectMapper();

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
        String originalFilename = getOriginalFilename(file);
        String fileType = getFileType(originalFilename);
        String title = getBaseName(originalFilename);
        String filePath = ARTICLE_DIR + userId + "/" + articleId + "_original." + fileType;
        log.info("开始上传文章: userId={}, articleId={}, filename={}, fileType={}, fileSize={}",
                userId, articleId, originalFilename, fileType, file.getSize());

        byte[] fileBytes;
        try {
            fileBytes = file.getBytes();
        } catch (Exception e) {
            log.error("读取上传文件失败: userId={}, filename={}", userId, originalFilename, e);
            throw new ClientException(BaseErrorCode.FILE_PARSE_FAILED);
        }

        BizArticlesDO article = BizArticlesDO.builder()
                .id(articleId)
                .userId(userId)
                .title(title)
                .originalFilename(originalFilename)
                .fileType(fileType)
                .mimeType(file.getContentType())
                .fileSize(file.getSize())
                .fileHash(sha256Hex(fileBytes))
                .filePath(filePath)
                .parseStatus(PARSE_STATUS_PROCESSING)
                .translationStatus(TRANSLATION_STATUS_PENDING)
                .analysisStatus(ANALYSIS_STATUS_PENDING)
                .status(STATUS_NORMAL)
                .build();

        try {
            s3Util.uploadFile(file, filePath);
            log.info("文章原文件上传成功: userId={}, articleId={}, filePath={}", userId, articleId, filePath);
            articleProcessingService.processUploadedArticle(article, fileBytes, originalFilename, file.getContentType());
            log.info("文章异步处理任务提交成功: userId={}, articleId={}", userId, articleId);
            return ArticleUploadRespDTO.builder()
                    .articleId(articleId)
                    .title(title)
                    .languageCode("unknown")
                    .parseStatus(PARSE_STATUS_PROCESSING)
                    .translationStatus(TRANSLATION_STATUS_PENDING)
                    .build();
        } catch (ClientException e) {
            article.setParseStatus(PARSE_STATUS_FAILED);
            bizArticlesMapper.updateById(article);
            throw e;
        } catch (Exception e) {
            article.setParseStatus(PARSE_STATUS_FAILED);
            bizArticlesMapper.updateById(article);
            log.error("文章上传失败: userId={}, articleId={}", userId, articleId, e);
            throw new ClientException(BaseErrorCode.FILE_UPLOAD_FAILED);
        }
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

    private List<ArticleVocabRespDTO> listArticleVocabs(String articleId, String userId, String analysisLevel, String languageCode) {
        List<RelArticleVocabDO> articleVocabs = relArticleVocabMapper.selectList(new LambdaQueryWrapper<RelArticleVocabDO>()
                .eq(RelArticleVocabDO::getArticleId, articleId)
                .eq(RelArticleVocabDO::getUserId, userId)
                .eq(RelArticleVocabDO::getAnalysisLevel, analysisLevel)
                .orderByAsc(RelArticleVocabDO::getFirstStartOffset));
        if (articleVocabs.isEmpty()) {
            return List.of();
        }

        Map<Long, String> translationsMap = getTranslationsMap(languageCode, articleVocabs.stream()
                .map(RelArticleVocabDO::getWordId)
                .collect(Collectors.toSet()));
        return articleVocabs.stream()
                .map(each -> ArticleVocabRespDTO.builder()
                        .articleVocabId(each.getId())
                        .wordId(each.getWordId())
                        .languageCode(each.getLanguageCode())
                        .baseWord(each.getBaseWord())
                        .matchedForms(each.getMatchedForms())
                        .occurrenceCount(each.getOccurrenceCount())
                        .firstMatchedText(each.getFirstMatchedText())
                        .firstSentence(each.getFirstSentence())
                        .translations(translationsMap.get(each.getWordId()))
                        .build())
                .collect(Collectors.toList());
    }

    private Map<Long, String> getTranslationsMap(String languageCode, Set<Long> wordIds) {
        Map<Long, String> result = new HashMap<>();
        if (wordIds.isEmpty()) {
            return result;
        }
        for (List<Long> batch : partition(new ArrayList<>(wordIds))) {
            if ("ja".equals(languageCode)) {
                List<BizVocabJpDO> vocabList = bizVocabJpMapper.selectBatchIds(batch);
                for (BizVocabJpDO vocab : vocabList) {
                    result.put(vocab.getId(), vocab.getTranslations());
                }
            } else {
                List<BizVocabEnDO> vocabList = bizVocabEnMapper.selectBatchIds(batch);
                for (BizVocabEnDO vocab : vocabList) {
                    result.put(vocab.getId(), vocab.getTranslations());
                }
            }
        }
        return result;
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

    private String sha256Hex(byte[] bytes) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(bytes);
            StringBuilder hex = new StringBuilder();
            for (byte b : hash) {
                hex.append(String.format("%02x", b));
            }
            return hex.toString();
        } catch (Exception e) {
            throw new ClientException(BaseErrorCode.SERVICE_ERROR);
        }
    }

    private <T> List<List<T>> partition(List<T> values) {
        List<List<T>> result = new ArrayList<>();
        for (int i = 0; i < values.size(); i += VOCAB_QUERY_BATCH_SIZE) {
            result.add(values.subList(i, Math.min(i + VOCAB_QUERY_BATCH_SIZE, values.size())));
        }
        return result;
    }
}
