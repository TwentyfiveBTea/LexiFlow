package com.btea.lexiflow.vocab.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.btea.lexiflow.vocab.dao.entity.RelVocabLibraryWordDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * @Author: TwentyfiveBTea
 * @Date: 2026/7/12
 * @Description: 词汇库词条关系访问层接口
 */
@Mapper
public interface RelVocabLibraryWordMapper extends BaseMapper<RelVocabLibraryWordDO> {
}
