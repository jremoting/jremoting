package com.github.jremoting.core;

public interface InvokeFilter {
	Object invoke(Invocation invocation, InvokeChain chain);
}
