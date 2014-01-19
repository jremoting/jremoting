package com.github.jremoting.dispatcher;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;

import com.github.jremoting.core.ChannelBuffer;

import io.netty.buffer.ByteBuf;

public class JRemotingChannelBuffer implements ChannelBuffer {
	private final ByteBuf nettyBuffer;
	
	public JRemotingChannelBuffer(ByteBuf nettyBuffer) {
		this.nettyBuffer = nettyBuffer;
	}

	@Override
	public int compareTo(ChannelBuffer o) {
		JRemotingChannelBuffer channelBuffer = (JRemotingChannelBuffer)o;
		return nettyBuffer.compareTo(channelBuffer.nettyBuffer);
	}

	@Override
	public int capacity() {
		return nettyBuffer.capacity();
	}

	@Override
	public void clear() {
		nettyBuffer.clear();
	}

	@Override
	public ChannelBuffer copy() {
		return new JRemotingChannelBuffer(nettyBuffer.copy());
	}

	@Override
	public ChannelBuffer copy(int index, int length) {
		return new JRemotingChannelBuffer(nettyBuffer.copy(index, length));
	}

	@Override
	public void discardReadBytes() {
		nettyBuffer.discardReadBytes();
	}

	@Override
	public void ensureWritableBytes(int writableBytes) {
		nettyBuffer.ensureWritable(writableBytes);
	}

	@Override
	public byte getByte(int index) {
		return nettyBuffer.getByte(index);
	}

	@Override
	public void getBytes(int index, byte[] dst) {
		nettyBuffer.getBytes(index, dst);
		
	}

	@Override
	public void getBytes(int index, byte[] dst, int dstIndex, int length) {
		nettyBuffer.getBytes(index, dst, dstIndex, length);
	}

	@Override
	public void getBytes(int index, ByteBuffer dst) {
		nettyBuffer.getBytes(index, dst);
	}

	@Override
	public void getBytes(int index, ChannelBuffer dst) {
		JRemotingChannelBuffer channelBuffer = (JRemotingChannelBuffer)dst;
		nettyBuffer.getBytes(index, channelBuffer.nettyBuffer);
	}

	@Override
	public void getBytes(int index, ChannelBuffer dst, int length) {
		JRemotingChannelBuffer channelBuffer = (JRemotingChannelBuffer)dst;
		nettyBuffer.getBytes(index, channelBuffer.nettyBuffer, length);
	}

	@Override
	public void getBytes(int index, ChannelBuffer dst, int dstIndex, int length) {
		JRemotingChannelBuffer channelBuffer = (JRemotingChannelBuffer)dst;
		nettyBuffer.getBytes(index, channelBuffer.nettyBuffer, dstIndex, length);
		
	}

	@Override
	public void getBytes(int index, OutputStream dst, int length)
			throws IOException {
		nettyBuffer.getBytes(index, dst, length);
	}

	@Override
	public boolean isDirect() {
		return nettyBuffer.isDirect();
	}

	@Override
	public void markReaderIndex() {
		nettyBuffer.markReaderIndex();
	}

