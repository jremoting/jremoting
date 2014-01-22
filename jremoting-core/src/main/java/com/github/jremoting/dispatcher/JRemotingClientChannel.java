package com.github.jremoting.dispatcher;

import java.net.InetSocketAddress;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.handler.timeout.IdleStateHandler;
import com.github.jremoting.core.InvocationHolder;
import com.github.jremoting.core.Invocation;
import com.github.jremoting.core.InvocationResult;
import com.github.jremoting.core.Protocal;
import com.github.jremoting.core.Protocal.Pong;
import com.github.jremoting.core.RpcFuture;
import com.github.jremoting.exception.RpcConnectFailedException;
import com.github.jremoting.exception.RpcInvokeTimeoutException;
import com.github.jremoting.util.Logger;
import com.github.jremoting.util.LoggerFactory;
import com.github.jremoting.util.NetUtil;

public class JRemotingClientChannel implements InvocationHolder   {
	
	private static final Logger logger = LoggerFactory.getLogger(JRemotingClientChannel.class);
	
	private volatile io.netty.channel.Channel nettyChannel;
	
	private long defaultTimeout = 1000 * 60 * 5; // /5 mins
	
	private final Protocal protocal;
	
	private final String remoteAddress;

	private final AtomicLong nextInvocationId = new AtomicLong(0);
	
	private final EventLoopGroup eventLoopGroup;
	
	private final ConcurrentHashMap<Long, JRemotingRpcFuture> futures = new ConcurrentHashMap<Long, JRemotingRpcFuture>();
	
	private final int heartbeatSeconds;
	
	public JRemotingClientChannel(EventLoopGroup eventLoopGroup,  Protocal protocal ,String remoteAddress, int heartbeatSeconds) {
		this.remoteAddress = remoteAddress;
		this.protocal = protocal;
		this.eventLoopGroup = eventLoopGroup;
		this.heartbeatSeconds = heartbeatSeconds;
	}

	public RpcFuture write(final Invocation invocation) {

	    long invocationId = nextInvocationId.getAndIncrement();
	    invocation.setInvocationId(invocationId);
	    
		connect();
		
		JRemotingRpcFuture rpcFuture = new JRemotingRpcFuture(invocation);

		nettyChannel.writeAndFlush(invocation).addListener(new ChannelFutureListener() {
			
			@Override
			public void operationComplete(ChannelFuture future) throws Exception {
			    if (!future.isSuccess()) {
			    	JRemotingRpcFuture rpcFuture = futures.remove(invocation.getInvocationId());
			    	if(rpcFuture != null) {
			    		rpcFuture.setResult(future.cause());
			    	}
	                future.channel().close();
	            }
			}
		});
	     
	    futures.put(invocationId, rpcFuture);
	    
	
	    long timeout = invocation.getTimeout() > 0 ? invocation.getTimeout() : defaultTimeout;

		nettyChannel.eventLoop().schedule(new Runnable() {
			@Override
			public void run() {
				JRemotingRpcFuture rpcFuture = futures.remove(invocation
						.getInvocationId());
				if (rpcFuture != null) {
					rpcFuture.setResult(new RpcInvokeTimeoutException("timout!"));
				}
			}
		}, timeout, TimeUnit.MILLISECONDS);

	    return rpcFuture;
	}
	
	private void connect() {
		
		if(nettyChannel == null || !nettyChannel.isActive()){
			synchronized (this) {
				
				if(nettyChannel != null && nettyChannel.isActive()) {
					return;
				}
				
				InetSocketAddress address = NetUtil.toInetSocketAddress(remoteAddress);
				
				Bootstrap b = new Bootstrap();
				b.group(eventLoopGroup).channel(NioSocketChannel.class)
						.remoteAddress(address)
						.handler(new ChannelInitializer<SocketChannel>() {
							public void initChannel(SocketChannel ch) throws Exception {
				
								if(heartbeatSeconds > 0) {
									ch.pipeline().addLast(new IdleStateHandler(0, 0, heartbeatSeconds));
								}
								ch.pipeline().addLast(new NettyClientCodec(protocal,JRemotingClientChannel.this),
										new NettyClientHandler());
							} 
						});
				try {
					
					ChannelFuture f = b.connect().sync();
					nettyChannel = f.channel();	

				} catch (Exception e) {
					throw new RpcConnectFailedException("connection failed!",e);
				}
			}
		}
	}	
	
	private class NettyClientHandler extends ChannelInboundHandlerAdapter {
		
		@Override
		public void channelInactive(ChannelHandlerContext ctx) throws Exception {
			System.out.println("channelInactive");
		}
		
		@Override
		public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
			if (evt instanceof IdleStateEvent) {
				ctx.writeAndFlush(protocal.getPing()).addListener(ChannelFutureListener.CLOSE_ON_FAILURE);
			} else {
				super.userEventTriggered(ctx, evt);
			}
		}
		
		@Override
		public void channelRead(ChannelHandlerContext ctx, Object msg)
				throws Exception {
			if(msg instanceof Pong) {
				System.out.println("PONG");
				if(logger.isDebugEnabled()) {
					logger.debug("PONG  from " + NetUtil.toStringAddress((InetSocketAddress)ctx.channel().remoteAddress()));
				}
			}
			
			if(msg instanceof InvocationResult) {
				InvocationResult invocationResult = (InvocationResult)msg;
				JRemotingRpcFuture rpcFuture =  futures.remove(invocationResult.getInvocation().getInvocationId());
				if(rpcFuture != null) {
					rpcFuture.setResult(invocationResult.getResult());
				}
			}
		}
	    @Override
	    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
	            throws Exception {
	    	wakeupWaitingCaller(cause);
	    	ctx.channel().close();
	    	ctx.fireExceptionCaught(cause);
	    }
	}
	
	/**
	 * wakeup all wait caller when current connection lost
	 */
	private void wakeupWaitingCaller(Throwable cause) {
		synchronized (this){
			for (JRemotingRpcFuture future : futures.values()) {
				future.setResult(cause);
			}
			futures.clear();
		}
	}


	@Override
	public Invocation getInvocation(long invocationId) {
		JRemotingRpcFuture future = futures.get(invocationId);
		if (future != null) {
			return future.getInvocation();
		}
		return null;
	}
	
	
}
