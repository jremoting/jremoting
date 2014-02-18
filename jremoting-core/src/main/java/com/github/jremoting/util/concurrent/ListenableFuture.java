package com.github.jremoting.util.concurrent;

import java.util.concurrent.Executor;
import java.util.concurrent.Future;

public interface ListenableFuture<V> extends Future<V> {
	 void addListener(FutureListener<V> listener, Executor executor);
	 void addListener(FutureListener<V> listener);
	 boolean isSuccess();
	 Throwable cause();
	 V result();
}
