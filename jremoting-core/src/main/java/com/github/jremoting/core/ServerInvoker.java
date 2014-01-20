package com.github.jremoting.core;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.concurrent.ConcurrentHashMap;

import com.github.jremoting.exception.RpcException;


public class ServerInvoker extends FinalFilter {
	
	private final ConcurrentHashMap<String, Object> providers = new ConcurrentHashMap<String, Object>();

	@Override
	protected Object doRpcInvoke(Invocation invocation) {
		String service = invocation.getService();
		Object provider = providers.get(service);
		
		if(provider == null) {
			throw new RpcException("can not find provider!");
		}
		
		Method targetMethod;
		try {
			Class<?>[] paramterTypes = new Class[invocation.getArgs().length];
		    targetMethod = provider.getClass().getMethod(invocation.getMethodName(), paramterTypes);
		
		} catch (Exception e) {
			throw new RpcException("can not find method!");
		}
		
		try {
			return targetMethod.invoke(provider, invocation.getArgs());
		} catch (IllegalArgumentException e) {
			throw new RpcException("servier error");
		} catch (IllegalAccessException e) {
			throw new RpcException("servier error");
		} catch (InvocationTargetException e) {
			throw new RpcException("servier error");
		}
	}
}
