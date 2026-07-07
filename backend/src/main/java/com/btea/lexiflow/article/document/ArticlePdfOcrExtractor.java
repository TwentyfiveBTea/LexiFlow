package com.btea.lexiflow.article.document;

import com.btea.lexiflow.article.llm.ArticleOcrProperties;
import com.btea.lexiflow.article.llm.prompt.ArticleOcrPrompt;
import com.btea.lexiflow.common.convention.errorcode.BaseErrorCode;
import com.btea.lexiflow.common.convention.exception.ClientException;
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
import java.util.function.Supplier;

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

    /**
     * OCR 提取 PDF 图片文本
     *
     * @param fileBytes PDF 文件字节数组
     * @return OCR 文本
     */
    public String extractText(byte[] fileBytes) {
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
                String pageText = callOcrWithRetry(() -> callGeminiOcr(imageBytes));
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

    private String callGeminiOcr(byte[] imageBytes) {
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
            throw new ClientException(BaseErrorCode.FILE_PARSE_FAILED);
        }
        return normalizeText(response.getChoices().get(0).getMessage().getContent());
    }

    private String callOcrWithRetry(Supplier<String> supplier) {
        int maxRetryTimes = getMaxRetryTimes();
        RuntimeException lastException = null;
        for (int i = 0; i <= maxRetryTimes; i++) {
            try {
                return supplier.get();
            } catch (RuntimeException e) {
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
        private List<GeminiChoice> choices;
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
