package com.btea.lexiflow.user.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.btea.lexiflow.user.dao.entity.BizUsersDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * @Author: TwentyfiveBTea
 * @Date: 2026/7/2 17:31
 * @Description: 用户访问层接口
 */
@Mapper
public interface BizUsersMapper extends BaseMapper<BizUsersDO> {
}
