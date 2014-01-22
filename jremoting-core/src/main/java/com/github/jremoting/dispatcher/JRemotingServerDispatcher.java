package com.github.jremoting.dispatcher;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.DecoderException;

import com.github.jremoting.core.Invocation;
import com.github.jremoting.core.InvocationResult;
import com.github.jremoting.core.ServerDispatcher;
import com.github.jremoting.core.ServiceProvider;
import com.github.jremoting.core.Protocal.Ping;
import com.github.jremoting.exception.RpcException;
import com.github.jremoting.exception.RpcProtocalException;
import com.github.jremoting.exception.RpcServerErrorException;
import com.github.jremoting.protocal.Protocals;

public class JRemotingServerDispatcher implements ServerDispatcher {

	private volatile boolean started = false;
	private final EventLoopGroup parentGroup;
	private final EventLoopGroup childGroup;
	private final Protocals protocals;
	private final Executor executor;
	private int port = 8686;
	//key = serviceName:serviceVersion 
	private Map<String, ServiceProvider> providers = new ConcurrentHashMap<String, ServiceProvider>();
	

	public JRemotingServerDispatcher(EventLoopGroup parentGroup,
			EventLoopGroup childGroup,
			Protocals protocals,
			Executor executor) {
		this.parentGroup = parentGroup;
		this.childGroup = childGroup;
		this.protocals = protocals;
		this.executor = executor;
	}
	
	
	@Override
	public void registerProvider(ServiceProvider provider) {
		providers.put(provider.getService(), provider);
	}
	
	
	@Override
	public void start() {
		if(started) {
			return;
		}
		
		synchronized (this) {
			if(started) {
				return;
			}
			ServerBootstrap bootstrap = new ServerBootstrap();
			bootstrap.group(parentGroup, childGroup)
			.channel(NioServerSocketChannel.class).childHandler(new ChannelInitializer<SocketChannel>() {
				public void initChannel(SocketChannel ch) throws Exception {
					
					ch.pipeline().addLast(new NettyServerCodec(protocals),
							new NettyServerHandler());
				}
			});
			
			try {
				bootstrap.bind(port).sync();
			} catch (InterruptedException e) {
				throw new RpcException("start failed!");
			}
			
			started = true;
		}
	}
	
	private  class NettyServerHandler extends ChannelInboundHandlerAdapter {
		
		@Override
		public void channelRead(final ChannelHandlerContext ctx, final Object msg) {
			
			if(msg instanceof Ping) {
				Ping ping = (Ping)msg;
				ctx.writeAndFlush(ping.getProtocal().getPong()).addListener(ChannelFutureListener.CLOSE_ON_FAILURE);
				return;
			}
			
			if(msg instanceof Invocation) {
				executor.execute(new Runnable() {
					@Override
					public void run() {
						Invocation invocation = (Invocation)msg;
						
						ServiceProvider provider = providers.get(invocation.getService());

						try {
							if(provider == null) {
								throw new RpcException("no provider find!");
							}
							invocation.setTarget(provider.getTarget());
							Object result = provider.getPipeline().invoke(invocation);
						    InvocationResult invocationResult = new InvocationResult(result, invocation);
							ctx.channel().writeAndFlush(invocationResult).addListener(ChannelFutureListener.CLOSE_ON_FAILURE);
						} catch (Exception e) {
							RpcServerErrorException serverErrorException = new RpcServerErrorException(e);
							InvocationResult errorResult = new InvocationResult(serverErrorException, invocation);
							ctx.channel().writeAndFlush(errorResult).addListener(ChannelFutureListener.CLOSE_ON_FAILURE);
						}
					}
				});
			}
		}
		
	    @Override
	    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
	            throws Exception {

	    	if(cause instanceof DecoderException && cause.getCause() instanceof RpcProtocalException) {
	    		RpcProtocalException protocalException = (RpcProtocalException)cause.getCause();
	    		if(protocalException.getBadInvocation() != null) {
	    			InvocationResult errorResult = new InvocationResult(protocalException, protocalException.getBadInvocation());
					ctx.channel().writeAndFlush(errorResult).addListener(ChannelFutureListener.CLOSE_ON_FAILURE);;
	    		}
	    		else {
	    			ctx.channel().writeAndFlush(protocalException.getMessage())
	    			.addListener(ChannelFutureListener.CLOSE);;
				}
			
	    	}
	    	else {
	    	 	ctx.channel().close();
		    	ctx.fireExceptionCaught(cause);
			}
	    }
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

}
