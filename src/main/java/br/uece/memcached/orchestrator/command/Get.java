package br.uece.memcached.orchestrator.command;

import io.netty.channel.ChannelHandlerContext;

import java.util.List;

import org.apache.commons.lang3.StringUtils;

import br.uece.memcached.orchestrator.ServersHandler;
import br.uece.memcached.orchestrator.endpoint.Server;
import br.uece.memcached.orchestrator.endpoint.ServersUtil;

public class Get extends Command {
	
	private static final String LAST_RESPONSE_MESSAGE = "END\r\n";
	
	private Server server;
	private String lastResponseMessage;
	
	Get(String commandMessage, ServersHandler serversHandler, ChannelHandlerContext context) {
		super(commandMessage, serversHandler, context);
		
		List<Server> candidatateServers = serversHandler.getServersByObjectKey(getKey());
		this.server = ServersUtil.minLoad(candidatateServers);
		
		this.server.registerMessageHandler(this);
		this.server.sendMessage(commandMessage);
	}
	
	@Override
	public void handle(String message) {
		getContext().writeAndFlush(message);
		lastResponseMessage = message;
		
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
		return CommandType.GET;
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
			while (!LAST_RESPONSE_MESSAGE.equals(lastResponseMessage)) {
				server.wait();
			}
		}
	}
	
	@Override
	public void finish() {
		this.server.unregisterMessageHandler();
	}
	
}
