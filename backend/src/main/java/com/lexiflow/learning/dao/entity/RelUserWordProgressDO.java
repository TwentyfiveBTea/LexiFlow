package com.lexiflow.learning.dao.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Date;

/**
 * @Author: TwentyfiveBTea
 * @Date: 2026/7/2 17:30
 * @Description: 用户单词学习进度实体类
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@TableName("rel_user_word_progress")
public class RelUserWordProgressDO {

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
     * 单词ID，结合 language_code 指向对应词表
     */
    private Long wordId;

    /**
     * 语言标识：en/ja
     */
    private String languageCode;

    /**
     * 掌握程度：0=新词, 1=学习中, 2=已掌握
     */
    private Integer status;

    /**
     * 复习次数
     */
    private Integer reviewCount;

    /**
     * 上次复习时间
     */
    private Date lastReviewedAt;

    /**
     * 下次复习时间
     */
    private Date nextReviewAt;

    /**
     * 简易度因子，参考 SuperMemo 算法
     */
    private BigDecimal easinessFactor;

    /**
     * 当前记忆周期，单位：天
     */
    private Integer intervalDays;

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
