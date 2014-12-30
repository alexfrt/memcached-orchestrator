package br.uece.memcached.orchestrator.command;

import io.netty.channel.ChannelHandlerContext;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import br.uece.memcached.orchestrator.endpoint.Server;
import br.uece.memcached.orchestrator.management.ServersHandler;

public class Set extends Command {
	
	private List<Server> servers;
	private List<Server> firstMessageSentTo;
	private Boolean isComplete;
	private Boolean hasResponded;
	
	Set(final String commandMessage, ServersHandler serversHandler, ChannelHandlerContext context) {
		super(commandMessage, serversHandler, context);
		
		this.isComplete = Boolean.FALSE;
		this.firstMessageSentTo = Collections.synchronizedList(new ArrayList<Server>());
		
		this.servers = serversHandler.getServersAssociatedWithKey(getKey());
		if (!servers.isEmpty()) {
			for (final Server server : servers) {
				new Thread(new Runnable() {
					@Override
					public void run() {
						server.registerMessageHandler(Set.this);
						server.sendMessage(commandMessage);
						
						synchronized (firstMessageSentTo) {
							firstMessageSentTo.add(server);
							firstMessageSentTo.notify();
						}
					}
				}).start();
			}
		}
		else {
			Server bestServer = serversHandler.getBestServer();
			this.servers = Arrays.asList(bestServer);
			
			bestServer.registerMessageHandler(this);
			bestServer.sendMessage(commandMessage);
			
			serversHandler.associateKeyToServer(getKey(), bestServer);
			
			synchronized (firstMessageSentTo) {
				firstMessageSentTo.add(bestServer);
				firstMessageSentTo.notify();
			}
		}
		
		hasResponded = Boolean.FALSE;
	}
	
	@Override
	public void handle(String message) {
		getContext().writeAndFlush(message);
		hasResponded = Boolean.TRUE;
		
		synchronized (servers) {
			servers.notify();
		}
	}
	
	@Override
	protected String extractKey(String commandMessage) {
		return StringUtils.substringBetween(commandMessage, " ");
	}
	
	@Override
	public CommandType getType() {
		return CommandType.SET;
	}
	
	@Override
	public void append(final String message) {
		if (!isComplete()) {
			for (final Server server : servers) {
				new Thread(new Runnable() {
					@Override
					public void run() {
						synchronized (firstMessageSentTo) {
							while (!firstMessageSentTo.contains(server)) {
								try {
									firstMessageSentTo.wait();
								} catch (InterruptedException e) {
									throw new RuntimeException(e);
								}
							}
						}
						
						server.sendMessage(message);
					}
				}).start();
			}
			
			isComplete = Boolean.TRUE;
		}
		else {
			throw new IllegalStateException("Set command already completed");
		}
	}
	
	@Override
	public boolean isComplete() {
		return isComplete;
	}
	
	@Override
	public void waitResponse() throws InterruptedException {
		synchronized (servers) {
			while (!hasResponded) {
				servers.wait();
			}
		}
	}
	
	@Override
	public void finish() {
		for (Server server : servers) {
			server.unregisterMessageHandler();
		}
		
		getServersHandler().releaseServerUsage(servers);
	}
	
}
