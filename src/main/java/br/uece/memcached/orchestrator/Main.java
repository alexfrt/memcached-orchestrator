package br.uece.memcached.orchestrator;

import br.uece.memcached.orchestrator.index.SharedIndex;

public final class Main {

    static final int CLIENTS_PORT = 9999;
    static final SharedIndex SHARED_INDEX;
    static final ServersHandler SERVERS_HANDLER;
    
    static {
    	try {
    		SHARED_INDEX = new SharedIndex();
			SERVERS_HANDLER = new ServersHandler();
		}
    	catch (Exception e) {
			throw new RuntimeException("Could not load app", e);
		}
    }
    
    public static void main(String[] args) throws Exception {
        new Orchestrator(CLIENTS_PORT, SERVERS_HANDLER);
    }
}