package br.uece.memcached.orchestrator.endpoint;

import java.util.List;

public class ServersUtil {
	
	public static Server minLoad(List<Server> servers) {
		Server bestServer = servers.get(0);
		for (Server server : servers) {
			if (server.getLoad() < bestServer.getLoad()) {
				bestServer = server;
			}
		}
		
		return bestServer;
	}

}
