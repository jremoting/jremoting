package com.github.jremoting.transport;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import com.github.jremoting.core.HeartbeatMessage;
import com.github.jremoting.core.Invoke;
import com.github.jremoting.core.InvokeResult;
import com.github.jremoting.exception.ConnectionLossException;
import com.github.jremoting.exception.ConnectionWriteException;
import com.github.jremoting.exception.TimeoutException;
import com.github.jremoting.util.Logger;
import com.github.jremoting.util.LoggerFactory;
import com.github.jremoting.util.NetUtil;

import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.handler.timeout.IdleStateEvent;


public class NettyClientHandler extends ChannelDuplexHandler {

	private final Map<Long, DefaultMessageFuture> futures = new HashMap<Long, DefaultMessageFuture>();
	private static final Logger LOGGER = LoggerFactory.getLogger(NettyClientHandler.class);

	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {

		String remoteAddress = NetUtil.toStringAddress(ctx.channel().remoteAddress());
		ConnectionLossException exception = new ConnectionLossException("connection lost remoteAddress->" + remoteAddress);
		for (DefaultMessageFuture future : futures.values()) {
			future.onResult(exception);
		}
		LOGGER.info("connection inactive remoteAddress->" + remoteAddress);
	}
	
	
	private class WriteChannelFutureListener implements ChannelFutureListener {

		private final long msgId;
		public WriteChannelFutureListener(long msgId) {
			this.msgId = msgId;
		}
		@Override
		public void operationComplete(ChannelFuture future) throws Exception {
			if(!future.isSuccess()) {
				DefaultMessageFuture msgFuture = futures.remove(msgId);
				if(msgFuture != null) {
					msgFuture.onResult(new ConnectionWriteException("msgId:" + msgId, future.cause()));
				}
				future.channel().close();
			}
		}
	}

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
   
        if(msg instanceof Invoke) {
    
        	final Invoke invoke  = (Invoke)msg;
		
        	if(invoke.getInvokeCount() == 0) {
        		futures.put(invoke.getId(), (DefaultMessageFuture)invoke.getResultFuture());
        	}
        	
        	invoke.incrementInvokeCount();
        	
			//schedule timeout task
			ctx.executor().schedule(new Runnable() {
				@Override
				public void run() {
					DefaultMessageFuture timoutFuture = futures.remove(invoke.getId());
					if (timoutFuture != null) {
						timoutFuture.onResult(new TimeoutException("invoke timeout :" + invoke.getTimeout()));
					}
				}
			}, invoke.getTimeout(), TimeUnit.MILLISECONDS);

			//write msg if failed notify caller and close channel
        	ctx.writeAndFlush(invoke, promise).addListener(new WriteChannelFutureListener(invoke.getId()));
        }
    }
    
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
    	if (msg instanceof InvokeResult) {
			InvokeResult invokeResult = (InvokeResult)msg;
			DefaultMessageFuture future = futures.remove(invokeResult.getId());
			if(future != null) {
				future.onResult(invokeResult.getResult());
			}
		}
    	else if(msg instanceof HeartbeatMessage) {
    		if(LOGGER.isDebugEnabled()) {
    			LOGGER.debug("PONG");
    		}
    	}
		else {
			ctx.fireChannelRead(msg);
		}
    }
    
    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
		if (evt instanceof IdleStateEvent) {
			ctx.writeAndFlush(HeartbeatMessage.PING).addListener(ChannelFutureListener.CLOSE_ON_FAILURE);
			if(LOGGER.isDebugEnabled()) {
    			LOGGER.debug("PING");
    		}
		}
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