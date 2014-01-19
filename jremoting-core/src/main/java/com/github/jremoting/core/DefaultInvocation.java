package com.github.jremoting.core;

public class DefaultInvocation extends AbstractInvocation {

	
	
	public DefaultInvocation(String serviceName, String serviceVersion,String methodName ,
			Object[] args, Class<?> returnType) {
		this(serviceName, serviceVersion, methodName, args, returnType, 0);
	}
	
	public DefaultInvocation(String serviceName, String serviceVersion,String methodName ,
			Object[] args, Class<?> returnType, long invocationId) {
		super(serviceName, serviceVersion, methodName, args, returnType, invocationId);
	}

	@Override
	public String getRemoteAddress() {
		return "127.0.0.1:20880";
	}

	@Override
	public long getInvocationId() {
		return super.getInvocationId();
	}

	@Override
	public String getProtocalName() {
		return "jremoting";
	}

	@Override
	public String getSerializeName() {
		return "fastjson";
	}
}
