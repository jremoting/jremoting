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

import com.github.jremoting.core.InvocationHolder;
import com.github.jremoting.core.Invocation;
import com.github.jremoting.core.InvocationResult;
import com.github.jremoting.core.InvocationWrapper;
import com.github.jremoting.core.Protocal.Pong;
import com.github.jremoting.core.RpcFuture;
import com.github.jremoting.exception.RpcInvokeTimeoutException;
import com.github.jremoting.util.Logger;
import com.github.jremoting.util.LoggerFactory;
import com.github.jremoting.util.NetUtils;

public class JRemotingClientChannel implements InvocationHolder   {
	
	private static final Logger logger = LoggerFactory.getLogger(JRemotingClientChannel.class);
	
	private volatile io.netty.channel.Channel nettyChannel;

	private final JRemotingClientDispatcher clientDispatcher;
	
	private final AtomicLong nextInvocationId = new AtomicLong(0);
	
	private final ConcurrentHashMap<Long, JRemotingRpcFuture> futures = new ConcurrentHashMap<Long, JRemotingRpcFuture>();
	
	public JRemotingClientChannel(JRemotingClientDispatcher clientDispatcher) {
		this.clientDispatcher = clientDispatcher;
	}

	public RpcFuture write(Invocation targetInvocation) {

	    final long invocationId = nextInvocationId.getAndIncrement();
	    
	    Invocation invocation =  new InvocationWrapper(targetInvocation) {
	    	@Override
	    	public long getInvocationId() {
	    		return invocationId;
	    	}
		}; 
			
		connect(invocation.getRemoteAddress());
		
		nettyChannel.write(invocation);
	     
		JRemotingRpcFuture rpcFuture = new JRemotingRpcFuture(invocation);

	    
	    futures.put(invocationId, rpcFuture);
	    
	    nettyChannel.eventLoop().scheduleWithFixedDelay(new Runnable() {
			@Override
			public void run() {
				JRemotingRpcFuture rpcFuture = futures.remove(invocationId);
				if(rpcFuture != null) {
					rpcFuture.setResult(new RpcInvokeTimeoutException("timout!"));
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
								ch.pipeline().addLast(new NettyClientCodec(), new NettyClientHandler());
							}
						});
				try {
					
					ChannelFuture f = b.connect().sync();
					nettyChannel = f.channel();	
					
					//TODO send heartbeat request in fixed internal

				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}
		}
	}
	
	private class NettyClientCodec extends ByteToMessageCodec<Invocation> {

		@Override
		protected void encode(ChannelHandlerContext ctx, Invocation msg, ByteBuf out)
				throws Exception {
			
			clientDispatcher.getProtocal().writeRequest(msg, new JRemotingChannelBuffer(out));
			
		}

		@Override
		protected void decode(ChannelHandlerContext ctx, ByteBuf in,
				List<Object> out) throws Exception {
		
			InvocationResult result = clientDispatcher.getProtocal()
					.readResponse(JRemotingClientChannel.this,
							new JRemotingChannelBuffer(in));
			if (result != null) {
				out.add(result);
			}
		}

	}
	
	
	private class NettyClientHandler extends ChannelInboundHandlerAdapter {
		@Override
		public void channelInactive(ChannelHandlerContext ctx) throws Exception {
			
			//maybe server provider is down so remove this channel from client dispatcher
			InetSocketAddress  socketAddress = (InetSocketAddress) ctx.channel().localAddress();
			
			String address =  NetUtils.toStringAddress(socketAddress);
			
			clientDispatcher.removeChannel(address);
		}

		@Override
		public void channelRead(ChannelHandlerContext ctx, Object msg)
				throws Exception {
			if(msg instanceof Pong) {
				if(logger.isDebugEnabled()) {
					logger.debug("PONG  from " + NetUtils.toStringAddress((InetSocketAddress)ctx.channel().remoteAddress()));
				}
			}
			
			if(msg instanceof InvocationResult) {
				InvocationResult invocationResult = (InvocationResult)msg;
				JRemotingRpcFuture rpcFuture =  futures.remove(invocationResult.getInvocationId());
				if(rpcFuture != null) {
					rpcFuture.setResult(invocationResult.getResult());
				}
			}
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
