package com.github.jremoting.remoting;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import com.github.jremoting.core.InvokeResult;
import com.github.jremoting.core.Message;


import com.github.jremoting.core.Protocal;
import com.github.jremoting.exception.TimeoutException;
import com.github.jremoting.util.Logger;
import com.github.jremoting.util.LoggerFactory;

import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.util.concurrent.ScheduledFuture;


public class NettyClientHandler extends ChannelDuplexHandler {

	private final Map<Long, DefaultMessageFuture> futures = new HashMap<Long, DefaultMessageFuture>();
	private long nextMsgId = 0;
	private ScheduledFuture<?> removeTimoutFutrueTask;
	private final Protocal protocal;
	private static final Logger LOGGER = LoggerFactory.getLogger(NettyClientHandler.class);
	
	
	private final Runnable timeoutFutureReaper = new Runnable() {
		@Override
		public void run() {
			List<Long> timeoutMsgIds = new ArrayList<Long>(futures.size());
			for (DefaultMessageFuture future : futures.values()) {
				if(future.isTimeout()) {
					timeoutMsgIds.add(future.getMessage().getId());
				}
			}

			for (Long msgId : timeoutMsgIds) {
				DefaultMessageFuture timeoutFuture = futures.remove(msgId);
				if(timeoutFuture != null) {
					TimeoutException timeoutException = new TimeoutException("invoke timeout :" + timeoutFuture.getMessage().getTimeout());
					if(!timeoutFuture.isDone()) {
						timeoutFuture.setResult(timeoutException);
					}
				}
			}
			
			if(LOGGER.isDebugEnabled()) {
				LOGGER.debug("remove timeout future count:" + timeoutMsgIds.size());
			}
		}
	};
	
	
	
	
	
	public NettyClientHandler(Protocal protocal) {
		this.protocal = protocal;
	}

	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		if(removeTimoutFutrueTask != null) {
			removeTimoutFutrueTask.cancel(true);
		}
		
		ctx.fireChannelInactive();
	}
    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        if(msg instanceof DefaultMessageFuture) {
        	DefaultMessageFuture future = (DefaultMessageFuture)msg;
        	Message message  = future.getMessage();
        	if(message.isTwoWay()) {
        		message.setId(nextMsgId++);
        		futures.put(message.getId(), future);
        		if(removeTimoutFutrueTask == null) {
        			removeTimoutFutrueTask = ctx.executor().scheduleAtFixedRate(timeoutFutureReaper, 5, 5, TimeUnit.SECONDS);
            	}
        	}
        	
        	ctx.writeAndFlush(message, promise);
        }
        else {
        	ctx.write(msg, promise);
		}
    }
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
    	if (msg instanceof InvokeResult) {
			InvokeResult invokeResult = (InvokeResult)msg;
			DefaultMessageFuture future = futures.remove(invokeResult.getId());
			if(future != null) {
				future.setResult(invokeResult.getResult());
			}
		}
		else {
			ctx.fireChannelRead(msg);
		}
    }
}
