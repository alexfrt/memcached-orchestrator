package br.uece.memcached.orchestrator;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import br.uece.memcached.orchestrator.management.ServersHandler;
import br.uece.memcached.orchestrator.management.SharedIndex;

public final class Main {

	private static final Logger LOGGER = Logger.getLogger(Main.class);
	static final int CLIENTS_PORT = 9999;

	public static void main(String[] args) throws Exception {
		List<InetSocketAddress> serversAddresses = new ArrayList<InetSocketAddress>(args.length);
		for (String arg : args) {
			String[] hostAndPort = arg.split(":");
			serversAddresses.add(new InetSocketAddress(hostAndPort[0], Integer.parseInt(hostAndPort[1])));
		}
		
		if (serversAddresses.isEmpty()) {
			LOGGER.error("No servers informed");
			return;
		}
		
		SharedIndex sharedIndex = new SharedIndex();
		ServersHandler serversHandler = new ServersHandler(sharedIndex, serversAddresses);
		
		new Orchestrator(CLIENTS_PORT, serversHandler);
	}
}