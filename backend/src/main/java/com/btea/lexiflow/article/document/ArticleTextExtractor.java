package com.btea.lexiflow.article.document;

import com.btea.lexiflow.article.llm.ArticleOcrProperties;
import com.btea.lexiflow.common.convention.errorcode.BaseErrorCode;
import com.btea.lexiflow.common.convention.exception.ClientException;
import com.btea.lexiflow.pay.model.AiProcessingContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.sax.BodyContentHandler;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Locale;

/**
 * @Author: TwentyfiveBTea
 * @Date: 2026/7/5 01:20
 * @Description: 文章文本解析器
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ArticleTextExtractor {

    private static final String PDF_EXTENSION = ".pdf";
    private static final String PDF_CONTENT_TYPE = "application/pdf";

    private final ArticlePdfOcrExtractor articlePdfOcrExtractor;
    private final ArticleOcrProperties articleOcrProperties;

    /**
     * 提取文章纯文本
     *
     * @param fileBytes 文件字节数组
     * @param filename 文件名
     * @param contentType 文件 MIME 类型
     * @param context AI处理计费上下文
     * @return 纯文本
     */
    public String extractText(byte[] fileBytes,
                              String filename,
                              String contentType,
                              AiProcessingContext context) {
        Metadata metadata = new Metadata();
        String tikaText = extractTextByTika(fileBytes, metadata);
        long meaningfulChars = countMeaningfulChars(tikaText);
        log.info("Tika 文章文本解析完成: filename={}, contentType={}, tikaContentType={}, textLength={}, meaningfulChars={}",
                filename, contentType, metadata.get(Metadata.CONTENT_TYPE), tikaText.length(), meaningfulChars);

        // 如果 Tika 解析的文本长度小于阈值，则使用 OCR 技术进行补充
        if (meaningfulChars >= getMinTextLength()) {
            return tikaText;
        }
        if (isPdf(filename, contentType, metadata)) {
            log.info("Tika 解析文本过短，开始使用 OCR 解析 PDF: filename={}, textLength={}, meaningfulChars={}",
                    filename, tikaText.length(), meaningfulChars);
            return articlePdfOcrExtractor.extractText(fileBytes, context);
        }
        throw new ClientException(BaseErrorCode.FILE_PARSE_FAILED);
    }

    private String extractTextByTika(byte[] fileBytes, Metadata metadata) {
        try (InputStream inputStream = new ByteArrayInputStream(fileBytes)) {
            AutoDetectParser parser = new AutoDetectParser();
            BodyContentHandler handler = new BodyContentHandler(-1);
            parser.parse(inputStream, handler, metadata, new ParseContext());
            return normalizeText(handler.toString());
        } catch (Exception e) {
            log.error("文章文本解析失败", e);
            throw new ClientException(BaseErrorCode.FILE_PARSE_FAILED);
        }
    }

    private boolean isPdf(String filename, String contentType, Metadata metadata) {
        if (filename != null && filename.toLowerCase(Locale.ROOT).endsWith(PDF_EXTENSION)) {
            return true;
        }
        if (contentType != null && PDF_CONTENT_TYPE.equalsIgnoreCase(contentType)) {
            return true;
        }
        String tikaContentType = metadata.get(Metadata.CONTENT_TYPE);
        return tikaContentType != null && tikaContentType.toLowerCase(Locale.ROOT).contains("pdf");
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
}
