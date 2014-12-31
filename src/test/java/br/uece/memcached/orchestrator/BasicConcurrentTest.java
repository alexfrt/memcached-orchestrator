package br.uece.memcached.orchestrator;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import net.rubyeye.xmemcached.MemcachedClient;
import net.rubyeye.xmemcached.XMemcachedClientBuilder;
import net.rubyeye.xmemcached.utils.AddrUtil;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.log4j.Logger;

public class BasicConcurrentTest {
	
	private static final Logger LOGGER = Logger.getLogger(BasicConcurrentTest.class);
	private static final Integer THREADS = 100;
	private static Boolean RUN = false;

	public static void main(String[] args) throws Exception {
		List<Thread> threads = new ArrayList<Thread>();
		for (int i = 0; i < THREADS; i++) {
			Thread thread = new Thread(new Runnable() {
				@Override
				public void run() {
					try {
						Random random = new Random();
						
						List<String> keys = new ArrayList<String>();
						for (int i = 0; i < 25; i++) {
							keys.add(RandomStringUtils.randomAlphanumeric(random.nextInt(5) + 3));
						}
						
						doWithTimer(random, keys);
					} catch (Exception e) {}
				}
			});
			
			thread.start();
			threads.add(thread);
		}
		
		RUN = true;
		synchronized (THREADS) {
			THREADS.notifyAll();
		}
		
		for (Thread thread : threads) {
			thread.join();
		}
		
		System.exit(0);
	}
	
	public static void doWithTimer(final Random random, final List<String> keys) throws Exception {
		MemcachedClient client = new XMemcachedClientBuilder(AddrUtil.getAddresses("localhost:9999")).build();

		int i = 1_000;
		int keysCount = keys.size();
		
		synchronized (THREADS) {
			while (!RUN) {
				THREADS.wait();
			}
		}
		
		Long start = System.currentTimeMillis();
		while (i-- > 0) {
			try {
				String key = keys.get(random.nextInt(keysCount));
				if (random.nextBoolean()) {
					client.set(key, 0, RandomStringUtils.randomAlphanumeric(random.nextInt(50) + 50));
				}
				else {
					client.get(key);
				}
			}
			catch (Exception e) {
				LOGGER.error(e);
			}
		}
		
		LOGGER.info(System.currentTimeMillis() - start);
		client.shutdown();
	}

}
