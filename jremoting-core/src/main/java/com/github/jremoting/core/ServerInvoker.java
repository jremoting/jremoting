package com.github.jremoting.core;

import java.lang.reflect.Method;

import com.github.jremoting.exception.RpcException;
import com.github.jremoting.util.ReflectionUtil;


public class ServerInvoker extends FinalFilter {
	
	@Override
	protected Object doRpcInvoke(Invocation invocation) {

		try {
			
			Method targetMethod = ReflectionUtil.findMethod(invocation.getTarget().getClass(), 
					invocation.getMethodName(),
					invocation.getParameterTypes());
			
		    if(targetMethod == null) {
		    	throw new RpcException("can not find method!");
		    }
		    
		    return targetMethod.invoke(invocation.getTarget(), invocation.getArgs());
		
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	
}
