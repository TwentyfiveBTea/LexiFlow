package com.lexiflow.learning.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lexiflow.learning.dao.entity.RelUserWordProgressDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * @Author: TwentyfiveBTea
 * @Date: 2026/7/2 17:32
 * @Description: 用户单词学习进度访问层接口
 */
@Mapper
public interface RelUserWordProgressMapper extends BaseMapper<RelUserWordProgressDO> {
}
