package com.github.jremoting.core.test;

import java.util.concurrent.Future;

import com.github.jremoting.core.test.TestService.HelloInput;
import com.github.jremoting.core.test.TestService.HelloOutput;

public interface AsyncTestService {
	public Future<HelloOutput> $hello(HelloInput input, int id);
	public Future<HelloOutput> $hello(HelloInput input, int id, Runnable callback);
	//public void $hello(HelloInput input, int id, Runnable callback);
}
