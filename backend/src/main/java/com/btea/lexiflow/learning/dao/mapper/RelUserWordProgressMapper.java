package com.btea.lexiflow.learning.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.btea.lexiflow.learning.dao.entity.RelUserWordProgressDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * @Author: TwentyfiveBTea
 * @Date: 2026/7/2 17:32
 * @Description: 用户单词学习进度访问层接口
 */
@Mapper
public interface RelUserWordProgressMapper extends BaseMapper<RelUserWordProgressDO> {

    /**
     * 锁定用户单词学习进度，防止并发复习覆盖
     *
     * @param userId 用户ID
     * @param wordId 单词ID
     * @param languageCode 语言标识
     * @return 用户单词学习进度
     */
    @Select("""
            SELECT *
            FROM rel_user_word_progress
            WHERE user_id = #{userId}
              AND word_id = #{wordId}
              AND language_code = #{languageCode}
              AND library_status = 1
            FOR UPDATE
            """)
    RelUserWordProgressDO selectForUpdate(@Param("userId") String userId,
                                          @Param("wordId") Long wordId,
                                          @Param("languageCode") String languageCode);
}
