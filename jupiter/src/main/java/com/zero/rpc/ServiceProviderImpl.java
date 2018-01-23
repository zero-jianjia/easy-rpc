package com.zero.rpc;


import org.zero.common.util.JConstants;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Provider implementation annotation.
 *
 * 建议每个服务实现通过此注解来指定服务版本信息, 如不希望业务代码对jupiter依赖也可以不使用此注解而手动去设置版本信息.
 *
 * jupiter
 * org.jupiter.rpc
 *
 * @author jiachun.fjc
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface ServiceProviderImpl {

    /**
     * 服务版本号, 通常在接口不兼容时版本号才需要升级
     */
    String version() default JConstants.DEFAULT_VERSION;
}
