package com.github.jremoting.core;

public interface ProxyFactory {
	
	Object getProxy(Class<?> interfaceType, InvokePipeline pipeline);
}
