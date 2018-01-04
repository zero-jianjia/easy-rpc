package com.zero.easyrpc.example.benchmark;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicLong;

import com.zero.easyrpc.client.consumer.Consumer;
import com.zero.easyrpc.client.consumer.ConsumerClient;
import com.zero.easyrpc.client.consumer.ConsumerConfig;
import com.zero.easyrpc.client.consumer.proxy.ProxyFactory;
import com.zero.easyrpc.example.generic.HelloService;
import com.zero.easyrpc.netty4.ClientConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * 2016-10-14 14:27:57.034 WARN  [main] [BenchmarkClient] - count=25600000
 * 2016-10-14 14:27:57.035 WARN  [main] [BenchmarkClient] - Request count: 25600000, time: 496 second, qps: 51612
 * 
 * 2016-10-18 16:06:38.906 WARN  [main] [BenchmarkClient] - count=12800000
 * 2016-10-18 16:06:38.906 WARN  [main] [BenchmarkClient] - Request count: 12800000, time: 199 second, qps: 64321
 */
public class BenchmarkClient {
	private static final Logger logger = LoggerFactory.getLogger(BenchmarkClient.class);
	
	public static void main(String[] args) throws Exception {
		
		int processors = Runtime.getRuntime().availableProcessors();

		ClientConfig registryNettyClientConfig = new ClientConfig();
		registryNettyClientConfig.setDefaultAddress("127.0.0.1:18010");

		ClientConfig provideClientConfig = new ClientConfig();

		ConsumerClient client = new ConsumerClient(registryNettyClientConfig, provideClientConfig, new ConsumerConfig());

		client.start();

		Consumer.SubscribeManager subscribeManager = client.subscribeService("TEST.SAYHELLO");

		if (!subscribeManager.waitForAvailable(3000L)) {
			throw new Exception("no service provider");
		}

		final HelloService helloService = ProxyFactory.factory(HelloService.class).consumer(client).timeoutMillis(3000l).newProxyInstance();

		for (int i = 0; i < 5000; i++) {
			String str = helloService.sayHello("zero");
		}
		final int t = 50000;
		final int step = 6;
		long start = System.currentTimeMillis();
		final CountDownLatch latch = new CountDownLatch(processors << step);
		final AtomicLong count = new AtomicLong();
		for (int i = 0; i < (processors << step); i++) {
			new Thread(new Runnable() {

				@Override
				public void run() {
					for (int i = 0; i < t; i++) {
						try {
							helloService.sayHello("Lyncc");

							if (count.getAndIncrement() % 10000 == 0) {
								logger.warn("count=" + count.get());
							}
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
					latch.countDown();
				}
			}).start();
		}
		try {
			latch.await();
			logger.warn("count=" + count.get());
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		long second = (System.currentTimeMillis() - start) / 1000;
		logger.warn("Request count: " + count.get() + ", time: " + second + " second, qps: " + count.get() / second);

	}

}