	@Override
	public void markWriterIndex() {
		nettyBuffer.markWriterIndex();
		
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
	public void readBytes(ByteBuffer dst) {
		nettyBuffer.readBytes(dst);
	}

	@Override
	public void readBytes(ChannelBuffer dst) {
		JRemotingChannelBuffer channelBuffer = (JRemotingChannelBuffer)dst;
		nettyBuffer.readBytes(channelBuffer.nettyBuffer);
	}

	@Override
	public void readBytes(ChannelBuffer dst, int length) {
		JRemotingChannelBuffer channelBuffer = (JRemotingChannelBuffer)dst;
		nettyBuffer.readBytes(channelBuffer.nettyBuffer, length);
	}

	@Override
	public void readBytes(ChannelBuffer dst, int dstIndex, int length) {
		JRemotingChannelBuffer channelBuffer = (JRemotingChannelBuffer)dst;
		nettyBuffer.readBytes(channelBuffer.nettyBuffer,dstIndex, length);
	}

	@Override
	public ChannelBuffer readBytes(int length) {
		return new JRemotingChannelBuffer(nettyBuffer.readBytes(length));
	}

	@Override
	public void resetReaderIndex() {
		nettyBuffer.resetReaderIndex();
		
	}

	@Override
	public void resetWriterIndex() {
		nettyBuffer.resetWriterIndex();
		
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
	public void readBytes(OutputStream dst, int length) throws IOException {
		nettyBuffer.readBytes(dst,length);
	}

	@Override
	public void setByte(int index, int value) {
		nettyBuffer.setByte(index, value);
		
	}

	@Override
	public void setBytes(int index, byte[] src) {
		nettyBuffer.setBytes(index, src);
		
	}

	@Override
	public void setBytes(int index, byte[] src, int srcIndex, int length) {
		nettyBuffer.setBytes(index, src, srcIndex, length);
	}

	@Override
	public void setBytes(int index, ByteBuffer src) {
		nettyBuffer.setBytes(index, src);
		
	}

	@Override
	public void setBytes(int index, ChannelBuffer src) {
		JRemotingChannelBuffer channelBuffer = (JRemotingChannelBuffer)src;
		nettyBuffer.setBytes(index, channelBuffer.nettyBuffer);
	}

	@Override
	public void setBytes(int index, ChannelBuffer src, int length) {
		JRemotingChannelBuffer channelBuffer = (JRemotingChannelBuffer)src;
		nettyBuffer.setBytes(index, channelBuffer.nettyBuffer, length);
		
	}

	@Override
	public void setBytes(int index, ChannelBuffer src, int srcIndex, int length) {
		JRemotingChannelBuffer channelBuffer = (JRemotingChannelBuffer)src;
		nettyBuffer.setBytes(index, channelBuffer.nettyBuffer, srcIndex, length);
		
	}

	@Override
	public int setBytes(int index, InputStream src, int length)
			throws IOException {
		return nettyBuffer.setBytes(index, src, length);
	}

	@Override
	public void setIndex(int readerIndex, int writerIndex) {
		nettyBuffer.setIndex(readerIndex, writerIndex);
		
	}

	@Override
	public void skipBytes(int length) {
		nettyBuffer.skipBytes(length);
		
	}

	@Override
	public ByteBuffer toByteBuffer() {

		return nettyBuffer.nioBuffer();
	}

	@Override
	public ByteBuffer toByteBuffer(int index, int length) {
		return nettyBuffer.nioBuffer(index, length);
	}

	@Override
	public boolean writable() {
		return nettyBuffer.isWritable();
	}

	@Override
	public int writableBytes() {
		return nettyBuffer.writableBytes();
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
	public void writeBytes(ByteBuffer src) {
		nettyBuffer.writeBytes(src);
		
	}

	@Override
	public void writeBytes(ChannelBuffer src) {
		JRemotingChannelBuffer channelBuffer = (JRemotingChannelBuffer)src;
		nettyBuffer.writeBytes(channelBuffer.nettyBuffer);
		
	}

	@Override
	public void writeBytes(ChannelBuffer src, int length) {
		JRemotingChannelBuffer channelBuffer = (JRemotingChannelBuffer)src;
		nettyBuffer.writeBytes(channelBuffer.nettyBuffer,length);
	}

	@Override
	public void writeBytes(ChannelBuffer src, int srcIndex, int length) {
		JRemotingChannelBuffer channelBuffer = (JRemotingChannelBuffer)src;
		nettyBuffer.writeBytes(channelBuffer.nettyBuffer,srcIndex, length);
		
	}

	@Override
	public int writeBytes(InputStream src, int length) throws IOException {
		return nettyBuffer.writeBytes(src, length);
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
	public byte[] array() {
		return nettyBuffer.array();
	}

	@Override
	public boolean hasArray() {
		return nettyBuffer.hasArray();
	}

	@Override
	public int arrayOffset() {
		return nettyBuffer.arrayOffset();
	}


}
