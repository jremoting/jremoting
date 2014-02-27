package com.github.jremoting.example;


import java.util.concurrent.Future;

import com.github.jremoting.example.TestService.HelloInput;
import com.github.jremoting.example.TestService.HelloOutput;
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
