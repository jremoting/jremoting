package com.github.jremoting.core;

import java.util.concurrent.Executor;
import java.util.concurrent.Future;

public interface MessageFuture extends Future<Object> {
	void addListener(Runnable listener, Executor executor);
}
