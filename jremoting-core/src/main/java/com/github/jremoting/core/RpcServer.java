package com.github.jremoting.core;

public interface RpcServer {
	 void start();
	 void close();
	 void register(ServiceProvider provider);
}
