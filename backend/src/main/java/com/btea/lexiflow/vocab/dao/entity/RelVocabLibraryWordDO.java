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
 * @Description: 词汇库词条关系实体类
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@TableName("rel_vocab_library_word")
public class RelVocabLibraryWordDO {

    /**
     * 唯一主键（雪花ID）
     */
    @TableId(type = IdType.ASSIGN_ID)
    private String id;

    /**
     * 词汇库ID
     */
    private String libraryId;

    /**
     * 所属用户ID
     */
    private String userId;

    /**
     * 词条ID，结合 language_code 指向对应词表
     */
    private Long wordId;

    /**
     * 语言标识：en/ja
     */
    private String languageCode;

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
