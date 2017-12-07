package com.zero.easyrpc.example.benchmark.big_request;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicLong;

import com.zero.easyrpc.client.consumer.Consumer;
import com.zero.easyrpc.client.consumer.ConsumerClient;
import com.zero.easyrpc.client.consumer.ConsumerConfig;
import com.zero.easyrpc.client.consumer.proxy.ProxyFactory;
import com.zero.easyrpc.example.generic.test_2.HelloService;
import com.zero.easyrpc.netty4.ClientConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 1k request 原样返回
 * 2016-10-18 16:40:38.851 WARN  [main] [BenchmarkClient] - count=12800000
 * 2016-10-18 16:40:38.851 WARN  [main] [BenchmarkClient] - Request count: 12800000, time: 257 second, qps: 49805
 * 
 * 2016-10-18 16:47:09.387 WARN  [main] [BenchmarkClient] - count=12800000
 * 2016-10-18 16:47:09.387 WARN  [main] [BenchmarkClient] - Request count: 12800000, time: 259 second, qps: 49420
 * 
 * 5k request 原样返回
 * 2016-10-18 17:19:50.419 WARN  [main] [BenchmarkClient] - count=12800000
 * 2016-10-18 17:19:50.419 WARN  [main] [BenchmarkClient] - Request count: 12800000, time: 543 second, qps: 23572
 * 
 * 10k request 原样返回
 * 2016-10-18 17:05:47.783 WARN  [main] [BenchmarkClient] - count=12800000
 * 2016-10-18 17:05:47.783 WARN  [main] [BenchmarkClient] - Request count: 12800000, time: 838 second, qps: 15274
 * 
 * 
 * 
 */
public class CommonBenchmarkClient {
	
	private static final Logger logger = LoggerFactory.getLogger(CommonBenchmarkClient.class);
	
	private static String request = null;
	
	private static int size = 5;
	
	public static void main(String[] args) throws Exception {
		
        int length = 1024 * size;
        StringBuilder builder = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            builder.append((char) (ThreadLocalRandom.current().nextInt(33, 128)));
        }
        request = builder.toString();
		
		int processors = Runtime.getRuntime().availableProcessors();

		ClientConfig registryNettyClientConfig = new ClientConfig();
		registryNettyClientConfig.setDefaultAddress("127.0.0.1:18010");

		ClientConfig provideClientConfig = new ClientConfig();

		ConsumerClient client = new ConsumerClient(registryNettyClientConfig, provideClientConfig, new ConsumerConfig());

		client.start();

		Consumer.SubscribeManager subscribeManager = client.subscribeService("LAOPOPO.TEST.SAYHELLO");

		if (!subscribeManager.waitForAvailable(3000l)) {
			throw new Exception("no service provider");
		}

		final HelloService helloService = ProxyFactory.factory(HelloService.class).consumer(client).timeoutMillis(3000l).newProxyInstance();

		for (int i = 0; i < 5000; i++) {
			String str = helloService.sayHello(request);
			System.out.println(str);
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
							helloService.sayHello(request);

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
