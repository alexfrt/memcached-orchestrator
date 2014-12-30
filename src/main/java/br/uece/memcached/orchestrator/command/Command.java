package br.uece.memcached.orchestrator.command;

import io.netty.channel.ChannelHandlerContext;
import br.uece.memcached.orchestrator.ServersHandler;
import br.uece.memcached.orchestrator.endpoint.MessageHandler;

public abstract class Command implements MessageHandler {

	private final String key;
	private final String commandMessage;
	
	private final ServersHandler serversHandler;
	private final ChannelHandlerContext context;
	
	public Command(String commandMessage, ServersHandler serversHandler, ChannelHandlerContext context) {
		this.commandMessage = commandMessage;
		this.key = extractKey(commandMessage);
		this.serversHandler = serversHandler;
		this.context = context;
	}
	
	protected abstract String extractKey(String commandMessage);
	
	public final String getKey() {
		return key;
	}
	
	public final String getCommandMessage() {
		return commandMessage;
	}
	
	protected final ServersHandler getServersHandler() {
		return serversHandler;
	}
	
	protected final ChannelHandlerContext getContext() {
		return context;
	}
	
	public abstract CommandType getType();
	
	public abstract void append(String message);
	
	public abstract boolean isComplete();

	public abstract void waitResponse() throws InterruptedException;

	public abstract void finish();
	
}
