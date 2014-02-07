package com.github.jremoting.invoker;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.github.jremoting.core.Invoke;
import com.github.jremoting.core.InvokeFilter;
import com.github.jremoting.core.InvokeFilterUtil;
import com.github.jremoting.core.ServiceProvider;
import com.github.jremoting.exception.RemotingException;
import com.github.jremoting.util.ReflectionUtil;


public class ServerRpcInvoker    {
	
	private final InvokeFilter head;
	
	//key = serviceName:serviceVersion 
	private Map<String, ServiceProvider> providers = new HashMap<String, ServiceProvider>();
	
	public ServerRpcInvoker(List<InvokeFilter> invokeFilters) {
		invokeFilters.add(new ServerTailInvokeFilter());
		this.head = InvokeFilterUtil.link(invokeFilters);
	}
	
	public  Object invoke(Invoke invoke) {
		ServiceProvider provider = providers.get(invoke.getServiceId());
		invoke.setTarget(provider.getTarget());
		
		return this.head.invoke(invoke);
	}
	
	public void register(ServiceProvider provider) {
		providers.put(provider.getServiceId(), provider);
	}

	private  class ServerTailInvokeFilter implements InvokeFilter {
		
		@Override
		public Object invoke(Invoke invoke) {
			try {

				Method targetMethod = ReflectionUtil.findMethod(invoke.getTarget().getClass(), 
						invoke.getMethodName(),
						invoke.getParameterTypes());
				
			    if(targetMethod == null) {
			    	throw new RemotingException("can not find method!");
			    }
			    
			    return targetMethod.invoke(invoke.getTarget(), invoke.getArgs());
			
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
		
		@Override
		public InvokeFilter getNext() {
			return null;
		}
		@Override
		public void setNext(InvokeFilter next) {
		}
	}
}
