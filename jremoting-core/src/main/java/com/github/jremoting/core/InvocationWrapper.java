package com.github.jremoting.core;

public abstract class InvocationWrapper implements Invocation {
	
	private final Invocation targetInvocation;
	
	public InvocationWrapper(Invocation targetInvocation) {
		this.targetInvocation = targetInvocation;
	}

	@Override
	public Object[] getArgs() {
		return targetInvocation.getArgs();
	}

	@Override
	public Class<?>[] getParameterTypes() {
		return targetInvocation.getParameterTypes();
	}

	@Override
	public Class<?> getReturnType() {
		return targetInvocation.getReturnType();
	}

	@Override
	public String getServiceName() {
		return targetInvocation.getServiceName();
	}

	@Override
	public String getServiceVersion() {
		return targetInvocation.getServiceVersion();
	}

	@Override
	public String getMethodName() {
		return targetInvocation.getMethodName();
	}

	@Override
	public String getRemoteAddress() {
		return targetInvocation.getRemoteAddress();
	}

	@Override
	public long getInvocationId() {
		return targetInvocation.getInvocationId();
	}

}
