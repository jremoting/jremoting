package com.github.jremoting.invoke;

import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import com.github.jremoting.core.AbstractInvokeFilter;
import com.github.jremoting.core.Invoke;
import com.github.jremoting.core.ServiceParticipantInfo;
import com.github.jremoting.exception.FailoverableException;
import com.github.jremoting.exception.RemotingException;
import com.github.jremoting.util.Logger;
import com.github.jremoting.util.LoggerFactory;

public class ClusterInvokeFilter extends AbstractInvokeFilter {
	
	private final static Logger LOGGER = LoggerFactory.getLogger(ClusterInvokeFilter.class);
	
	private AtomicLong nextIndex = new AtomicLong(0);

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
		

		
		int nextProviderIndex = (int)(nextIndex.getAndIncrement());
		
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
}
