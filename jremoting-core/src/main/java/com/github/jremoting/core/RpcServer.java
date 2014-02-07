package com.github.jremoting.core;

public interface RpcServer {
	 void start();
	 void stop();
	 void register(ServiceProvider provider);
}
