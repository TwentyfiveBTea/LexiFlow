package com.btea.lexiflow.admin.service;

import com.btea.lexiflow.admin.dto.req.AdminGrantCreditsReqDTO;
import com.btea.lexiflow.admin.dto.req.AdminLoginReqDTO;
import com.btea.lexiflow.admin.dto.resp.AdminCreditUsageRespDTO;
import com.btea.lexiflow.admin.dto.resp.AdminCreditsSummaryRespDTO;
import com.btea.lexiflow.admin.dto.resp.AdminLoginRespDTO;
import com.btea.lexiflow.admin.dto.resp.AdminUserRespDTO;
import com.btea.lexiflow.common.convention.result.PageRespDTO;

/**
 * @Author: TwentyfiveBTea
 * @Date: 2026/7/24
 * @Description: 管理员服务接口
 */
public interface AdminService {

    /**
     * 管理员登录
     *
     * @param reqDTO 管理员登录请求参数
     * @return 管理员登录信息
     */
    AdminLoginRespDTO login(AdminLoginReqDTO reqDTO);

    /**
     * 分页查询普通用户注册信息
     *
     * @param page 页码
     * @param pageSize 每页数量
     * @return 用户注册分页数据
     */
    PageRespDTO<AdminUserRespDTO> listUsers(Integer page, Integer pageSize);

    /**
     * 查询Credits使用汇总
     *
     * @return 各时间范围的Credits使用量
     */
    AdminCreditsSummaryRespDTO getCreditsSummary();

    /**
     * 分页查询全部用户Credits使用记录
     *
     * @param page 页码
     * @param pageSize 每页数量
     * @return Credits使用记录分页数据
     */
    PageRespDTO<AdminCreditUsageRespDTO> listCreditUsage(Integer page, Integer pageSize);

    /**
     * 向指定用户赠送Credits
     *
     * @param reqDTO 赠送Credits请求参数
     */
    void grantCredits(AdminGrantCreditsReqDTO reqDTO);
}
