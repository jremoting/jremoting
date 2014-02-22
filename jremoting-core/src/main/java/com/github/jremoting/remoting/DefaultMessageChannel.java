package com.github.jremoting.remoting;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;

import java.net.InetSocketAddress;
import java.util.concurrent.ConcurrentHashMap;

import com.github.jremoting.core.Invoke;
import com.github.jremoting.core.MessageChannel;
import com.github.jremoting.core.Protocal;
import com.github.jremoting.exception.ConnectionRefusedException;
import com.github.jremoting.util.NetUtil;

public class DefaultMessageChannel implements MessageChannel  {
	
	//key=  remoteIp:port 
	private final ConcurrentHashMap<String, Channel> channels = new ConcurrentHashMap<String, Channel>(); 
	private final ConcurrentHashMap<String, Object> channelLocks = new ConcurrentHashMap<String, Object>();
	
	private final EventLoopGroup eventLoopGroup;
	private final Protocal protocal;
	
	public DefaultMessageChannel(EventLoopGroup eventLoopGroup, Protocal protocal) {
		this.eventLoopGroup = eventLoopGroup;
		this.protocal = protocal;
	}
	
	
	@Override
	public void send(Invoke invoke) {
		
		String address = invoke.getRemoteAddress();

		Channel channel = channels.get(address);

		if (channel == null || !channel.isActive()) {
			channel = connect(address);
		}

		channel.writeAndFlush(invoke);
	}
	
	private Channel connect(String remoteAddress) {
		Object channelLock = channelLocks.get(remoteAddress);
		if (channelLock == null) {
			channelLocks.putIfAbsent(remoteAddress, new Object());
			channelLock = channelLocks.get(remoteAddress);
		}

		synchronized (channelLock) {
			Channel channel = channels.get(remoteAddress);

			if (channel != null && channel.isActive()) {
				return channel;
			}

			InetSocketAddress address = NetUtil
					.toInetSocketAddress(remoteAddress);

			Bootstrap b = new Bootstrap();
			b.group(eventLoopGroup).channel(NioSocketChannel.class)
					.remoteAddress(address)
					.option(ChannelOption.TCP_NODELAY, true)
					.option(ChannelOption.SO_KEEPALIVE, true)
					.option(ChannelOption.SO_REUSEADDR, true)
					.handler(new ChannelInitializer<SocketChannel>() {
						@Override
						public void initChannel(SocketChannel ch)
								throws Exception {
							ch.pipeline().addLast(new IdleStateHandler(0,0,5),
									new NettyMessageCodec(protocal),
									new NettyClientHandler());
						}
					});
			try {

				ChannelFuture f = b.connect().sync();
				channel = f.channel();
				
				channels.put(remoteAddress, channel);
				return channel;

			} catch (Exception e) {
				throw new ConnectionRefusedException("connection failed!", e);
			}
		}
	}


	@Override
	public void close() {
		eventLoopGroup.shutdownGracefully();
	}
}
