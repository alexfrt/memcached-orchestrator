package br.uece.memcached.orchestrator.command;

import java.util.List;

import org.apache.commons.lang3.StringUtils;

import io.netty.channel.ChannelHandlerContext;
import br.uece.memcached.orchestrator.ServersHandler;
import br.uece.memcached.orchestrator.endpoint.Server;
import br.uece.memcached.orchestrator.endpoint.ServersUtil;

public class Delete extends Command {
	
	private Server server;
	private Boolean hasResponded;

	Delete(String commandMessage, ServersHandler serversHandler, ChannelHandlerContext context) {
		super(commandMessage, serversHandler, context);
		
		List<Server> candidatateServers = serversHandler.getServersByObjectKey(getKey());
		this.server = ServersUtil.minLoad(candidatateServers);
		
		this.server.registerMessageHandler(this);
		this.server.sendMessage(commandMessage);
		
		this.hasResponded = Boolean.FALSE;
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
		return StringUtils.substringAfterLast(commandMessage, " ");
	}
	
	@Override
	public CommandType getType() {
		return CommandType.DELETE;
	}
	
	@Override
	public void append(String message) {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public boolean isComplete() {
		return Boolean.TRUE;
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
