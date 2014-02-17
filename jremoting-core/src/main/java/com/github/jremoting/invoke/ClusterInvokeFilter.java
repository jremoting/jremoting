package com.github.jremoting.invoke;

import java.util.List;
import com.github.jremoting.core.AbstractInvokeFilter;
import com.github.jremoting.core.Invoke;
import com.github.jremoting.core.ServiceParticipantInfo;
import com.github.jremoting.exception.FailoverableException;
import com.github.jremoting.exception.RemotingException;
import com.github.jremoting.util.Logger;
import com.github.jremoting.util.LoggerFactory;
import com.github.jremoting.util.concurrent.ListenableFuture;

public class ClusterInvokeFilter extends AbstractInvokeFilter {
	
	private final static Logger LOGGER = LoggerFactory.getLogger(ClusterInvokeFilter.class);
	
	@Override
	public Object invoke(Invoke invoke) {
		// if invoke already has remote address then skip registry for debug use
		if(invoke.getRemoteAddress() != null) {
			return getNext().invoke(invoke);
		}
		
		List<ServiceParticipantInfo> providers = invoke.getRegistry().getProviders(invoke.getServiceName());
		
		if(providers == null || providers.isEmpty()) {
			throw new RemotingException("no provier for service " + invoke.getServiceName());
		}
		

		
		int nextProviderIndex = (int)(invoke.getId());
		
		//if encounter failoverable exception will try next provider in provider list
		//if all failed then throw last exception
		for (int i = 0 ; i < providers.size() ; i++) {
			ServiceParticipantInfo provider = providers.get(nextProviderIndex % providers.size());
			invoke.setRemoteAddress(provider.getAddress());
			
			try {
				return getNext().invoke(invoke);
			} catch (FailoverableException ex) {

				//if this is last provider then rethrow this exception
				if(i == providers.size() -1) {
					throw ex;
				}
				else {
					
					LOGGER.info("encounter failoverable error "
							+ ex.getClass().getName() + " when invoke provider->"
							+ provider.getAddress() + " will try another provider.");
					
					nextProviderIndex++;
					continue;
				}
			}
		}
		
		throw new RemotingException("should not happen!");
	}
	
	
	private static class ClusterAsyncInvokeContext {
		public final List<ServiceParticipantInfo> providers;
		public int nextProviderIndex = 0;
		public int tryTimes = 0 ;
		
		public ClusterAsyncInvokeContext( List<ServiceParticipantInfo> providers, int nextProviderIndex) {
			this.providers = providers;
			this.nextProviderIndex = nextProviderIndex;
			this.tryTimes = providers.size();
		}
		
		public static final String CONTEXT_KEY = ClusterAsyncInvokeContext.class.getName();
	}
	
	@Override
	public ListenableFuture<Object> beginInvoke(Invoke invoke) {
		// if invoke already has remote address then skip registry for debug use
		if (invoke.getRemoteAddress() != null) {
			return getNext().beginInvoke(invoke);
		}

		ClusterAsyncInvokeContext context = (ClusterAsyncInvokeContext) invoke
				.getAsyncContext(ClusterAsyncInvokeContext.CONTEXT_KEY);
		if(context == null) {
			List<ServiceParticipantInfo> providers = invoke.getRegistry()
					.getProviders(invoke.getServiceName());
			context = new ClusterAsyncInvokeContext(providers, (int)invoke.getId());
			invoke.setAsyncContext(ClusterAsyncInvokeContext.CONTEXT_KEY, context);
		}
	
		invoke.setRemoteAddress(context.providers.get(context.nextProviderIndex++ % context.providers.size()).getAddress());
		context.tryTimes--;
		return getNext().beginInvoke(invoke);
	}

	@Override
	public void endInvoke(Invoke invoke, Object result) {
		if(result instanceof FailoverableException) {
			ClusterAsyncInvokeContext context = (ClusterAsyncInvokeContext) invoke
					.getAsyncContext(ClusterAsyncInvokeContext.CONTEXT_KEY);
			
			if(context.tryTimes > 0) {
				this.beginInvoke(invoke);
				return;
			}
		}
		
		getPrev().endInvoke(invoke, result);
	}
}
