package com.btea.lexiflow.pay.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.btea.lexiflow.pay.dao.entity.BizPaymentOrderDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

/**
 * @Author: TwentyfiveBTea
 * @Date: 2026/7/16
 * @Description: 支付订单访问层接口
 */
@Mapper
public interface BizPaymentOrderMapper extends BaseMapper<BizPaymentOrderDO> {

    /**
     * 锁定支付订单
     *
     * @param orderNo 商户订单号
     * @return 支付订单
     */
    @Select("""
            SELECT *
            FROM biz_payment_order
            WHERE order_no = #{orderNo}
            FOR UPDATE
            """)
    BizPaymentOrderDO selectByOrderNoForUpdate(@Param("orderNo") String orderNo);

    /**
     * 将到期的待支付订单标记为已过期
     *
     * @param pendingStatus 待支付状态
     * @param expiredStatus 已过期状态
     * @return 影响行数
     */
    @Update("""
            UPDATE biz_payment_order
            SET order_status = #{expiredStatus},
                updated_at = NOW()
            WHERE order_status = #{pendingStatus}
              AND expires_at <= NOW()
            """)
    int expirePendingOrders(@Param("pendingStatus") Integer pendingStatus,
                            @Param("expiredStatus") Integer expiredStatus);

    /**
     * 条件标记单个待支付订单为已过期
     *
     * @param id 订单ID
     * @param pendingStatus 待支付状态
     * @param expiredStatus 已过期状态
     * @return 影响行数
     */
    @Update("""
            UPDATE biz_payment_order
            SET order_status = #{expiredStatus},
                updated_at = NOW()
            WHERE id = #{id}
              AND order_status = #{pendingStatus}
              AND expires_at <= NOW()
            """)
    int expireOrderIfPending(@Param("id") String id,
                             @Param("pendingStatus") Integer pendingStatus,
                             @Param("expiredStatus") Integer expiredStatus);
}
