package br.uece.memcached.orchestrator;

import java.util.Arrays;
import java.util.List;

import br.uece.memcached.orchestrator.endpoint.Server;

public class ServersHandler {

	private Server client;

	public ServersHandler() throws Exception {
		client = new Server("localhost", 11211);
	}

	public List<Server> getServersByObjectKey(String key) {
		return Arrays.asList(client);
	}

}