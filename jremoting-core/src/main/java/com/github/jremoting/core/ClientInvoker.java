package com.github.jremoting.core;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

public class ClientInvoker implements InvocationHandler {

	private final InvokePipeline pipeline;
	private final Protocal protocal;
	private final Serializer serializer;
	private final String serviceName;
	private final String serviceVersion;
	private final String remoteAddress;
	
	public ClientInvoker(InvokePipeline pipeline , Protocal protocal
			, Serializer serializer,String serviceName, 
			String serviceVersion,String remoteAddress) {
		this.pipeline = pipeline;
		this.protocal = protocal;
		this.serializer = serializer;
		this.serviceVersion =serviceVersion;
		this.serviceName = serviceName;
		this.remoteAddress = remoteAddress;
	}
	@Override
	public Object invoke(Object proxy, Method method, Object[] args)
			throws Throwable {
		DefaultInvocation invocation = new DefaultInvocation(serviceName, 
				serviceVersion, method.getName(),
				args, method.getReturnType(), protocal, serializer.getId());
		if(remoteAddress != null) {
			invocation.setRemoteAddress(remoteAddress);
		}
		return pipeline.invoke(invocation);
	}
}
