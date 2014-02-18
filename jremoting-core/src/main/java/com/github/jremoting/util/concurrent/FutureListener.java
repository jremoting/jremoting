package com.github.jremoting.util.concurrent;

public interface FutureListener<V> {
	 void operationComplete(ListenableFuture<V> future);
}