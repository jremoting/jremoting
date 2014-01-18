package com.github.jremoting.core;

public interface Protocol {
	void writeRequest(ChannelBuffer buffer);
	Object readResponse(Channel channel , ChannelBuffer buffer);
}
