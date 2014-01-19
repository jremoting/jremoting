package com.github.jremoting.core;

public class DefaultClientInvocation extends AbstractInvocation {

	
	public DefaultClientInvocation(String serviceName, String serviceVersion,String methodName , Object[] args, 
			Class<?>[] parameterTypes, Class<?> returnType) {
		super(serviceName, serviceVersion, methodName, args, parameterTypes, returnType);
	}

	@Override
	public String getRemoteAddress() {
		return "127.0.0.1:20880";
	}

	@Override
	public long getInvocationId() {
		return 0;
	}
}
