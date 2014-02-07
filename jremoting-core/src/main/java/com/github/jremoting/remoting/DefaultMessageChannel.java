package com.github.jremoting.remoting;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.net.InetSocketAddress;
import java.util.concurrent.ConcurrentHashMap;

import com.github.jremoting.core.Message;
import com.github.jremoting.core.MessageChannel;
import com.github.jremoting.core.MessageFuture;
import com.github.jremoting.core.Protocal;
import com.github.jremoting.exception.ConnectFailedException;
import com.github.jremoting.util.NetUtil;

public class DefaultMessageChannel implements MessageChannel  {
	
	//key=  remoteIp:port 
	private final ConcurrentHashMap<String, Channel> channels = new ConcurrentHashMap<String, Channel>(); 
	private final ConcurrentHashMap<String, Object> channelLocks = new ConcurrentHashMap<String, Object>();
	
	private final EventLoopGroup eventLoopGroup;
	
	public DefaultMessageChannel(EventLoopGroup eventLoopGroup) {
		this.eventLoopGroup = eventLoopGroup;
	}
	
	
	@Override
	public MessageFuture send(Message msg) {
		String address = msg.getRemoteAddress();

		Channel channel = channels.get(address);

		if (channel == null || !channel.isActive()) {
			channel = connect(address, msg.getProtocal());
		}
		
		DefaultMessageFuture future = new DefaultMessageFuture(msg);
	
	    channel.writeAndFlush(future);
	    
	    return future;
	}
	
	private Channel connect(String remoteAddress,final Protocal protocal) {
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
					.handler(new ChannelInitializer<SocketChannel>() {
						@Override
						public void initChannel(SocketChannel ch)
								throws Exception {
							ch.pipeline().addLast(
									new NettyMessageCodec(protocal),
									new NettyClientHandler(protocal));
						}
					});
			try {

				ChannelFuture f = b.connect().sync();
				channel = f.channel();
				
				channels.put(remoteAddress, channel);
				return channel;

			} catch (Exception e) {
				throw new ConnectFailedException("connection failed!", e);
			}
		}
	}


	@Override
	public void close() {
		eventLoopGroup.shutdownGracefully();
	}



	
}
