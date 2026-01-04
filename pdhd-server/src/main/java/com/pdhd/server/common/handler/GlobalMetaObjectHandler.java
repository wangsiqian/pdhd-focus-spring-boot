package com.pdhd.server.common.handler;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * 自动填充实体时间的字段
 *
 * @author wangsiqian
 */
@Component
public class GlobalMetaObjectHandler implements MetaObjectHandler {
    @Override
    public void insertFill(MetaObject metaObject) {
        strictInsertFill(metaObject, "createdAt", Date.class, new Date());
        strictInsertFill(metaObject, "updatedAt", Date.class, new Date());
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        strictUpdateFill(metaObject, "createdAt", Date.class, new Date());
    }
}
