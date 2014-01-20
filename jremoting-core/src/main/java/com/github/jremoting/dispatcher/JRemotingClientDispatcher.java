package com.github.jremoting.dispatcher;

import io.netty.channel.EventLoopGroup;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;

import com.github.jremoting.core.FinalFilter;
import com.github.jremoting.core.Invocation;
import com.github.jremoting.core.RpcFuture;
import com.github.jremoting.exception.RpcException;
import com.github.jremoting.protocal.Protocals;

public class JRemotingClientDispatcher extends FinalFilter {
	
	private ConcurrentHashMap<String, JRemotingClientChannel> channels = new ConcurrentHashMap<String, JRemotingClientChannel>(); 
	
	private final EventLoopGroup eventLoopGroup;
	
	private final Protocals protocals;
	
	public JRemotingClientDispatcher(EventLoopGroup eventLoopGroup , Protocals protocals) {
		this.eventLoopGroup = eventLoopGroup;
		this.protocals = protocals;
	}
	
	@Override
	protected Object doRpcInvoke(Invocation invocation)  {
		String address = invocation.getRemoteAddress();

		JRemotingClientChannel channel = channels.get(address);

		if (channel == null) {
			channel = new JRemotingClientChannel(this, address); 
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
	

	public EventLoopGroup getEventLoopGroup() {
		return eventLoopGroup;
	}
	
	public void removeChannel(String address) {
		channels.remove(address);
	}

	public Protocals getProtocals() {
		return protocals;
	}

	


}
