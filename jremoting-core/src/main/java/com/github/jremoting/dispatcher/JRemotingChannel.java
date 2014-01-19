package com.github.jremoting.dispatcher;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.ByteToMessageCodec;

import com.github.jremoting.core.Channel;
import com.github.jremoting.core.Invocation;
import com.github.jremoting.core.InvocationResult;
import com.github.jremoting.core.RpcFuture;
import com.github.jremoting.core.RpcTimeoutException;
import com.github.jremoting.util.NetUtils;



public class JRemotingChannel implements Channel {
	

	private volatile io.netty.channel.Channel nettyChannel;

	private final JRemotingClientDispatcher clientDispatcher;
	
	private AtomicLong nextInvocationId = new AtomicLong(0);
	
	private ConcurrentHashMap<Long, JRemotingRpcFuture> futures = new ConcurrentHashMap<Long, JRemotingRpcFuture>();
	
	public JRemotingChannel(JRemotingClientDispatcher clientDispatcher) {
		this.clientDispatcher = clientDispatcher;
	}
	
	@Override
	public RpcFuture write(Invocation invocation) {

		connect(invocation.getAddress());

		nettyChannel.write(invocation);
	
	    JRemotingRpcFuture rpcFuture = new JRemotingRpcFuture(invocation);
	    
	    final long invocationIdId = nextInvocationId.getAndIncrement();
	    
	    futures.put(invocationIdId, rpcFuture);
	    
	    nettyChannel.eventLoop().scheduleWithFixedDelay(new Runnable() {
			@Override
			public void run() {
				JRemotingRpcFuture rpcFuture = futures.remove(invocationIdId);
				if(rpcFuture != null) {
					rpcFuture.setResult(new RpcTimeoutException("timout!"));
				}
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
				
				InetSocketAddress remoteAddress = NetUtils.toInetSocketAddress(address);
				
				Bootstrap b = new Bootstrap();
				b.group(clientDispatcher.getEventLoopGroup()).channel(NioSocketChannel.class)
						.remoteAddress(remoteAddress)
						.handler(new ChannelInitializer<SocketChannel>() {
							public void initChannel(SocketChannel ch) throws Exception {
								ch.pipeline().addLast(new NettyCodec(), new NettyClientHandler());
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
	
	private class NettyCodec extends ByteToMessageCodec<Invocation> {

		@Override
		protected void encode(ChannelHandlerContext ctx, Invocation msg, ByteBuf out)
				throws Exception {
			// TODO Auto-generated method stub
			
		}

		@Override
		protected void decode(ChannelHandlerContext ctx, ByteBuf in,
				List<Object> out) throws Exception {
			// TODO Auto-generated method stub
		}

	}
	
	
	private class NettyClientHandler extends ChannelInboundHandlerAdapter {
		@Override
		public void channelInactive(ChannelHandlerContext ctx) throws Exception {
			InetSocketAddress  socketAddress = (InetSocketAddress) ctx.channel().localAddress();
			
			String address =  NetUtils.toStringAddress(socketAddress);
			
			clientDispatcher.removeAddress(address);
		}

		@Override
		public void channelRead(ChannelHandlerContext ctx, Object msg)
				throws Exception {
			if(msg instanceof InvocationResult) {
				InvocationResult invocationResult = (InvocationResult)msg;
				JRemotingRpcFuture rpcFuture =  futures.remove(invocationResult.getInvocationId());
				if(rpcFuture != null) {
					rpcFuture.setResult(invocationResult.getResult());
				}
			}
		}
	}
	
	
}
