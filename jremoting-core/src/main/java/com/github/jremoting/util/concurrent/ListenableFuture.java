package com.github.jremoting.util.concurrent;

import java.util.concurrent.Executor;
import java.util.concurrent.Future;

public interface ListenableFuture<V> extends Future<V> {
	 void setListener(Runnable listener, Executor executor);
}
