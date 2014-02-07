package com.github.jremoting.remoting;

import java.util.List;

import com.github.jremoting.core.ErrorMessage;
import com.github.jremoting.core.Message;
import com.github.jremoting.core.Protocal;
import com.github.jremoting.exception.ProtocalException;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageCodec;


public class NettyMessageCodec extends  ByteToMessageCodec<Message>{
	
	private final Protocal  protocal;

	public NettyMessageCodec(Protocal  protocal) {
		this.protocal = protocal;
	}

	@Override
	protected void encode(ChannelHandlerContext ctx, Message msg, ByteBuf out)
			throws Exception {
		msg.getProtocal().encode(msg, new DefaultByteBuffer(out));
	}

	@Override
	protected void decode(ChannelHandlerContext ctx, ByteBuf in,
			List<Object> out) throws Exception {
	
		Message msg = protocal.decode(new DefaultByteBuffer(in));
		if(msg == ErrorMessage.NEED_MORE_INPUT_MESSAGE) {
			return;
		}
		
		if (msg != null) {
			out.add(msg);
			return;
		}
	
		throw new ProtocalException("unknown  msg", null, null);
	}

}
