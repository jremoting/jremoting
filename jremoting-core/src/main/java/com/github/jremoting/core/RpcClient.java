package com.github.jremoting.core;

public interface RpcClient {
	Object invoke(Invoke invoke);
	void register(ServiceConsumer consumer);
	void close();
	void start();
}
