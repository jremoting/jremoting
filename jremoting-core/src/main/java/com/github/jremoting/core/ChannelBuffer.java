package com.github.jremoting.core;


/**
 * netty buffer sub interface
 * @author hanjie
 *
 */
public interface ChannelBuffer extends Comparable<ChannelBuffer> {
	
	/**
	 * StringLength(byte) + String
	 * @param value
	 */
	void writeUTF8(String value);
	/**
	 * StringLength(byte) + String
	 * @param value
	 */
	String readUTF8();

	void writeLong(long value);

	void writeShort(short value);

	void writeInt(int value);

	long readLong();

	short readShort();

	int readInt();

	void markReaderIndex();

	boolean readable();

	int readableBytes();

	byte readByte();

	void readBytes(byte[] dst);

	void readBytes(byte[] dst, int dstIndex, int length);

	void resetReaderIndex();

	int readerIndex();

	void readerIndex(int readerIndex);

	void skipBytes(int length);

	void writeByte(int value);

	void writeBytes(byte[] src);

	void writeBytes(byte[] src, int index, int length);

	int writerIndex();

	void writerIndex(int writerIndex);
	
	ChannelBuffer slice(int index, int length);
	
	byte[] array();

}
