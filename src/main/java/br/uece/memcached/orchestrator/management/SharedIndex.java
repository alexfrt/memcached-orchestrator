package br.uece.memcached.orchestrator.management;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

import com.google.common.util.concurrent.AtomicDouble;

public class SharedIndex {
	
	private Map<String, Object> indexMap;
	
	private Map<String, AtomicDouble> hitCounters;
	private AtomicDouble requestCounter;
	
	private ThreadPoolExecutor threadPoolExecutor;
	
	public SharedIndex() {
		this.indexMap = new ConcurrentHashMap<String, Object>();
		this.hitCounters = new ConcurrentHashMap<String, AtomicDouble>();
		this.requestCounter = new AtomicDouble(0);
		
		this.threadPoolExecutor = (ThreadPoolExecutor) Executors.newFixedThreadPool(10);
	}
	
	public void put(String key, Object value) {
		indexMap.put(key, value);
		hitCounters.put(key, new AtomicDouble(1));
	}
	
	public Object get(final String key) {
		final Object value = indexMap.get(key);
		
		threadPoolExecutor.execute(new Runnable() {
			@Override
			public void run() {
				requestCounter.addAndGet(1);
				if (value != null) {
					hitCounters.get(key).addAndGet(1);
				}
			}
		});
		
		return value;
	}
	
	public Object remove(String key) {
		hitCounters.remove(key);
		return indexMap.remove(key);
	}
	
	public SharedIndexStatistics flushStatistics() {
		return new SharedIndexStatistics(this.hitCounters, this.requestCounter);
	}
	
}
