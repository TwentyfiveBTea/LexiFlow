package com.lexiflow.article.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lexiflow.article.dao.entity.RelArticleVocabDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * @Author: TwentyfiveBTea
 * @Date: 2026/7/2 17:27
 * @Description: 文章命中词汇汇总访问层接口
 */
@Mapper
public interface RelArticleVocabMapper extends BaseMapper<RelArticleVocabDO> {
}
