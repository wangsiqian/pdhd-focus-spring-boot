package com.pdhd.server.common.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.cglib.beans.BeanCopier;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

/**
 * Bean 对象拷贝工具类
 *
 * @author wangsiqian
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class BeanUtils {
    /**
     * 转换单个对象 浅复制,用于无嵌套对象的拷贝
     *
     * @param source   源对象
     * @param supplier 目标对象提供接口
     * @return 目标对象
     */
    public static <S, T> T copy(S source, Supplier<T> supplier) {
        if (source == null) {
            return null;
        }
        T target = supplier.get();
        BeanCopier beanCopier = BeanCopier.create(source.getClass(), target.getClass(), false);
        beanCopier.copy(source, target, null);
        return target;
    }

    /**
     * 转换集合中的多个对象，浅拷贝
     *
     * @param sourceList 源集合
     * @param supplier   目标对象提供接口
     * @return 目标对象的集合
     * @author wangsiqian
     */
    public static <S, T> List<T> copies(List<S> sourceList, Supplier<T> supplier) {
        if (CollectionUtils.isEmpty(sourceList)) {
            return Collections.emptyList();
        }

        List<T> target = new ArrayList<>(sourceList.size());
        for (S source : sourceList) {
            target.add(copy(source, supplier));
        }

        return target;
    }
}
