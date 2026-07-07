package com.btea.lexiflow.article.llm;

import com.btea.lexiflow.common.convention.errorcode.BaseErrorCode;
import com.btea.lexiflow.common.convention.exception.ClientException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * @Author: TwentyfiveBTea
 * @Date: 2026/7/6 10:08
 * @Description: 文章大模型 JSON 响应解析器
 */
@Component
@RequiredArgsConstructor
public class ArticleLlmResponseParser {

    private final ObjectMapper objectMapper;

    /**
     * 将大模型响应解析为指定类型
     *
     * @param response 大模型响应文本
     * @param clazz 目标类型
     * @return 解析后的对象
     */
    public <T> T parse(String response, Class<T> clazz) {
        try {
            return objectMapper.readValue(extractJson(response), clazz);
        } catch (Exception e) {
            throw new ClientException(BaseErrorCode.AI_RESPONSE_PARSE_FAILED);
        }
    }

    private String extractJson(String response) {
        if (response == null || response.isBlank()) {
            throw new ClientException(BaseErrorCode.AI_RESPONSE_PARSE_FAILED);
        }
        String text = response.trim();
        if (text.startsWith("```json")) {
            text = text.substring("```json".length()).trim();
        } else if (text.startsWith("```")) {
            text = text.substring("```".length()).trim();
        }
        if (text.endsWith("```")) {
            text = text.substring(0, text.length() - "```".length()).trim();
        }

        int objectStart = text.indexOf('{');
        int objectEnd = text.lastIndexOf('}');
        if (objectStart < 0 || objectEnd < objectStart) {
            throw new ClientException(BaseErrorCode.AI_RESPONSE_PARSE_FAILED);
        }
        return text.substring(objectStart, objectEnd + 1);
    }
}
