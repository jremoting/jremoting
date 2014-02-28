package com.github.jremoting.transport;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.RejectedExecutionException;

import com.github.jremoting.core.HeartbeatMessage;
import com.github.jremoting.core.Invoke;
import com.github.jremoting.core.InvokeResult;
import com.github.jremoting.core.Registry;
import com.github.jremoting.core.ServiceProvider;
import com.github.jremoting.exception.ServerBusyException;
import com.github.jremoting.exception.ServerErrorException;
import com.github.jremoting.exception.ServiceUnavailableException;
import com.github.jremoting.invoke.ServerInvokeFilterChain;
import com.github.jremoting.util.JvmUtil;
import com.github.jremoting.util.Logger;
import com.github.jremoting.util.LoggerFactory;
import com.github.jremoting.util.NetUtil;
import com.github.jremoting.util.ReflectionUtil;
import com.github.jremoting.util.concurrent.FutureCallback;

import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;



public class NettyServerHandler extends ChannelDuplexHandler {
	private static final Logger LOGGER = LoggerFactory.getLogger(NettyServerHandler.class);
	private final ServerInvokeFilterChain invokeFilterChain;
	private final Executor executor;
	private final Registry registry;
	private final Map<String, ServiceProvider> providers;
	
	public NettyServerHandler(Executor executor, ServerInvokeFilterChain invokeFilterChain,
			 Registry registry,Map<String, ServiceProvider> providers) {
		this.executor = executor;
		this.invokeFilterChain = invokeFilterChain;
		this.registry = registry;
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
			ServiceProvider provider = this.registry.getLocalProviders().get(invoke.getServiceId());
			if(provider == null) {
				provider = providers.get(invoke.getServiceId());
			}
			
			if(provider == null) {
				InvokeResult errorResult = new InvokeResult(new ServiceUnavailableException("no provider found"), invoke.getId(),
						invoke.getSerializer());
				ctx.writeAndFlush(errorResult).addListener(ChannelFutureListener.CLOSE_ON_FAILURE);
				return;
			}
			
			invoke.setProvider(provider);
			invoke.setTarget(provider.getTarget());
			invoke.setRemoteAddress(NetUtil.toStringAddress(ctx.channel().remoteAddress()));
		
			findTargetMethod(invoke, provider);
			
			if(invoke.getTargetMethod() == null) {
				InvokeResult errorResult = new InvokeResult(new ServiceUnavailableException("mo method found"), invoke.getId(),
						invoke.getSerializer());
				ctx.writeAndFlush(errorResult).addListener(ChannelFutureListener.CLOSE_ON_FAILURE);
				return;
			}
			
			Executor serviceExecutor = provider.getExecutor();
			if(serviceExecutor == null) {
				serviceExecutor = executor;
			}

			try {
				serviceExecutor.execute( new Runnable() {
					@Override
					public void run() {
						
						if(invoke.isAsync())  {
							doAsyncInvoke(invoke, ctx);
						}
						else {
							doSyncInvoke(invoke, ctx);
						}
					}
				});
				
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

	private void findTargetMethod(final Invoke invoke, ServiceProvider provider) {
		if(provider.isSupportAsync()) {
			Class<?>[] asyncParameterTypes = new Class<?>[invoke.getParameterTypes().length + 1];
			System.arraycopy(invoke.getParameterTypes(), 0, asyncParameterTypes, 0, invoke.getParameterTypes().length);
			asyncParameterTypes[invoke.getParameterTypes().length] = FutureCallback.class;
			
			Method targetMethod  = ReflectionUtil.findMethod(invoke.getTarget().getClass(), 
					"$" + invoke.getMethodName(),
					asyncParameterTypes);
			
		     if(targetMethod != null) {
		    	 invoke.setAsync(true);
		    	 invoke.setTargetMethod(targetMethod);
		     }
		} 

		if(invoke.getTargetMethod() == null) {
			Method targetMethod  = ReflectionUtil.findMethod(invoke.getTarget().getClass(), 
					 invoke.getMethodName(),
					invoke.getParameterTypes());
			if(targetMethod != null) {
				invoke.setAsync(false);
				invoke.setTargetMethod(targetMethod);
			}
		}
	}
	

	private void doSyncInvoke(final Invoke invoke,
			final ChannelHandlerContext ctx) {
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
	
	private void doAsyncInvoke(final Invoke invoke, final ChannelHandlerContext ctx ) {
		FutureCallback<Object> callback = new FutureCallback<Object>() {

			@Override
			public void onSuccess(Object result) {
				try {
					invokeFilterChain.endInvoke(invoke, result);
				} catch (Throwable th) {
					LOGGER.error("error happens when run server filter's endInvoke chain , msg->" + th.getMessage(), th);
				}
				
	
				InvokeResult invokeResult = new InvokeResult(result,invoke.getId(), invoke.getSerializer());
				ctx.writeAndFlush(invokeResult).addListener(ChannelFutureListener.CLOSE_ON_FAILURE);
				
			}

			@Override
			public void onFailure(Throwable t) {
				try {
					invokeFilterChain.endInvoke(invoke, t);
				} catch (Throwable th) {
					LOGGER.error("error happens when run server filter's endInvoke chain , msg->" + th.getMessage(), th);
				}
	
				InvokeResult errorResult = new InvokeResult(new ServerErrorException(t.getMessage()), invoke.getId(),
						invoke.getSerializer());
				ctx.writeAndFlush(errorResult ).addListener(ChannelFutureListener.CLOSE_ON_FAILURE);
			}
		};
		
		Object[] asyncArgs = new Object[invoke.getArgs().length + 1];
		System.arraycopy(invoke.getArgs(), 0, asyncArgs, 0, invoke.getArgs().length);
		asyncArgs[invoke.getArgs().length] = callback;

		invoke.setArgs(asyncArgs);
		invokeFilterChain.beginInvoke(invoke);
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
