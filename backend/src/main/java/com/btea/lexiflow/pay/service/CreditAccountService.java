package com.btea.lexiflow.pay.service;

import com.btea.lexiflow.pay.dto.resp.CreditAccountRespDTO;

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
}
