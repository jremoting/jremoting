package com.github.jremoting.dispatcher;

import java.net.InetSocketAddress;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

import com.github.jremoting.core.Channel;
import com.github.jremoting.core.Invocation;
import com.github.jremoting.core.RpcException;
import com.github.jremoting.core.RpcFuture;



public class JRemotingChannel implements Channel {
	

	private volatile io.netty.channel.Channel nettyChannel;

	private final JRemotingClientDispatcher clientDispatcher;
	
	private AtomicLong nextRequestId = new AtomicLong(0);
	
	private ConcurrentHashMap<Long, JRemotingRpcFuture> futures = new ConcurrentHashMap<Long, JRemotingRpcFuture>();
	
	public JRemotingChannel(JRemotingClientDispatcher clientDispatcher) {
		this.clientDispatcher = clientDispatcher;
	}
	
	@Override
	public RpcFuture write(Invocation invocation) {

		connect(invocation.getAddress());
		
	    try {
			nettyChannel.write(invocation).await(5000);
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
	    
	    final JRemotingRpcFuture rpcFuture = new JRemotingRpcFuture(invocation);
	    
	    final long requestId = nextRequestId.getAndIncrement();
	    
	    futures.put(requestId, rpcFuture);
	    
	    nettyChannel.eventLoop().scheduleWithFixedDelay(new Runnable() {
			@Override
			public void run() {
				futures.remove(requestId);
				rpcFuture.setResult(new RpcException("timout!"));
			}
		}, 5000, 0, TimeUnit.MILLISECONDS);
	    
	    return rpcFuture;
	}
	
	private void connect(String address) {
		
		if(nettyChannel == null || !nettyChannel.isActive()){
			
			synchronized (this) {
				
				if(nettyChannel != null && nettyChannel.isActive()) {
					return;
				}
				
				String[] addressParts = address.split(":");
				String host = addressParts[0];
				int port =  Integer.parseInt(addressParts[1]);
				
				Bootstrap b = new Bootstrap();
				b.group(clientDispatcher.getGroup()).channel(NioSocketChannel.class)
						.remoteAddress(new InetSocketAddress(host, port))
						.handler(new ChannelInitializer<SocketChannel>() {
							public void initChannel(SocketChannel ch) throws Exception {
								ch.pipeline().addLast(new NettyCodec());
							}
						});
				try {
					ChannelFuture f = b.connect().sync();
					nettyChannel = f.channel();	

				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}
		}
	}
	
	
}
