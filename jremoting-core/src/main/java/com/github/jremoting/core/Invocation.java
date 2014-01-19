package com.github.jremoting.core;

public interface Invocation {
	Object[] getArgs();
	Class<?>[] getParameterTypes();
	Class<?> getReturnType();
	String getServiceName();
	String getServiceVersion();
	String getMethodName();
	String getAddress();
	long getInvocationId();
	
}
