package com.github.jremoting.core;

import java.util.concurrent.Future;

public interface MessageFuture extends Future<Object> {
	void addListener(MessageFutureListener listener);
	void removeListener(MessageFutureListener listener);
}
