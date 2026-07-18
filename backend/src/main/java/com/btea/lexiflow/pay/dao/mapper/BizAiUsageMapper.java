package com.btea.lexiflow.pay.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.btea.lexiflow.pay.dao.entity.BizAiUsageDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

/**
 * @Author: TwentyfiveBTea
 * @Date: 2026/7/16
 * @Description: AI模型用量访问层接口
 */
@Mapper
public interface BizAiUsageMapper extends BaseMapper<BizAiUsageDO> {

    /**
     * 汇总文章处理成功且待结算的Credits成本
     *
     * @param processingNo 文章处理编号
     * @param successStatus 请求成功状态
     * @param pendingBillingStatus 待结算状态
     * @return Credits成本合计
     */
    @Select("""
            SELECT COALESCE(SUM(credits_cost), 0)
            FROM biz_ai_usage
            WHERE processing_no = #{processingNo}
              AND request_status = #{successStatus}
              AND billing_status = #{pendingBillingStatus}
            """)
    Long sumPendingCredits(@Param("processingNo") String processingNo,
                           @Param("successStatus") Integer successStatus,
                           @Param("pendingBillingStatus") Integer pendingBillingStatus);

    /**
     * 批量更新文章处理AI用量计费状态
     *
     * @param processingNo 文章处理编号
     * @param oldBillingStatus 原计费状态
     * @param newBillingStatus 新计费状态
     * @return 影响行数
     */
    @Update("""
            UPDATE biz_ai_usage
            SET billing_status = #{newBillingStatus},
                updated_at = NOW()
            WHERE processing_no = #{processingNo}
              AND billing_status = #{oldBillingStatus}
            """)
    int updateBillingStatus(@Param("processingNo") String processingNo,
                            @Param("oldBillingStatus") Integer oldBillingStatus,
                            @Param("newBillingStatus") Integer newBillingStatus);
}
