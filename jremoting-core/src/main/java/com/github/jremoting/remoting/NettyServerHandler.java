package com.github.jremoting.remoting;


import java.util.concurrent.Executor;

import com.github.jremoting.core.HeartbeatMessage;
import com.github.jremoting.core.Invoke;
import com.github.jremoting.core.InvokeResult;
import com.github.jremoting.invoke.ServerInvokeFilterChain;

import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;



public class NettyServerHandler extends ChannelDuplexHandler {
	private final ServerInvokeFilterChain invokeFilterChain;
	private final Executor executor;
	
	public NettyServerHandler(Executor executor, ServerInvokeFilterChain invokeFilterChain) {
		this.executor = executor;
		this.invokeFilterChain = invokeFilterChain;
	}
	
	@Override
	public void channelRead(final ChannelHandlerContext ctx, Object msg)
			throws Exception {
		if(msg instanceof HeartbeatMessage) {
			HeartbeatMessage heartbeatMessage = (HeartbeatMessage)msg;
			if(heartbeatMessage.isTwoWay()) {
				ctx.writeAndFlush(new HeartbeatMessage(false, heartbeatMessage.getProtocal(), 
						heartbeatMessage.getSerializer()));
			}
		}
		else if(msg instanceof Invoke) {
			final Invoke invoke = (Invoke)msg;
			
			executor.execute(new Runnable() {
				@Override
				public void run() {
					Object result = invokeFilterChain.invoke(invoke);
					InvokeResult invokeResult = new InvokeResult(result, invoke.getId(),invoke.getProtocal(), invoke.getSerializer());
					ctx.writeAndFlush(invokeResult);
				}
			});
		}
		else {
			ctx.fireChannelRead(msg);
		}
	}
}
