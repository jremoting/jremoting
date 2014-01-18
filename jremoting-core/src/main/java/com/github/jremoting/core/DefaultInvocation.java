package com.github.jremoting.core;

public class DefaultInvocation implements Invocation {

	private final String serviceVersion;
	private final String methodName;
	private final String serviceName;
	private final Object[] args;
	private final Class<?>[] parameterTypes;
	private final Class<?> returnType;
	
	
	public DefaultInvocation(String serviceName, String serviceVersion,String methodName , Object[] args, 
			Class<?>[] parameterTypes, Class<?> returnType) {
		this.args = args;
		this.parameterTypes = parameterTypes;
		this.serviceName = serviceName;
		this.serviceVersion = serviceVersion;
		this.methodName = methodName;
		this.returnType = returnType;
	}

	@Override
	public Object[] getArgs() {
		return args;
	}

	@Override
	public Class<?>[] getParameterTypes() {
		return parameterTypes;
	}

	@Override
	public String getServiceName() {
		return serviceName;
	}

	@Override
	public String getServiceVersion() {
		return serviceVersion;
	}

	@Override
	public String getMethodName() {
		return methodName;
	}

	@Override
	public Class<?> getReturnType() {
		return returnType;
	}
}
