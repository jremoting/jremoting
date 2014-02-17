package com.github.jremoting.remoting;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.RejectedExecutionException;

import com.github.jremoting.core.HeartbeatMessage;
import com.github.jremoting.core.Invoke;
import com.github.jremoting.core.InvokeResult;
import com.github.jremoting.core.ServiceProvider;
import com.github.jremoting.exception.ServerBusyException;
import com.github.jremoting.exception.ServerErrorException;
import com.github.jremoting.exception.ServiceUnavailableException;
import com.github.jremoting.invoke.ServerInvokeFilterChain;
import com.github.jremoting.util.JvmUtil;
import com.github.jremoting.util.Logger;
import com.github.jremoting.util.LoggerFactory;
import com.github.jremoting.util.NetUtil;

import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;



public class NettyServerHandler extends ChannelDuplexHandler {
	private static final Logger LOGGER = LoggerFactory.getLogger(NettyServerHandler.class);
	private final ServerInvokeFilterChain invokeFilterChain;
	private final Executor executor;
	private final Map<String, ServiceProvider> providers;
	
	public NettyServerHandler(Executor executor, ServerInvokeFilterChain invokeFilterChain,
			Map<String, ServiceProvider> providers) {
		this.executor = executor;
		this.invokeFilterChain = invokeFilterChain;
		this.providers = providers;
	}
	
	@Override
	public void channelRead(final ChannelHandlerContext ctx, Object msg)
			throws Exception {
		if(msg instanceof HeartbeatMessage) {
			HeartbeatMessage heartbeatMessage = (HeartbeatMessage)msg;
			if(heartbeatMessage.isTwoWay()) {
				ctx.writeAndFlush(HeartbeatMessage.PONG);
			}
			return;
		}

		if(msg instanceof Invoke) {
			final Invoke invoke = (Invoke)msg;
			ServiceProvider provider = providers.get(invoke.getServiceName());
			
			if(provider == null) {
				
				InvokeResult errorResult = new InvokeResult(new ServiceUnavailableException(), invoke.getId(),
						invoke.getSerializer());
				ctx.writeAndFlush(errorResult).addListener(ChannelFutureListener.CLOSE_ON_FAILURE);
				return;
			}
			
			invoke.setTarget(provider.getTarget());
			
			Runnable serviceRunnable = new Runnable() {
				@Override
				public void run() {
					try {
						Object result = invokeFilterChain.invoke(invoke);
						InvokeResult invokeResult = new InvokeResult(result,invoke.getId(), invoke.getSerializer());
						ctx.writeAndFlush(invokeResult).addListener(ChannelFutureListener.CLOSE_ON_FAILURE);
					} catch (Throwable e) {
						InvokeResult errorResult = new InvokeResult(new ServerErrorException(e.getMessage()), invoke.getId(),
								invoke.getSerializer());
						ctx.writeAndFlush(errorResult ).addListener(ChannelFutureListener.CLOSE_ON_FAILURE);
					}
				}
			};

			try {
				if (provider.getExecutor() != null) {
					provider.getExecutor().execute(serviceRunnable);
				} else {
					executor.execute(serviceRunnable);
				}
			} catch (RejectedExecutionException e) {
				
				JvmUtil.dumpJvmInfo();
				
				InvokeResult errorResult = new InvokeResult(new ServerBusyException(), invoke.getId(),
						invoke.getSerializer());
				ctx.writeAndFlush(errorResult).addListener(ChannelFutureListener.CLOSE_ON_FAILURE);

			}
		}
		else {
			ctx.fireChannelRead(msg);
		}
	}
	
	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		String remoteAddress = NetUtil.toStringAddress(ctx.channel().remoteAddress());
		LOGGER.info("connection inactive remoteAddress->" + remoteAddress);
	}
	
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
            throws Exception {
    	String remoteAddress = NetUtil.toStringAddress(ctx.channel().remoteAddress());
    	if(cause instanceof IOException) {
    		
            LOGGER.info("remoteAddress->" + remoteAddress + " " + cause.getMessage());
    	}
    	else {
            LOGGER.error("remoteAddress->" + remoteAddress + " " + cause.getMessage(), cause);
		}
    }
}
