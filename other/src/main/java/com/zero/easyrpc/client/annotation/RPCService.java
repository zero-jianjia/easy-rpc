package com.zero.easyrpc.client.annotation;

import java.lang.annotation.*;

/**
 * 服务提供端提供服务的annotation
 * Created by jianjia1 on 17/12/04.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD })
@Documented
public @interface RPCService {

    public String serviceName() default "";					//服务名
    public int weight() default 50;							//负载访问权重
    public String responsibilityName() default "system";	//负责人名
    public int connCount() default 1;						//单实例连接数，注册中心该参数有效，直连无效
    public boolean isVIPService() default false;			//是否是VIP服务
    public boolean isSupportDegradeService() default false; //是否支持降级
    public String degradeServicePath() default "";			//如果支持降级，降级服务的路径
    public String degradeServiceDesc() default "";			//降级服务的描述
    public boolean isFlowController() default true;		    //是否单位时间限流
    public long maxCallCountInMinute() default 100000;		//单位时间的最大调用量

}

//     编织的服务信息基本信息应该有
//        1）服务的IP地址
//        2）端口号
//        3）服务名，这个应该是唯一的
//        4）是否是VIP服务，如果是VIP服务则当启动NettyServer需要在port-2的端口上监听
//        5）是否支持降级
//        6）降级服务的方法路径，这边降级做的比较简单，其实就是一个mock方法
//        7）降级服务的基本描述（其实并不是必要的，不过却可以用来做统计）
//        8）服务的权重，这个是很必要的，应该每个系统的线上实例肯定不止一台，而每一台实例的性能也不一样，有些服务器的性能好一点，内存大一点，可以设置大一点，最大100，最小1，这样在服务端调用该实例的时候，默认是使用加权随机负载均衡的算法，去随机访问服务提供端的
//        9）连接数，该连接数表示的是一个Consumer实例与一个Provider实例之间连接数，一般情况下，一个连接就够用了，特殊情况下，可以设置多个链接