package com.github.jremoting.remoting;


import java.util.concurrent.Executor;

import com.github.jremoting.core.HeartbeatMessage;
import com.github.jremoting.core.Invoke;
import com.github.jremoting.core.InvokeResult;
import com.github.jremoting.invoker.ServerRpcInvoker;

import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;



public class NettyServerHandler extends ChannelDuplexHandler {
	private final ServerRpcInvoker serverRpcInvoker;
	private final Executor executor;
	
	public NettyServerHandler(Executor executor, ServerRpcInvoker serverRpcInvoker) {
		this.executor = executor;
		this.serverRpcInvoker = serverRpcInvoker;
	}
	
	@Override
	public void channelRead(final ChannelHandlerContext ctx, Object msg)
			throws Exception {
		if(msg instanceof HeartbeatMessage) {
			HeartbeatMessage heartbeatMessage = (HeartbeatMessage)msg;
			if(heartbeatMessage.isTwoWay()) {
				ctx.writeAndFlush(new HeartbeatMessage(false, heartbeatMessage.getProtocal(), 
						heartbeatMessage.getSerializerId()));
			}
		}
		else if(msg instanceof Invoke) {
			final Invoke invoke = (Invoke)msg;
			
			executor.execute(new Runnable() {
				@Override
				public void run() {
					Object result = serverRpcInvoker.invoke(invoke);
					InvokeResult invokeResult = new InvokeResult(result, invoke.getId(),invoke.getProtocal(), invoke.getSerializerId());
					ctx.writeAndFlush(invokeResult);
				}
			});
		}
		else {
			ctx.fireChannelRead(msg);
		}
	}
}
