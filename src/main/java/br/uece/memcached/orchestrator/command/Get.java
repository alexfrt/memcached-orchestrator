package br.uece.memcached.orchestrator.command;

import io.netty.channel.ChannelHandlerContext;

import java.util.List;

import org.apache.commons.lang3.StringUtils;

import br.uece.memcached.orchestrator.endpoint.Server;
import br.uece.memcached.orchestrator.endpoint.ServersUtil;
import br.uece.memcached.orchestrator.management.ServersHandler;

public class Get extends Command {
	
	private static final String LAST_RESPONSE_MESSAGE = "END\r\n";
	
	private Server server;
	private String lastResponseMessage;
	
	Get(String commandMessage, ServersHandler serversHandler, ChannelHandlerContext context) {
		super(commandMessage, serversHandler, context);
		
		List<Server> servers = serversHandler.getServersAssociatedWithKey(getKey());
		if (!servers.isEmpty()) {
			this.server = ServersUtil.minLoad(servers);
			
			this.server.registerMessageHandler(this);
			this.server.sendMessage(commandMessage);
		}
		else {
			context.writeAndFlush(LAST_RESPONSE_MESSAGE);
		}
	}
	
	@Override
	public void handle(String message) {
		if (server != null) {
			getContext().writeAndFlush(message);
			lastResponseMessage = message;
			
			synchronized (server) {
				server.notify();
			}
		}
	}
	
	@Override
	protected String extractKey(String commandMessage) {
		return StringUtils.substringAfterLast(commandMessage, " ").replace("\r\n", "");
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
		if (server != null) {
			
			synchronized (server) {
				while (!LAST_RESPONSE_MESSAGE.equals(lastResponseMessage)) {
					server.wait();
				}
			}
		}
	}
	
	@Override
	public void finish() {
		if (server != null) {
			this.server.unregisterMessageHandler();
		}
	}
	
}
