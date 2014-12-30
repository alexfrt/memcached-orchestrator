package br.uece.memcached.orchestrator.management;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class ResourcePool<T> {

	private BlockingQueue<T> idleResources;
	private Set<T> busyResources;
	
	public ResourcePool(Integer size, ResourceBuilder<T> resourceBuilder) {
		this.idleResources = new LinkedBlockingQueue<T>();
		this.busyResources = new HashSet<T>();
		
		for (int i = 0; i < size; i++) {
			idleResources.add(resourceBuilder.build());
		}
	}
	
	public T getResource() {
		try {
			T resource = idleResources.take();
			busyResources.add(resource);
			
			return resource;
		}
		catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
	}
	
	public void returnResource(T resource) {
		if (busyResources.remove(resource)) {
			idleResources.add(resource);
		}
	}
	
	public interface ResourceBuilder<T> {
		T build();
	}
}
