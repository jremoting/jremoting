package com.github.jremoting.remoting;

import java.util.HashMap;
import java.util.Map;

import com.github.jremoting.core.InvokeResult;
import com.github.jremoting.core.Message;


import com.github.jremoting.core.Protocal;

import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;


public class NettyClientHandler extends ChannelDuplexHandler {

	private final Map<Long, DefaultMessageFuture> futures = new HashMap<Long, DefaultMessageFuture>();
	private long nextMsgId = 0;
	
	private final Protocal protocal;
	
	public NettyClientHandler(Protocal protocal) {
		this.protocal = protocal;
	}
	
    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        if(msg instanceof DefaultMessageFuture) {
        	DefaultMessageFuture future = (DefaultMessageFuture)msg;
        	Message message  = future.getMessage();
        	message.setId(nextMsgId++);
        	if(message.isTwoWay()) {
        		futures.put(message.getId(), future);
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
