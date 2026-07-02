package com.lexiflow.article.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lexiflow.article.dao.entity.RelArticleVocabOccurrenceDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * @Author: TwentyfiveBTea
 * @Date: 2026/7/2 17:25
 * @Description: 文章词汇出现位置访问层接口
 */
@Mapper
public interface RelArticleVocabOccurrenceMapper extends BaseMapper<RelArticleVocabOccurrenceDO> {
}
