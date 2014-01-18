package com.github.jremoting.dispatcher;

import io.netty.buffer.ByteBuf;

public class JRemotingChannelBuffer {
	private final ByteBuf nettyBuffer;
	
	public JRemotingChannelBuffer(ByteBuf nettyBuffer) {
		this.nettyBuffer = nettyBuffer;
	}
	
	public void writeBytes(byte[] value) {
		nettyBuffer.writeBytes(value);
	}
}
