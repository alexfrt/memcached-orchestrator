package br.uece.memcached.orchestrator.management;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import br.uece.memcached.orchestrator.endpoint.Server;
import br.uece.memcached.orchestrator.endpoint.ServersUtil;

public class ServersHandler {
	
	private SharedIndex sharedIndex;
	
	private Map<InetSocketAddress, Server> servers;
	private List<Server> serverList;
	
	//TODO implement the Server pool
	
	public ServersHandler(SharedIndex sharedIndex, List<InetSocketAddress> serversAddresses) throws Exception {
		this.sharedIndex = sharedIndex;
		
		this.servers = new HashMap<InetSocketAddress, Server>(serversAddresses.size());
		this.serverList = new ArrayList<Server>(serversAddresses.size());
		
		for (InetSocketAddress inetSocketAddress : serversAddresses) {
			Server server = new Server(inetSocketAddress);
			
			this.servers.put(inetSocketAddress, server);
			this.serverList.add(server);
		}
	}
	
	public void associateKeyToServer(String key, Server server) {
		sharedIndex.put(key, Arrays.asList(server.getAddress()));
	}
	
	public List<Server> disassociateKeyFromServers(String key) {
		@SuppressWarnings("unchecked")
		List<InetSocketAddress> holders = (List<InetSocketAddress>) sharedIndex.remove(key);
		
		return convertAddressesToServers(holders);
	}
	
	public List<Server> getServersAssociatedWithKey(String key) {
		@SuppressWarnings("unchecked")
		List<InetSocketAddress> holders = (List<InetSocketAddress>) sharedIndex.get(key);
		
		return convertAddressesToServers(holders);
	}

	public List<Server> getBestServers() {
		//TODO improve performance
		//TODO select based on some threshold
		
		List<Server> servers = new ArrayList<Server>(serverList);
		if (serverList.size() > 3) {
			
			List<Server> selecteds = new ArrayList<Server>(3);
			
			for (int i = 0; i < 3; i++) {
				Server server = ServersUtil.minLoad(servers);
				selecteds.add(server);
				servers.remove(server);
			}
			
			return selecteds;
		}
		else {
			return servers;
		}
	}
	
	private List<Server> convertAddressesToServers(List<InetSocketAddress> holders) {
		if (holders != null && !holders.isEmpty()) {
			List<Server> servers = new ArrayList<Server>(holders.size());
			for (InetSocketAddress holderAddress : holders) {
				servers.add(this.servers.get(holderAddress));
			}
			
			return servers;
		}
		else {
			return new ArrayList<Server>(0);
		}
	}

}