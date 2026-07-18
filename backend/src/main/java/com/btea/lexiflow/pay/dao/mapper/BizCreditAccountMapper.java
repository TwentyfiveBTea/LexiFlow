package com.btea.lexiflow.pay.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.btea.lexiflow.pay.dao.entity.BizCreditAccountDO;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * @Author: TwentyfiveBTea
 * @Date: 2026/7/16
 * @Description: 用户Credits账户访问层接口
 */
@Mapper
public interface BizCreditAccountMapper extends BaseMapper<BizCreditAccountDO> {

    /**
     * 幂等创建用户Credits账户
     *
     * @param id 账户ID
     * @param userId 用户ID
     * @param status 账户状态
     * @return 影响行数
     */
    @Insert("""
            INSERT IGNORE INTO biz_credit_account
                (id, user_id, available_credits, frozen_credits, status, created_at, updated_at)
            VALUES
                (#{id}, #{userId}, 0, 0, #{status}, NOW(), NOW())
            """)
    int insertIgnore(@Param("id") String id,
                     @Param("userId") String userId,
                     @Param("status") Integer status);

    /**
     * 锁定用户Credits账户
     *
     * @param userId 用户ID
     * @return Credits账户
     */
    @Select("""
            SELECT *
            FROM biz_credit_account
            WHERE user_id = #{userId}
            FOR UPDATE
            """)
    BizCreditAccountDO selectByUserIdForUpdate(@Param("userId") String userId);
}
