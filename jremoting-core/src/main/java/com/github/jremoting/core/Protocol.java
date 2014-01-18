package com.github.jremoting.core;

public interface Protocol {
	int getMagic();
	void writeRequest(ChannelBuffer buffer);
	Object readResponse(Channel channel , ChannelBuffer buffer);
}
