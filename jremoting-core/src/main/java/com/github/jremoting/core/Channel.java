package com.github.jremoting.core;

public interface Channel {
	RpcFuture write(Invocation invocation);
}
