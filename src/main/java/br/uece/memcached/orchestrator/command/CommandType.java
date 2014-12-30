package br.uece.memcached.orchestrator.command;

import io.netty.channel.ChannelHandlerContext;
import br.uece.memcached.orchestrator.ServersHandler;

public enum CommandType {
	
	SET(true),
	GET(false),
	GETS(false),
	DELETE(false),
	VERSION(false),
	QUIT(false);
	
	private Boolean appendable;
	
	private CommandType(Boolean appendable) {
		this.appendable = appendable;
	}
	
	public Boolean isAppendable() {
		return appendable;
	}
	
	public static Command newInstance(String command, ServersHandler serversHandler, ChannelHandlerContext context) {
		String commandType;
		try {
			commandType = command.substring(0, command.indexOf(' '));
		}
		catch (Exception e) {
			commandType = command.replace("\r\n", "");
		}
		
		switch (commandType) {
		case "set":
			return new Set(command, serversHandler, context);
		case "get":
			return new Get(command, serversHandler, context);
		case "delete":
			return new Delete(command, serversHandler, context);
		case "version":
			return new Version(command, context);
		case "quit":
			return new Quit(command, context);
		default:
			throw new UnsupportedOperationException(String.format("Command type [%s] not supported", commandType));
		}
	}

}
