package com.btea.lexiflow.vocab.dao.entity;

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
 * @Date: 2026/7/12
 * @Description: 用户词汇库实体类
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@TableName("biz_vocab_library")
public class BizVocabLibraryDO {

    /**
     * 唯一主键（雪花ID）
     */
    @TableId(type = IdType.ASSIGN_ID)
    private String id;

    /**
     * 所属用户ID
     */
    private String userId;

    /**
     * 词汇库名称
     */
    private String name;

    /**
     * 语言标识：en/ja
     */
    private String languageCode;

    /**
     * 词汇库描述
     */
    private String description;

    /**
     * 状态：0=已删除，1=正常
     */
    private Integer status;

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
