package com.github.jremoting.core;

public abstract class AbstractInvocation implements Invocation {
	private final String serviceVersion;
	private final String methodName;
	private final String serviceName;
	private final Object[] args;
	private final Class<?> returnType;
	private final long invacationId;
	
	public AbstractInvocation(String serviceName, String serviceVersion,String methodName ,
			Object[] args, Class<?> returnType, long invocationId) {
		this.args = args;

		this.serviceName = serviceName;
		this.serviceVersion = serviceVersion;
		this.methodName = methodName;
		this.returnType = returnType;
		this.invacationId = invocationId;
	}
	
	@Override
	public Object[] getArgs() {
		return args;
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
	
	@Override
	public String getRemoteAddress() {
		return null;
	}

	@Override
	public long getInvocationId() {
		return invacationId;
	}

	@Override
	public String getProtocalName() {
		return null;
	}

	@Override
	public String getSerializeName() {
		return null;
	}
}
