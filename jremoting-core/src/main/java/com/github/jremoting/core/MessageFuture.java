package com.github.jremoting.core;

import com.github.jremoting.util.concurrent.ListenableFuture;

public interface MessageFuture extends ListenableFuture<Object> {
	void setResult(Object obj);
}
