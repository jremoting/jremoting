package com.github.jremoting.core;

public abstract class FinalFilter implements InvokeFilter  {

	@Override
	public final Object invoke(Invocation invocation, InvokeChain chain) {
		return doRpcInvoke(invocation);
	}
	
	protected abstract Object doRpcInvoke(Invocation invocation);

}
