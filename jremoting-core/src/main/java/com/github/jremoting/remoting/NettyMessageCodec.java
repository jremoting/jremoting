package com.github.jremoting.remoting;

import java.util.List;

import com.github.jremoting.core.ErrorMessage;
import com.github.jremoting.core.Message;
import com.github.jremoting.core.Protocal;

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
		try {
			protocal.encode(msg, new DefaultByteBuffer(out));
		} catch (Exception e) {
			ctx.fireExceptionCaught(e);
		}
	}

	@Override
	protected void decode(ChannelHandlerContext ctx, ByteBuf in,
			List<Object> out) throws Exception {
	
		try {
			Message msg = protocal.decode(new DefaultByteBuffer(in));
			if(msg == ErrorMessage.NEED_MORE_INPUT_MESSAGE) {
				return;
			}
			
			if (msg != null) {
				out.add(msg);
				return;
			}
		} catch (Exception e) {
			ctx.fireExceptionCaught(e);
		}

	}

}
