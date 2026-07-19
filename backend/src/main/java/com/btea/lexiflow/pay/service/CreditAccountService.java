package com.btea.lexiflow.pay.service;

import com.btea.lexiflow.common.convention.result.PageRespDTO;
import com.btea.lexiflow.pay.dto.resp.CreditAccountRespDTO;
import com.btea.lexiflow.pay.dto.resp.CreditLedgerRespDTO;

/**
 * @Author: TwentyfiveBTea
 * @Date: 2026/7/16
 * @Description: Credits账户服务接口
 */
public interface CreditAccountService {

    /**
     * 幂等初始化用户Credits账户
     *
     * @param userId 用户ID
     */
    void initializeAccount(String userId);

    /**
     * 获取当前用户Credits账户
     *
     * @return Credits账户
     */
    CreditAccountRespDTO getCurrentAccount();

    /**
     * 获取当前用户的文章Credits使用记录
     *
     * @param page 页码
     * @param pageSize 每页记录数
     * @return 文章Credits使用记录分页结果
     */
    PageRespDTO<CreditLedgerRespDTO> listCurrentLedger(Integer page, Integer pageSize);
}
