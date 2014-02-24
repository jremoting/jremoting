package com.github.jremoting.core;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executor;


public class Invoke extends Message {
	//basic field will write to protocal body
	private final String version;
	private final String methodName;
	private final String interfaceName;
	private final String group;
	


	private  Object[] args;
	private final Class<?>[] parameterTypes;
	private final String[] parameterTypeNames;
	
	
	//run time field used
	private final String serviceName;
	private ServiceConsumer consumer;
	private Registry registry;
	private ServiceProvider provider;
	private int invokeCount;
	
	
	//client async invoke field
	private boolean isAsync = false;

	private Executor asyncInvokeExecutor;
	private MessageFuture resultFuture;
	private Map<String, Object> asyncContexts;
	private InvokeFilter invokeChain;

	//server async invoke field
	private Object target;
	private Method targetMethod;

	public Invoke(String interfaceName, String version, String group,String methodName ,
			Serializer serializer, Object[] args, Class<?>[] parameterTypes) {
		super(true, serializer);
		this.args = args;
		this.interfaceName = interfaceName;
		this.version = version;
		this.methodName = methodName;
		this.group = group;
		this.parameterTypes = parameterTypes;
		
		this.parameterTypeNames = new String[this.parameterTypes.length];
		for (int i = 0; i < this.parameterTypeNames.length; i++) {
			this.parameterTypeNames[i] = this.parameterTypes[i].getName();
		}
		this.serviceName = this.interfaceName + ":" + this.version;


	}
	
	public Invoke(String interfaceName, String version,String group, String methodName ,
			Serializer serializer, Object[] args, String[] parameterTypeNames) {
		super(true, serializer);
		this.args = args;
		this.interfaceName = interfaceName;
		this.version = version;
		this.methodName = methodName;
		this.parameterTypeNames = parameterTypeNames;
		this.parameterTypes = null;
		this.group = group;
		this.serviceName = this.interfaceName + ":" + this.version;
	}
	
	
	public Object[] getArgs() {
		return args;
	}
	
	public void setArgs(Object[] args) {
		this.args =args;
	}


	public String getInterfaceName() {
		return interfaceName;
	}

	
	public String getVersion() {
		return version;
	}

	public String getGroup() {
		return group;
	}
	
	public String getMethodName() {
		return methodName;
	}
	
	public String getServiceName() {
		return serviceName;
	}

	public Object getTarget() {
		return target;
	}

	
	public void setTarget(Object target) {
		this.target = target;
	}

	public Class<?>[] getParameterTypes() {
		return parameterTypes;
	}


	public Registry getRegistry() {
		return registry;
	}


	public void setRegistry(Registry registry) {
		this.registry = registry;
	}

	public String[] getParameterTypeNames() {
		return parameterTypeNames;
	}

	public Executor getAsyncInvokeExecutor() {
		return asyncInvokeExecutor;
	}
	
	
	public void setAsyncInvokeExecutor(Executor asyncInvokeExecutor) {
		this.asyncInvokeExecutor = asyncInvokeExecutor;
	}
	
	public boolean isAsync() {
		return isAsync;
	}
	public void setAsync(boolean isAsync) {
		this.isAsync = isAsync;
	}

	public int getRetry() {
		return consumer.getRetry();
	}

	public MessageFuture getResultFuture() {
		return resultFuture;
	}

	public void setResultFuture(MessageFuture resultFuture) {
		if(this.resultFuture != null) {
			throw new IllegalStateException("invoke can only bind to one result future!");
		}
		this.resultFuture = resultFuture;
	}

	public Object getAsyncContext(String key) {
		if(asyncContexts== null) {
			asyncContexts = new HashMap<String, Object>();
		}
		return asyncContexts.get(key);
	}
	
	public void setAsyncContext(String key, Object context) {
		this.asyncContexts.put(key, context);
	}

	public InvokeFilter getInvokeChain() {
		return invokeChain;
	}

	public void setInvokeChain(InvokeFilter invokeChain) {
		this.invokeChain = invokeChain;
	}

	public Method getTargetMethod() {
		return targetMethod;
	}

	public void setTargetMethod(Method targetMethod) {
		this.targetMethod = targetMethod;
	}

	public ServiceConsumer getConsumer() {
		return consumer;
	}

	public void setConsumer(ServiceConsumer consumer) {
		this.consumer = consumer;
	}

	public ServiceProvider getProvider() {
		return provider;
	}

	public void setProvider(ServiceProvider provider) {
		this.provider = provider;
	}

	public int getInvokeCount() {
		return invokeCount;
	}

	public void incrementInvokeCount() {
		this.invokeCount++;
	}
	
}
