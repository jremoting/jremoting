package com.github.jremoting.core;



public class Invoke extends Message {
	private final String serviceVersion;
	private final String methodName;
	private final String serviceName;
	private final Object[] args;
	private final Class<?> returnType;
	private final Class<?>[] parameterTypes;

	private Object target;
	
	
	public Invoke(String serviceName, String serviceVersion,String methodName ,
			Object[] args, Class<?>[] parameterTypes,
			Class<?> returnType,
			Protocal protocal ,int serializerId) {
		super(true, protocal, serializerId);
		this.args = args;
		this.serviceName = serviceName;
		this.serviceVersion = serviceVersion;
		this.methodName = methodName;
		this.parameterTypes = parameterTypes;
		this.returnType = returnType;
	}
	

	public Object[] getArgs() {
		return args;
	}


	public String getServiceName() {
		return serviceName;
	}

	
	public String getServiceVersion() {
		return serviceVersion;
	}

	
	public String getMethodName() {
		return methodName;
	}

	
	public Class<?> getReturnType() {
		return returnType;
	}
	
	public String getServiceId() {
		return this.serviceName + ":" + this.serviceVersion;
	}

	public Object getTarget() {
		return target;
	}

	
	public void setTarget(Object target) {
		this.target = target;
	}

	public Class<?>[] getParameterTypes() {
		return parameterTypes;
	}
	
}
