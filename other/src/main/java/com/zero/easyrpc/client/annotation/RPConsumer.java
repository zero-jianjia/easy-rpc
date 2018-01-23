package com.zero.easyrpc.client.annotation;

import java.lang.annotation.*;

/**
 * Created by jianjia1 on 17/12/07.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD })
@Documented
public @interface RPConsumer {

    public String serviceName() default "";//服务名

}