package com.github.jremoting.core.test;


import java.util.concurrent.Future;

import com.github.jremoting.core.test.TestService.HelloInput;
import com.github.jremoting.core.test.TestService.HelloOutput;
import com.github.jremoting.util.concurrent.ListenableFuture;
/**
 * client async interface
 * @author hanjie
 *
 */
public interface AsyncTestService {
	public ListenableFuture<HelloOutput> $hello(HelloInput input, int id);
	public Future<HelloOutput> $hello(HelloInput input, int id, Runnable callback);
}
