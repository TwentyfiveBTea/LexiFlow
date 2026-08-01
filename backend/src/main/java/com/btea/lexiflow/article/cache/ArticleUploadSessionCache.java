package com.btea.lexiflow.article.cache;

import com.btea.lexiflow.common.convention.errorcode.BaseErrorCode;
import com.btea.lexiflow.common.convention.exception.ClientException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.btea.lexiflow.article.constant.ArticleRedisConstant.UPLOAD_SESSION_KEY_PREFIX;
import static com.btea.lexiflow.article.constant.ArticleRedisConstant.UPLOAD_SESSION_TTL;

/**
 * @Author: TwentyfiveBTea
 * @Date: 2026/8/1
 * @Description: 文章直传会话Redis缓存
 */
@Component
@RequiredArgsConstructor
public class ArticleUploadSessionCache {

    private static final String STATUS_PENDING = "PENDING";
    private static final String STATUS_PROCESSING = "PROCESSING";
    private static final String STATUS_COMPLETED = "COMPLETED";

    private static final DefaultRedisScript<Long> CLAIM_SCRIPT = new DefaultRedisScript<>("""
            local status = redis.call('HGET', KEYS[1], 'status')
            if not status then
                return 0
            end
            if status == 'COMPLETED' then
                return 2
            end
            if status ~= 'PENDING' then
                return -1
            end
            redis.call('HSET', KEYS[1], 'status', 'PROCESSING')
            redis.call('EXPIRE', KEYS[1], ARGV[1])
            return 1
            """, Long.class);

    private final StringRedisTemplate stringRedisTemplate;

    /**
     * 创建文章直传会话
     *
     * @param session 上传会话
     */
    public void create(ArticleUploadSession session) {
        Map<String, String> values = new HashMap<>();
        values.put("articleId", session.articleId());
        values.put("userId", session.userId());
        values.put("objectKey", session.objectKey());
        values.put("filename", session.filename());
        values.put("fileType", session.fileType());
        values.put("contentType", session.contentType());
        values.put("fileSize", String.valueOf(session.fileSize()));
        values.put("status", STATUS_PENDING);
        try {
            String key = sessionKey(session.articleId());
            stringRedisTemplate.opsForHash().putAll(key, values);
            stringRedisTemplate.expire(key, UPLOAD_SESSION_TTL);
        } catch (Exception e) {
            throw new ClientException(BaseErrorCode.SERVICE_ERROR);
        }
    }

    /**
     * 查询文章直传会话
     *
     * @param articleId 文章ID
     * @return 上传会话，不存在时返回null
     */
    public ArticleUploadSession get(String articleId) {
        try {
            Map<Object, Object> values = stringRedisTemplate.opsForHash().entries(sessionKey(articleId));
            if (values.isEmpty()) {
                return null;
            }
            return new ArticleUploadSession(
                    value(values, "articleId"),
                    value(values, "userId"),
                    value(values, "objectKey"),
                    value(values, "filename"),
                    value(values, "fileType"),
                    value(values, "contentType"),
                    Long.parseLong(value(values, "fileSize")),
                    value(values, "status"));
        } catch (ClientException e) {
            throw e;
        } catch (Exception e) {
            throw new ClientException(BaseErrorCode.SERVICE_ERROR);
        }
    }

    /**
     * 原子占用待完成的上传会话
     *
     * @param articleId 文章ID
     * @return 1=占用成功，2=已经完成，0=不存在，-1=正在处理
     */
    public long claim(String articleId) {
        try {
            Long result = stringRedisTemplate.execute(CLAIM_SCRIPT,
                    List.of(sessionKey(articleId)),
                    String.valueOf(UPLOAD_SESSION_TTL.toSeconds()));
            return result == null ? 0L : result;
        } catch (Exception e) {
            throw new ClientException(BaseErrorCode.SERVICE_ERROR);
        }
    }

    /**
     * 将上传会话标记为已完成
     *
     * @param articleId 文章ID
     */
    public void markCompleted(String articleId) {
        updateStatus(articleId, STATUS_COMPLETED);
    }

    /**
     * 释放未成功提交的上传会话
     *
     * @param articleId 文章ID
     */
    public void releaseClaim(String articleId) {
        updateStatus(articleId, STATUS_PENDING);
    }

    /**
     * 删除文章直传会话
     *
     * @param articleId 文章ID
     */
    public void delete(String articleId) {
        try {
            stringRedisTemplate.delete(sessionKey(articleId));
        } catch (Exception e) {
            throw new ClientException(BaseErrorCode.SERVICE_ERROR);
        }
    }

    private void updateStatus(String articleId, String status) {
        try {
            String key = sessionKey(articleId);
            stringRedisTemplate.opsForHash().put(key, "status", status);
            stringRedisTemplate.expire(key, UPLOAD_SESSION_TTL);
        } catch (Exception e) {
            throw new ClientException(BaseErrorCode.SERVICE_ERROR);
        }
    }

    private String value(Map<Object, Object> values, String field) {
        Object value = values.get(field);
        if (value == null) {
            throw new ClientException(BaseErrorCode.ARTICLE_UPLOAD_SESSION_NOT_FOUND);
        }
        return String.valueOf(value);
    }

    private String sessionKey(String articleId) {
        return UPLOAD_SESSION_KEY_PREFIX + articleId;
    }
}
