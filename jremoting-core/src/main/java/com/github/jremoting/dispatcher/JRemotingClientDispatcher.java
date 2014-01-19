package com.github.jremoting.dispatcher;

import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;

import java.util.concurrent.ConcurrentHashMap;

import com.github.jremoting.core.Channel;
import com.github.jremoting.core.ClientDispatcher;
import com.github.jremoting.core.Invocation;
import com.github.jremoting.core.RpcFuture;

public class JRemotingClientDispatcher implements ClientDispatcher {
	
	private ConcurrentHashMap<String, Channel> channels = new ConcurrentHashMap<String, Channel>(); 
	
	private EventLoopGroup eventLoopGroup = new NioEventLoopGroup();
	
	
	
	@Override
	public RpcFuture dispatch(Invocation invocation) {
		String address = invocation.getAddress();
		
		Channel channel = channels.get(address);
		
		if(channel == null) {
			channels.putIfAbsent(address, new JRemotingChannel(this));
		}
		
		return channel.write(invocation);
	}

	public EventLoopGroup getEventLoopGroup() {
		return eventLoopGroup;
	}
	
	public void removeAddress(String address) {
		channels.remove(address);
	}

}
