package com.github.jremoting.core;

public interface MessageChannel {
	 void send(Invoke msg);
	 void close();
}
