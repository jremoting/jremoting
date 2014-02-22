package com.github.jremoting.invoke;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.concurrent.Executor;

import com.github.jremoting.core.ServiceConsumer;
import com.github.jremoting.util.concurrent.FutureListener;
import com.github.jremoting.util.concurrent.ListenableFuture;

public class ClientInvocationHandler implements InvocationHandler {


	private final ServiceConsumer consumer;
	
	public ClientInvocationHandler(ServiceConsumer consumer) {
		this.consumer = consumer;
	}
	@Override
	public Object invoke(Object proxy, Method method, Object[] args)
			throws Throwable {
		
		String realMethodName = null;
		Class<?>[] realParameterTypes = null;
		Object[] realArgs = null;
		boolean isAsync = false;
		Runnable callback = null;
		Executor methodCallbackExecutor = null;
		
		//isAsync
		if(method.getName().startsWith("$")) {
			isAsync = true;
			realMethodName = method.getName().replace("$", "");

			if(args.length > 0 && args[args.length - 1] instanceof Runnable) {
				callback = (Runnable) args[args.length - 1];
			}
			if(callback != null && args.length > 1 && args[args.length - 2] instanceof Executor) {
				methodCallbackExecutor = (Executor)args[args.length - 2];
			}
			
			if(callback == null && methodCallbackExecutor == null) {
				  realParameterTypes = method.getParameterTypes();
				  realArgs = args;
			}
			else if (callback != null && methodCallbackExecutor != null) {
				realArgs = new Object[args.length -2];
				realParameterTypes = new Class<?>[args.length - 2];
				for (int i = 0; i < realParameterTypes.length; i++) {
					realArgs[i] = args[i];
					realParameterTypes[i] = method.getParameterTypes()[i];
				}
			}
			else if(callback != null) {
				realArgs = new Object[args.length -1];
				realParameterTypes = new Class<?>[args.length - 1];
				for (int i = 0; i < realArgs.length; i++) {
					realArgs[i] = args[i];
					realParameterTypes[i] = method.getParameterTypes()[i];
				}
			}
		}
		//sync invoke
		else {
			realParameterTypes = method.getParameterTypes();
			realArgs = args;
			realMethodName = method.getName();
		}
		
		String[] parameterTypeNames = new String[realParameterTypes.length]; 
		for (int i = 0; i < parameterTypeNames.length; i++) {
			parameterTypeNames[i] = realParameterTypes[i].getName();
		}
	
		if(isAsync) {
			@SuppressWarnings("unchecked")
			ListenableFuture<Object> future =  (ListenableFuture<Object>) this.consumer.$invoke(realMethodName,
					parameterTypeNames, realArgs);
			
			final Runnable finalCallback = callback;
			if (finalCallback != null) {
				if (methodCallbackExecutor != null) {
					future.addListener(new FutureListener<Object>() {
						@Override
						public void operationComplete(
								ListenableFuture<Object> future) {
							finalCallback.run();

						}
					}, methodCallbackExecutor);
				} else {
					future.addListener(new FutureListener<Object>() {
						@Override
						public void operationComplete(
								ListenableFuture<Object> future) {
							finalCallback.run();

						}
					});
				}
			}
			return future;
		}
		else {
			return this.consumer.invoke(realMethodName, parameterTypeNames, realArgs);
		}
	}
}
