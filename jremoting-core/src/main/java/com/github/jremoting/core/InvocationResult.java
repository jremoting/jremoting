package com.github.jremoting.core;

public class InvocationResult {

	private final Object result;
	private final Invocation invocation;

	public InvocationResult(Object result, Invocation invocation) {
		this.result = result;
		this.invocation = invocation;
	}

	public Object getResult() {
		return result;
	}
	
	public   Invocation getInvocation() {
		return invocation;
	}

}
