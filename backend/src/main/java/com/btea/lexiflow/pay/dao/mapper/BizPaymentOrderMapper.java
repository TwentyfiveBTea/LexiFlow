package com.btea.lexiflow.pay.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.btea.lexiflow.pay.dao.entity.BizPaymentOrderDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * @Author: TwentyfiveBTea
 * @Date: 2026/7/16
 * @Description: 支付订单访问层接口
 */
@Mapper
public interface BizPaymentOrderMapper extends BaseMapper<BizPaymentOrderDO> {
}
