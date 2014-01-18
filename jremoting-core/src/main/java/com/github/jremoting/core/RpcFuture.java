package com.github.jremoting.core;

import java.util.concurrent.Future;

public interface RpcFuture extends Future<Object> {
	void addListener(RpcFutureListener listener);
	void removeListener(RpcFutureListener listener);
}
