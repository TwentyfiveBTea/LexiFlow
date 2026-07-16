package com.btea.lexiflow.pay.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.btea.lexiflow.pay.dao.entity.BizCreditLedgerDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * @Author: TwentyfiveBTea
 * @Date: 2026/7/16
 * @Description: Credits变动流水访问层接口
 */
@Mapper
public interface BizCreditLedgerMapper extends BaseMapper<BizCreditLedgerDO> {
}
