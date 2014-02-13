package com.github.jremoting.core;



public class Invoke extends Message {
	private final String version;
	private final String methodName;
	private final String interfaceName;
	private final Object[] args;
	private final Class<?> returnType;
	private final Class<?>[] parameterTypes;

	private Object target;
	private ServiceRegistry registry;
	
	
	public Invoke(String interfaceName, String version,String methodName ,
			Object[] args, Class<?>[] parameterTypes,
			Class<?> returnType, Serializer serializer) {
		super(true, serializer);
		this.args = args;
		this.interfaceName = interfaceName;
		this.version = version;
		this.methodName = methodName;
		this.parameterTypes = parameterTypes;
		this.returnType = returnType;
	}
	
	public Object[] getArgs() {
		return args;
	}


	public String getInterfaceName() {
		return interfaceName;
	}

	
	public String getVersion() {
		return version;
	}

	
	public String getMethodName() {
		return methodName;
	}

	
	public Class<?> getReturnType() {
		return returnType;
	}
	
	public String getServiceName() {
		return this.interfaceName + ":" + this.version;
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


	public ServiceRegistry getRegistry() {
		return registry;
	}


	public void setRegistry(ServiceRegistry registry) {
		this.registry = registry;
	}
	
}
