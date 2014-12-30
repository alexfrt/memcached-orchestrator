package br.uece.memcached.orchestrator.endpoint;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.Delimiters;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import br.uece.memcached.orchestrator.GenericHandler;

public final class Server {
	
	private EventLoopGroup eventLoopGroup;
	private Channel channel;
	
	private ChannelPipeline pipeline;
	private Boolean hasMessageHandler;
	
	public Server(String host, Integer port) {
		eventLoopGroup = new NioEventLoopGroup();
		
		Bootstrap bootstrap = new Bootstrap();
		bootstrap.group(eventLoopGroup)
		.channel(NioSocketChannel.class)
		.handler(new ChannelInitializer<SocketChannel>() {
			
			private final StringDecoder DECODER = new StringDecoder();
			private final StringEncoder ENCODER = new StringEncoder();
			
			@Override
			protected void initChannel(SocketChannel channel) throws Exception {
				ChannelPipeline pipeline = channel.pipeline();
				
				pipeline.addLast(new DelimiterBasedFrameDecoder(8192, false, Delimiters.lineDelimiter()));
				pipeline.addLast(DECODER);
				pipeline.addLast(ENCODER);
				
				Server.this.pipeline = pipeline;
			}
		});
		
		try {
			channel = bootstrap.connect(host, port).sync().channel();
		}
		catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
		
		hasMessageHandler = Boolean.FALSE;
	}
	
	public void sendMessage(String message) {
		channel.writeAndFlush(message);
	}
	
	public void registerMessageHandler(final MessageHandler messageHandler) {
		if (hasMessageHandler) {
			throw new IllegalStateException("Message handler already registered");
		}
		
		pipeline.addLast(new GenericHandler() {
			@Override
			protected void channelRead0(ChannelHandlerContext ctx, String msg) throws Exception {
				messageHandler.handle(msg);
			}
		});
		
		hasMessageHandler = true;
	}
	
	public void unregisterMessageHandler() {
		if (!hasMessageHandler) {
			throw new IllegalStateException("There is no message handler registered");
		}
		
		pipeline.removeLast();
		hasMessageHandler = false;
	}
	
	public Float getLoad() {
		return 10f;
	}
	
}