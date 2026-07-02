package com.btea.lexiflow.article.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.btea.lexiflow.article.dao.entity.BizArticlesDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * @Author: TwentyfiveBTea
 * @Date: 2026/7/2 17:29
 * @Description: 用户文章访问层接口
 */
@Mapper
public interface BizArticlesMapper extends BaseMapper<BizArticlesDO> {
}
