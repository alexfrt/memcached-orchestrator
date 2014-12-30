package br.uece.memcached.orchestrator.command;

import java.util.List;

import io.netty.channel.ChannelHandlerContext;

import org.apache.commons.lang3.StringUtils;

import br.uece.memcached.orchestrator.endpoint.Server;
import br.uece.memcached.orchestrator.management.ServersHandler;

public class Delete extends Command {
	
	private static final String DELETED_MESSAGE = "DELETED\r\n";
	private static final String NOTFOUND_MESSAGE = "NOT_FOUND\r\n";
	
	Delete(String commandMessage, ServersHandler serversHandler, ChannelHandlerContext context) {
		super(commandMessage, serversHandler, context);
		List<Server> servers = serversHandler.disassociateKeyFromServers(getKey());
		
		if (servers.isEmpty()) {
			context.writeAndFlush(NOTFOUND_MESSAGE);
		}
		else {
			context.writeAndFlush(DELETED_MESSAGE);
		}
	}
	
	@Override
	public void handle(String message) {
	}
	
	@Override
	protected String extractKey(String commandMessage) {
		return StringUtils.substringAfterLast(commandMessage, " ").replace("\r\n", "");
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
	}
	
	@Override
	public void finish() {
	}

}
