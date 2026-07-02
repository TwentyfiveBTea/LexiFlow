package com.btea.lexiflow.infrastructure.security.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

/**
 * @Author: TwentyfiveBTea
 * @Date: 2026/7/2 13:05
 * @Description: JWT 配置类
 */
@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "jwt")
public class JwtConfig {

    /**
     * JWT签名密钥
     */
    private String secret;

    /**
     * Token过期时间
     */
    private Duration expiration;

    /**
     * 请求头名称
     */
    private String header = "Authorization";

    /**
     * Token前缀
     */
    private String prefix = "Bearer ";

    /**
     * Token前缀长度
     */
    private Integer prefixLength = 7;
}
