package com.github.jremoting.core;

public interface MessageChannel {
	 MessageFuture send(Message msg);
	 void close();
}
