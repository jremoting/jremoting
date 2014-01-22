package com.github.jremoting.core;

import java.lang.reflect.Method;

import com.github.jremoting.exception.RpcException;


public class ServerInvoker extends FinalFilter {
	
	@Override
	protected Object doRpcInvoke(Invocation invocation) {

		Method targetMethod;
		try {
			
			Class<?>[] paramterTypes = null;
			if (invocation.getArgs() != null) {
				paramterTypes = new Class[invocation.getArgs().length];
				for (int i = 0; i < paramterTypes.length; i++) {
					paramterTypes[i] = invocation.getArgs()[i].getClass();
				}
			}
			
			targetMethod = findMethod(invocation.getTarget().getClass(), invocation.getMethodName(),paramterTypes);
			
		    if(targetMethod == null) {
		    	throw new RpcException("can not find method!");
		    }
		    
		    return targetMethod.invoke(invocation.getTarget(), invocation.getArgs());
		
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	private static Method findMethod(Class<?> clazz, String name, Class<?>[] paramTypes) {

		Class<?> searchType = clazz;
		while (searchType != null) {
			Method[] methods = (searchType.isInterface() ? searchType.getMethods() : searchType.getDeclaredMethods());
			for (Method method : methods) {
				if (name.equals(method.getName()) && isParameterTypeMatch(paramTypes, method.getParameterTypes())) {
					return method;
				}
			}
			searchType = searchType.getSuperclass();
		}
		
		return null;
	}
	private static boolean isParameterTypeMatch(Class<?>[] actual ,Class<?>[] expected) {
		if(actual == null && (expected == null|| expected.length == 0)) {
			return true;
		}
		
		if(actual != null && expected != null) {
			return true;
		}
		
		if(actual.length != expected.length) {
			return false;
		}
		for (int i=0;i<actual.length ;i++) {
			if(actual[i] != expected[i]) {
				return false;
			}
		}
		return true;
	}
}
