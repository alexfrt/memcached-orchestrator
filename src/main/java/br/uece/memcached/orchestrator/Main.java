package br.uece.memcached.orchestrator;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.log4j.Logger;

import br.uece.memcached.orchestrator.management.ServersHandler;
import br.uece.memcached.orchestrator.management.SharedIndex;

public final class Main {

	private static final Logger LOGGER = Logger.getLogger(Main.class);
	
	public static void main(String[] args) throws Exception {
		if (args.length < 3) {
			LOGGER.error("Insufficient parameters");
			return;
		}
		
		Integer clientsPort = Integer.parseInt(args[0]);
		Integer connectionsCount = Integer.parseInt(args[1]);
		
		List<InetSocketAddress> serversAddresses = new ArrayList<InetSocketAddress>(args.length);
		for (String arg : Arrays.copyOfRange(args, 2, args.length)) {
			String[] hostAndPort = arg.split(":");
			serversAddresses.add(new InetSocketAddress(hostAndPort[0], Integer.parseInt(hostAndPort[1])));
		}
		
		SharedIndex sharedIndex = new SharedIndex();
		ServersHandler serversHandler = new ServersHandler(sharedIndex, serversAddresses, connectionsCount);
		
		new Orchestrator(clientsPort, serversHandler);
	}
}