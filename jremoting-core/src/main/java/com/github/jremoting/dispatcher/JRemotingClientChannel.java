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
import io.netty.handler.codec.MessageToByteEncoder;
import io.netty.handler.codec.ReplayingDecoder;

import com.github.jremoting.core.InvocationHolder;
import com.github.jremoting.core.Invocation;
import com.github.jremoting.core.InvocationResult;
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
	
	private final String remoteAddress;

	private final JRemotingClientDispatcher clientDispatcher;
	
	private final AtomicLong nextInvocationId = new AtomicLong(0);
	
	private final ConcurrentHashMap<Long, JRemotingRpcFuture> futures = new ConcurrentHashMap<Long, JRemotingRpcFuture>();
	
	public JRemotingClientChannel(JRemotingClientDispatcher clientDispatcher, String remoteAddress) {
		this.clientDispatcher = clientDispatcher;
		this.remoteAddress = remoteAddress;
	}

	public RpcFuture write(final Invocation invocation) {

	    long invocationId = nextInvocationId.getAndIncrement();
	    invocation.setInvocationId(invocationId);
	    
		connect();
		
		nettyChannel.writeAndFlush(invocation);
	     
		JRemotingRpcFuture rpcFuture = new JRemotingRpcFuture(invocation);

	    
	    futures.put(invocationId, rpcFuture);
	    
/*	    nettyChannel.eventLoop().scheduleWithFixedDelay(new Runnable() {
			@Override
			public void run() {
				JRemotingRpcFuture rpcFuture = futures.remove(invocation.getInvocationId());
				if(rpcFuture != null) {
					rpcFuture.setResult(new RpcInvokeTimeoutException("timout!"));
				}
			}
		}, 5000, 0, TimeUnit.MILLISECONDS);*/
	    
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
				b.group(clientDispatcher.getEventLoopGroup()).channel(NioSocketChannel.class)
						.remoteAddress(address)
						.handler(new ChannelInitializer<SocketChannel>() {
							public void initChannel(SocketChannel ch) throws Exception {
				
								ch.pipeline().addLast(new NettyClientEncoder(), new NettyClientDecoder(),
										new NettyClientHandler());
							}
						});
				try {
					
					ChannelFuture f = b.connect().sync();
					nettyChannel = f.channel();	
					
					//TODO send heartbeat request in fixed internal

				} catch (Exception e) {
					throw new RpcConnectFailedException("connection failed!",e);
				}
			}
		}
	}
	
	private class NettyClientDecoder extends ReplayingDecoder<Void> {
		@Override
		protected void decode(ChannelHandlerContext ctx, ByteBuf in,
				List<Object> out) throws Exception {
			InvocationResult result = clientDispatcher.getProtocals()
					.readResponse(JRemotingClientChannel.this,
							new JRemotingChannelBuffer(in));
			if (result != null) {
				out.add(result);
			}
		}
	}
	
	private class NettyClientEncoder extends MessageToByteEncoder<Invocation> {

		@Override
		protected void encode(ChannelHandlerContext ctx, Invocation msg,
				ByteBuf out) throws Exception {
			msg.getProtocal().writeRequest(msg, new JRemotingChannelBuffer(out));
		}
		
	}
	
	
	private class NettyClientHandler extends ChannelInboundHandlerAdapter {
		@Override
		public void channelInactive(ChannelHandlerContext ctx) throws Exception {
			
			//maybe server provider is down so remove this channel from client dispatcher
			InetSocketAddress  socketAddress = (InetSocketAddress) ctx.channel().localAddress();
			
			String address =  NetUtil.toStringAddress(socketAddress);
			
			clientDispatcher.removeChannel(address);
		}

		@Override
		public void channelRead(ChannelHandlerContext ctx, Object msg)
				throws Exception {
			if(msg instanceof Pong) {
				if(logger.isDebugEnabled()) {
					logger.debug("PONG  from " + NetUtil.toStringAddress((InetSocketAddress)ctx.channel().remoteAddress()));
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
