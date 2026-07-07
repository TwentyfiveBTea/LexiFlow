package com.btea.lexiflow.article.service.impl;

import com.btea.lexiflow.article.dto.resp.ArticleTranslatedParagraphDTO;
import com.btea.lexiflow.article.dto.resp.ArticleTranslationChunkDTO;
import com.btea.lexiflow.article.dto.resp.ArticleTranslationChunkRespDTO;
import com.btea.lexiflow.article.dto.resp.ArticleTranslationProfileDTO;
import com.btea.lexiflow.article.dto.resp.ArticleTranslationResultDTO;
import com.btea.lexiflow.article.dto.resp.ArticleTranslationTermDTO;
import com.btea.lexiflow.article.llm.ArticleLlmResponseParser;
import com.btea.lexiflow.article.llm.ArticleParagraphSplitter;
import com.btea.lexiflow.article.llm.ArticleTranslationProperties;
import com.btea.lexiflow.article.llm.prompt.ArticleTranslationPrompt;
import com.btea.lexiflow.article.service.ArticleTranslationService;
import com.btea.lexiflow.common.convention.errorcode.BaseErrorCode;
import com.btea.lexiflow.common.convention.exception.ClientException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.ChatModel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static com.btea.lexiflow.article.constant.ArticleConstant.TRANSLATION_STATUS_PENDING;
import static com.btea.lexiflow.article.constant.ArticleConstant.TRANSLATION_STATUS_SUCCESS;

