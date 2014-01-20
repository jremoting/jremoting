package com.github.jremoting.core;

public class InvocationResult {

	private final long invocationId;
	private final Object result;
	private final Invocation invocation;

	public InvocationResult(Object result, Invocation invocation) {
		this.invocationId = invocation.getInvocationId();
		this.result = result;
		this.invocation = invocation;
	}
	public InvocationResult(long invocationId, Object result) {
		this.invocationId = invocationId;
		this.result = result;
		this.invocation = null;
	}

	public Object getResult() {
		return result;
	}

	public long getInvocationId() {
		return invocationId;
	}
	
	public   Invocation getInvocation() {
		return invocation;
	}

}
