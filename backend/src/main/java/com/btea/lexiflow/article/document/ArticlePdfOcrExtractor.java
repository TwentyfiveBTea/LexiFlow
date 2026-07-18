package com.btea.lexiflow.article.document;

import cn.hutool.core.util.IdUtil;
import com.btea.lexiflow.article.llm.ArticleOcrProperties;
import com.btea.lexiflow.article.llm.prompt.ArticleOcrPrompt;
import com.btea.lexiflow.common.convention.errorcode.BaseErrorCode;
import com.btea.lexiflow.common.convention.exception.ClientException;
import com.btea.lexiflow.pay.constant.AiUsageConstant;
import com.btea.lexiflow.pay.model.AiProcessingContext;
import com.btea.lexiflow.pay.service.AiRequestReservationEstimator;
import com.btea.lexiflow.pay.service.AiUsageService;
import com.btea.lexiflow.pay.service.CreditReservationService;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.*;
import java.util.function.IntFunction;

/**
 * @Author: TwentyfiveBTea
 * @Date: 2026/7/7 15:00
 * @Description: 文章 PDF OCR 解析器
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ArticlePdfOcrExtractor {

    private static final String CHAT_COMPLETIONS_PATH = "chat/completions";

    private final ArticleOcrProperties articleOcrProperties;
    private final AiUsageService aiUsageService;
    private final CreditReservationService creditReservationService;
    private final AiRequestReservationEstimator reservationEstimator;

    /**
     * OCR 提取 PDF 图片文本
     *
     * @param fileBytes PDF 文件字节数组
     * @param context AI处理计费上下文
     * @return OCR 文本
     */
    public String extractText(byte[] fileBytes, AiProcessingContext context) {
        if (!Boolean.TRUE.equals(articleOcrProperties.getEnabled())) {
            throw new ClientException(BaseErrorCode.FILE_PARSE_FAILED);
        }
        if (articleOcrProperties.getApiKey() == null || articleOcrProperties.getApiKey().isBlank()) {
            log.error("文章 PDF OCR 失败: Gemini API Key 未配置");
            throw new ClientException(BaseErrorCode.FILE_PARSE_FAILED);
        }

        try (PDDocument document = Loader.loadPDF(fileBytes)) {
            int pageCount = document.getNumberOfPages();
            int maxPages = getMaxPages();
            if (pageCount > maxPages) {
                log.error("文章 PDF OCR 失败: pageCount={}, maxPages={}", pageCount, maxPages);
                throw new ClientException(BaseErrorCode.FILE_PARSE_FAILED);
            }

            log.info("开始文章 PDF OCR: pageCount={}, renderDpi={}", pageCount, getRenderDpi());
            PDFRenderer renderer = new PDFRenderer(document);
            List<String> pageTexts = new ArrayList<>();
            for (int pageIndex = 0; pageIndex < pageCount; pageIndex++) {
                byte[] imageBytes = renderPage(renderer, pageIndex);
                if (imageBytes.length > getMaxImageBytes()) {
                    log.error("文章 PDF OCR 页面图片过大: page={}, imageBytes={}, maxImageBytes={}",
                            pageIndex + 1, imageBytes.length, getMaxImageBytes());
                    throw new ClientException(BaseErrorCode.FILE_PARSE_FAILED);
                }
                int pageNumber = pageIndex + 1;
                String requestNo = IdUtil.getSnowflakeNextIdStr();
                long estimatedCredits = reservationEstimator.estimateOcr(articleOcrProperties.getMaxOutputTokens());
                creditReservationService.reserveAdditional(
                        context, "OCR:" + pageNumber, estimatedCredits);
                String pageText = callOcrWithRetry(attemptNo -> callGeminiOcr(
                        imageBytes, context, requestNo, attemptNo, pageNumber));
                pageTexts.add(pageText);
                log.info("文章 PDF OCR 页面完成: page={}, imageBytes={}, textLength={}",
                        pageIndex + 1, imageBytes.length, pageText.length());
            }

            String result = normalizeText(String.join("\n\n", pageTexts));
            if (countMeaningfulChars(result) < getMinTextLength()) {
                log.error("文章 PDF OCR 结果过短: textLength={}, meaningfulChars={}, minTextLength={}",
                        result.length(), countMeaningfulChars(result), getMinTextLength());
                throw new ClientException(BaseErrorCode.FILE_PARSE_FAILED);
            }
            log.info("文章 PDF OCR 完成: pageCount={}, textLength={}, meaningfulChars={}",
                    pageCount, result.length(), countMeaningfulChars(result));
            return result;
        } catch (ClientException e) {
            throw e;
        } catch (Exception e) {
            log.error("文章 PDF OCR 解析失败", e);
            throw new ClientException(BaseErrorCode.FILE_PARSE_FAILED);
        }
    }

    private byte[] renderPage(PDFRenderer renderer, int pageIndex) throws Exception {
        BufferedImage image = renderer.renderImageWithDPI(pageIndex, getRenderDpi(), ImageType.RGB);
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            ImageIO.write(image, getImageFormat(), outputStream);
            return outputStream.toByteArray();
        }
    }

    private String callGeminiOcr(byte[] imageBytes,
                                 AiProcessingContext context,
                                 String requestNo,
                                 int attemptNo,
                                 int pageNumber) {
        String usageId = aiUsageService.startAttempt(
                context,
                requestNo,
                attemptNo,
                AiUsageConstant.SCENE_PDF_OCR,
                pageNumber,
                "GEMINI",
                articleOcrProperties.getModelName());
        try {
            String imageBase64 = Base64.getEncoder().encodeToString(imageBytes);
            GeminiRequest request = GeminiRequest.builder()
                    .model(articleOcrProperties.getModelName())
                    .messages(List.of(GeminiMessage.user(List.of(
                            GeminiContent.text(ArticleOcrPrompt.PAGE_OCR_PROMPT),
                            GeminiContent.image("data:image/" + getImageFormat() + ";base64," + imageBase64)
                    ))))
                    .maxTokens(articleOcrProperties.getMaxOutputTokens())
                    .build();

            GeminiResponse response = RestClient.create()
                    .post()
                    .uri(buildChatCompletionsUrl())
                    .contentType(MediaType.APPLICATION_JSON)
                    .headers(headers -> headers.setBearerAuth(articleOcrProperties.getApiKey()))
                    .body(request)
                    .retrieve()
                    .body(GeminiResponse.class);

            if (response == null || response.getChoices() == null || response.getChoices().isEmpty()
                    || response.getChoices().get(0).getMessage() == null
                    || response.getChoices().get(0).getMessage().getContent() == null) {
                aiUsageService.completeFailure(usageId, "OCR响应内容无效");
                throw new ClientException(BaseErrorCode.FILE_PARSE_FAILED);
            }
            GeminiUsage usage = response.getUsage();
            if (usage == null || usage.getPromptTokens() == null || usage.getCompletionTokens() == null) {
                aiUsageService.completeUnknown(usageId, "OCR响应缺少Token Usage");
                throw new ClientException(BaseErrorCode.AI_USAGE_INVALID);
            }
            aiUsageService.completeSuccess(
                    usageId,
                    response.getId(),
                    usage.getPromptTokens(),
                    usage.getCompletionTokens());
            creditReservationService.ensureActualUsageCovered(
                    context, requestNo + ":" + attemptNo);
            return normalizeText(response.getChoices().get(0).getMessage().getContent());
        } catch (ClientException e) {
            throw e;
        } catch (RuntimeException e) {
            aiUsageService.completeUnknown(usageId, getErrorMessage(e));
            throw e;
        }
    }

    private String callOcrWithRetry(IntFunction<String> function) {
        int maxRetryTimes = getMaxRetryTimes();
        RuntimeException lastException = null;
        for (int i = 0; i <= maxRetryTimes; i++) {
            try {
                return function.apply(i + 1);
            } catch (RuntimeException e) {
                if (isBillingFailure(e)) {
                    throw e;
                }
                lastException = e;
                if (i < maxRetryTimes) {
                    log.warn("文章 PDF OCR 调用失败，准备重试: currentRetry={}, maxRetry={}, error={}",
                            i, maxRetryTimes, getErrorMessage(e));
                } else {
                    log.error("文章 PDF OCR 调用最终失败: retryTimes={}, error={}",
                            maxRetryTimes, getErrorMessage(e), e);
                }
            }
        }
        if (lastException instanceof ClientException) {
            throw lastException;
        }
        throw new ClientException(BaseErrorCode.FILE_PARSE_FAILED);
    }

    private boolean isBillingFailure(Throwable throwable) {
        if (!(throwable instanceof ClientException clientException)) {
            return false;
        }
        String errorCode = clientException.getErrorCode();
        return BaseErrorCode.CREDIT_BALANCE_INSUFFICIENT.code().equals(errorCode)
                || BaseErrorCode.CREDIT_ACCOUNT_FROZEN.code().equals(errorCode)
                || BaseErrorCode.CREDIT_RESERVATION_CONFLICT.code().equals(errorCode);
    }

    private String getErrorMessage(Throwable e) {
        if (e == null) {
            return "unknown";
        }
        String message = e.getMessage();
        return e.getClass().getSimpleName() + (message == null || message.isBlank() ? "" : ": " + message);
    }

    private String buildChatCompletionsUrl() {
        String baseUrl = articleOcrProperties.getBaseUrl();
        if (baseUrl == null || baseUrl.isBlank()) {
            throw new ClientException(BaseErrorCode.FILE_PARSE_FAILED);
        }
        String normalizedBaseUrl = baseUrl.endsWith("/") ? baseUrl : baseUrl + "/";
        return normalizedBaseUrl.endsWith(CHAT_COMPLETIONS_PATH + "/")
                ? normalizedBaseUrl.substring(0, normalizedBaseUrl.length() - 1)
                : normalizedBaseUrl + CHAT_COMPLETIONS_PATH;
    }

    private String normalizeText(String text) {
        if (text == null) {
            return "";
        }
        return text.replace("\r\n", "\n")
                .replace("\r", "\n")
                .replaceAll("[ \t]+", " ")
                .replaceAll("\n{3,}", "\n\n")
                .trim();
    }

    private long countMeaningfulChars(String text) {
        if (text == null || text.isBlank()) {
            return 0;
        }
        return text.codePoints()
                .filter(Character::isLetterOrDigit)
                .count();
    }

    private int getMinTextLength() {
        return articleOcrProperties.getMinTextLength() == null
                ? 80
                : Math.max(articleOcrProperties.getMinTextLength(), 1);
    }

    private int getMaxPages() {
        return articleOcrProperties.getMaxPages() == null
                ? 300
                : Math.max(articleOcrProperties.getMaxPages(), 1);
    }

    private int getRenderDpi() {
        return articleOcrProperties.getRenderDpi() == null
                ? 180
                : Math.max(articleOcrProperties.getRenderDpi(), 72);
    }

    private String getImageFormat() {
        String imageFormat = articleOcrProperties.getImageFormat();
        if (imageFormat == null || imageFormat.isBlank()) {
            return "png";
        }
        return imageFormat.trim().toLowerCase(Locale.ROOT);
    }

    private int getMaxImageBytes() {
        return articleOcrProperties.getMaxImageBytes() == null
                ? 4 * 1024 * 1024
                : Math.max(articleOcrProperties.getMaxImageBytes(), 1);
    }

    private int getMaxRetryTimes() {
        return articleOcrProperties.getMaxRetryTimes() == null
                ? 3
                : Math.max(articleOcrProperties.getMaxRetryTimes(), 0);
    }

    @lombok.Builder
    private record GeminiRequest(String model,
                                 List<GeminiMessage> messages,
                                 @JsonProperty("max_tokens") Integer maxTokens) {
    }

    private record GeminiMessage(String role, List<GeminiContent> content) {

        private static GeminiMessage user(List<GeminiContent> content) {
            return new GeminiMessage("user", content);
        }
    }

    private record GeminiContent(String type,
                                 String text,
                                 @JsonProperty("image_url") Map<String, String> imageUrl) {

        private static GeminiContent text(String text) {
            return new GeminiContent("text", text, null);
        }

        private static GeminiContent image(String imageUrl) {
            return new GeminiContent("image_url", null, Map.of("url", imageUrl));
        }
    }

    @Data
    private static class GeminiResponse {
        private String id;
        private List<GeminiChoice> choices;
        private GeminiUsage usage;
    }

    @Data
    private static class GeminiUsage {
        @JsonProperty("prompt_tokens")
        private Long promptTokens;

        @JsonProperty("completion_tokens")
        private Long completionTokens;
    }

    @Data
    private static class GeminiChoice {
        private GeminiResponseMessage message;
    }

    @Data
    private static class GeminiResponseMessage {
        private String content;
    }
}
