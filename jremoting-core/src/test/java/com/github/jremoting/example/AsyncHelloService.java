package com.github.jremoting.example;

import com.github.jremoting.util.concurrent.ListenableFuture;

public interface AsyncHelloService {
	/**
	 * 异步方法的签名规则是同步方法名加前缀 $, 返回结果为ListenableFuture
	 */
	ListenableFuture<String> $hello(String name);
}
