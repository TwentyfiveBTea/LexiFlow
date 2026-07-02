package com.btea.lexiflow.infrastructure.s3;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * @Author: TwentyfiveBTea
 * @Date: 2026/7/2 14:30
 * @Description: S3 配置类
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "aws.s3")
public class S3Config {

    /**
     * endpoint
     */
    private String endpoint;

    /**
     * 区域
     */
    private String region;

    /**
     * 存储桶名称
     */
    private String bucketName;

    /**
     * 访问id
     */
    private String accessKeyId;

    /**
     * 访问密钥
     */
    private String secretAccessKey;

    /**
     * 是否使用 path style 访问方式
     */
    private boolean pathStyleAccess;
}
