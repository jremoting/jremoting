package com.github.jremoting.core;

public abstract class RpcInvoker implements InvokeFilter  {

	@Override
	public Object invoke(Invocation invocation, InvokeChain chain) {
		return doRpcInvoke(invocation);
	}
	
	protected abstract Object doRpcInvoke(Invocation invocation);

}
