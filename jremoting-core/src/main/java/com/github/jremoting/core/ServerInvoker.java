package com.github.jremoting.core;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import com.github.jremoting.exception.RpcException;


public class ServerInvoker extends FinalFilter {
	
	@Override
	protected Object doRpcInvoke(Invocation invocation) {

		Method targetMethod;
		try {
			Class<?>[] paramterTypes = new Class[invocation.getArgs().length];
			for (int i = 0; i < paramterTypes.length; i++) {
				paramterTypes[i] = invocation.getArgs()[i].getClass();
			}
		    targetMethod = invocation.getTarget().getClass().getMethod(invocation.getMethodName(), paramterTypes);
		
		} catch (Exception e) {
			throw new RpcException("can not find method!");
		}
		
		try {
			return targetMethod.invoke(invocation.getTarget(), invocation.getArgs());
		} catch (IllegalArgumentException e) {
			throw new RpcException("servier error");
		} catch (IllegalAccessException e) {
			throw new RpcException("servier error");
		} catch (InvocationTargetException e) {
			throw new RpcException("servier error");
		}
	}
}
