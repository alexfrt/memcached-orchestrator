package br.uece.memcached.orchestrator;

import org.apache.log4j.Logger;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

public abstract class GenericHandler extends SimpleChannelInboundHandler<String> {
	
	protected Logger logger;
	
	public GenericHandler() {
		logger = Logger.getLogger(getClass());
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext context, Throwable cause) throws Exception {
		Channel channel = context.channel();
		
		logger.error(
			String.format("Exception caught in channel. Local address: [%s], Remote address: [%s]", 
			channel.localAddress(), channel.remoteAddress()),
		cause);
		
		context.write(String.format("CLIENT_ERROR %s\r\n", cause.getMessage()));
		context.close();
	}

}
