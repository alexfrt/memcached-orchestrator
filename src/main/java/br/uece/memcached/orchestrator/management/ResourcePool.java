package br.uece.memcached.orchestrator.management;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class ResourcePool<T> {

	private BlockingQueue<T> idleResources;
	
	public ResourcePool(Integer size, ResourceBuilder<T> resourceBuilder) {
		this.idleResources = new LinkedBlockingQueue<T>();
		
		for (int i = 0; i < size; i++) {
			idleResources.add(resourceBuilder.build());
		}
	}
	
	public T getResource() {
		try {
			return idleResources.take();
		}
		catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
	}
	
	public void returnResource(T resource) {
		idleResources.add(resource);
	}
	
	public interface ResourceBuilder<T> {
		T build();
	}
}
