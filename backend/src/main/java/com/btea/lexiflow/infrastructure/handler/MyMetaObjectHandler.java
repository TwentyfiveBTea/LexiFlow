package com.btea.lexiflow.infrastructure.handler;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * @Author: TwentyfiveBTea
 * @Date: 2026/7/2 13:47
 * @Description: MyBatis-Plus 自动填充处理器
 */
@Component
public class MyMetaObjectHandler implements MetaObjectHandler {

    /**
     * 插入时的填充策略
     */
    @Override
    public void insertFill(MetaObject metaObject) {
        this.strictInsertFill(metaObject, "createdAt", Date.class, new Date());
        this.strictInsertFill(metaObject, "updatedAt", Date.class, new Date());
    }

    /**
     * 更新时的填充策略
     */
    @Override
    public void updateFill(MetaObject metaObject) {
        this.strictUpdateFill(metaObject, "updatedAt", Date.class, new Date());
    }
}
