package com.github.jremoting.dispatcher;

import java.util.List;

import com.github.jremoting.core.Invocation;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageCodec;

public class NettyCodec extends ByteToMessageCodec<Invocation> {

	@Override
	protected void encode(ChannelHandlerContext ctx, Invocation msg, ByteBuf out)
			throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void decode(ChannelHandlerContext ctx, ByteBuf in,
			List<Object> out) throws Exception {
		// TODO Auto-generated method stub
		
	}

}
