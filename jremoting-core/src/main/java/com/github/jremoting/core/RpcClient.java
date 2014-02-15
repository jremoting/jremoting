package com.github.jremoting.core;

public interface RpcClient {
	Object invoke(Invoke invoke);
	void register(ServiceParticipantInfo consumerInfo);
	void close();
	void start();
}
