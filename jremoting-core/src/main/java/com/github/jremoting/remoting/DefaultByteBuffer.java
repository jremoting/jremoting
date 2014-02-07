package com.github.jremoting.remoting;

import java.nio.charset.Charset;

import com.github.jremoting.io.ByteBuffer;

import io.netty.buffer.ByteBuf;

public class DefaultByteBuffer implements ByteBuffer {
	private final ByteBuf nettyBuffer;
	public static final Charset UTF8 = Charset.forName("UTF-8"); 
	
	public DefaultByteBuffer(ByteBuf nettyBuffer) {
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
	public int compareTo(ByteBuffer o) {
		DefaultByteBuffer channelBuffer = (DefaultByteBuffer)o;
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
	public ByteBuffer slice(int index, int length) {
		 return new DefaultByteBuffer(nettyBuffer.slice(index, length));
	}

	@Override
	public byte[] array() {
		return nettyBuffer.array();
	}
}
