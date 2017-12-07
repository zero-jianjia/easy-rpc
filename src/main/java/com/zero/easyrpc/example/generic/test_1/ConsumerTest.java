package com.zero.easyrpc.example.generic.test_1;

import com.zero.easyrpc.client.consumer.Consumer;
import com.zero.easyrpc.client.consumer.ConsumerClient;
import com.zero.easyrpc.client.consumer.ConsumerConfig;
import com.zero.easyrpc.common.utils.ChannelGroup;
import com.zero.easyrpc.netty4.ClientConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class ConsumerTest {

	private static final Logger logger = LoggerFactory.getLogger(ConsumerTest.class);
	
	private static ConsumerClient commonclient;

	public static void main(String[] args) throws Throwable {

		Thread t = new Thread(new SubcribeResultScanner(), "timeout.scanner");
		t.setDaemon(true);
		t.start();

		ClientConfig registryNettyClientConfig = new ClientConfig();
		registryNettyClientConfig.setDefaultAddress("127.0.0.1:18010");

		ClientConfig provideClientConfig = new ClientConfig();

		ConsumerClient client = new ConsumerClient(registryNettyClientConfig, provideClientConfig, new ConsumerConfig());

		client.start();

		commonclient = client;
		Consumer.SubscribeManager subscribeManager = client.subscribeService("LAOPOPO.TEST.SAYHELLO");

		if (!subscribeManager.waitForAvailable(3000l)) {
			throw new Exception("no service provider");
		}

		Object obj = client.call("LAOPOPO.TEST.SAYHELLO", "shine");
		if (obj instanceof String) {
			System.out.println((String) obj);
		}
		
		Consumer.SubscribeManager subscribeManager2 = client.subscribeService("LAOPOPO.TEST.SAYBYE");
		
		if (!subscribeManager2.waitForAvailable(3000l)) {
			throw new Exception("no service provider");
		}

		Object obj1 = client.call("LAOPOPO.TEST.SAYBYE", "shine");
		if (obj1 instanceof String) {
			System.out.println((String) obj1);
		}

	}

	private static class SubcribeResultScanner implements Runnable {

		@Override
		public void run() {

			for (;;) {
				try {
					logger.info("统计中");
					Thread.sleep(10000);
					
					@SuppressWarnings("static-access")
					ConcurrentMap<String, CopyOnWriteArrayList<ChannelGroup>> result = commonclient.getGroups();
					if(result != null){
						for(String serviceName:result.keySet()){
							System.out.println(serviceName);
							for(ChannelGroup channelGroup :result.get(serviceName)){
								System.out.println(channelGroup.toString());
							}
						}
					}
				} catch (Throwable t) {
					logger.error("An exception has been caught while scanning the timeout acknowledges {}.", t);
				}
			}
		}
	}

}
