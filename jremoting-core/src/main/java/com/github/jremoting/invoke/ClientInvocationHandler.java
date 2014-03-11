package com.github.jremoting.invoke;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

import com.github.jremoting.core.ServiceConsumer;


public class ClientInvocationHandler implements InvocationHandler {


	private final ServiceConsumer consumer;
	
	public ClientInvocationHandler(ServiceConsumer consumer) {
		this.consumer = consumer;
	}
	@Override
	public Object invoke(Object proxy, Method method, Object[] args)
			throws Throwable {

		String[] parameterTypeNames = new String[method.getParameterTypes().length]; 
		for (int i = 0; i < parameterTypeNames.length; i++) {
			parameterTypeNames[i] = method.getParameterTypes()[i].getName();
		}
	
		if(method.getName().startsWith("$")) {
			return this.consumer.$invoke(method.getName().replace("$", ""),parameterTypeNames, args);
		}
		else {
			return this.consumer.invoke(method.getName(), parameterTypeNames, args);
		}
	}
}
