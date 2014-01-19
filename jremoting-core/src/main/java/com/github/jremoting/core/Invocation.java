package com.github.jremoting.core;

public interface Invocation {
	Object[] getArgs();
	Class<?> getReturnType();
	String getServiceName();
	String getServiceVersion();
	String getMethodName();
	String getRemoteAddress();
	long getInvocationId();
	String getProtocalName();
	String getSerializeName();
	
}
