package com.github.jremoting.dispatcher;

import java.util.List;

import com.github.jremoting.core.Invocation;
import com.github.jremoting.core.InvocationResult;
import com.github.jremoting.protocal.Protocals;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageCodec;

public class NettyServerCodec extends ByteToMessageCodec<InvocationResult> {
	
	private final Protocals protocals;
	
	public NettyServerCodec(Protocals protocals) {
		this.protocals = protocals;
	}

	@Override
	protected void encode(ChannelHandlerContext ctx, InvocationResult msg,
			ByteBuf out) throws Exception {
		msg.getInvocation().getProtocal().writeResponse(msg, new NettyChannelBuffer(out));
	}

	@Override
	protected void decode(ChannelHandlerContext ctx, ByteBuf in,
			List<Object> out) throws Exception {
		Invocation invocation = protocals.readRequest(new NettyChannelBuffer(in));
		if(invocation != null) {
			out.add(invocation);
		}
	}
}
