package com.btea.lexiflow.article.service.impl;

import com.btea.lexiflow.article.dao.entity.BizArticlesDO;
import com.btea.lexiflow.article.dao.mapper.BizArticlesMapper;
import com.btea.lexiflow.article.document.ArticleLanguageDetector;
import com.btea.lexiflow.article.document.ArticleTextExtractor;
import com.btea.lexiflow.article.dto.resp.ArticleTranslationResultDTO;
import com.btea.lexiflow.article.service.ArticleProcessingService;
import com.btea.lexiflow.article.service.ArticleTranslationService;
import com.btea.lexiflow.common.convention.errorcode.BaseErrorCode;
import com.btea.lexiflow.common.convention.exception.ClientException;
import com.btea.lexiflow.infrastructure.s3.S3Util;
import com.btea.lexiflow.pay.model.AiProcessingContext;
import com.btea.lexiflow.pay.service.CreditReservationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Date;

import static com.btea.lexiflow.article.constant.ArticleConstant.PARSE_STATUS_SUCCESS;
import static com.btea.lexiflow.article.constant.ArticleConstant.TRANSLATION_STATUS_PROCESSING;
import static com.btea.lexiflow.article.constant.ArticleConstant.TRANSLATION_STATUS_SUCCESS;

/**
 * @Author: TwentyfiveBTea
 * @Date: 2026/7/7 15:00
 * @Description: 文章异步处理服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ArticleProcessingServiceImpl implements ArticleProcessingService {

    private final BizArticlesMapper bizArticlesMapper;
    private final ArticleTextExtractor articleTextExtractor;
    private final ArticleLanguageDetector articleLanguageDetector;
    private final ArticleTranslationService articleTranslationService;
    private final S3Util s3Util;
    private final CreditReservationService creditReservationService;

    /**
     * 异步处理已上传文章
     *
     * @param article 文章记录
     * @param fileBytes 文件字节数组
     * @param originalFilename 原始文件名
     * @param contentType 文件 MIME 类型
     * @param context AI处理计费上下文
     */
    @Async("articleTaskExecutor")
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void processUploadedArticle(BizArticlesDO article,
                                       byte[] fileBytes,
                                       String originalFilename,
                                       String contentType,
                                       AiProcessingContext context) {
        log.info("开始异步处理文章: userId={}, articleId={}, filename={}", article.getUserId(), article.getId(), originalFilename);
        try {
            bizArticlesMapper.insert(article);
            log.info("文章记录创建成功: userId={}, articleId={}", article.getUserId(), article.getId());

            String originalContent = articleTextExtractor.extractText(
                    fileBytes, originalFilename, contentType, context);
            log.info("文章文本解析完成: userId={}, articleId={}, contentLength={}",
                    article.getUserId(), article.getId(), originalContent.length());
            String languageCode = articleLanguageDetector.detectLanguage(originalContent);
            log.info("文章语言识别完成: userId={}, articleId={}, languageCode={}", article.getUserId(), article.getId(), languageCode);

            ArticleTranslationResultDTO translationResult = translateArticleContent(article, originalContent, context);
            if (!Integer.valueOf(TRANSLATION_STATUS_SUCCESS).equals(translationResult.getTranslationStatus())) {
                throw new ClientException(BaseErrorCode.ARTICLE_TRANSLATION_FAILED);
            }

            String parsedContent = translationResult.getParsedContent();
            article.setParsedContent(parsedContent);
            article.setContentHash(sha256Hex(parsedContent.getBytes(StandardCharsets.UTF_8)));
            article.setLanguageCode(languageCode);
            article.setWordCount(articleLanguageDetector.countWords(originalContent, languageCode));
            article.setCharCount(originalContent.length());
            article.setParseStatus(PARSE_STATUS_SUCCESS);
            article.setParsedAt(new Date());
            article.setTranslationStatus(translationResult.getTranslationStatus());
            if (Boolean.TRUE.equals(translationResult.getTranslated())) {
                article.setTranslatedAt(new Date());
            }
            bizArticlesMapper.updateById(article);
            registerCreditSettlementAfterCommit(context.processingNo());
            log.info("文章异步处理完成: userId={}, articleId={}, languageCode={}, wordCount={}, charCount={}, translationStatus={}",
                    article.getUserId(), article.getId(), languageCode, article.getWordCount(), article.getCharCount(), article.getTranslationStatus());
        } catch (ClientException e) {
            creditReservationService.release(context.processingNo());
            deleteOriginalFile(article);
            log.error("文章异步处理失败，数据库事务将回滚: userId={}, articleId={}, errorCode={}, errorMessage={}",
                    article.getUserId(), article.getId(), e.getErrorCode(), e.getErrorMessage(), e);
            throw e;
        } catch (Exception e) {
            creditReservationService.release(context.processingNo());
            deleteOriginalFile(article);
            log.error("文章异步处理失败，数据库事务将回滚: userId={}, articleId={}", article.getUserId(), article.getId(), e);
            throw new ClientException(BaseErrorCode.SERVICE_ERROR);
        }
    }

    private ArticleTranslationResultDTO translateArticleContent(BizArticlesDO article,
                                                                String originalContent,
                                                                AiProcessingContext context) {
        article.setTranslationStatus(TRANSLATION_STATUS_PROCESSING);
        log.info("开始翻译文章内容: userId={}, articleId={}, contentLength={}",
                article.getUserId(), article.getId(), originalContent.length());
        ArticleTranslationResultDTO translationResult = articleTranslationService.translate(originalContent, context);
        log.info("文章内容翻译完成: userId={}, articleId={}, translationStatus={}, translated={}",
                article.getUserId(), article.getId(), translationResult.getTranslationStatus(), translationResult.getTranslated());
        return translationResult;
    }

    private void registerCreditSettlementAfterCommit(String processingNo) {
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            creditReservationService.settle(processingNo);
            return;
        }
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                creditReservationService.settle(processingNo);
            }
        });
    }

    private void deleteOriginalFile(BizArticlesDO article) {
        boolean deleted = s3Util.deleteFile(article.getFilePath());
        log.info("文章原文件清理完成: userId={}, articleId={}, ossDeleted={}", article.getUserId(), article.getId(), deleted);
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
}
