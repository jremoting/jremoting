package com.github.jremoting.dispatcher;

import java.util.List;

import com.github.jremoting.core.Invocation;
import com.github.jremoting.core.InvocationHolder;
import com.github.jremoting.core.InvocationResult;
import com.github.jremoting.core.Protocal;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageCodec;

public class NettyClientCodec extends ByteToMessageCodec<Invocation>{
	
	private final Protocal protocal;
	private final InvocationHolder invocationHolder;
	
	public NettyClientCodec(Protocal protocal, InvocationHolder invocationHolder) {
		this.protocal = protocal;
		this.invocationHolder = invocationHolder;
	}

	@Override
	protected void encode(ChannelHandlerContext ctx, Invocation msg, ByteBuf out)
			throws Exception {
		protocal.writeRequest(msg, new NettyChannelBuffer(out));
	}

	@Override
	protected void decode(ChannelHandlerContext ctx, ByteBuf in,
			List<Object> out) throws Exception {
		InvocationResult result = protocal.readResponse(invocationHolder,new NettyChannelBuffer(in));
		if (result != null) {
			out.add(result);
		}
	}

}
