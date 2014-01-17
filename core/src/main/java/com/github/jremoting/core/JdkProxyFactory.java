package com.github.jremoting.core;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

public class JdkProxyFactory implements ProxyFactory {

	@Override
	public Object getProxy(Class<?> interfaceType, final InvokePipeline pipeline) {
		
		InvocationHandler invocationHandler = new InvocationHandler() {
			@Override
			public Object invoke(Object proxy, Method method, Object[] args)
					throws Throwable {
				return pipeline.invoke(new DefaultInvocation(args));
			}
		};
		return Proxy.newProxyInstance(interfaceType.getClassLoader(), 
				new Class[]{interfaceType}, invocationHandler);
	}

}
