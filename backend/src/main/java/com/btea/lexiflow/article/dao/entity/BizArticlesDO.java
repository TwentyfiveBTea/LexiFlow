package com.btea.lexiflow.article.dao.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * @Author: TwentyfiveBTea
 * @Date: 2026/7/2 17:28
 * @Description: 用户文章实体类
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@TableName("biz_articles")
public class BizArticlesDO {

    /**
     * 唯一主键（雪花ID）
     */
    @TableId(type = IdType.ASSIGN_ID)
    private String id;

    /**
     * 用户ID
     */
    private String userId;

    /**
     * 文章标题
     */
    private String title;

    /**
     * 原始文件名
     */
    private String originalFilename;

    /**
     * 文件类型：pdf/docx/txt/md
     */
    private String fileType;

    /**
     * 文件 MIME 类型
     */
    private String mimeType;

    /**
     * 文件大小，单位：字节
     */
    private Long fileSize;

    /**
     * 原始文件哈希值，用于校验或去重
     */
    private String fileHash;

    /**
     * 原始文件存储路径
     */
    private String filePath;

    /**
     * 解析后的文章正文内容，按自然段落存储原文和中文翻译
     */
    private String parsedContent;

    /**
     * 解析后正文内容哈希值
     */
    private String contentHash;

    /**
     * 文章主语言标识：en/ja/fr/de/es/zh
     */
    private String languageCode;

    /**
     * 词数统计
     */
    private Integer wordCount;

    /**
     * 字符数统计
     */
    private Integer charCount;

    /**
     * 解析状态：0=待解析, 1=解析中, 2=解析成功, 3=解析失败
     */
    private Integer parseStatus;

    /**
     * 翻译状态：0=待翻译, 1=翻译中, 2=翻译成功, 3=翻译失败
     */
    private Integer translationStatus;

    /**
     * 词汇分析状态：0=待分析, 1=分析中, 2=至少一次分析成功, 3=分析失败
     */
    private Integer analysisStatus;

    /**
     * 文章状态：0=正常, 1=已删除
     */
    private Integer status;

    /**
     * 解析完成时间
     */
    private Date parsedAt;

    /**
     * 翻译完成时间
     */
    private Date translatedAt;

    /**
     * 最近一次词汇分析完成时间
     */
    private Date analyzedAt;

    /**
     * 删除时间
     */
    private Date deletedAt;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private Date createdAt;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Date updatedAt;
}
