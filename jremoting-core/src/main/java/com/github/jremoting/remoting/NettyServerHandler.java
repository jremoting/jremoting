package com.github.jremoting.remoting;

import java.util.Map;
import java.util.concurrent.Executor;

import com.github.jremoting.core.HeartbeatMessage;
import com.github.jremoting.core.Invoke;
import com.github.jremoting.core.InvokeResult;
import com.github.jremoting.core.ServiceProvider;
import com.github.jremoting.invoke.ServerInvokeFilterChain;
import com.github.jremoting.util.Logger;
import com.github.jremoting.util.LoggerFactory;

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
		}
		else if(msg instanceof Invoke) {
			final Invoke invoke = (Invoke)msg;
			ServiceProvider provider = providers.get(invoke.getServiceName());
			invoke.setTarget(provider.getTarget());
			
			Runnable serviceRunnable = new Runnable() {
				@Override
				public void run() {
					Object result = invokeFilterChain.invoke(invoke);
					InvokeResult invokeResult = new InvokeResult(result, invoke.getId(),invoke.getSerializer());
					ctx.writeAndFlush(invokeResult).addListener(ChannelFutureListener.FIRE_EXCEPTION_ON_FAILURE);
				}
			};
			
			if(provider.getExecutor() != null) {
				provider.getExecutor().execute(serviceRunnable);
			}
			else {
				executor.execute(serviceRunnable);
			}
		}
		else {
			ctx.fireChannelRead(msg);
		}
	}
	
	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		
		ctx.fireChannelInactive();
		
		LOGGER.info("server connection inactive!");
	}
	
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
            throws Exception {
        LOGGER.error(cause.getMessage(), cause);
    }
}
