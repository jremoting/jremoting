package com.github.jremoting.dispatcher;

import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;

import com.github.jremoting.core.FinalFilter;
import com.github.jremoting.core.Invocation;
import com.github.jremoting.core.Protocal;
import com.github.jremoting.core.RpcFuture;
import com.github.jremoting.exception.RpcException;

public class JRemotingClientDispatcher extends FinalFilter {
	
	private ConcurrentHashMap<String, JRemotingClientChannel> channels = new ConcurrentHashMap<String, JRemotingClientChannel>(); 
	
	private EventLoopGroup eventLoopGroup = new NioEventLoopGroup();
	
	private final Protocal protocal;
	
	public JRemotingClientDispatcher(Protocal protocal) {
		this.protocal = protocal;
	}
	
	@Override
	protected Object doRpcInvoke(Invocation invocation)  {
		String address = invocation.getRemoteAddress();

		JRemotingClientChannel channel = channels.get(address);

		if (channel == null) {
			channels.putIfAbsent(address, new JRemotingClientChannel(this));
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

	public Protocal getProtocal() {
		return protocal;
	}

	


}
