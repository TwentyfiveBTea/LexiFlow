package com.btea.lexiflow.infrastructure.s3;

import com.btea.lexiflow.common.convention.errorcode.BaseErrorCode;
import com.btea.lexiflow.common.convention.exception.ClientException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.ResponseTransformer;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3ClientBuilder;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.model.CopyObjectRequest;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.io.InputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.time.Duration;

/**
 * @Author: TwentyfiveBTea
 * @Date: 2026/7/2 16:07
 * @Description: S3 兼容存储工具类
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class S3Util {

    private final S3Config s3Config;

    /**
     * 创建 S3 客户端
     */
    public S3Client createClient() {
        S3ClientBuilder builder = S3Client.builder()
                .region(Region.of(s3Config.getRegion()))
                .credentialsProvider(credentialsProvider())
                .forcePathStyle(s3Config.isPathStyleAccess());

        if (s3Config.getEndpoint() != null && !s3Config.getEndpoint().isBlank()) {
            builder.endpointOverride(URI.create(s3Config.getEndpoint()));
        }

        return builder.build();
    }

    /**
     * 创建S3预签名上传地址
     *
     * @param objectKey 文件存储路径
     * @param contentType 文件MIME类型
     * @param duration 地址有效时间
     * @return 预签名上传地址
     */
    public String createPresignedUploadUrl(String objectKey, String contentType, Duration duration) {
        PutObjectRequest objectRequest = PutObjectRequest.builder()
                .bucket(s3Config.getBucketName())
                .key(objectKey)
                .contentType(contentType)
                .build();
        PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
                .signatureDuration(duration)
                .putObjectRequest(objectRequest)
                .build();
        try (S3Presigner presigner = createPresigner()) {
            PresignedPutObjectRequest request = presigner.presignPutObject(presignRequest);
            return request.url().toString();
        } catch (Exception e) {
            log.error("生成S3预签名上传地址失败，Bucket: {}, Key: {}", s3Config.getBucketName(), objectKey, e);
            throw new ClientException(BaseErrorCode.FILE_UPLOAD_FAILED);
        }
    }

    /**
     * 查询S3文件元数据
     *
     * @param objectKey 文件存储路径
     * @return 文件元数据
     */
    public HeadObjectResponse headObject(String objectKey) {
        try (S3Client s3Client = createClient()) {
            return s3Client.headObject(HeadObjectRequest.builder()
                    .bucket(s3Config.getBucketName())
                    .key(objectKey)
                    .build());
        } catch (Exception e) {
            log.error("查询S3文件失败，Bucket: {}, Key: {}", s3Config.getBucketName(), objectKey, e);
            throw new ClientException(BaseErrorCode.FILE_NOT_FOUND);
        }
    }

    /**
     * 将S3文件下载到本地路径
     *
     * @param objectKey 文件存储路径
     * @param destination 本地目标路径
     */
    public void downloadToFile(String objectKey, Path destination) {
        GetObjectRequest request = GetObjectRequest.builder()
                .bucket(s3Config.getBucketName())
                .key(objectKey)
                .build();
        try (S3Client s3Client = createClient()) {
            s3Client.getObject(request, ResponseTransformer.toFile(destination));
        } catch (Exception e) {
            log.error("下载S3文件失败，Bucket: {}, Key: {}", s3Config.getBucketName(), objectKey, e);
            throw new ClientException(BaseErrorCode.FILE_NOT_FOUND);
        }
    }

    /**
     * 在S3中复制文件
     *
     * @param sourceKey 源文件存储路径
     * @param targetKey 目标文件存储路径
     */
    public void copyFile(String sourceKey, String targetKey) {
        CopyObjectRequest request = CopyObjectRequest.builder()
                .copySource(s3Config.getBucketName() + "/" + sourceKey)
                .bucket(s3Config.getBucketName())
                .key(targetKey)
                .build();
        try (S3Client s3Client = createClient()) {
            s3Client.copyObject(request);
        } catch (Exception e) {
            log.error("复制S3文件失败，Bucket: {}, Source: {}, Target: {}",
                    s3Config.getBucketName(), sourceKey, targetKey, e);
            throw new ClientException(BaseErrorCode.FILE_UPLOAD_FAILED);
        }
    }

    /**
     * 上传文件到 S3
     *
     * @param file 要上传的文件
     * @param objectKey 文件在 S3 中的存储路径
     * @return 文件在 S3 中的 object key
     */
    public String uploadFile(MultipartFile file, String objectKey) {
        try (S3Client s3Client = createClient()) {
            log.info("开始上传文件到 S3，Bucket: {}, Key: {}", s3Config.getBucketName(), objectKey);

            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(s3Config.getBucketName())
                    .key(objectKey)
                    .contentType(file.getContentType())
                    .build();

            PutObjectResponse response = s3Client.putObject(
                    putObjectRequest,
                    RequestBody.fromInputStream(file.getInputStream(), file.getSize())
            );
            log.info("文件上传完成，ETag: {}", response.eTag());
            return objectKey;
        } catch (Exception e) {
            log.error("文件上传失败，Bucket: {}, Key: {}", s3Config.getBucketName(), objectKey, e);
            throw new ClientException(BaseErrorCode.FILE_UPLOAD_FAILED);
        }
    }

    /**
     * 上传字节数组到 S3
     *
     * @param bytes 要上传的字节数组
     * @param objectKey 文件在 S3 中的存储路径
     * @param contentType 文件 MIME 类型
     * @return 文件在 S3 中的 object key
     */
    public String uploadBytes(byte[] bytes, String objectKey, String contentType) {
        try (S3Client s3Client = createClient()) {
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(s3Config.getBucketName())
                    .key(objectKey)
                    .contentType(contentType)
                    .build();
            s3Client.putObject(putObjectRequest, RequestBody.fromBytes(bytes));
            return objectKey;
        } catch (Exception e) {
            log.error("字节数组上传失败，Bucket: {}, Key: {}", s3Config.getBucketName(), objectKey, e);
            throw new ClientException(BaseErrorCode.FILE_UPLOAD_FAILED);
        }
    }

    /**
     * 上传文本到 S3
     *
     * @param text 要上传的文本内容
     * @param objectKey 文本在 S3 中的存储路径
     * @return 文本在 S3 中的 object key
     */
    public String uploadText(String text, String objectKey) {
        byte[] bytes = text.getBytes(StandardCharsets.UTF_8);
        try (S3Client s3Client = createClient()) {
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(s3Config.getBucketName())
                    .key(objectKey)
                    .contentType("text/plain; charset=utf-8")
                    .build();
            s3Client.putObject(putObjectRequest, RequestBody.fromBytes(bytes));
            return objectKey;
        } catch (Exception e) {
            log.error("文本上传失败，Bucket: {}, Key: {}", s3Config.getBucketName(), objectKey, e);
            throw new ClientException(BaseErrorCode.FILE_UPLOAD_FAILED);
        }
    }

    /**
     * 读取 S3 文件
     *
     * @param objectKey 文件在 S3 中的存储路径
     * @return 文件输入流
     */
    public InputStream getObject(String objectKey) {
        S3Client s3Client = createClient();
        try {
            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                    .bucket(s3Config.getBucketName())
                    .key(objectKey)
                    .build();
            return s3Client.getObject(getObjectRequest);
        } catch (Exception e) {
            s3Client.close();
            log.error("文件读取失败，Bucket: {}, Key: {}", s3Config.getBucketName(), objectKey, e);
            throw new ClientException(BaseErrorCode.FILE_NOT_FOUND);
        }
    }

    /**
     * 删除文件
     *
     * @param objectKey 文件在 S3 中的存储路径
     * @return 是否删除成功
     */
    public boolean deleteFile(String objectKey) {
        try (S3Client s3Client = createClient()) {
            DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                    .bucket(s3Config.getBucketName())
                    .key(objectKey)
                    .build();
            s3Client.deleteObject(deleteObjectRequest);
            log.info("文件删除成功，Key: {}", objectKey);
            return true;
        } catch (Exception e) {
            log.error("文件删除失败，Key: {}", objectKey, e);
            return false;
        }
    }

    private S3Presigner createPresigner() {
        S3Presigner.Builder builder = S3Presigner.builder()
                .region(Region.of(s3Config.getRegion()))
                .credentialsProvider(credentialsProvider())
                .serviceConfiguration(S3Configuration.builder()
                        .pathStyleAccessEnabled(s3Config.isPathStyleAccess())
                        .build());
        if (s3Config.getEndpoint() != null && !s3Config.getEndpoint().isBlank()) {
            builder.endpointOverride(URI.create(s3Config.getEndpoint()));
        }
        return builder.build();
    }

    private StaticCredentialsProvider credentialsProvider() {
        AwsBasicCredentials credentials = AwsBasicCredentials.create(
                s3Config.getAccessKeyId(),
                s3Config.getSecretAccessKey());
        return StaticCredentialsProvider.create(credentials);
    }
}
