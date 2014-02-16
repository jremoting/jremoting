package com.github.jremoting.core;

public interface MessageChannel {
	 MessageFuture send(Invoke msg);
	 void close();
}
