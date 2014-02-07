package com.github.jremoting.core;

public interface RpcClient {
	 MessageFuture send(Message msg);
}
