package com.btea.lexiflow.pay.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.btea.lexiflow.pay.dao.entity.BizCreditReservationDO;
import com.btea.lexiflow.pay.dto.resp.CreditLedgerRespDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

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

    /**
     * 查询用户已结算的文章Credits使用记录
     *
     * @param userId 用户ID
     * @param reservationStatus 预占结算状态
     * @param requestStatus AI请求成功状态
     * @param billingStatus AI计费结算状态
     * @param offset 分页偏移量
     * @param pageSize 每页记录数
     * @return 文章Credits使用记录
     */
    @Select("""
            SELECT r.article_id AS article_id,
                   r.consumed_credits AS total_credits,
                   COALESCE(SUM(CASE WHEN u.scene = 1 THEN u.credits_cost ELSE 0 END), 0) AS ocr_credits,
                   COALESCE(SUM(CASE WHEN u.scene IN (2, 3) THEN u.credits_cost ELSE 0 END), 0)
                       AS translation_credits,
                   r.completed_at AS completed_at
            FROM biz_credit_reservation r
            LEFT JOIN biz_ai_usage u
              ON u.processing_no = r.processing_no
             AND u.request_status = #{requestStatus}
             AND u.billing_status = #{billingStatus}
            WHERE r.user_id = #{userId}
              AND r.status = #{reservationStatus}
            GROUP BY r.id, r.article_id, r.consumed_credits, r.completed_at
            ORDER BY r.completed_at DESC
            LIMIT #{offset}, #{pageSize}
            """)
    List<CreditLedgerRespDTO> selectSettledUsageByUser(
            @Param("userId") String userId,
            @Param("reservationStatus") Integer reservationStatus,
            @Param("requestStatus") Integer requestStatus,
            @Param("billingStatus") Integer billingStatus,
            @Param("offset") Long offset,
            @Param("pageSize") Integer pageSize);

    /**
     * 统计用户已结算的文章Credits使用记录数量
     *
     * @param userId 用户ID
     * @param reservationStatus 预占结算状态
     * @return 已结算记录数量
     */
    @Select("""
            SELECT COUNT(*)
            FROM biz_credit_reservation
            WHERE user_id = #{userId}
              AND status = #{reservationStatus}
            """)
    long countSettledUsageByUser(
            @Param("userId") String userId,
            @Param("reservationStatus") Integer reservationStatus);
}
