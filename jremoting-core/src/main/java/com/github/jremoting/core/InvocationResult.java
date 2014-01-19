package com.github.jremoting.core;

public class InvocationResult {

	private final long invocationId;
	private final Object result;

	public InvocationResult(long invocationId, Object result) {
		this.invocationId = invocationId;
		this.result = result;
	}

	public Object getResult() {
		return result;
	}

	public long getInvocationId() {
		return invocationId;
	}

}
