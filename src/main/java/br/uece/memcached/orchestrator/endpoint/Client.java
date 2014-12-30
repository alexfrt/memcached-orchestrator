package br.uece.memcached.orchestrator.endpoint;

import io.netty.channel.ChannelHandlerContext;
import br.uece.memcached.orchestrator.GenericHandler;
import br.uece.memcached.orchestrator.ServersHandler;
import br.uece.memcached.orchestrator.command.Command;
import br.uece.memcached.orchestrator.command.CommandType;

public class Client extends GenericHandler {
	
	private ServersHandler serversHandler;
	private Command currentCommand;
	
	public Client(ServersHandler serversHandler) {
		this.serversHandler = serversHandler;
		this.currentCommand = null;
	}
	
	@Override
	protected void channelRead0(ChannelHandlerContext context, String message) throws Exception {
		if (currentCommand == null) {
			this.currentCommand = CommandType.newInstance(message, serversHandler, context);
		}
		else if (currentCommand.getType().isAppendable()) {
			currentCommand.append(message);
		}
		
		if (currentCommand.isComplete()) {
			currentCommand.waitResponse();
			currentCommand.finish();
		}
	}
	
	@Override
	public void channelReadComplete(ChannelHandlerContext handlerContext) {
		handlerContext.flush();
		currentCommand = null;
	}
}
