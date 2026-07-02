package com.lexiflow.infrastructure.security.util;

import com.lexiflow.common.constant.RedisCacheConstant;
import com.lexiflow.common.convention.exception.ClientException;
import com.lexiflow.infrastructure.security.config.JwtConfig;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * @Author: TwentyfiveBTea
 * @Date: 2026/7/2 15:57
 * @Description: JWT 工具类
 */
@Slf4j
@Component
public class JwtUtil {

    private final JwtConfig jwtConfig;
    private final SecretKey secretKey;
    private final StringRedisTemplate stringRedisTemplate;

    public JwtUtil(JwtConfig jwtConfig, StringRedisTemplate stringRedisTemplate) {
        this.jwtConfig = jwtConfig;
        this.stringRedisTemplate = stringRedisTemplate;
        this.secretKey = Keys.hmacShaKeyFor(jwtConfig.getSecret().getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 生成用户 Token
     */
    public String generateUserToken(String userId) {
        String token = generateToken(userId);
        String key = RedisCacheConstant.USER_TOKEN_KEY_PREFIX + userId;
        stringRedisTemplate.opsForValue().set(key, token, jwtConfig.getExpiration().toMillis(), TimeUnit.MILLISECONDS);
        return token;
    }

    /**
     * 生成 Token
     */
    private String generateToken(String subject) {
        Date now = new Date();
        Date expiration = new Date(now.getTime() + jwtConfig.getExpiration().toMillis());
        return Jwts.builder()
                .subject(subject)
                .issuedAt(now)
                .expiration(expiration)
                .signWith(secretKey, Jwts.SIG.HS256)
                .compact();
    }

    /**
     * 创建 Token 解析器
     */
    private JwtParser createParser() {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build();
    }

    /**
     * 解析 Token
     */
    public Claims parseToken(String token) {
        try {
            return createParser()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (ExpiredJwtException e) {
            log.warn("Token已过期: {}", e.getMessage());
            throw new ClientException("Token已过期，请重新登录");
        } catch (SignatureException e) {
            log.warn("Token签名验证失败: {}", e.getMessage());
            throw new ClientException("Token无效");
        } catch (MalformedJwtException e) {
            log.warn("Token格式错误: {}", e.getMessage());
            throw new ClientException("Token格式错误");
        } catch (IllegalArgumentException e) {
            log.warn("Token为空: {}", e.getMessage());
            throw new ClientException("Token不能为空");
        } catch (Exception e) {
            log.error("Token解析失败: {}", e.getMessage());
            throw new ClientException("Token解析失败");
        }
    }

    /**
     * 解析用户 ID
     */
    public String parseUserId(String token) {
        Claims claims = parseToken(token);
        return claims.getSubject();
    }

    /**
     * 验证 Token 是否有效
     */
    public boolean validateToken(String token) {
        try {
            parseToken(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 从请求头提取 Token
     */
    public String extractTokenFromHeader(String authHeader) {
        if (authHeader == null || !authHeader.startsWith(jwtConfig.getPrefix())) {
            throw new ClientException("Authorization请求头格式错误，应为: " + jwtConfig.getPrefix() + "xxx");
        }
        return authHeader.substring(jwtConfig.getPrefixLength());
    }

    /**
     * 退出登录 - 删除 Redis 中的用户 Token
     *
     * @param userId 用户ID
     */
    public void logout(String userId) {
        String key = RedisCacheConstant.USER_TOKEN_KEY_PREFIX + userId;
        stringRedisTemplate.delete(key);
        log.info("用户 {} 已退出登录，Token 已从 Redis 中删除", userId);
    }

    /**
     * 检查用户 Token 是否存在于 Redis 中
     *
     * @param userId 用户ID
     * @return 是否存在
     */
    public boolean isTokenExists(String userId) {
        String key = RedisCacheConstant.USER_TOKEN_KEY_PREFIX + userId;
        return Boolean.TRUE.equals(stringRedisTemplate.hasKey(key));
    }

    /**
     * 检查请求 Token 是否与 Redis 中的用户 Token 一致
     *
     * @param userId 用户ID
     * @param token 请求 Token
     * @return 是否一致
     */
    public boolean isTokenMatched(String userId, String token) {
        String key = RedisCacheConstant.USER_TOKEN_KEY_PREFIX + userId;
        String cachedToken = stringRedisTemplate.opsForValue().get(key);
        return token.equals(cachedToken);
    }

    /**
     * 获取 JwtConfig
     *
     * @return JwtConfig
     */
    public JwtConfig getJwtConfig() {
        return jwtConfig;
    }
}
