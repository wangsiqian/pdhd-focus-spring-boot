package com.pdhd.server.common.config;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import io.swagger.annotations.Api;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import springfox.documentation.RequestHandler;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2WebMvc;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * @author wangsiqian
 */
@EnableSwagger2WebMvc
@ConditionalOnProperty(value = "knife4j.enable", havingValue = "true")
public abstract class BaseSwaggerConfiguration implements BeanFactoryAware, InitializingBean {
    protected DefaultListableBeanFactory beanFactory;
    /**
     * suffix of docket bean name
     */
    private final String DOCKET_BEAN_NAME_SUFFIX = "Docket";

    private final String DEFAULT_GROUP_NAME = "V1";

    @Override
    public void afterPropertiesSet() {
        Map<String, Object> beansWithAnnotation = beanFactory.getBeansWithAnnotation(Api.class);

        Map<String, List<String>> groupNamesWithPackage = new HashMap<>();
        // 接口分组
        beansWithAnnotation.forEach(
                (beanName, clazz) -> {
                    Api api = clazz.getClass().getAnnotation(Api.class);
                    String groupName =
                            StrUtil.isBlank(api.value()) ? DEFAULT_GROUP_NAME : api.value();
                    groupNamesWithPackage.compute(
                            groupName,
                            (key, value) -> {
                                if (CollectionUtil.isEmpty(value)) {
                                    value = new ArrayList<>();
                                }
                                value.add(clazz.getClass().getPackage().getName());
                                return value;
                            });
                });

        groupNamesWithPackage.forEach(
                (groupName, packages) -> {
                    Docket docket =
                            new Docket(DocumentationType.SWAGGER_2)
                                    .apiInfo(apiInfo())
                                    .groupName(groupName)
                                    .select()
                                    .apis(basePackages(packages))
                                    .paths(PathSelectors.any())
                                    .build();
                    beanFactory.registerSingleton(groupName + DOCKET_BEAN_NAME_SUFFIX, docket);
                });
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        this.beanFactory = (DefaultListableBeanFactory) beanFactory;
    }

    /**
     * 构建swagger接口信息参数
     *
     * @return swagger参数
     * @author wangsiqian
     */
    protected abstract ApiInfo apiInfo();

    /**
     * 自定义宝选择器，扫描多个包
     *
     * @param packages 包集合
     * @author wangsiqian
     */
    public Predicate<RequestHandler> basePackages(List<String> packages) {
        return input -> declaringClass(input).map(handlerPackages(packages)).orElse(true);
    }

    private Function<Class<?>, Boolean> handlerPackages(List<String> packages) {
        return input -> {
            // 循环判断匹配
            for (String aPackage : packages) {
                boolean isMatch = input.getPackage().getName().startsWith(aPackage);
                if (isMatch) {
                    return true;
                }
            }
            return false;
        };
    }

    private static Optional<Class<?>> declaringClass(RequestHandler input) {
        return Optional.ofNullable(input.declaringClass());
    }
}
