package com.github.jremoting.dispatcher;

import io.netty.channel.EventLoopGroup;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;

import com.github.jremoting.core.FinalFilter;
import com.github.jremoting.core.Invocation;
import com.github.jremoting.core.RpcFuture;
import com.github.jremoting.exception.RpcException;

public class JRemotingClientDispatcher extends FinalFilter {
	
	//key=  remoteIp:port 
	private ConcurrentHashMap<String, JRemotingClientChannel> channels = new ConcurrentHashMap<String, JRemotingClientChannel>(); 
	
	private final EventLoopGroup eventLoopGroup;

	public JRemotingClientDispatcher(EventLoopGroup eventLoopGroup) {
		this.eventLoopGroup = eventLoopGroup;
	}
	
	@Override
	protected Object doRpcInvoke(Invocation invocation)  {
		String address = invocation.getRemoteAddress();

		JRemotingClientChannel channel = channels.get(address);

		if (channel == null) {
			channel = new JRemotingClientChannel(eventLoopGroup, invocation.getProtocal() ,address); 
			channels.putIfAbsent(address, channel);
		}

		RpcFuture rpcFuture = channel.write(invocation);
		try {
			return rpcFuture.get();
		} catch (InterruptedException e) {
			throw new RpcException(e);
		} catch (ExecutionException e) {
			throw new RpcException(e);
		}
	}
}
