package com.github.jremoting.core.test;


import com.github.jremoting.core.test.TestService.HelloInput;
import com.github.jremoting.core.test.TestService.HelloOutput;
import com.github.jremoting.util.concurrent.ListenableFuture;

public interface AsyncTestService {
	public ListenableFuture<HelloOutput> $hello(HelloInput input, int id);
	public ListenableFuture<HelloOutput> $hello(HelloInput input, int id, Runnable callback);
	//public void $hello(HelloInput input, int id, Runnable callback);
}
