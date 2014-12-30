package br.uece.memcached.orchestrator.command;

import java.util.List;

import org.apache.commons.lang3.StringUtils;

import io.netty.channel.ChannelHandlerContext;
import br.uece.memcached.orchestrator.ServersHandler;
import br.uece.memcached.orchestrator.endpoint.Server;
import br.uece.memcached.orchestrator.endpoint.ServersUtil;

public class Set extends Command {
	
	private Server server;
	private Boolean isComplete;
	private Boolean hasResponded;
	
	Set(String commandMessage, ServersHandler serversHandler, ChannelHandlerContext context) {
		super(commandMessage, serversHandler, context);
		this.isComplete = Boolean.FALSE;
		
		List<Server> candidatateServers = serversHandler.getServersByObjectKey(getKey());
		this.server = ServersUtil.minLoad(candidatateServers);
		
		this.server.registerMessageHandler(this);
		this.server.sendMessage(commandMessage);
		
		hasResponded = Boolean.FALSE;
	}
	
	@Override
	public void handle(String message) {
		getContext().writeAndFlush(message);
		hasResponded = Boolean.TRUE;
		
		synchronized (server) {
			server.notify();
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
	public void append(String message) {
		if (!isComplete()) {
			this.server.sendMessage(message);
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
		synchronized (server) {
			while (!hasResponded) {
				server.wait();
			}
		}
	}
	
	@Override
	public void finish() {
		this.server.unregisterMessageHandler();
	}
	
}
