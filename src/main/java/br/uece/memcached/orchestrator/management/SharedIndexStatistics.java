package br.uece.memcached.orchestrator.management;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.google.common.util.concurrent.AtomicDouble;

public class SharedIndexStatistics {

	private final Map<String, Double> hitCounters;
	private final Double requestCounter;

	SharedIndexStatistics(Map<String, AtomicDouble> hitCounters, AtomicDouble requestCounter) {
		HashMap<String, Double> tempCounters = new HashMap<String, Double>(hitCounters.size());
		for (Map.Entry<String, AtomicDouble> entry : hitCounters.entrySet()) {
			tempCounters.put(entry.getKey(), entry.getValue().getAndSet(0));
		}
		
		this.hitCounters = Collections.unmodifiableMap(tempCounters);
		this.requestCounter = requestCounter.getAndSet(0);
	}
	
	public Map<String, Double> getHitCounters() {
		return hitCounters;
	}
	
	public Double getRequestCounter() {
		return requestCounter;
	}
	
	@Override
	public String toString() {
		return String.format("%.0f requests for %.0f keys", requestCounter, hitCounters.size());
	}
	
}
