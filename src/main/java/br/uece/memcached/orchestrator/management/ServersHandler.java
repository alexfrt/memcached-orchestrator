package br.uece.memcached.orchestrator.management;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import br.uece.memcached.orchestrator.endpoint.Server;
import br.uece.memcached.orchestrator.endpoint.ServersUtil;
import br.uece.memcached.orchestrator.management.ResourcePool.ResourceBuilder;

import com.google.common.collect.Iterables;

public class ServersHandler {
	
	private Random random;
	
	private SharedIndex sharedIndex;
	private Map<InetSocketAddress, ResourcePool<Server>> servers;
	
	public ServersHandler(SharedIndex sharedIndex, List<InetSocketAddress> serversAddresses, Integer connectionsCount) throws Exception {
		this.random = new Random();
		this.sharedIndex = sharedIndex;
		this.servers = new HashMap<InetSocketAddress, ResourcePool<Server>>(serversAddresses.size());
		
		for (final InetSocketAddress inetSocketAddress : serversAddresses) {
			
			this.servers.put(inetSocketAddress, new ResourcePool<Server>(connectionsCount, new ResourceBuilder<Server>() {
				@Override
				public Server build() {
					return new Server(inetSocketAddress);
				}
			}));
		}
	}
	
	public void associateKeyToServer(String key, Server server) {
		sharedIndex.put(key, Arrays.asList(server.getAddress()));
	}
	
	public Boolean disassociateKeyFromServers(String key) {
		@SuppressWarnings("unchecked")
		List<InetSocketAddress> addresses = (List<InetSocketAddress>) sharedIndex.remove(key);
		return addresses != null && !addresses.isEmpty();
	}
	
	public List<Server> getServersAssociatedWithKey(String key) {
		@SuppressWarnings("unchecked")
		List<InetSocketAddress> addresses = (List<InetSocketAddress>) sharedIndex.get(key);
		
		if (addresses != null && !addresses.isEmpty()) {
			List<Server> servers = new ArrayList<Server>(addresses.size());
			
			for (InetSocketAddress address : addresses) {
				servers.add(this.servers.get(address).getResource());
			}
			
			return servers;
		}
		else {
			return new ArrayList<Server>(0);
		}
	}
	
	public Server getBestServerAssociatedWithKey(String key) {
		//TODO get the load from some other place, avoiding resource lock
		
		List<Server> servers = getServersAssociatedWithKey(key);
		
		if (servers.size() > 1) {
			Server bestServer = ServersUtil.minLoad(servers);
			
			servers.remove(bestServer);
			releaseServerUsage(servers);
			
			return bestServer;
		}
		else {
			return Iterables.getOnlyElement(servers, null);
		}
	}

	public Server getBestServer() {
		//TODO select based on some threshold
		
		List<ResourcePool<Server>> servers = new ArrayList<ResourcePool<Server>>(this.servers.values());
		return servers.get(random.nextInt(servers.size())).getResource();
	}
	
	public void releaseServerUsage(List<Server> servers) {
		for (Server server : servers) {
			this.servers.get(server.getAddress()).returnResource(server);
		}
	}
	
	public void releaseServerUsage(Server... servers) {
		releaseServerUsage(Arrays.asList(servers));
	}
	
}