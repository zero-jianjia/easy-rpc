package com.zero.rpc;


import org.zero.common.util.JConstants;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Provider interface annotation.
 *
 * 建议每个服务接口通过此注解来指定服务信息, 如不希望业务代码对jupiter依赖也可以不使用此注解而手动去设置服务信息.
 *
 * jupiter
 * org.jupiter.rpc
 *
 * @author jiachun.fjc
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface ServiceProvider {

    /**
     * 服务组别
     */
    String group() default JConstants.DEFAULT_GROUP;

    /**
     * 服务名称
     */
    String name() default "";
}
