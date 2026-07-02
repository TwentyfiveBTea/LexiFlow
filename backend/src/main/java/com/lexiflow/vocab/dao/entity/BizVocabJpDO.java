package com.lexiflow.vocab.dao.entity;

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
 * @Date: 2026/7/2 17:29
 * @Description: 日语词汇实体类
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@TableName("biz_vocab_jp")
public class BizVocabJpDO {

    /**
     * 唯一主键
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 单词（汉字或假名）
     */
    private String word;

    /**
     * 假名（读音）
     */
    private String kana;

    /**
     * 翻译列表 [{translation, type}]
     */
    private String translations;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private Date createdAt;
}
