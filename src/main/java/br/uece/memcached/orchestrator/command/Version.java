package br.uece.memcached.orchestrator.command;

import io.netty.channel.ChannelHandlerContext;

public class Version extends Command {

	public Version(String commandMessage, ChannelHandlerContext context) {
		super(commandMessage, null, context);
		context.write("VERSION 1.4.14 (Ubuntu)\r\n");
	}

	@Override
	public void handle(String message) {
	}

	@Override
	protected String extractKey(String commandMessage) {
		return null;
	}

	@Override
	public CommandType getType() {
		return CommandType.VERSION;
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
