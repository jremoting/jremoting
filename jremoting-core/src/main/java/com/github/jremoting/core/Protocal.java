package com.github.jremoting.core;

import com.github.jremoting.exception.ProtocalException;
import com.github.jremoting.io.ByteBuffer;

public interface Protocal {
	void encode(Message msg, ByteBuffer buffer) throws ProtocalException;
	Message decode(ByteBuffer buffer) throws ProtocalException;
	ServiceRegistry getRegistry();
}
