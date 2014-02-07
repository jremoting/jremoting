package com.github.jremoting.invoke;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

import com.github.jremoting.core.Invoke;
import com.github.jremoting.core.Protocal;
import com.github.jremoting.core.RpcClient;
import com.github.jremoting.core.Serializer;

public class ClientInvocationHandler implements InvocationHandler {

	private final RpcClient rpcClient;
	private final Protocal protocal;
	private final Serializer serializer;
	private final String serviceName;
	private final String serviceVersion;
	private final String remoteAddress;
	
	
	
	public ClientInvocationHandler(RpcClient rpcClient, 
			Protocal protocal, 
			Serializer serializer,
			String serviceName, 
			String serviceVersion,
			String remoteAddress) {
		
		this.rpcClient = rpcClient;
		this.protocal = protocal;
		this.serializer = serializer;
		this.serviceVersion =serviceVersion;
		this.serviceName = serviceName;
		this.remoteAddress = remoteAddress;
	}
	@Override
	public Object invoke(Object proxy, Method method, Object[] args)
			throws Throwable {
		
		Invoke invoke = new Invoke(serviceName, 
				serviceVersion,
				method.getName(),
				args,
				method.getParameterTypes() ,
				method.getReturnType(), 
				protocal, 
				serializer);
		
		if(remoteAddress != null) {
			invoke.setRemoteAddress(remoteAddress);
		}
		return rpcClient.invoke(invoke);
	}
}
