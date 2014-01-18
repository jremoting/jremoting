package com.github.jremoting.core;

public interface ProxyFactory {
	
	Object getProxy(Class<?> interfaceType, String serviceVersion, InvokePipeline pipeline);
	
	
}
