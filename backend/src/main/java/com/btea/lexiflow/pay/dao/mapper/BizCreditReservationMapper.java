package com.btea.lexiflow.pay.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.btea.lexiflow.pay.dao.entity.BizCreditReservationDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * @Author: TwentyfiveBTea
 * @Date: 2026/7/16
 * @Description: Credits预占记录访问层接口
 */
@Mapper
public interface BizCreditReservationMapper extends BaseMapper<BizCreditReservationDO> {

    /**
     * 锁定文章处理Credits预占记录
     *
     * @param processingNo 文章处理编号
     * @return Credits预占记录
     */
    @Select("""
            SELECT *
            FROM biz_credit_reservation
            WHERE processing_no = #{processingNo}
            FOR UPDATE
            """)
    BizCreditReservationDO selectByProcessingNoForUpdate(@Param("processingNo") String processingNo);
}
