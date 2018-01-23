package com.zero.easyrpc.z_example.spring;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * 
 */
public class SpringConsumerTest {
	
	public static void main(String[] args) {
		ApplicationContext ctx = new ClassPathXmlApplicationContext("classpath:spring-consumer.xml");
		HelloService service = ctx.getBean(HelloService.class);
        try {
        	String result1 = service.sayHello("Lyncc");
            System.out.println(result1);
        } catch (Exception e) {
            e.printStackTrace();
        }
	}

}
