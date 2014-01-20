package com.github.jremoting.core;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

public class ClientInvoker implements InvocationHandler {

	private final InvokePipeline pipeline;
	private final Protocal protocal;
	private final Serializer serializer;
	private final String serviceName;
	private final String serviceVersion;
	
	public ClientInvoker(InvokePipeline pipeline , Protocal protocal
			, Serializer serializer,String serviceName, String serviceVersion) {
		this.pipeline = pipeline;
		this.protocal = protocal;
		this.serializer = serializer;
		this.serviceVersion =serviceVersion;
		this.serviceName = serviceName;
	}
	@Override
	public Object invoke(Object proxy, Method method, Object[] args)
			throws Throwable {
		DefaultInvocation invocation = new DefaultInvocation(serviceName, 
				serviceVersion, method.getName(),
				args, method.getReturnType(), protocal, serializer.getId());
		return pipeline.invoke(invocation);
	}
}
