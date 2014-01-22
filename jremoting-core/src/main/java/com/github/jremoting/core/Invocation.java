package com.github.jremoting.core;

public interface Invocation {
	Object[] getArgs();
	Class<?> getReturnType();
	Class<?>[] getParameterTypes();
	String getServiceName();
	String getServiceVersion();
	String getService();
	String getMethodName();
	String getRemoteAddress();
	void setRemoteAddress(String address);
	long getInvocationId();
	void setInvocationId(long id);
	Protocal getProtocal();
	int getSerializerId();
	Object getTarget();
	void setTarget(Object target);
	long getTimeout();
	
}
