/**
 * @Author: TwentyfiveBTea
 * @Date: 2026/7/24
 * @Description: 管理员数据访问层接口
 */
package com.btea.lexiflow.admin.dao.mapper;

import com.btea.lexiflow.admin.dto.resp.AdminCreditUsageRespDTO;
import com.btea.lexiflow.admin.dto.resp.AdminUserRespDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.Date;
import java.util.List;

/**
 * 管理员数据访问层接口。
 */
@Mapper
public interface AdminMapper {

    /**
     * 统计普通用户注册数量。
     *
     * @param userRole 普通用户角色
     * @return 注册用户总数
     */
    @Select("""
            SELECT COUNT(*)
            FROM biz_users
            WHERE role = #{userRole}
            """)
    long countUsers(@Param("userRole") String userRole);

    /**
     * 分页查询普通用户注册列表。
     *
     * @param userRole 普通用户角色
     * @param offset 分页偏移量
     * @param pageSize 每页数量
     * @return 用户注册列表
     */
    @Select("""
            SELECT id AS user_id,
                   username,
                   created_at AS registered_at
            FROM biz_users
            WHERE role = #{userRole}
            ORDER BY created_at DESC
            LIMIT #{offset}, #{pageSize}
            """)
    List<AdminUserRespDTO> selectUsers(@Param("userRole") String userRole,
                                       @Param("offset") Long offset,
                                       @Param("pageSize") Integer pageSize);

    /**
     * 统计指定时间之后已结算文章处理的Credits使用量。
     *
     * @param reservationStatus 已结算状态
     * @param from 开始时间
     * @return Credits使用量
     */
    @Select("""
            SELECT COALESCE(SUM(consumed_credits), 0)
            FROM biz_credit_reservation
            WHERE status = #{reservationStatus}
              AND completed_at >= #{from}
            """)
    long sumConsumedCreditsSince(@Param("reservationStatus") Integer reservationStatus,
                                 @Param("from") Date from);

    /**
     * 统计全部用户已结算文章处理的Credits使用记录数量。
     *
     * @param reservationStatus 已结算状态
     * @return 使用记录总数
     */
    @Select("""
            SELECT COUNT(*)
            FROM biz_credit_reservation
            WHERE status = #{reservationStatus}
            """)
    long countCreditUsage(@Param("reservationStatus") Integer reservationStatus);

    /**
     * 分页查询全部用户已结算文章处理的Credits使用记录。
     *
     * @param reservationStatus 已结算状态
     * @param requestStatus AI请求成功状态
     * @param billingStatus AI计费结算状态
     * @param offset 分页偏移量
     * @param pageSize 每页数量
     * @return Credits使用记录
     */
    @Select("""
            SELECT r.user_id AS user_id,
                   u.username AS username,
                   COALESCE(a.title, r.article_id) AS article_title,
                   r.consumed_credits AS total_credits,
                   COALESCE(SUM(CASE WHEN ai.scene = 1 THEN ai.credits_cost ELSE 0 END), 0) AS ocr_credits,
                   COALESCE(SUM(CASE WHEN ai.scene IN (2, 3) THEN ai.credits_cost ELSE 0 END), 0)
                       AS translation_credits,
                   r.completed_at AS completed_at
            FROM biz_credit_reservation r
            LEFT JOIN biz_users u
              ON u.id = r.user_id
            LEFT JOIN biz_articles a
              ON a.id = r.article_id
             AND a.user_id = r.user_id
            LEFT JOIN biz_ai_usage ai
              ON ai.processing_no = r.processing_no
             AND ai.request_status = #{requestStatus}
             AND ai.billing_status = #{billingStatus}
            WHERE r.status = #{reservationStatus}
            GROUP BY r.id, r.user_id, u.username, a.title, r.article_id, r.consumed_credits, r.completed_at
            ORDER BY r.completed_at DESC
            LIMIT #{offset}, #{pageSize}
            """)
    List<AdminCreditUsageRespDTO> selectCreditUsage(@Param("reservationStatus") Integer reservationStatus,
                                                     @Param("requestStatus") Integer requestStatus,
                                                     @Param("billingStatus") Integer billingStatus,
                                                     @Param("offset") Long offset,
                                                     @Param("pageSize") Integer pageSize);
}
