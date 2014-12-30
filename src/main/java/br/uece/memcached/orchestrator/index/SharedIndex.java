package br.uece.memcached.orchestrator.index;

import java.util.HashMap;

public class SharedIndex {
	
	private HashMap<String, Object> hashMap = new HashMap<String, Object>();
	
	public void put(String key, Object value) {
		hashMap.put(key, value);
	}
	
	public Object get(String key) {
		return hashMap.get(key);
	}

}