/**
 * @Author: TwentyfiveBTea
 * @Date: 2026/7/6 10:13
 * @Description: 文章翻译服务实现类
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class ArticleTranslationServiceImpl implements ArticleTranslationService {

    private final ChatModel chatModel;
    private final ObjectMapper objectMapper;
    private final ArticleParagraphSplitter articleParagraphSplitter;
    private final ArticleLlmResponseParser articleLlmResponseParser;
    private final ArticleTranslationProperties articleTranslationProperties;

    /**
     * 翻译文章正文
     *
     * @param originalContent 原始正文内容
     * @return 翻译结果
     */
    @Override
    public ArticleTranslationResultDTO translate(String originalContent) {
        if (originalContent == null || originalContent.isBlank()) {
            throw new ClientException(BaseErrorCode.ARTICLE_TRANSLATION_FAILED);
        }
        log.info("开始翻译文章正文: contentLength={}", originalContent.length());
        // 作为兜底，但是一般不会走这块代码
        if (!Boolean.TRUE.equals(articleTranslationProperties.getEnabled())) {
            log.info("文章翻译功能未启用，返回原文内容: contentLength={}", originalContent.length());
            return ArticleTranslationResultDTO.builder()
                    .parsedContent(originalContent)
                    .translationStatus(TRANSLATION_STATUS_PENDING)
                    .translated(false)
                    .build();
        }

        List<String> paragraphs = articleParagraphSplitter.splitParagraphs(originalContent);
        if (paragraphs.isEmpty()) {
            throw new ClientException(BaseErrorCode.ARTICLE_TRANSLATION_FAILED);
        }
        log.info("文章正文段落切分完成: paragraphCount={}", paragraphs.size());

        // 构建文章翻译全局上下文资料
        ArticleTranslationProfileDTO profile = buildGlobalProfile(originalContent);
        Map<String, ArticleTranslationTermDTO> dynamicTerms = new LinkedHashMap<>();
        addTerms(dynamicTerms, profile.getTerms());
        log.info("文章翻译全局上下文构建完成: initialTermCount={}, entityCount={}",
                dynamicTerms.size(), profile.getEntities().size());

        // 构建文章翻译分块
        List<ArticleTranslationChunkDTO> chunks = buildChunks(paragraphs);
        log.info("文章翻译分块构建完成: paragraphCount={}, chunkCount={}", paragraphs.size(), chunks.size());
        List<ArticleTranslatedParagraphDTO> translatedParagraphs = new ArrayList<>();
        String previousChunkSummary = "无";
        String previousChunkTail = "无";

        for (ArticleTranslationChunkDTO chunk : chunks) {
            log.info("开始翻译文章分块: chunkIndex={}, startParagraphIndex={}, paragraphCount={}, dynamicTermCount={}",
                    chunk.getChunkIndex(), chunk.getStartParagraphIndex(), chunk.getParagraphs().size(), dynamicTerms.size());
            // 对每个分块进行翻译
            ArticleTranslationChunkRespDTO chunkResult = translateChunk(chunk, profile, dynamicTerms, previousChunkSummary, previousChunkTail);
            // 规范分块翻译
            List<ArticleTranslatedParagraphDTO> normalizedParagraphs = normalizeChunkResult(chunk, chunkResult);
            translatedParagraphs.addAll(normalizedParagraphs);

            if (Boolean.TRUE.equals(articleTranslationProperties.getDynamicTermsEnabled())) {
                addTerms(dynamicTerms, chunkResult.getNewTerms());
            }
            previousChunkSummary = chunkResult.getChunkSummary() == null || chunkResult.getChunkSummary().isBlank()
                    ? previousChunkSummary
                    : chunkResult.getChunkSummary();
            previousChunkTail = buildPreviousChunkTail(translatedParagraphs);
            log.info("文章分块翻译完成: chunkIndex={}, translatedParagraphCount={}, dynamicTermCount={}",
                    chunk.getChunkIndex(), translatedParagraphs.size(), dynamicTerms.size());
        }

        String parsedContent = buildParsedContent(translatedParagraphs);
        log.info("文章正文翻译完成: paragraphCount={}, parsedContentLength={}", translatedParagraphs.size(), parsedContent.length());
        return ArticleTranslationResultDTO.builder()
                .parsedContent(parsedContent)
                .translationStatus(TRANSLATION_STATUS_SUCCESS)
                .translated(true)
                .build();
    }

    private ArticleTranslationProfileDTO buildGlobalProfile(String originalContent) {
        // 作为兜底，但是一般不会走这块代码
        if (!Boolean.TRUE.equals(articleTranslationProperties.getGlobalProfileEnabled())) {
            log.info("文章翻译全局上下文功能未启用，使用默认上下文: contentLength={}", originalContent.length());
            return ArticleTranslationProfileDTO.builder()
                    .summary("无")
                    .terms(List.of())
                    .entities(List.of())
                    .styleRules(List.of("译文自然、流畅，不要机器腔", "保留原文段落结构"))
                    .build();
        }

        log.info("开始构建文章翻译全局上下文: contentLength={}", originalContent.length());
        String userPrompt = ArticleTranslationPrompt.GLOBAL_PROFILE_USER_PROMPT
                .replace("{{article_content}}", originalContent);
        // 重试兜底调用 LLM
        String response = callLlmWithRetry(() -> callLlm(ArticleTranslationPrompt.GLOBAL_PROFILE_SYSTEM_PROMPT, userPrompt));
        ArticleTranslationProfileDTO profile = articleLlmResponseParser.parse(response, ArticleTranslationProfileDTO.class);
        if (profile.getTerms() == null) {
            profile.setTerms(List.of());
        }
        if (profile.getEntities() == null) {
            profile.setEntities(List.of());
        }
        if (profile.getStyleRules() == null) {
            profile.setStyleRules(List.of());
        }
        log.info("文章翻译全局上下文解析完成: termCount={}, entityCount={}, styleRuleCount={}",
                profile.getTerms().size(), profile.getEntities().size(), profile.getStyleRules().size());
        return profile;
    }

    private ArticleTranslationChunkRespDTO translateChunk(ArticleTranslationChunkDTO chunk,
                                                          ArticleTranslationProfileDTO profile,
                                                          Map<String, ArticleTranslationTermDTO> dynamicTerms,
                                                          String previousChunkSummary,
                                                          String previousChunkTail) {
        String userPrompt = ArticleTranslationPrompt.CHUNK_TRANSLATION_USER_PROMPT
                .replace("{{global_context_profile}}", toJson(profile))
                .replace("{{dynamic_terms}}", toJson(dynamicTerms.values()))
                .replace("{{previous_chunk_summary}}", previousChunkSummary)
                .replace("{{previous_chunk_tail}}", previousChunkTail)
                .replace("{{current_chunk}}", buildCurrentChunkPrompt(chunk));
        log.info("开始调用 LLM 翻译文章分块: chunkIndex={}, startParagraphIndex={}, paragraphCount={}",
                chunk.getChunkIndex(), chunk.getStartParagraphIndex(), chunk.getParagraphs().size());
        String response = callLlmWithRetry(() -> callLlm(ArticleTranslationPrompt.CHUNK_TRANSLATION_SYSTEM_PROMPT, userPrompt));
        ArticleTranslationChunkRespDTO chunkResult = articleLlmResponseParser.parse(response, ArticleTranslationChunkRespDTO.class);
        if (chunkResult.getParagraphs() == null) {
            throw new ClientException(BaseErrorCode.ARTICLE_TRANSLATION_FAILED);
        }
        if (chunkResult.getNewTerms() == null) {
            chunkResult.setNewTerms(List.of());
        }
        if (chunkResult.getOpenReferences() == null) {
            chunkResult.setOpenReferences(List.of());
        }
        return chunkResult;
    }

    private String callLlm(String systemPrompt, String userPrompt) {
        List<ChatMessage> messages = List.of(SystemMessage.from(systemPrompt), UserMessage.from(userPrompt));
        String response = chatModel.chat(messages).aiMessage().text();
        if (response == null || response.isBlank()) {
            throw new ClientException(BaseErrorCode.ARTICLE_TRANSLATION_FAILED);
        }
        return response.trim();
    }

    private String callLlmWithRetry(Supplier<String> supplier) {
        int maxRetryTimes = articleTranslationProperties.getMaxRetryTimes() == null
                ? 0
                : Math.max(articleTranslationProperties.getMaxRetryTimes(), 0);
        RuntimeException lastException = null;
        for (int i = 0; i <= maxRetryTimes; i++) {
            try {
                return supplier.get();
            } catch (RuntimeException e) {
                lastException = e;
                log.warn("文章翻译 LLM 调用失败，准备重试: currentRetry={}, maxRetry={}", i, maxRetryTimes, e);
            }
        }
        if (lastException instanceof ClientException) {
            throw lastException;
        }
        throw new ClientException(BaseErrorCode.ARTICLE_TRANSLATION_FAILED);
    }

    private List<ArticleTranslationChunkDTO> buildChunks(List<String> paragraphs) {
        int chunkSize = articleTranslationProperties.getMaxParagraphsPerChunk() == null
                ? 12
                : Math.max(articleTranslationProperties.getMaxParagraphsPerChunk(), 1);
        List<ArticleTranslationChunkDTO> chunks = new ArrayList<>();
        for (int start = 0; start < paragraphs.size(); start += chunkSize) {
            int end = Math.min(start + chunkSize, paragraphs.size());
            chunks.add(ArticleTranslationChunkDTO.builder()
                    .chunkIndex(chunks.size())
                    .startParagraphIndex(start)
                    .paragraphs(paragraphs.subList(start, end))
                    .build());
        }
        return chunks;
    }

    private String buildCurrentChunkPrompt(ArticleTranslationChunkDTO chunk) {
        StringBuilder builder = new StringBuilder();
        List<String> paragraphs = chunk.getParagraphs();
        for (int i = 0; i < paragraphs.size(); i++) {
            int paragraphIndex = chunk.getStartParagraphIndex() + i;
            builder.append("段落 ").append(paragraphIndex).append(":\n")
                    .append(paragraphs.get(i))
                    .append("\n\n");
        }
        return builder.toString().trim();
    }

    /**
     * 规范分块翻译：主要是防止模型返回顺序错乱、漏段、多段、重复段、原文被改写等问题
     */
    private List<ArticleTranslatedParagraphDTO> normalizeChunkResult(ArticleTranslationChunkDTO chunk,
                                                                      ArticleTranslationChunkRespDTO chunkResult) {
        Map<Integer, ArticleTranslatedParagraphDTO> translatedMap = chunkResult.getParagraphs().stream()
                .filter(Objects::nonNull)
                .filter(each -> each.getIndex() != null)
                .collect(Collectors.toMap(ArticleTranslatedParagraphDTO::getIndex, each -> each, (first, second) -> first));

        // 以原始 chunk 的段落顺序为准，逐段检查有没有译文
        List<ArticleTranslatedParagraphDTO> result = new ArrayList<>();
        for (int i = 0; i < chunk.getParagraphs().size(); i++) {
            int paragraphIndex = chunk.getStartParagraphIndex() + i;
            ArticleTranslatedParagraphDTO translated = translatedMap.get(paragraphIndex);
            if (translated == null || translated.getTranslation() == null || translated.getTranslation().isBlank()) {
                throw new ClientException(BaseErrorCode.ARTICLE_TRANSLATION_FAILED);
            }
            result.add(ArticleTranslatedParagraphDTO.builder()
                    .index(paragraphIndex)
                    .source(chunk.getParagraphs().get(i))
                    .translation(translated.getTranslation().trim())
                    .build());
        }
        return result;
    }

    private String buildPreviousChunkTail(List<ArticleTranslatedParagraphDTO> translatedParagraphs) {
        int tailSize = articleTranslationProperties.getOverlapTailSentences() == null
                ? 3
                : Math.max(articleTranslationProperties.getOverlapTailSentences(), 1);
        int start = Math.max(translatedParagraphs.size() - tailSize, 0);
        StringBuilder builder = new StringBuilder();
        for (int i = start; i < translatedParagraphs.size(); i++) {
            ArticleTranslatedParagraphDTO paragraph = translatedParagraphs.get(i);
            builder.append("段落 ").append(paragraph.getIndex()).append(" 原文：\n")
                    .append(paragraph.getSource()).append("\n")
                    .append("译文：\n")
                    .append(paragraph.getTranslation()).append("\n\n");
        }
        return builder.toString().trim();
    }

    private String buildParsedContent(List<ArticleTranslatedParagraphDTO> translatedParagraphs) {
        StringBuilder builder = new StringBuilder();
        for (ArticleTranslatedParagraphDTO paragraph : translatedParagraphs) {
            builder.append(paragraph.getSource()).append("\n")
                    .append(paragraph.getTranslation()).append("\n\n");
        }
        return builder.toString().trim();
    }

    private void addTerms(Map<String, ArticleTranslationTermDTO> dynamicTerms, List<ArticleTranslationTermDTO> terms) {
        if (!Boolean.TRUE.equals(articleTranslationProperties.getDynamicTermsEnabled()) || terms == null) {
            return;
        }
        for (ArticleTranslationTermDTO term : terms) {
            if (term == null || term.getSource() == null || term.getSource().isBlank()
                    || term.getTarget() == null || term.getTarget().isBlank()) {
                continue;
            }
            dynamicTerms.put(term.key(), term);
        }
    }

    private String toJson(Object object) {
        try {
            return objectMapper.writeValueAsString(object);
        } catch (Exception e) {
            throw new ClientException(BaseErrorCode.ARTICLE_TRANSLATION_FAILED);
        }
    }
}
