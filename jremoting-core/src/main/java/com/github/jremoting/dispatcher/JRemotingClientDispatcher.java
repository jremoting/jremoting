package com.github.jremoting.dispatcher;

import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;

import java.util.concurrent.ConcurrentHashMap;

import com.github.jremoting.core.ClientDispatcher;
import com.github.jremoting.core.Invocation;
import com.github.jremoting.core.Protocal;
import com.github.jremoting.core.RpcFuture;

public class JRemotingClientDispatcher implements ClientDispatcher {
	
	private ConcurrentHashMap<String, JRemotingClientChannel> channels = new ConcurrentHashMap<String, JRemotingClientChannel>(); 
	
	private EventLoopGroup eventLoopGroup = new NioEventLoopGroup();
	
	private final Protocal protocal;
	
	public JRemotingClientDispatcher(Protocal protocal) {
		this.protocal = protocal;
	}

	@Override
	public RpcFuture dispatch(Invocation invocation) {
		
		String address = invocation.getRemoteAddress();
		
		JRemotingClientChannel channel = channels.get(address);
		
		if(channel == null) {
			channels.putIfAbsent(address, new JRemotingClientChannel(this));
		}
		
		return channel.write(invocation);
	}

	public EventLoopGroup getEventLoopGroup() {
		return eventLoopGroup;
	}
	
	public void removeChannel(String address) {
		channels.remove(address);
	}

	public Protocal getProtocal() {
		return protocal;
	}


}
