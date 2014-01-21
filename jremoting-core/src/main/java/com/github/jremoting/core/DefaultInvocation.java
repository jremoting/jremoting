package com.github.jremoting.core;

public  class DefaultInvocation implements Invocation {
	private final String serviceVersion;
	private final String methodName;
	private final String serviceName;
	private final Object[] args;
	private final Class<?> returnType;
	
	private final Protocal protocal;
	private final int serializerId;
	
	private String remoteAddress = "127.0.0.1:6688";
	private  long invacationId;
	private Object target;
	
	
	public DefaultInvocation(String serviceName, String serviceVersion,String methodName ,
			Object[] args, Class<?> returnType, Protocal protocal ,int serializerId) {
		this.args = args;

		this.serviceName = serviceName;
		this.serviceVersion = serviceVersion;
		this.methodName = methodName;
		this.returnType = returnType;
		this.protocal = protocal;
		this.serializerId = serializerId;
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
		return remoteAddress;
	}
	
	@Override
	public void setRemoteAddress(String address) {
		this.remoteAddress = address;
	}

	@Override
	public long getInvocationId() {
		return invacationId;
	}
	
	@Override
	public void setInvocationId(long id) {
		this.invacationId = id;
	}

	@Override
	public Protocal getProtocal() {
		return protocal;
	}

	@Override
	public int getSerializerId() {
		return serializerId;
	}

	@Override
	public String getService() {
		return this.serviceName + ":" + this.serviceVersion;
	}

	@Override
	public Object getTarget() {
		return target;
	}

	@Override
	public void setTarget(Object target) {
		this.target = target;
	}

	@Override
	public long getTimeout() {
		// TODO Auto-generated method stub
		return 0;
	}
}
