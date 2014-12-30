package br.uece.memcached.orchestrator;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.Delimiters;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import br.uece.memcached.orchestrator.endpoint.Client;
import br.uece.memcached.orchestrator.management.ServersHandler;

public class Orchestrator {

	private final ServersHandler serversHandler;
	
	private final EventLoopGroup bossGroup;
	private final EventLoopGroup workerGroup;
	private final Channel channel;
	
	public Orchestrator(Integer port, ServersHandler serversHandler) throws Exception {
		this.serversHandler = serversHandler;
		
		bossGroup = new NioEventLoopGroup();
		workerGroup = new NioEventLoopGroup();
		
		ServerBootstrap serverBootstrap = new ServerBootstrap();
		serverBootstrap.group(bossGroup, workerGroup)
		.channel(NioServerSocketChannel.class)
		.handler(new LoggingHandler(LogLevel.INFO))
		.childHandler(new Initializer());
		
		ChannelFuture channelFuture = serverBootstrap.bind(port);
		channel = channelFuture.sync().channel();
	}
	
	public void close() {
		channel.close().syncUninterruptibly();
		bossGroup.shutdownGracefully();
		workerGroup.shutdownGracefully();
	}
	
	private class Initializer extends ChannelInitializer<SocketChannel> {
		@Override
		public void initChannel(SocketChannel socketChannel) throws Exception {
			ChannelPipeline pipeline = socketChannel.pipeline();
			
			pipeline.addLast(new DelimiterBasedFrameDecoder(8192, false, Delimiters.lineDelimiter()));
			pipeline.addLast(new StringDecoder());
			pipeline.addLast(new StringEncoder());
			
			pipeline.addLast(new Client(serversHandler));
		}
	}
	
}
