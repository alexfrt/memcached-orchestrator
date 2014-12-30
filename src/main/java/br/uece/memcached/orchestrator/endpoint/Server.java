package br.uece.memcached.orchestrator.endpoint;

import java.net.InetSocketAddress;

import org.apache.log4j.Logger;

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
	
	private static final Logger LOGGER = Logger.getLogger(Server.class);
	
	private final InetSocketAddress address;
	
	private EventLoopGroup eventLoopGroup;
	private Channel channel;
	
	private ChannelPipeline pipeline;
	private Boolean hasMessageHandler;
	
	public Server(InetSocketAddress address) {
		this.address = address;
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
			channel = bootstrap.connect(address).sync().channel();
		}
		catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
		
		hasMessageHandler = Boolean.FALSE;
		LOGGER.info(String.format("Connected to server [%s]", address));
	}
	
	public InetSocketAddress getAddress() {
		return address;
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

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((address == null) ? 0 : address.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Server other = (Server) obj;
		if (address == null) {
			if (other.address != null)
				return false;
		} else if (!address.equals(other.address))
			return false;
		return true;
	}
	
}