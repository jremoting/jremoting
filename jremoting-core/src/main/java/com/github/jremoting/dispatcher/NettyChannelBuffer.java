package com.github.jremoting.dispatcher;

import java.nio.charset.Charset;

import com.github.jremoting.core.ChannelBuffer;

import io.netty.buffer.ByteBuf;

public class NettyChannelBuffer implements ChannelBuffer {
	private final ByteBuf nettyBuffer;
	public static final Charset UTF8 = Charset.forName("UTF-8"); 
	
	public NettyChannelBuffer(ByteBuf nettyBuffer) {
		this.nettyBuffer = nettyBuffer;
	}
	
	@Override
	public void writeLong(long value) {
		nettyBuffer.writeLong(value);
	}
	@Override
	public void writeShort(short value) {
		nettyBuffer.writeShort(value);
	}
	
	@Override
	public void writeInt(int value) {
		nettyBuffer.writeInt(value);
	}
	
	@Override
	public long readLong() {
		return nettyBuffer.readLong();
	}

	@Override
	public short readShort() {
		return nettyBuffer.readShort();
	}

	@Override
	public int readInt() {
		return nettyBuffer.readInt();
	}

	@Override
	public int compareTo(ChannelBuffer o) {
		NettyChannelBuffer channelBuffer = (NettyChannelBuffer)o;
		return nettyBuffer.compareTo(channelBuffer.nettyBuffer);
	}


	@Override
	public void markReaderIndex() {
		nettyBuffer.markReaderIndex();
	}


	@Override
	public boolean readable() {
		return nettyBuffer.isReadable();
	}

	@Override
	public int readableBytes() {
		return nettyBuffer.readableBytes();
	}

	@Override
	public byte readByte() {
		return nettyBuffer.readByte();
	}

	@Override
	public void readBytes(byte[] dst) {
		nettyBuffer.readBytes(dst);
	}

	@Override
	public void readBytes(byte[] dst, int dstIndex, int length) {
		nettyBuffer.readBytes(dst, dstIndex, length);
	}



	@Override
	public void resetReaderIndex() {
		nettyBuffer.resetReaderIndex();
		
	}


	@Override
	public int readerIndex() {
		return nettyBuffer.readerIndex();
	}

	@Override
	public void readerIndex(int readerIndex) {
		nettyBuffer.readerIndex(readerIndex);
	}


	@Override
	public void skipBytes(int length) {
		nettyBuffer.skipBytes(length);
		
	}


	@Override
	public void writeByte(int value) {
		nettyBuffer.writeByte(value);
		
	}

	@Override
	public void writeBytes(byte[] src) {
		nettyBuffer.writeBytes(src);
		
	}

	@Override
	public void writeBytes(byte[] src, int index, int length) {
		nettyBuffer.writeBytes(src, index, length);
	}


	@Override
	public int writerIndex() {
		return nettyBuffer.writerIndex();
	}

	@Override
	public void writerIndex(int writerIndex) {
		nettyBuffer.writerIndex(writerIndex);
		
	}

	@Override
	public ChannelBuffer slice(int index, int length) {
		 return new NettyChannelBuffer(nettyBuffer.slice(index, length));
	}

	@Override
	public void writeUTF8(String value) {
		byte[] data = value.getBytes(UTF8);
		nettyBuffer.writeInt(value.length());
		nettyBuffer.writeBytes(data);
	}

	@Override
	public String readUTF8() {
		int length = nettyBuffer.readInt();
		byte[] data = new byte[length];
		nettyBuffer.readBytes(data);
		return new String(data, UTF8);
	}
}
